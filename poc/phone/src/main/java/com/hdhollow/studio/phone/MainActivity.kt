package com.hdhollow.studio.phone

import android.Manifest
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

/**
 * 폰 동반 앱. 화면 공유 동의를 받아 [MirrorService] 를 띄우고 상태를 보여 준다.
 * 촬영은 이 앱이 하지 않는다. 사용자는 기본 카메라·틱톡·유튜브 라이브 앱을 그대로 쓴다.
 */
class MainActivity : ComponentActivity() {

    private val capture = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
        val data = r.data
        if (r.resultCode == RESULT_OK && data != null) {
            ContextCompat.startForegroundService(this, MirrorService.startIntent(this, r.resultCode, data))
        }
    }

    // 알림 권한은 없어도 송출은 된다(알림의 버튼만 안 보임). 결과와 상관없이 동의 화면으로
    private val notifications = registerForActivityResult(ActivityResultContracts.RequestPermission()) { launchCapture() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    val status by MirrorService.status.collectAsState()
                    PhoneScreen(status, onStart = ::startSharing, onStop = { startService(MirrorService.stopIntent(this)) })
                }
            }
        }
    }

    private fun startSharing() {
        val needNotif = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        if (needNotif) notifications.launch(Manifest.permission.POST_NOTIFICATIONS) else launchCapture()
    }

    private fun launchCapture() {
        // Android 14+ 동의 화면에서 "앱 하나만 공유"를 고르면 그 앱 화면만 차에 보인다. 앱을 바꿔 쓰려면 "전체 화면"
        capture.launch(getSystemService(MediaProjectionManager::class.java).createScreenCaptureIntent())
    }
}

@Composable
private fun PhoneScreen(status: MirrorService.Status, onStart: () -> Unit, onStop: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Car Studio", style = MaterialTheme.typography.headlineSmall)
        Text(if (status.running) "송출 중 · ${status.fps} fps · ${status.frame}" else "대기 중")
        Text(if (status.clients > 0) "차량 연결됨" else "차량 연결 대기 중")
        status.error?.let { Text("오류: $it", color = MaterialTheme.colorScheme.error) }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onStart, enabled = !status.running) { Text("화면 공유 시작") }
            OutlinedButton(onStop, enabled = status.running) { Text("중지") }
        }
        Text(
            "연결 → 화면 공유 시작 → 쓰던 카메라·라이브 앱을 여세요. 차 화면 가운데에 폰 화면이 뜹니다.",
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            "Android 14 이상의 동의 화면에서 \"앱 하나만 공유\"를 고르면 그 앱 화면만 차에 보입니다. 앱을 바꿔 가며 쓰려면 \"전체 화면\"을 고르세요.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
