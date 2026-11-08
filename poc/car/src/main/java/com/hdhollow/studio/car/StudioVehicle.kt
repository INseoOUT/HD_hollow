package com.hdhollow.studio.car

import ai.pleos.playground.vehicle.Vehicle
import ai.pleos.playground.vehicle.api.Light as VehicleLight
import ai.pleos.playground.vehicle.api.model.area.SeatArea
import ai.pleos.playground.vehicle.api.model.capability.AreaBounds
import ai.pleos.playground.vehicle.constant.control.LightState
import ai.pleos.playground.vehicle.constant.control.LightSwitch
import ai.pleos.playground.vehicle.listener.AmbientLightBrightnessColorListener
import ai.pleos.playground.vehicle.listener.AmbientLightBrightnessListener
import ai.pleos.playground.vehicle.listener.LightStateListener
import android.car.Car
import android.car.VehiclePropertyIds
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/** 앰비언트 영역 하나의 현재 값. 패널 표시와 링라이트 연동에 쓴다. */
data class AmbientArea(
    val brightness: Int? = null,
    val bounds: AreaBounds<Int>? = null, // 밝기 범위. null 이면 이 영역은 밝기 설정 불가
    val color: Int? = null, // ARGB
    val colorSupported: Boolean = false,
    val switch: LightSwitch? = null,
    val switchOptions: List<LightSwitch> = emptyList(),
)

/** 차량 조명 상태(조회 결과). 좌석 목록은 enum 이 아니라 capability 콜백이 준 SeatArea 키다. */
data class Lighting(
    val ambient: Map<SeatArea, AmbientArea> = emptyMap(),
    val cabin: LightSwitch? = null,
    val cabinOptions: List<LightSwitch> = emptyList(),
    val reading: Map<SeatArea, LightSwitch?> = emptyMap(),
    val readingOptions: Map<SeatArea, List<LightSwitch>> = emptyMap(),
) {
    /** 밝기나 색 중 하나라도 바꿀 수 있는 앰비언트 영역이 있다. */
    val ambientReady: Boolean get() = ambient.values.any { it.bounds != null || it.colorSupported }
    val hasCabin: Boolean get() = cabinOptions.isNotEmpty()
    val hasReading: Boolean get() = readingOptions.isNotEmpty()
}

/** 스튜디오에 필요한 차량 상태. */
data class CarState(
    val gear: Int? = null, // null = 아직 못 읽음
    val gearFailed: Boolean = false, // 기어 구독 실패 또는 오류
    val lighting: Lighting = Lighting(),
) {
    /** P·N이면 true, 그 밖의 기어(D·R 등)나 기어 읽기 실패면 false, 아직 모르면 null. */
    val usable: Boolean?
        get() = when (gear) {
            StudioVehicle.GEAR_PARK, StudioVehicle.GEAR_NEUTRAL -> true
            null -> if (gearFailed) false else null
            else -> false
        }
}

/**
 * Vehicle SDK(조명) + AAOS Car API(기어) 래퍼.
 *
 * 조명은 시작할 때 원래 값을 읽어 "되돌리는 동작"을 하나씩 쌓아 두고, stop() 에서 전부 실행한 뒤 SDK 를 해제한다.
 * 먼저 해제하면 되돌리는 set 이 버려지므로 콜백 수를 세어 기다린다(콜백이 안 오면 1초 뒤 해제).
 * 바꾼 적이 없으면 되돌리지 않는다.
 */
class StudioVehicle(context: Context) {

    private val vehicle = Vehicle(context.applicationContext)
    private val car: Car? = runCatching { Car.createCar(context.applicationContext) }.getOrNull()
    private val _state = MutableStateFlow(CarState())
    val state: StateFlow<CarState> = _state.asStateFlow()

    // SDK 콜백 스레드가 문서에 없어서 동시 접근 가능한 컬렉션을 쓴다
    private val restore = CopyOnWriteArrayList<(done: () -> Unit) -> Unit>()
    private val cleanups = CopyOnWriteArrayList<() -> Unit>() // 리스너 해제
    @Volatile private var changed = false // 하나라도 바꿨으면 종료 시 복원
    @Volatile private var stopped = false // 복원 뒤 늦게 도착한 set 이 스튜디오 값을 다시 칠하지 않도록

    private val gearCallback = object : CarPropertyManager.CarPropertyEventCallback {
        override fun onChangeEvent(value: CarPropertyValue<*>) {
            _state.update { it.copy(gear = value.value as? Int) }
        }

        override fun onErrorEvent(propId: Int, zone: Int) {
            Log.w(TAG, "기어 오류 prop=$propId zone=$zone")
            _state.update { it.copy(gear = null, gearFailed = true) }
        }
    }

