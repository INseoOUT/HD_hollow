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
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.hdhollow.studio.shared.LightingParams
import com.hdhollow.studio.shared.StudioState
import com.hdhollow.studio.ui.CarScreenSection
import com.hdhollow.studio.ui.ColorSection
import com.hdhollow.studio.ui.PresetSection
import com.hdhollow.studio.ui.RingLightSection
import com.hdhollow.studio.ui.StatusPill
import com.hdhollow.studio.ui.StepRow
import com.hdhollow.studio.ui.StudioCard
import com.hdhollow.studio.ui.StudioColors
import com.hdhollow.studio.ui.StudioTheme
import com.hdhollow.studio.ui.ToggleRow

/**
 * 폰 동반 앱. 화면 공유 동의를 받아 [MirrorService] 를 띄우고, 상태와 조명·프리셋·차 화면 상태(차 패널과 같은 구역)를 보여 준다.
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
        // targetSdk 35 는 화면 끝까지 그린다. 시스템의 라이트·다크를 따라 상태 바 아이콘 색도 바뀐다
        val bars = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        enableEdgeToEdge(bars, bars)
        MirrorService.init(this) // 마지막 조명 값 불러오기
        handleRestart(intent)
        setContent {
            StudioTheme {
                Surface(Modifier.fillMaxSize(), color = StudioColors.Ink) {
                    val status by MirrorService.status.collectAsState()
                    val lighting by MirrorService.lighting.collectAsState()
                    val floating by MirrorService.floating.collectAsState()
                    val studio by MirrorService.studio.collectAsState()
                    PhoneScreen(
                        status, lighting, studio, floating.on,
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
        // 기본 안내는 "앱 하나만 공유"(11/09 결정): 고른 촬영 앱 화면만 차에 보이고 알림·다른 앱·플로팅 컨트롤은 안 비친다.
        // "전체 화면"은 앱을 바꿔 가며 쓸 때만. 어느 쪽인지는 시스템 동의 화면에서 사용자가 고른다(앱이 강제할 수 없음)
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
    studio: StudioState,
    floating: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onFloating: (Boolean) -> Unit,
    onTethering: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Selene", color = StudioColors.Text, style = MaterialTheme.typography.headlineMedium)
            Text("폰 화면은 차 화면으로, 차 조명은 스튜디오 조명으로", color = StudioColors.TextDim, style = MaterialTheme.typography.bodyMedium)
        }

        ShareCard(status, onStart, onStop)
        if (!status.running) HowToCard()

        // 아래 네 구역은 차 패널과 같은 컴포넌트·같은 순서다(poc/ui StudioSections): 지금 값·밝기 → 프리셋 → 색 → 차 화면
        val linked = status.clients > 0
        RingLightSection(
            lighting,
            onLevel = { v -> MirrorService.updateLighting { it.copy(level = v) } },
            note = "차와 같은 값",
        )
        PresetSection(
            lighting, studio.userScenes,
            onApply = MirrorService::applyScene,
            onSave = MirrorService::saveScene,
            onDelete = MirrorService::deleteScene,
            canEdit = linked,
        )
        ColorSection(
            lighting,
            onKelvin = { k -> MirrorService.updateLighting { it.copy(kelvin = k, preset = null) } },
            onMood = { hex -> MirrorService.updateLighting { it.copy(preset = hex) } },
        )
        CarScreenSection(
            studio, onChange = { s -> MirrorService.updateStudio { s } },
            enabled = linked, note = if (linked) "차와 같은 값" else "차와 연결되면 바꿀 수 있습니다",
        )

        StudioCard(padding = 16.dp) {
            ToggleRow("플로팅 컨트롤", "촬영 앱 위에 작은 조절판을 띄웁니다. 기본인 \"앱 하나만 공유\"에서는 차에 비치지 않습니다.", floating, onFloating)
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            TextButton(onTethering, contentPadding = ButtonDefaults.TextButtonContentPadding) { Text("USB 테더링 설정 열기") }
            Text(
                "실차에서 USB 로 연결할 때는 폰의 USB 테더링을 켭니다(차가 폰 네트워크에 붙습니다). 에뮬레이터 데모는 adb forward 경로를 씁니다.",
                color = StudioColors.TextFaint, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 12.dp),
            )
        }
    }
}

/** 송출 상태와 시작·중지. 가장 큰 버튼 하나만 둔다. */
@Composable
private fun ShareCard(status: MirrorService.Status, onStart: () -> Unit, onStop: () -> Unit) {
    val linked = status.clients > 0
    StudioCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f).padding(end = 12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(if (status.running) "송출 중" else "대기 중", color = StudioColors.Text, style = MaterialTheme.typography.titleLarge)
                Text(
                    when {
                        !status.running -> "화면 공유를 시작하면 차 화면 가운데에 폰 화면이 뜹니다"
                        status.frame.isBlank() -> "차가 연결되면 송출이 시작됩니다"
                        else -> "차 화면으로 보내는 중 · 초당 ${status.fps}장"
                    },
                    color = StudioColors.TextDim, style = MaterialTheme.typography.bodySmall,
                )
            }
            StatusPill(if (linked) "차량 연결됨" else "차량 대기 중", if (linked) StudioColors.Ok else StudioColors.TextFaint)
        }
        status.error?.let { Text("오류: $it", color = StudioColors.Danger, style = MaterialTheme.typography.bodySmall) }
        if (status.running) {
            OutlinedButton(
                onStop, Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = StudioColors.Danger),
                border = BorderStroke(1.dp, StudioColors.Danger.copy(alpha = 0.6f)),
            ) { Text("공유 중지", style = MaterialTheme.typography.labelLarge) }
        } else {
            Button(onStart, Modifier.fillMaxWidth().height(52.dp)) { Text("화면 공유 시작", style = MaterialTheme.typography.labelLarge) }
        }
    }
}

/** 사용 순서. 기본 안내는 "앱 하나만 공유"(11/09 결정). 송출이 시작되면 숨긴다. */
@Composable
private fun HowToCard() {
    StudioCard {
        Text("이렇게 쓰세요", color = StudioColors.TextDim, style = MaterialTheme.typography.titleSmall)
        StepRow(1, "차와 연결", "USB 또는 같은 네트워크. 차 화면에서 Selene를 엽니다")
        StepRow(2, "화면 공유 시작", "동의 화면에서 \"앱 하나만 공유\"를 고릅니다")
        StepRow(3, "쓰던 카메라·라이브 앱 선택", "그 앱 화면만 차에 뜨고 알림·다른 앱은 보이지 않습니다. 앱을 바꿔 가며 쓸 때만 \"전체 화면\"")
    }
}
