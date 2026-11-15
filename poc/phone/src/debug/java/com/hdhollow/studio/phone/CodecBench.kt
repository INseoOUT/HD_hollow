package com.hdhollow.studio.phone

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaFormat
import android.os.Build
import android.util.Log
import android.view.Surface
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import kotlin.random.Random

/**
 * 디버그 빌드 전용 코덱 벤치: 같은 그림을 JPEG(지금 경로)와 H.264(`MediaCodec`, Surface 입력)로 각각 n장 인코드·디코드해
 * 장당 시간과 크기를 logcat(`CodecBench`)에 찍는다. "H.264 로 바꿀 만큼 JPEG 가 문제인가"를 같은 기기에서 비교하기 위한 것.
 * 에뮬레이터의 H.264 인코더는 소프트웨어(c2.android.avc.encoder)라 실기기(하드웨어)보다 훨씬 느리게 나온다는 점을 읽을 때 감안한다.
 * 프레임은 카메라 프리뷰처럼 매 장 조금씩 다르게(원본과 6px 옮긴 그림을 번갈아) 준다. 결과 H.264 스트림은 외부 파일 디렉터리 bench.h264 에 남긴다.
 */
object CodecBench {
    private const val TAG = "CodecBench"
    private const val MIME = MediaFormat.MIMETYPE_VIDEO_AVC
    private const val WARMUP = 3
    private const val TIMEOUT_US = 300_000L

    fun run(context: Context, image: String, n: Int, kbps: Int, w: Int, h: Int, quality: Int, decoderName: String?) {
        Thread({
            runCatching { bench(context, image, n, kbps, w, h, quality, decoderName) }
                .onFailure { Log.e(TAG, "벤치 실패", it) }
        }, "codec-bench").start()
    }

    private fun bench(context: Context, image: String, n: Int, kbps: Int, w: Int, h: Int, quality: Int, decoderName: String?) {
        val src = source(image, w, h)
        val frames = listOf(src, shifted(src, 6))
        Log.d(TAG, "시작 ${w}x${h} ${n}장 그림=${image.ifEmpty { "합성" }} (${Build.MODEL}, API ${Build.VERSION.SDK_INT})")
        jpeg(frames, n, quality)
        h264(context, frames, n, kbps, decoderName)
    }