    fun start() {
        // 기어는 사용 가부를 정하므로 Vehicle SDK 연결 성공 여부와 상관없이 먼저 구독한다
        val gearOk = runCatching {
            val cpm = car!!.getCarManager(Car.PROPERTY_SERVICE) as CarPropertyManager
            cpm.registerCallback(gearCallback, VehiclePropertyIds.GEAR_SELECTION, CarPropertyManager.SENSOR_RATE_ONCHANGE)
        }.onFailure { Log.w(TAG, "기어 구독 실패", it) }.getOrDefault(false)
        if (!gearOk) _state.update { it.copy(gearFailed = true) }

        runCatching { vehicle.initialize() }.onFailure { Log.w(TAG, "Vehicle.initialize 실패", it); return }
        val light = vehicle.getLight()
        readAmbient(light)
        readCabin(light)
        readReading(light)
    }

    // ---- 조회(원래 값) + 외부 변경 추적 ----

    private fun readAmbient(light: VehicleLight) {
        light.checkAmbientLightBrightnessCapabilities({ _, areas ->
            areas.forEach { (area, bounds) ->
                updateAmbient(area) { it.copy(bounds = bounds) }
                light.getAmbientLightBrightness(area, { v ->
                    Log.d(TAG, "앰비언트 밝기 원래 값 $v (${bounds.minValue}..${bounds.maxValue}) ${area.label()}")
                    if (v != null) {
                        updateAmbient(area) { it.copy(brightness = v) }
                        restore += { done -> light.setAmbientLightBrightness(area, v, { _, _ -> done() }, { done() }) }
                    }
                }, { Log.w(TAG, "앰비언트 밝기 읽기 실패", it) })
                val l = object : AmbientLightBrightnessListener {
                    override fun onBrightnessUpdated(brightness: Int?) {
                        updateAmbient(area) { it.copy(brightness = brightness) }
                    }
                    override fun onFailed(e: Exception) {
                        Log.w(TAG, "앰비언트 밝기 리스너 오류", e)
                    }
                }
                light.registerAmbientLightBrightnessListener(area, l)
                cleanups += { light.unregisterAmbientLightBrightnessListener(area, l) }
            }
        }, { Log.w(TAG, "앰비언트 밝기 capability 실패", it) })

        light.checkAmbientLightBrightnessColorCapabilities({ _, areas ->
            areas.forEach { area ->
                updateAmbient(area) { it.copy(colorSupported = true) }
                light.getAmbientLightBrightnessColor(area, { c ->
                    Log.d(TAG, "앰비언트 색 원래 값 #${c?.let(Integer::toHexString)} ${area.label()}")
                    if (c != null) {
                        updateAmbient(area) { it.copy(color = c) }
                        restore += { done -> light.setAmbientLightBrightnessColor(area, c, { _, _ -> done() }, { done() }) }
                    }
                }, { Log.w(TAG, "앰비언트 색 읽기 실패", it) })
                val l = object : AmbientLightBrightnessColorListener {
                    override fun onBrightnessColorUpdated(argbColor: Int?) {
                        updateAmbient(area) { it.copy(color = argbColor) }
                    }
                    override fun onFailed(e: Exception) {
                        Log.w(TAG, "앰비언트 색 리스너 오류", e)
                    }
                }
                light.registerAmbientLightBrightnessColorListener(area, l)
                cleanups += { light.unregisterAmbientLightBrightnessColorListener(area, l) }
            }
        }, { Log.w(TAG, "앰비언트 색 capability 실패", it) })

        light.checkAmbientLightSwitchCapabilities({ _, areas ->
            areas.forEach { (area, options) ->
                updateAmbient(area) { it.copy(switchOptions = options) }
                light.getAmbientLightSwitch(area, { sw ->
                    Log.d(TAG, "앰비언트 스위치 원래 값 $sw ${area.label()}")
                    if (sw != null) {
                        updateAmbient(area) { it.copy(switch = sw) }
                        restore += { done -> light.setAmbientLightSwitch(area, sw, { _, _ -> done() }, { done() }) }
                    }
                }, { Log.w(TAG, "앰비언트 스위치 읽기 실패", it) })
            }
        }, { Log.w(TAG, "앰비언트 스위치 capability 실패", it) })
    }

    private fun readCabin(light: VehicleLight) {
        light.checkCabinLightSwitchCapabilities({ _, options ->
            _state.update { s -> s.copy(lighting = s.lighting.copy(cabinOptions = options)) }
            light.getCabinLightSwitch({ sw ->
                Log.d(TAG, "실내등 원래 값 $sw")
                if (sw != null) {
                    setCabinState(sw)
                    restore += { done -> light.setCabinLightSwitch(sw, { done() }, { done() }) }
                }
            }, { Log.w(TAG, "실내등 읽기 실패", it) })
            // 스위치 변경 리스너는 없고 상태(켜짐/꺼짐) 리스너만 있다. 바깥에서 바꾸면 패널이 따라가게 상태로 근사한다
            val l = object : LightStateListener {
                override fun onLightStateUpdated(state: LightState?) {
                    state?.let { setCabinState(if (it == LightState.OFF) LightSwitch.OFF else LightSwitch.ON) }
                }
                override fun onFailed(e: Exception) {
                    Log.w(TAG, "실내등 리스너 오류", e)
                }
            }
            light.registerCabinLightState(l)
            cleanups += { light.unregisterCabinLightState(l) }
        }, { Log.w(TAG, "실내등 capability 실패", it) })
    }

