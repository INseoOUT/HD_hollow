package com.hdhollow.studio.car

import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.android.car.ui.FocusArea
import com.android.car.ui.FocusParkingView
import com.hdhollow.studio.shared.LightingParams
import com.hdhollow.studio.shared.Protocol
import com.hdhollow.studio.shared.Scene
import com.hdhollow.studio.shared.StudioCommand
import com.hdhollow.studio.shared.StudioState
import com.hdhollow.studio.ui.CarThemes
import com.hdhollow.studio.ui.StudioColors
import com.hdhollow.studio.ui.StudioTheme
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** 가장자리 조명 안쪽 창의 모서리 반지름. 조명이 모서리에서 부드럽게 감싸도록. */
private val INNER_RADIUS = 40.dp

class MainActivity : ComponentActivity() {

    private lateinit var studio: StudioVehicle
    private lateinit var link: StudioLinkClient
    private val light = StudioLight()
    private lateinit var presets: ScenePresets
    private val exiting = MutableStateFlow(false)
    @Volatile private var lastIncoming: LightingParams? = null // 폰에서 온 마지막 값. 되돌려 보내지 않기 위해
    @Volatile private var lastIncomingState: StudioState? = null // 폰에서 온 마지막 앱 상태. 같은 이유

    @OptIn(FlowPreview::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        studio = StudioVehicle(this).also { it.start() }
        presets = ScenePresets(this)
        val dm = resources.displayMetrics
        link = StudioLinkClient(dm.widthPixels, dm.heightPixels)

        // 지난번 조명 값(사용자가 바꾼 것만, ts>0). 폰 값과 어느 쪽이 최신인지는 ts 로 가린다
        val lightPrefs = getSharedPreferences("studio_light", MODE_PRIVATE)
        lightPrefs.getString(KEY_LAST_LIGHT, null)?.let(LightingParams::fromJson)?.takeIf { it.ts > 0 }?.let(light::apply)

        // P·N이 아닌 기어를 한 번이라도 보면 경고 후 종료한다(그 사이 P로 돌아와도 되살리지 않음).
        // 실행 시점에 이미 D·R이면 아무것도 켜지 않은 채 같은 경로로 종료 = 주행 중 실행 차단.
        // 컴포지션 밖에서 감시: D에서 Pleos가 앱을 가려 onStop이 오면 Compose 재구성이 멈추기 때문
        lifecycleScope.launch {
            studio.state.first { it.usable == false }
            exiting.value = true
            studio.restoreNow() // 종료(3초 뒤)보다 먼저 되돌린다. 그 사이 죽어도 조명은 원래대로
            delay(3000)
            finish()
        }

        val active = combine(studio.state, exiting) { s, e -> s.usable == true && !e }.distinctUntilChanged()

        // 사용 가능한 동안만 폰과 연결한다(USB 액세서리 → TCP 순, PhoneLink). 끊기면 클라이언트가 2초마다 다시 붙는다
        lifecycleScope.launch {
            active.collectLatest { ok -> if (ok) link.run { PhoneLink.connect(this@MainActivity) } }
        }

        // 폰에서 온 조명 값. 내 값보다 최신일 때만 받는다(마지막으로 바뀐 쪽이 기준. 재접속 때 차 기본값이 폰 값을 덮지 않도록)
        lifecycleScope.launch {
            link.incomingLight.collect { p ->
                if (p.src != Protocol.ROLE_CAR && p.supersedes(light.params(Protocol.ROLE_CAR))) {
                    lastIncoming = p
                    light.apply(p)
                }
            }
        }

        // 바뀐 값을 저장해 다음 실행에 이어 쓴다
        lifecycleScope.launch {
            snapshotFlow { light.params(Protocol.ROLE_CAR) }.debounce(300)
                .collect { if (it.ts > 0) lightPrefs.edit().putString(KEY_LAST_LIGHT, it.toJson()).apply() }
        }

        // 내 조명 값 → 폰(폰에서 온 값은 되돌려 보내지 않음) + 앰비언트 연동. 150ms 디바운스.
        // 원래 값을 읽은 영역이 늘거나 폰이 다시 붙으면 현재 값을 다시 적용·전송한다
        lifecycleScope.launch {
            val mine = snapshotFlow { light.params(Protocol.ROLE_CAR) to light.ambientLinked }.debounce(150)
            val areas = studio.state.map { it.lighting.ambient.size }.distinctUntilChanged()
            combine(mine, areas, active, link.connected) { (p, linked), _, ok, _ -> Triple(p, linked, ok) }
                .collect { (p, linked, ok) ->
                    if (!ok) return@collect
                    if (lastIncoming?.sameLight(p) != true) link.sendLight(p)
                    if (linked) studio.syncAmbient(p.level, LightLogic.argb(p))
                }
        }

        // 차 화면 테마는 다음 실행에도 이어 쓴다(풀 라이트·다이얼 대상·앰비언트 연동은 실행마다 기본값)
        lightPrefs.getString(KEY_THEME, null)?.takeIf { it in StudioState.THEMES }?.let { light.theme = it }
        lifecycleScope.launch { snapshotFlow { light.theme }.collect { lightPrefs.edit().putString(KEY_THEME, it).apply() } }

        // 앱 상태(STATE): 차가 기준. 폰이 붙으면 지금 상태를 보내고, 바뀔 때마다 보낸다(폰에서 온 값은 되돌려 보내지 않음)
        lifecycleScope.launch {
            val mine = combine(snapshotFlow { light.state(emptyList()) }, presets.user) { s, scenes -> s.copy(userScenes = scenes) }
            combine(mine, link.connected) { s, c -> s to c }.collect { (s, c) ->
                if (!c) {
                    lastIncomingState = null // 다시 붙으면 무조건 한 번 보낸다
                } else if (lastIncomingState?.sameState(s) != true) {
                    link.sendState(s)
                }
            }
        }
        // 폰에서 바꾼 앱 상태. 사용자 프리셋 목록은 차가 기준이라 내 목록을 붙여 비교한다
        lifecycleScope.launch {
            link.incomingState.collect { s ->
                if (s.src == Protocol.ROLE_CAR) return@collect
                lastIncomingState = s.copy(userScenes = presets.user.value)
                light.apply(s)
            }
        }
        // 폰의 부탁: 프리셋 적용(실내등·독서등 포함. 패널에서 누른 것과 같은 경로)·저장·삭제
        lifecycleScope.launch {
            link.commands.collect { c ->
                when (c.cmd) {
                    StudioCommand.SCENE_APPLY -> presets.all.firstOrNull { it.id == c.id }?.let(::applyScene)
                    StudioCommand.SCENE_SAVE -> presets.saveCurrent(light)
                    StudioCommand.SCENE_DELETE -> c.id?.let(presets::delete)
                }
            }
        }

        // 디버그 빌드의 DebugReceiver(adb 로 조명 값·프리셋 변경)용 고리
        DebugHooks.light = light
        DebugHooks.applyScene = { name ->
            presets.all.firstOrNull { it.name == name }?.let { applyScene(it); true } ?: false
        }

        val dial = DialTarget(this, onRotate = light::rotate, onPress = { light.dialColor = !light.dialColor })
        val screen = ComposeView(this).apply {
            setContent { StudioTheme(car = true, palette = CarThemes.palette(light.theme)) { StudioScreen(studio, light, link, presets, exiting) } }
        }
        // RotaryService 요건: 창마다 FocusParkingView(맨 앞, FocusArea 밖) + 포커스 대상은 FocusArea 안.
        // 하나라도 없으면 회전을 버리거나 포커스를 Driving view로 넘긴다
        setContentView(FrameLayout(this).apply {
            addView(FocusParkingView(context))
            addView(FocusArea(context).apply {
                addView(FrameLayout(context).apply { addView(dial); addView(screen) }, -1, -1)
                setDefaultFocus(dial)
            }, -1, -1)
        })
        dial.requestFocus()
    }

