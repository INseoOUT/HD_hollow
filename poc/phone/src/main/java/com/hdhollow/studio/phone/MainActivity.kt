package com.hdhollow.studio.phone

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.usb.UsbManager
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.hdhollow.studio.shared.ColorTemp
import com.hdhollow.studio.shared.LightingParams
import kotlin.math.roundToInt

/**
 * 폰 동반 앱. 화면 공유 동의를 받아 [MirrorService] 를 띄우고, 상태와 조명 조절(P8)을 보여 준다.
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
        MirrorService.init(this) // 마지막 조명 값 불러오기
        handleRestart(intent)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    val status by MirrorService.status.collectAsState()
                    val lighting by MirrorService.lighting.collectAsState()
                    val floating by MirrorService.floating.collectAsState()
                    PhoneScreen(
                        status, lighting, floating.on,
                        onStart = ::startSharing,
                        onStop = { startService(MirrorService.stopIntent(this)) },
                        onFloating = ::setFloating,
                        onTethering = ::openTetheringSettings,
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 오버레이 권한 설정에서 돌아오면 같은 요청을 다시 보내 플로팅 컨트롤이 뜨게 한다
        if (MirrorService.floating.value.on) MirrorService.setFloating(true)
        handleUsbAccessory(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleUsbAccessory(intent)
        handleRestart(intent)
    }

    /** '화면 공유가 끝났어요' 알림을 탭해 왔으면 바로 동의 화면으로(공유 앱이 닫혀 공유가 끝난 경우의 재시작 경로) */
    private fun handleRestart(intent: Intent?) {
        if (intent?.getBooleanExtra(MirrorService.EXTRA_RESTART, false) != true) return
        intent.removeExtra(MirrorService.EXTRA_RESTART)
        if (!MirrorService.status.value.running) startSharing()
    }

    /** 차가 AOA 로 폰을 액세서리 모드로 바꾸면 시스템이 이 인텐트로 앱을 띄운다. 송출 중이면 서비스가 그 경로를 연다(P13) */
    private fun handleUsbAccessory(intent: Intent?) {
        if (intent?.action != UsbManager.ACTION_USB_ACCESSORY_ATTACHED) return
        if (MirrorService.status.value.running) startService(Intent(this, MirrorService::class.java).setAction(MirrorService.ACTION_ATTACH_USB))
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

    private fun setFloating(on: Boolean) {
        MirrorService.setFloating(on)
        if (on && !FloatingControl.canDraw(this)) {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        }
    }

    /** 실차 USB 테더링 경로용. 테더링 화면은 공개 액션이 없어 AOSP 클래스명을 먼저, 안 되면 무선 설정으로 */
    private fun openTetheringSettings() {
        val direct = Intent().setClassName("com.android.settings", "com.android.settings.TetherSettings")
        runCatching { startActivity(direct) }.onFailure { startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS)) }
    }
}

@Composable
private fun PhoneScreen(
    status: MirrorService.Status,
    lighting: LightingParams,
    floating: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onFloating: (Boolean) -> Unit,
    onTethering: () -> Unit,
) {
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

        LightingControls(lighting)

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("플로팅 컨트롤", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "촬영 앱 위에 작은 조절판을 띄웁니다. 전체 화면을 공유하면 차에도 비치므로 \"앱 하나만 공유\"와 함께 쓰세요.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Switch(floating, onFloating)
        }

        OutlinedButton(onTethering) { Text("USB 테더링 설정 열기") }
        Text(
            "실차에서 USB 로 연결할 때는 폰의 USB 테더링을 켭니다(차가 폰 네트워크에 붙습니다). 에뮬레이터 데모는 adb forward 경로를 씁니다. " +
                "Android 14 이상의 동의 화면에서 \"앱 하나만 공유\"를 고르면 그 앱 화면만 차에 보입니다.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

/** 밝기·색온도·무드 색. 차 다이얼과 같은 값을 공유한다(P8). */
@Composable
private fun LightingControls(p: LightingParams) {
    Column(
        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("조명", style = MaterialTheme.typography.titleMedium)
            Box(Modifier.size(22.dp).background(Color(ColorTemp.colorFor(p)), CircleShape).border(1.dp, Color.Gray, CircleShape))
        }
        Text("밝기 ${(p.level * 100).roundToInt()}%")
        Slider(p.level, { v -> MirrorService.updateLighting { it.copy(level = v) } }, Modifier.fillMaxWidth())
        Text("색온도 ${p.preset?.let(FloatingControl::presetLabel) ?: "${p.kelvin}K"}")
        Slider(
            p.kelvin.toFloat(),
            { v ->
                val k = (v / LightingParams.KELVIN_SLIDER_STEP).roundToInt() * LightingParams.KELVIN_SLIDER_STEP
                MirrorService.updateLighting { it.copy(kelvin = k, preset = null) }
            },
            Modifier.fillMaxWidth(),
            valueRange = LightingParams.KELVIN_MIN.toFloat()..LightingParams.KELVIN_MAX.toFloat(),
            steps = (LightingParams.KELVIN_MAX - LightingParams.KELVIN_MIN) / LightingParams.KELVIN_SLIDER_STEP - 1,
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("무드 색", style = MaterialTheme.typography.bodySmall)
            LightingParams.PRESETS.forEach { name ->
                val c = Color(ColorTemp.MOOD_COLORS.getValue(name))
                Box(
                    Modifier.size(32.dp).background(c, CircleShape)
                        .border(if (p.preset == name) 3.dp else 1.dp, if (p.preset == name) Color.Black else Color.Gray, CircleShape)
                        .clickable { MirrorService.updateLighting { it.copy(preset = if (it.preset == name) null else name) } }
                )
            }
        }
    }
}
