package com.hdhollow.studio.phone

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.OrientationEventListener
import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import java.io.ByteArrayOutputStream
import java.net.Inet4Address
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {

    private val server = MjpegServer(PORT)
    private val analysisExecutor = Executors.newSingleThreadExecutor()
    private var status by mutableStateOf("카메라 권한 대기 중")
    private var framesSent by mutableIntStateOf(0)
    private var useFront by mutableStateOf(true)
    @Volatile private var mirror = true // 전면일 때만 좌우 반전(셀카처럼). 분석 스레드에서 읽음
    private var provider: ProcessCameraProvider? = null
    private lateinit var analysis: ImageAnalysis

    // 폰 방향 → 영상 회전값. 가로로 들면 차 화면에도 가로 영상이 꽉 차게 나온다
    private val orientation by lazy {
        object : OrientationEventListener(this) {
            override fun onOrientationChanged(deg: Int) {
                if (deg == ORIENTATION_UNKNOWN || !::analysis.isInitialized) return
                analysis.targetRotation = when (deg) {
                    in 45 until 135 -> Surface.ROTATION_270
                    in 135 until 225 -> Surface.ROTATION_180
                    in 225 until 315 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }
            }
        }
    }

    override fun onResume() { super.onResume(); orientation.enable() }
    override fun onPause() { orientation.disable(); super.onPause() }

    private val requestCamera =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startStreaming() else status = "카메라 권한이 거부되었습니다"
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var clients by mutableIntStateOf(0)
            LaunchedEffect(Unit) {
                while (true) { clients = server.clientCount.get(); delay(500) }
            }
            MaterialTheme {
              Surface(Modifier.fillMaxSize()) { // 배경·글자색 대비
                Column(
                    Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Car Studio Cam (PoC)", style = MaterialTheme.typography.headlineSmall)
                    Text("차량 앱에 입력할 주소", style = MaterialTheme.typography.labelLarge)
                    Text(wifiAddress()?.let { "$it:$PORT" } ?: "Wi-Fi IP 없음", style = MaterialTheme.typography.headlineMedium)
                    Text("상태: $status")
                    Text("접속한 차량: $clients · 생성 프레임: $framesSent")
                    Button({ useFront = !useFront; bindCamera() }) {
                        Text(if (useFront) "후면 카메라로 전환" else "전면 카메라로 전환")
                    }
                    Text("같은 Wi-Fi에 연결된 차량(에뮬레이터 PC)에서 접속하세요.", style = MaterialTheme.typography.bodySmall)
                }
              }
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startStreaming()
        } else {
            requestCamera.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startStreaming() {
        server.start()
        val providerFuture = ProcessCameraProvider.getInstance(this)
        providerFuture.addListener({
            analysis = ImageAnalysis.Builder()
                .setResolutionSelector(
                    ResolutionSelector.Builder()
                        .setResolutionStrategy(
                            ResolutionStrategy(
                                android.util.Size(640, 480),
                                ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER_THEN_HIGHER
                            )
                        ).build()
                )
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
            analysis.setAnalyzer(analysisExecutor) { image ->
                val rotated = image.toBitmap().let { bmp ->
                    val m = android.graphics.Matrix().apply {
                        postRotate(image.imageInfo.rotationDegrees.toFloat())
                        if (mirror) postScale(-1f, 1f)
                    }
                    Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true)
                }
                val jpeg = ByteArrayOutputStream().also { rotated.compress(Bitmap.CompressFormat.JPEG, 70, it) }.toByteArray()
                server.publish(jpeg)
                framesSent++
                image.close()
            }
            provider = providerFuture.get()
            bindCamera()
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCamera() {
        val p = provider ?: return
        val front = useFront && p.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
        mirror = front
        p.unbindAll()
        p.bindToLifecycle(this, if (front) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA, analysis)
        status = "송출 중 (포트 $PORT, ${if (front) "전면" else "후면"})"
    }

    private fun wifiAddress(): String? {
        val cm = getSystemService(ConnectivityManager::class.java)
        val props = cm.getLinkProperties(cm.activeNetwork) ?: return null
        return props.linkAddresses.map { it.address }.firstOrNull { it is Inet4Address && !it.isLoopbackAddress }?.hostAddress
    }

    override fun onDestroy() {
        server.stop()
        analysisExecutor.shutdown()
        super.onDestroy()
    }

    companion object { const val PORT = 8080 }
}
