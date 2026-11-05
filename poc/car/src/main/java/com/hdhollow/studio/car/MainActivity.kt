package com.hdhollow.studio.car

import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.ComposeView
import com.android.car.ui.FocusArea
import com.android.car.ui.FocusParkingView
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/** Virtual Console 앰비언트 색 4종 + 따뜻한 흰색. */
private val PRESETS = listOf(
    "White" to Color(0xFFFFFFFF),
    "Warm" to Color(0xFFFFE2B8),
    "Cyan" to Color(0xFF00E5FF),
    "Blue" to Color(0xFF2F6BFF),
    "Purple" to Color(0xFFB04DFF),
)

private val INNER_RADIUS = 40.dp

/** 조명 상태. 다이얼 뷰(View)와 화면(Compose)이 같이 쓴다. */
class Light {
    var level by mutableFloatStateOf(0.6f)
    var colorIdx by mutableIntStateOf(0)
    var dialColor by mutableStateOf(false) // 다이얼이 조절하는 대상: false=밝기, true=색 (화면 터치·nudge로 전환)

    fun rotate(step: Int) {
        if (dialColor) colorIdx = (colorIdx + step).mod(PRESETS.size)
        else level = (level + step * 0.05f).coerceIn(0f, 1f)
    }
}

class MainActivity : ComponentActivity() {

    private lateinit var studio: StudioVehicle
    private val light = Light()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        studio = StudioVehicle(this).also { it.start() }
        val dial = DialTarget(this, onRotate = light::rotate, onPress = { light.dialColor = !light.dialColor })
        val screen = ComposeView(this).apply {
            setContent { MaterialTheme(colorScheme = darkColorScheme()) { StudioScreen(studio, light) } }
        }
        // RotaryService 요건: 창마다 FocusParkingView(맨 앞, FocusArea 밖) + 포커스 대상은 FocusArea 안.
        // 하나라도 없으면 회전을 버리거나 포커스를 Driving view로 넘긴다.
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
        // 링라이트처럼 쓰기 위해 이 창의 화면 밝기를 최대로(Pleos에서 허용되는지 PoC 확인 항목)
        window.attributes = window.attributes.apply { screenBrightness = 1f }
    }

    override fun onPause() {
        window.attributes = window.attributes.apply { screenBrightness = -1f }
        super.onPause()
    }

    override fun onDestroy() {
        studio.stop()
        super.onDestroy()
    }
}