    private fun applyScene(scene: Scene) {
        CarScenes.applyLight(scene, light)
        CarScenes.applyVehicle(scene, studio.state.value.lighting, studio)
    }

    override fun onResume() {
        super.onResume()
        // 링라이트처럼 쓰기 위해 이 창의 화면 밝기를 최대로(실차 허용 여부는 확인 항목)
        window.attributes = window.attributes.apply { screenBrightness = 1f }
    }

    override fun onPause() {
        window.attributes = window.attributes.apply { screenBrightness = -1f }
        super.onPause()
    }

    override fun onDestroy() {
        DebugHooks.light = null
        DebugHooks.applyScene = null
        studio.stop() // 조명 원래 값 복원 포함
        super.onDestroy()
    }

    companion object {
        private const val KEY_LAST_LIGHT = "last"
        private const val KEY_THEME = "theme"
    }
}

@Composable
private fun StudioScreen(
    studio: StudioVehicle, light: StudioLight, link: StudioLinkClient, presets: ScenePresets, exitingFlow: StateFlow<Boolean>,
) {
    val car by studio.state.collectAsState()
    val exiting by exitingFlow.collectAsState()
    val frame by link.frames.collectAsState()
    val connected by link.connected.collectAsState()
    val share by link.status.collectAsState()
    val edgeColor = Color(light.argb)

    Box(
        Modifier
            .fillMaxSize()
            .background(edgeColor) // 바깥 = 링라이트
    ) {
        if (light.fullLight) {
            // 풀 라이트(C13): 모니터·패널을 숨기고 앱 영역 전체를 조명으로. 밝기는 색의 밝기로. 탭하면 복귀, 다이얼은 그대로
            Box(
                Modifier.fillMaxSize()
                    .background(lerp(Color.Black, edgeColor, 0.5f + 0.5f * light.level)) // 60% 에서도 충분히 밝게
                    .clickable { light.fullLight = false },
                contentAlignment = Alignment.BottomCenter
            ) {
                Text(
                    "풀 라이트 · 다이얼로 밝기·흰 빛 톤 조절 · 화면을 탭하면 돌아갑니다",
                    color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(32.dp).background(Color(0x66000000), CircleShape).padding(horizontal = 22.dp, vertical = 12.dp),
                )
            }
        } else {
            // 안쪽 창: 밝기가 올라가면 테두리가 두꺼워진다
            Box(
                Modifier.fillMaxSize()
                    .padding((8 + 72 * light.level).dp)
                    .clip(RoundedCornerShape(INNER_RADIUS))
                    .background(StudioColors.Ink) // 테마의 가장 깊은 바닥
                    .padding(16.dp)
            ) {
                Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // 왼쪽: 폰 화면 모니터(세로 영상은 Fit, 남는 폭은 검정)
                    Box(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(24.dp)), contentAlignment = Alignment.Center) {
                        val f = frame
                        if (f != null) {
                            Image(f.asImageBitmap(), "phone screen", Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                            // "앱 하나만 공유"에서 그 앱이 가려지면 프레임이 검게 온다. 이유를 띄운다
                            if (share?.visible == false) {
                                Box(Modifier.fillMaxSize().background(Color(0xB3000000)), contentAlignment = Alignment.Center) {
                                    Message("공유한 앱이 폰에서 가려져 있습니다", "폰에서 그 앱을 다시 앞으로 가져오세요")
                                }
                            }
                        } else {
                            val stopped = share?.stopped == true
                            val (title, body) = when {
                                stopped -> "폰 화면 공유가 끝났습니다" to "폰에서 '화면 공유 시작'을 다시 눌러 주세요"
                                connected -> "폰 화면을 기다리는 중" to "폰의 Car Studio 에서 화면 공유를 시작해 주세요"
                                else -> "폰 연결 대기 중" to "폰을 연결하고 Car Studio 에서 화면 공유를 시작해 주세요"
                            }
                            MonitorPlaceholder(title, body, steps = !connected && !stopped)
                        }
                    }
                    // 오른쪽: 조명 패널
                    LightPanel(light, car.lighting, studio, presets, connected = connected, streaming = frame != null)
                }
            }
        }
        val notice: Pair<String, String?>? = when {
            exiting && car.gearFailed -> "기어 상태를 확인할 수 없어 스튜디오를 종료합니다" to null
            exiting -> "주행 기어(D·R)에서는 스튜디오를 사용할 수 없어 종료합니다" to "P 또는 N에서 다시 실행해 주세요"
            car.usable == null -> "차량 상태 확인 중…" to null
            else -> null
        }
        if (notice != null) {
            Box(Modifier.fillMaxSize().background(Color(0xF0000000)), contentAlignment = Alignment.Center) {
                Column(Modifier.widthIn(max = 760.dp).padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(notice.first, color = StudioColors.Text, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                    notice.second?.let { Text(it, color = StudioColors.TextDim, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center) }
                }
            }
        }
    }
}