    private fun readReading(light: VehicleLight) {
        light.checkReadingLightSwitchCapabilities({ _, areas ->
            _state.update { s -> s.copy(lighting = s.lighting.copy(readingOptions = areas)) }
            areas.keys.forEach { area ->
                light.getReadingLightSwitch(area, { sw ->
                    Log.d(TAG, "독서등 원래 값 $sw ${area.label()}")
                    if (sw != null) {
                        setReadingState(area, sw)
                        restore += { done -> light.setReadingLightSwitch(area, sw, { done() }, { done() }) }
                    }
                }, { Log.w(TAG, "독서등 읽기 실패", it) })
                val l = object : LightStateListener {
                    override fun onLightStateUpdated(state: LightState?) {
                        state?.let { setReadingState(area, if (it == LightState.OFF) LightSwitch.OFF else LightSwitch.ON) }
                    }
                    override fun onFailed(e: Exception) {
                        Log.w(TAG, "독서등 리스너 오류", e)
                    }
                }
                light.registerReadingLightState(area, l)
                cleanups += { light.unregisterReadingLightState(area, l) }
            }
        }, { Log.w(TAG, "독서등 capability 실패", it) })
    }

    // ---- 설정 ----

    /** 링라이트와 같은 색·밝기를 앰비언트에. level 0..1, argb = 0xAARRGGBB. 원래 값을 읽은 영역만 바꾼다. */
    fun syncAmbient(level: Float, argb: Int) {
        if (stopped) return
        val lighting = _state.value.lighting
        if (!lighting.ambientReady) return
        changed = true
        val light = vehicle.getLight()
        lighting.ambient.forEach { (area, a) ->
            a.bounds?.let { b ->
                val v = (b.minValue + (b.maxValue - b.minValue) * level).toInt()
                light.setAmbientLightBrightness(area, v, { _, set -> updateAmbient(area) { it.copy(brightness = set) } },
                    { Log.w(TAG, "앰비언트 밝기 set 실패", it) })
            }
            if (a.colorSupported) {
                light.setAmbientLightBrightnessColor(area, argb, { _, set -> updateAmbient(area) { it.copy(color = set) } },
                    { Log.w(TAG, "앰비언트 색 set 실패", it) })
            }
        }
    }

    fun setAmbientSwitch(area: SeatArea, sw: LightSwitch) {
        if (stopped) return
        changed = true
        vehicle.getLight().setAmbientLightSwitch(area, sw, { _, set -> updateAmbient(area) { it.copy(switch = set) } },
            { Log.w(TAG, "앰비언트 스위치 set 실패", it) })
    }

    fun setCabin(sw: LightSwitch) {
        if (stopped) return
        changed = true
        vehicle.getLight().setCabinLightSwitch(sw, { set -> set?.let(::setCabinState) }, { Log.w(TAG, "실내등 set 실패", it) })
    }

    fun setReading(area: SeatArea, sw: LightSwitch) {
        if (stopped) return
        changed = true
        vehicle.getLight().setReadingLightSwitch(area, sw, { set -> setReadingState(area, set) },
            { Log.w(TAG, "독서등 set 실패", it) })
    }

    /** 조명을 원래 값으로 되돌린 뒤 해제한다. */
    fun stop() {
        stopped = true
        runCatching { (car?.getCarManager(Car.PROPERTY_SERVICE) as? CarPropertyManager)?.unregisterCallback(gearCallback) }
        cleanups.forEach { runCatching { it() } }
        val released = AtomicBoolean(false)
        val release = {
            if (released.compareAndSet(false, true)) {
                runCatching { vehicle.release() }
                car?.disconnect()
            }
        }
        if (!changed || restore.isEmpty()) return release()

        val pending = AtomicInteger(restore.size)
        val done = { if (pending.decrementAndGet() == 0) release() }
        restore.forEach { action -> runCatching { action(done) }.onFailure { done() } }
        Handler(Looper.getMainLooper()).postDelayed({ release() }, 1000) // 콜백이 안 오면 1초 뒤 해제
    }

    // ---- 상태 갱신 ----

    private fun updateAmbient(area: SeatArea, f: (AmbientArea) -> AmbientArea) = _state.update { s ->
        val cur = s.lighting.ambient[area] ?: AmbientArea()
        s.copy(lighting = s.lighting.copy(ambient = s.lighting.ambient + (area to f(cur))))
    }

    private fun setCabinState(sw: LightSwitch) = _state.update { s -> s.copy(lighting = s.lighting.copy(cabin = sw)) }

    private fun setReadingState(area: SeatArea, sw: LightSwitch?) = _state.update { s ->
        s.copy(lighting = s.lighting.copy(reading = s.lighting.reading + (area to sw)))
    }

    companion object {
        private const val TAG = "StudioVehicle"

        // android.car.VehicleGear 값. Pleos SDK 에는 기어 API 가 없어 AAOS Car API 로 읽는다
        const val GEAR_NEUTRAL = 1
        const val GEAR_PARK = 4
    }
}
