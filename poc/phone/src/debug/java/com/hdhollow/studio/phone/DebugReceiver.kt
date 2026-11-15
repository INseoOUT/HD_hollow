package com.hdhollow.studio.phone

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * 디버그 빌드 전용 명령 수신기(테스트 자동화). 탭 좌표에 기대지 않고 조명 값·플로팅 컨트롤을 바꾸고, fps 측정용 손잡이를 돌린다.
 *   adb shell am broadcast -n com.hdhollow.studio.phone/.DebugReceiver --ei kelvin 3300 --ef level 0.9 --es preset cyan|none --ez floating true
 *   --ei quality 40(JPEG 품질, 즉시) --ei side 720(가상 디스플레이 한 변, 다음 공유부터)
 *   --es bench /sdcard/Download/bench.png(코덱 벤치. 빈 문자열이면 합성 그림) [--ei n 30 --ei kbps 4000 --ei w 540 --ei h 1200 --es dec c2.android.avc.decoder]
 */
class DebugReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        MirrorService.init(context)
        val e = intent.extras ?: return
        if (e.containsKey("kelvin") || e.containsKey("level") || e.containsKey("preset")) {
            MirrorService.updateLighting { p ->
                p.copy(
                    kelvin = if (e.containsKey("kelvin")) e.getInt("kelvin") else p.kelvin,
                    level = if (e.containsKey("level")) e.getFloat("level") else p.level,
                    preset = when {
                        e.containsKey("preset") -> e.getString("preset")?.takeIf { it != "none" }
                        e.containsKey("kelvin") -> null // 색온도를 주면 무드 색은 풀린다
                        else -> p.preset
                    },
                )
            }
        }
        if (e.containsKey("floating")) MirrorService.setFloating(e.getBoolean("floating"))
        if (e.containsKey("quality")) MirrorService.jpegQuality = e.getInt("quality")
        if (e.containsKey("side")) MirrorService.maxSide = e.getInt("side")
        if (e.containsKey("bench")) {
            CodecBench.run(
                context, e.getString("bench").orEmpty(), n = e.getInt("n", 30), kbps = e.getInt("kbps", 4000),
                w = e.getInt("w", 540), h = e.getInt("h", 1200), quality = MirrorService.jpegQuality, decoderName = e.getString("dec"),
            )
        }
        Log.d(TAG, "적용 ${MirrorService.lighting.value}")
    }

    companion object {
        private const val TAG = "DebugReceiver"
    }
}
