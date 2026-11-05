package com.hdhollow.studio.car

import ai.pleos.playground.vehicle.Vehicle
import ai.pleos.playground.vehicle.api.model.area.SeatArea
import ai.pleos.playground.vehicle.api.model.area.WindowArea
import ai.pleos.playground.vehicle.api.model.capability.AreaBounds
import ai.pleos.playground.vehicle.listener.HvacFanSpeedListener
import ai.pleos.playground.vehicle.listener.WindowPositionListener
import android.car.Car
import android.car.VehiclePropertyIds
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** 스튜디오에 필요한 차량 상태. null = 아직 모름(미지원·실패 포함). */
data class CarState(
    val gear: Int? = null,
    val fanSpeed: Int? = null,
    val openWindows: Set<String> = emptySet(),
    val ambientAreas: String = "확인 중",
    val log: List<String> = emptyList(),
)

/**
 * Vehicle SDK(앰비언트·공조·창문) + AAOS Car API(기어) 래퍼.
 * PoC 목적: 각 호출이 에뮬레이터에서 실제로 되는지 화면 로그로 확인한다.
 */
class StudioVehicle(context: Context) {

    private val vehicle = Vehicle(context.applicationContext)
    private val car: Car? = runCatching { Car.createCar(context.applicationContext) }.getOrNull()
    private val _state = MutableStateFlow(CarState())
    val state: StateFlow<CarState> = _state.asStateFlow()

    private var brightnessAreas: Map<SeatArea, AreaBounds<Int>> = emptyMap()
    private var colorAreas: List<SeatArea> = emptyList()
    private val fanListeners = mutableListOf<Pair<SeatArea, HvacFanSpeedListener>>()
    private val windowListeners = mutableListOf<Pair<WindowArea, WindowPositionListener>>()

    private val gearCallback = object : CarPropertyManager.CarPropertyEventCallback {
        override fun onChangeEvent(value: CarPropertyValue<*>) {
            _state.update { it.copy(gear = value.value as? Int) }
        }
        override fun onErrorEvent(propId: Int, zone: Int) = log("기어 오류 prop=$propId zone=$zone")
    }

    fun start() {
        runCatching { vehicle.initialize() }.onFailure { log("Vehicle.initialize 실패: ${it.message}"); return }
        log("Vehicle SDK 연결됨")
        val light = vehicle.getLight()
        light.checkAmbientLightBrightnessCapabilities({ _, areas ->
            brightnessAreas = areas
            _state.update { it.copy(ambientAreas = areas.entries.joinToString { (a, b) -> "${a.values.joinToString("+")}(${b.minValue}..${b.maxValue})" }.ifEmpty { "없음" }) }
            log("앰비언트 밝기 지원 영역 ${areas.size}개")
        }, { log("앰비언트 밝기 capability 실패: ${it.message}") })
        light.checkAmbientLightBrightnessColorCapabilities({ _, areas ->
            colorAreas = areas
            log("앰비언트 색 지원 영역 ${areas.size}개")
        }, { log("앰비언트 색 capability 실패: ${it.message}") })

        val hvac = vehicle.getHVAC()
        hvac.checkHvacFanSpeedCapabilities({ _, areas ->
            areas.keys.forEach { area ->
                val l = object : HvacFanSpeedListener {
                    override fun onHvacFanSpeedUpdated(speed: Int?) = _state.update { it.copy(fanSpeed = speed) }
                    override fun onFailed(e: Exception) = log("풍량 구독 오류: ${e.message}")
                }
                hvac.registerHvacFanSpeed(area, l)
                fanListeners += area to l
            }
            log("풍량 구독 ${areas.size}개 영역")
        }, { log("풍량 capability 실패: ${it.message}") })

        val window = vehicle.getWindow()
        window.checkWindowPositionCapabilities({ _, areas ->
            areas.keys.forEach { area ->
                val name = area.values.joinToString("+")
                val l = object : WindowPositionListener {
                    override fun onWindowPositionUpdated(pos: Int?) = _state.update { s ->
                        s.copy(openWindows = if ((pos ?: 0) > 0) s.openWindows + name else s.openWindows - name)
                    }
                    override fun onFailed(e: Exception) = log("창문 구독 오류: ${e.message}")
                }
                window.registerWindowPosition(area, l)
                windowListeners += area to l
            }
            log("창문 구독 ${areas.size}개 영역")
        }, { log("창문 capability 실패: ${it.message}") })

        runCatching {
            val cpm = car!!.getCarManager(Car.PROPERTY_SERVICE) as CarPropertyManager
            cpm.registerCallback(gearCallback, VehiclePropertyIds.GEAR_SELECTION, CarPropertyManager.SENSOR_RATE_ONCHANGE)
            log("기어 구독 시작")
        }.onFailure { log("기어 구독 실패: ${it.message}") }
    }

    /** 앰비언트 라이트를 스튜디오 조명과 맞춘다. level 0..1, argb = 0xAARRGGBB. */
    fun syncAmbient(level: Float, argb: Int) {
        val light = vehicle.getLight()
        brightnessAreas.forEach { (area, b) ->
            val v = (b.minValue + (b.maxValue - b.minValue) * level).toInt()
            light.setAmbientLightBrightness(area, v, { _, set -> log("앰비언트 밝기 set OK → $set") },
                { log("앰비언트 밝기 set 실패: ${it.message}") })
        }
        colorAreas.forEach { area ->
            light.setAmbientLightBrightnessColor(area, argb, { _, set -> log("앰비언트 색 set OK → #${Integer.toHexString(set)}") },
                { log("앰비언트 색 set 실패: ${it.message}") })
        }
    }

    fun stop() {
        runCatching {
            val hvac = vehicle.getHVAC(); fanListeners.forEach { (a, l) -> hvac.unregisterHvacFanSpeed(a, l) }
            val window = vehicle.getWindow(); windowListeners.forEach { (a, l) -> window.unregisterWindowPosition(a, l) }
            (car?.getCarManager(Car.PROPERTY_SERVICE) as? CarPropertyManager)?.unregisterCallback(gearCallback)
            vehicle.release()
        }
        car?.disconnect()
    }

    private fun log(msg: String) = _state.update { it.copy(log = (listOf(msg) + it.log).take(8)) }

    companion object {
        // android.car.VehicleGear 값
        const val GEAR_PARK = 4
        const val GEAR_DRIVE = 8
        const val GEAR_REVERSE = 2
    }
}