    // ---- JPEG: 지금 MirrorService 와 같은 경로(Bitmap.compress) + 차와 같은 디코드(RGB_565) ----
    private fun jpeg(frames: List<Bitmap>, n: Int, quality: Int) {
        val out = ByteArrayOutputStream(256 * 1024)
        val opts = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.RGB_565 }
        var encNs = 0L; var decNs = 0L; var bytes = 0L
        repeat(WARMUP + n) { i ->
            val b = frames[i % frames.size]
            val t0 = System.nanoTime()
            out.reset()
            b.compress(Bitmap.CompressFormat.JPEG, quality, out)
            val arr = out.toByteArray()
            val t1 = System.nanoTime()
            BitmapFactory.decodeByteArray(arr, 0, arr.size, opts)
            val t2 = System.nanoTime()
            if (i >= WARMUP) { encNs += t1 - t0; decNs += t2 - t1; bytes += arr.size }
        }
        val sizes = frames.map { b -> out.reset(); b.compress(Bitmap.CompressFormat.JPEG, quality, out); out.size() / 1024 }
        Log.d(TAG, "JPEG q$quality ${frames[0].width}x${frames[0].height}: 인코드 ${ms(encNs, n)}ms 디코드 ${ms(decNs, n)}ms 장당 ${bytes / n / 1024}KB (${n}장, 원본·옮긴 그림 각 ${sizes.joinToString("·")}KB)")
    }

    // ---- H.264: Surface 입력(가상 디스플레이가 그대로 그릴 경로) → 디코더(차 쪽) ----
    private fun h264(context: Context, frames: List<Bitmap>, n: Int, kbps: Int, decoderName: String?) {
        val w = frames[0].width; val h = frames[0].height
        val format = MediaFormat.createVideoFormat(MIME, w, h).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            setInteger(MediaFormat.KEY_BIT_RATE, kbps * 1000)
            setInteger(MediaFormat.KEY_FRAME_RATE, 15)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)
        }
        val enc = MediaCodec.createEncoderByType(MIME)
        enc.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        val surface = enc.createInputSurface()
        enc.start()
        var dec: MediaCodec? = null
        val stream = File(context.getExternalFilesDir(null), "bench.h264").outputStream().buffered()
        val info = MediaCodec.BufferInfo()
        var encNs = 0L; var decNs = 0L; var bytes = 0L; var encoded = 0; var decoded = 0; var keyframes = 0
        var hardware = false

        val decInfo = MediaCodec.BufferInfo() // 인코더 쪽 info 와 섞이지 않게
        fun decode(data: ByteBuffer, flags: Int, count: Boolean): Long {
            val d = dec ?: return 0
            val t0 = System.nanoTime()
            val inIdx = d.dequeueInputBuffer(TIMEOUT_US)
            if (inIdx < 0) return 0
            d.getInputBuffer(inIdx)!!.apply { clear(); put(data) }
            d.queueInputBuffer(inIdx, 0, data.limit(), System.nanoTime() / 1000, flags)
            while (true) {
                val outIdx = d.dequeueOutputBuffer(decInfo, TIMEOUT_US)
                if (outIdx >= 0) { d.releaseOutputBuffer(outIdx, false); if (count) decoded++; break }
                if (outIdx == MediaCodec.INFO_TRY_AGAIN_LATER) break // 디코더가 한 장을 쥐고 있음(지연). 다음 장에서 나온다
            }
            return System.nanoTime() - t0
        }

        try {
            repeat(WARMUP + n) { i ->
                val measured = i >= WARMUP
                val t0 = System.nanoTime()
                draw(surface, frames[i % frames.size])
                // 이 장의 출력이 나올 때까지(또는 잠깐) 기다린다. 순차 측정이라 장당 지연 = 인코드 시간
                var got = false
                while (!got) {
                    val idx = enc.dequeueOutputBuffer(info, TIMEOUT_US)
                    when {
                        idx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                            val f = enc.outputFormat
                            val d = decoderName?.let { MediaCodec.createByCodecName(it) } ?: MediaCodec.createDecoderByType(MIME)
                            d.configure(f, null, null, 0) // csd-0/1 이 들어 있다
                            d.start()
                            dec = d
                            hardware = Build.VERSION.SDK_INT >= 29 && enc.codecInfo.isHardwareAccelerated
                            Log.d(TAG, "인코더 ${enc.name}(hw=$hardware) 디코더 ${d.name}(hw=${Build.VERSION.SDK_INT >= 29 && d.codecInfo.isHardwareAccelerated})")
                        }
                        idx >= 0 -> {
                            val buf = enc.getOutputBuffer(idx)!!
                            val flags = info.flags
                            val config = flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0
                            val copy = ByteArray(info.size).also { buf.position(info.offset); buf.get(it) }
                            stream.write(copy)
                            enc.releaseOutputBuffer(idx, false)
                            if (!config) {
                                val t1 = System.nanoTime()
                                val dNs = decode(ByteBuffer.wrap(copy), 0, measured)
                                if (measured) {
                                    encNs += t1 - t0; decNs += dNs; bytes += copy.size; encoded++
                                    if (flags and MediaCodec.BUFFER_FLAG_KEY_FRAME != 0) keyframes++
                                }
                                got = true
                            } else dec?.let { decode(ByteBuffer.wrap(copy), MediaCodec.BUFFER_FLAG_CODEC_CONFIG, false) }
                        }
                        idx == MediaCodec.INFO_TRY_AGAIN_LATER -> { Log.w(TAG, "인코더 출력 대기 초과(장 $i)"); got = true }
                    }
                }
            }
        } finally {
            runCatching { enc.signalEndOfInputStream() }
            runCatching { enc.stop(); enc.release() }
            runCatching { dec?.stop(); dec?.release() }
            surface.release()
            stream.close()
        }
        val encName = if (hardware) "하드웨어" else "소프트웨어"
        Log.d(TAG, "H.264 ${kbps}kbps ${w}x${h} ($encName 인코더): 인코드 ${ms(encNs, encoded)}ms 디코드 ${ms(decNs, decoded)}ms 장당 ${if (encoded > 0) bytes / encoded / 1024 else 0}KB (${encoded}장, 키프레임 $keyframes, 디코드 ${decoded}장) → ${File(context.getExternalFilesDir(null), "bench.h264")}")
        Log.d(TAG, "코덱 목록: " + MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos
            .filter { it.supportedTypes.any { t -> t.equals(MIME, true) } }
            .joinToString { "${it.name}${if (it.isEncoder) "(enc)" else "(dec)"}${if (Build.VERSION.SDK_INT >= 29 && it.isHardwareAccelerated) "·hw" else ""}" })
    }

    private fun draw(surface: Surface, b: Bitmap) {
        val c = try { surface.lockHardwareCanvas() } catch (e: Exception) { surface.lockCanvas(null) }
        c.drawBitmap(b, 0f, 0f, null)
        surface.unlockCanvasAndPost(c)
    }

    private fun ms(totalNs: Long, count: Int): String = if (count == 0) "-" else "%.1f".format(totalNs / count / 1e6)

    /** 그림 파일을 w×h 로 맞춘다. 없으면 카메라 프리뷰 흉내(그라데이션 + 무작위 사각형 + 글자) */
    private fun source(path: String, w: Int, h: Int): Bitmap {
        val file = path.takeIf { it.isNotEmpty() }?.let { BitmapFactory.decodeFile(it) }
        if (file != null) return Bitmap.createScaledBitmap(file, w, h, true).copy(Bitmap.Config.ARGB_8888, false)
        val b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        val rnd = Random(1)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        for (y in 0 until h step 4) {
            p.color = Color.rgb(40 + y * 150 / h, 80, 200 - y * 120 / h)
            c.drawRect(0f, y.toFloat(), w.toFloat(), y + 4f, p)
        }
        repeat(300) {
            p.color = Color.argb(200, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
            val x = rnd.nextInt(w).toFloat(); val y = rnd.nextInt(h).toFloat()
            c.drawRect(x, y, x + rnd.nextInt(10, 120), y + rnd.nextInt(10, 120), p)
        }
        p.color = Color.WHITE; p.textSize = 48f
        repeat(20) { c.drawText("Selene 벤치 $it", 20f, 80f + it * 56f, p) }
        return b
    }

    private fun shifted(src: Bitmap, dx: Int): Bitmap {
        val b = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        Canvas(b).apply { drawColor(Color.BLACK); drawBitmap(src, dx.toFloat(), 0f, null) }
        return b
    }
}