/** 폰 화면이 아직 없을 때: 폰 윤곽, 상태 문장, (연결 전이면) 사용 순서. */
@Composable
private fun MonitorPlaceholder(title: String, body: String, steps: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(28.dp)) {
        Box(Modifier.size(150.dp, 300.dp).border(2.dp, StudioColors.Line, RoundedCornerShape(30.dp)), contentAlignment = Alignment.TopCenter) {
            Box(Modifier.padding(top = 16.dp).size(44.dp, 6.dp).background(StudioColors.Line, CircleShape))
        }
        Message(title, body)
        if (steps) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("폰 연결", "화면 공유 시작", "앱 하나만 공유").forEachIndexed { i, s -> StepPill(i + 1, s) }
            }
        }
    }
}

@Composable
private fun Message(title: String, body: String?) {
    Column(Modifier.padding(horizontal = 32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, color = StudioColors.Text, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        if (body != null) Text(body, color = StudioColors.TextDim, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
    }
}

@Composable
private fun StepPill(n: Int, text: String) {
    Row(
        Modifier.background(StudioColors.SurfaceHigh, CircleShape).padding(start = 6.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(28.dp).background(StudioColors.Text, CircleShape), contentAlignment = Alignment.Center) {
            Text("$n", color = StudioColors.Ink, style = MaterialTheme.typography.labelMedium)
        }
        Text(text, color = StudioColors.Text, style = MaterialTheme.typography.labelLarge)
    }
}