@Composable
private fun StudioScreen(studio: StudioVehicle, light: Light) {
    val car by studio.state.collectAsState()
    val level = light.level
    val colorIdx = light.colorIdx
    var ambientSync by remember { mutableStateOf(true) }
    var address by remember { mutableStateOf("") }
    var connectTo by remember { mutableStateOf<String?>(null) }
    var frame by remember { mutableStateOf<Frame?>(null) }
    var streamStatus by remember { mutableStateOf("폰 미연결") }
    var fps by remember { mutableFloatStateOf(0f) }

    val driving = car.gear == StudioVehicle.GEAR_DRIVE || car.gear == StudioVehicle.GEAR_REVERSE
    val edgeColor = PRESETS[colorIdx].second

    // 주행 기어면 스트림을 끊는다(주행 중 영상 표시 금지)
    LaunchedEffect(driving) { if (driving) connectTo = null }

    LaunchedEffect(connectTo) {
        val addr = connectTo ?: run { frame = null; return@LaunchedEffect }
        streamStatus = "$addr 연결 중"
        var count = 0
        var windowStart = System.currentTimeMillis()
        runCatching {
            mjpegFrames(addr).collect { f ->
                frame = f
                count++
                val now = System.currentTimeMillis()
                if (now - windowStart >= 1000) {
                    fps = count * 1000f / (now - windowStart); count = 0; windowStart = now
                }
                streamStatus = "수신 중 · 프레임 나이 ≈ ${f.receivedAt - f.sentAt}ms(시계 오차 포함)"
            }
            streamStatus = "스트림 종료"
        }.onFailure { streamStatus = "연결 실패: ${it.message}" }
    }

    // 조명 변경을 차량 앰비언트에 반영(150ms 디바운스)
    LaunchedEffect(level, colorIdx, ambientSync) {
        if (!ambientSync) return@LaunchedEffect
        delay(150)
        studio.syncAmbient(level, edgeColor.toArgb())
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(edgeColor) // 바깥 = 링라이트
    ) {
        // 안쪽 창: 모서리를 둥글게 깎아 조명이 모서리에서 부드럽게 감싸도록
        Box(
            Modifier.fillMaxSize()
                .padding((8 + 72 * level).dp)
                .clip(RoundedCornerShape(INNER_RADIUS))
                .background(Color.Black)
                .padding(12.dp)
        ) {
            // 가운데: 폰 카메라 모니터
            val f = frame
            if (f != null) {
                Image(f.bitmap.asImageBitmap(), "phone camera", Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
            } else {
                Text("폰 카메라 모니터\n(아래에서 폰 주소 입력)", Modifier.align(Alignment.Center), color = Color.Gray, fontSize = 22.sp)
            }

            StatusPanel(car, streamStatus, fps, Modifier.align(Alignment.TopStart))

            Column(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                    .background(Color(0xCC000000)).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(address, { address = it }, Modifier.width(320.dp), label = { Text("폰 주소 (예: 192.168.0.12:8080)") }, singleLine = true)
                    Button({ connectTo = address.trim().ifEmpty { null } }, enabled = !driving) { Text("연결") }
                    Button({ connectTo = null }) { Text("끊기") }
                    Text("앰비언트 연동", color = Color.White)
                    Switch(ambientSync, { ambientSync = it })
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "밝기 ${(level * 100).toInt()}%\n다이얼: ${if (light.dialColor) "색" else "밝기"} ⇄",
                        color = Color.White,
                        // 에뮬레이터에선 다이얼 누름·nudge가 앱에 안 와서(RotaryService가 소비) 터치로 전환
                        modifier = Modifier.width(110.dp).clickable { light.dialColor = !light.dialColor }
                    )
                    Slider(level, { light.level = it }, Modifier.width(360.dp))
                    PRESETS.forEachIndexed { i, (name, c) ->
                        Box(
                            Modifier.size(36.dp).background(c, CircleShape)
                                .border(if (i == colorIdx) 3.dp else 0.dp, Color.White, CircleShape)
                                .clickable { light.colorIdx = i }
                        )
                        if (i == colorIdx) Text(name, color = Color.White)
                    }
                }
            }
        }

        if (driving) {
            Box(Modifier.fillMaxSize().background(Color(0xF0000000)), contentAlignment = Alignment.Center) {
                Text("주행 중에는 스튜디오를 사용할 수 없습니다.\n기어를 P로 바꿔 주세요.", color = Color.White, fontSize = 28.sp)
            }
        }
    }
}

@Composable
private fun StatusPanel(car: CarState, stream: String, fps: Float, modifier: Modifier) {
    val gearName = when (car.gear) {
        StudioVehicle.GEAR_PARK -> "P"; StudioVehicle.GEAR_DRIVE -> "D"; StudioVehicle.GEAR_REVERSE -> "R"
        null -> "?"; else -> "기타(${car.gear})"
    }
    val warnings = buildList {
        if ((car.fanSpeed ?: 0) >= 3) add("풍량 ${car.fanSpeed}단: 녹음 소음 주의")
        if (car.openWindows.isNotEmpty()) add("창문 열림(${car.openWindows.joinToString()}): 외부 소음 주의")
    }
    Column(modifier.background(Color(0xAA000000)).padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text("기어 $gearName · 풍량 ${car.fanSpeed ?: "?"} · ${"%.1f".format(fps)} fps", color = Color.White)
        Text("영상: $stream", color = Color.LightGray, fontSize = 12.sp)
        Text("앰비언트 영역: ${car.ambientAreas}", color = Color.LightGray, fontSize = 12.sp)
        warnings.forEach { Text("⚠ $it", color = Color(0xFFFFC857)) }
        car.log.forEach { Text("· $it", color = Color.Gray, fontSize = 11.sp) }
    }
}
