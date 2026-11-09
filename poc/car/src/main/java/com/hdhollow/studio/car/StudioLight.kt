package com.hdhollow.studio.car

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.hdhollow.studio.shared.ColorTemp
import com.hdhollow.studio.shared.LightingParams
import kotlin.math.roundToInt

/** 다이얼·슬라이더·폰이 공유하는 조명 값의 순수 계산. 테스트 대상. */
object LightLogic {
    /** 다이얼 1칸 = 250K. 범위에서 멈춘다. */
    fun stepKelvin(kelvin: Int, step: Int): Int =
        (kelvin + step * LightingParams.KELVIN_DIAL_STEP).coerceIn(LightingParams.KELVIN_MIN, LightingParams.KELVIN_MAX)

    /** 다이얼 1칸 = 0.05. 소수 둘째 자리로 맞춰 부동소수 오차가 쌓이지 않게 한다. */
    fun stepLevel(level: Float, step: Int): Float {
        val percent = (level * 100).roundToInt() + step * (LightingParams.LEVEL_DIAL_STEP * 100).roundToInt()
        return percent.coerceIn(0, 100) / 100f
    }

    /** 링라이트와 앰비언트에 같이 넣는 색. preset 이 있으면 무드 색, 없으면 색온도. */
    fun argb(params: LightingParams): Int = ColorTemp.colorFor(params)
}

/**
 * 조명 상태. 다이얼 뷰(View)와 화면(Compose), 폰 동기화가 같이 쓴다.
 * 색은 색온도([kelvin]) 또는 무드 색([preset]) 중 하나다. 무드 색을 고르면 색온도 모드에서 벗어나고,
 * 색온도를 돌리면 무드 색이 풀린다.
 * [ts] 는 사용자가 값을 마지막으로 바꾼 시각(0 = 기본값). 폰 값과 어느 쪽이 최신인지 가릴 때 쓴다(LightingParams.supersedes).
 */
class StudioLight(private val now: () -> Long = System::currentTimeMillis) {
    private var levelState by mutableFloatStateOf(LightingParams.LEVEL_DEFAULT)
    private var kelvinState by mutableIntStateOf(LightingParams.KELVIN_DEFAULT)
    private var presetState by mutableStateOf<String?>(null)
    var ts by mutableLongStateOf(0L)
        private set

    var level: Float
        get() = levelState
        set(v) {
            levelState = v.coerceIn(0f, 1f)
            ts = now()
        }
    val kelvin: Int get() = kelvinState
    val preset: String? get() = presetState

    var dialColor by mutableStateOf(false) // 다이얼이 조절하는 대상: false=밝기, true=색온도 (화면 터치·nudge 로 전환)
    var ambientLinked by mutableStateOf(true) // 링라이트 색·밝기를 앰비언트에도 넣는다(패널 토글)
    var fullLight by mutableStateOf(false) // 풀 라이트 모드: 모니터·패널을 숨기고 앱 영역 전체를 조명으로

    fun rotate(step: Int) {
        if (dialColor) chooseKelvin(LightLogic.stepKelvin(kelvin, step)) else level = LightLogic.stepLevel(level, step)
    }

    fun chooseKelvin(k: Int) {
        presetState = null
        kelvinState = k.coerceIn(LightingParams.KELVIN_MIN, LightingParams.KELVIN_MAX)
        ts = now()
    }

    fun choosePreset(name: String?) {
        presetState = name?.takeIf { it in LightingParams.PRESETS }
        ts = now()
    }

    /** 폰(또는 저장된 값)에서 온 값을 그대로 받는다. ts 도 그 값의 것을 따른다. */
    fun apply(p: LightingParams) {
        val n = p.normalized()
        levelState = n.level
        kelvinState = n.kelvin
        presetState = n.preset
        ts = p.ts
    }

    /** 지금 값. */
    fun params(src: String): LightingParams = LightingParams(kelvin = kelvin, level = level, preset = preset, src = src, ts = ts)

    val argb: Int get() = LightLogic.argb(params(""))

    /** 패널 라벨용. 예: "3500K", "퍼플". */
    val colorLabel: String
        get() = when (preset) {
            null -> "${kelvin}K"
            "cyan" -> "시안"
            "blue" -> "블루"
            "purple" -> "퍼플"
            else -> preset ?: ""
        }
}
