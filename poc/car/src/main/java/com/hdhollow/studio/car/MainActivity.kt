package com.hdhollow.studio.car

import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
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

        val dial = DialTarget(this, onRotate = light::rotate, onPress = { light.dialColor = !light.dialColor })
        val screen = ComposeView(this).apply {
            setContent { MaterialTheme(colorScheme = darkColorScheme()) { StudioScreen(studio, light, link, presets, exiting) } }
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
        studio.stop() // 조명 원래 값 복원 포함
        super.onDestroy()
    }

    companion object {
        private const val KEY_LAST_LIGHT = "last"
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
                    "풀 라이트 · 다이얼로 밝기·색온도 조절 · 화면을 탭하면 돌아갑니다",
                    color = Color(0x99000000), fontSize = 16.sp, modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            // 안쪽 창: 밝기가 올라가면 테두리가 두꺼워진다
            Box(
                Modifier.fillMaxSize()
                    .padding((8 + 72 * light.level).dp)
                    .clip(RoundedCornerShape(INNER_RADIUS))
                    .background(Color.Black)
                    .padding(12.dp)
            ) {
                Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // 왼쪽: 폰 화면 모니터(세로 영상은 Fit, 남는 폭은 검정)
                    Box(Modifier.weight(1f).fillMaxHeight()) {
                        val f = frame
                        if (f != null) {
                            Image(f.asImageBitmap(), "phone screen", Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                        } else {
                            Text(
                                if (connected) "폰 화면을 기다리는 중…\n폰에서 화면 공유를 시작해 주세요"
                                else "폰 화면 연결 대기 중\n폰을 연결하고 Car Studio에서 화면 공유를 시작해 주세요",
                                Modifier.align(Alignment.Center), color = Color.Gray, fontSize = 22.sp, textAlign = TextAlign.Center
                            )
                        }
                    }
                    // 오른쪽: 조명 패널
                    LightPanel(light, car.lighting, studio, presets)
                }
            }
        }
        val notice = when {
            exiting && car.gearFailed -> "기어 상태를 확인할 수 없어 스튜디오를 종료합니다."
            exiting -> "주행 기어(D·R)에서는 스튜디오를 사용할 수 없어 종료합니다.\nP 또는 N에서 다시 실행해 주세요."
            car.usable == null -> "차량 상태 확인 중…"
            else -> null
        }
        if (notice != null) {
            Box(Modifier.fillMaxSize().background(Color(0xF0000000)), contentAlignment = Alignment.Center) {
                Text(notice, color = Color.White, fontSize = 28.sp, textAlign = TextAlign.Center)
            }
        }
    }
}

