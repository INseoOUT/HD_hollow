# Door (도어)

> 마지막 확인일: 2026-10-28  
> 기준 버전: Vehicle SDK 2.3.2  
> 원본:
> - [Door API Reference](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/door/entry)
> - [Vehicle.getDoor()](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/get-door)
> - [Vehicle SDK 소개](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/intro)
> - [프로젝트 설정](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/project-setup)
> - [도메인 객체 획득](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/obtain-domain-object)
> - [Capability 확인](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/check-capability)
> - [Get](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/get-property)
> - [Set](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/set-property)
> - [Callback](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/callback)
> - [권한 선언](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-permission)
> - [에러 코드](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-error-code)

Provides APIs for door lock, door position, and glove box.

- 획득: `vehicle.getDoor()` ([원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/get-door))
- 권한: 조회 `pleos.car.permission.CAR_DOORS` · 제어 `pleos.car.permission.CONTROL_CAR_DOORS`
- 함수 17개 (deprecated 2개 포함), 리스너 3개

## 기능 요약

| 기능 | Capability | Get | Set | Callback |
|---|:-:|:-:|:-:|:-:|
| GloveBoxLock | ✓ | ✓ | ✓ | ✓ |
| DoorLock | ✓ | ✓ | ✓ | ✓ |
| DoorPosition | ✓ | ✓ | ✓ | ✓ |

## 함수 목록

설명은 공식 KDoc 원문(영문)입니다. 괄호 안은 파라미터, → 뒤는 반환 타입입니다.

| 함수 | 파라미터 | 설명 | 권한 | 원본 |
|---|---|---|---|---|
| `checkGloveBoxLockCapabilities` | `onComplete: (CapabilityConfig) -> Unit, onFailed: (Exception) -> Unit` | Gets the glove box lock capabilities. | `CAR_DOORS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/door/check-glove-box-lock-capabilities) |
| `checkDoorLockCapabilities` | `onComplete: (List<DoorArea>) -> Unit, onFailed: (Exception) -> Unit` | ⚠️ Deprecated. Use the new checkDoorLockCapabilities API that returns CapabilityConfig. | `CAR_DOORS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/door/check-door-lock-capabilities) |
| `checkDoorLockCapabilities` | `onComplete: (CapabilityConfig, List<DoorArea>) -> Unit, onFailed: (Exception) -> Unit` | Gets DoorLock capabilities. | `CAR_DOORS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/door/check-door-lock-capabilities) |
| `checkDoorPositionCapabilities` | `onComplete: (Map<DoorArea, AreaBounds<Int>>) -> Unit, onFailed: (Exception) -> Unit` | ⚠️ Deprecated. Use the new checkDoorPositionCapabilities API that returns CapabilityConfig. | `CAR_DOORS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/door/check-door-position-capabilities) |
| `checkDoorPositionCapabilities` | `onComplete: (CapabilityConfig, Map<DoorArea, AreaBounds<Int>>) -> Unit, onFailed: (Exception) -> Unit` | Gets DoorPosition capabilities. | `CAR_DOORS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/door/check-door-position-capabilities) |
| `getDoorLockState` | `area: DoorArea, onComplete: (Boolean?) -> Unit, onFailed: (Exception) -> Unit` | Gets current door lock state. | `CAR_DOORS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/door/get-door-lock-state) |
| `setGloveBoxLock` | `value: Boolean, onComplete: (Boolean?) -> Unit = {}, onFailed: (Exception) -> Unit = {}` | Sets the current glove lock state. | `CONTROL_CAR_DOORS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/door/set-glove-box-lock) |
| `getCurrentGloveBoxLockState` | `onComplete: (Boolean?) -> Unit, onFailed: (Exception) -> Unit` | Gets the current glove lock state. | `CAR_DOORS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/door/get-current-glove-box-lock-state) |
| `getCurrentDoorPosition` | `area: DoorArea, onComplete: (Int?) -> Unit, onFailed: (Exception) -> Unit` | Gets the current door position state. | `CAR_DOORS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/door/get-current-door-position) |
| `setDoorLock` | `area: DoorArea, value: Boolean, onComplete: (DoorArea, Boolean?) -> Unit = { _, _ -> }, onFailed: (Exception) -> Unit = {}` | Sets the current door lock state. | `CONTROL_CAR_DOORS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/door/set-door-lock) |
| `setDoorPosition` | `area: DoorArea, value: Int, onComplete: (DoorArea, Int?) -> Unit = { _, _ -> }, onFailed: (Exception) -> Unit = {}` | Sets the current door position state. The maximum (fully open) value is vehicle-dependent and is not fixed. Call `checkDoorPositionCapabilities` first. That API's callback returns `CapabilityConfig` plus a `Map`<`DoorArea`, `AreaBounds`<`Int`>>; read each area's `AreaBounds.minValue`/`AreaBounds.maxValue` from that map (not from `CapabilityConfig`). Min is typically 0 for fully closed. | `CONTROL_CAR_DOORS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/door/set-door-position) |
| `registerDoorPosition` | `area: DoorArea, listener: DoorPositionListener` | Registers the door area to get property updates. | `CAR_DOORS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/door/register-door-position) |
| `unregisterDoorPosition` | `area: DoorArea, listener: DoorPositionListener` | Stops getting property updates for the given door area. | `CAR_DOORS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/door/unregister-door-position) |
| `registerDoorLockState` | `area: DoorArea, listener: DoorLockStateListener` | Registers the door lock state to get property updates. | `CAR_DOORS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/door/register-door-lock-state) |
| `unregisterDoorLockState` | `area: DoorArea, listener: DoorLockStateListener` | Stops getting property updates for the given door lock state. | `CAR_DOORS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/door/unregister-door-lock-state) |
| `registerGloveBoxLockState` | `listener: GloveBoxLockStateListener` | Registers the glove box lock state to get property updates. | `CAR_DOORS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/door/register-glove-box-lock-state) |
| `unregisterGloveBoxLockState` | `listener: GloveBoxLockStateListener` | Stops getting property updates for the given glove box lock state. | `CAR_DOORS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/door/unregister-glove-box-lock-state) |

## 리스너 (Callback)

`register*` 함수에 넘기는 리스너와 콜백 시그니처입니다.

| 리스너 | 콜백 | 설명 | 원본 |
|---|---|---|---|
| `DoorPositionListener` | `onPositionUpdated(pos: Int?)` | Callback invoked when the door position updates. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.listener/door-position-listener/entry) |
| `DoorLockStateListener` | `onDoorLockStateUpdated(state: Boolean?)` | Callback invoked when the door lock state updates. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.listener/door-lock-state-listener/entry) |
| `GloveBoxLockStateListener` | `onGloveBoxLockStateUpdated(state: Boolean?)` | Callback invoked when the GloveBox lock state updates. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.listener/glove-box-lock-state-listener/entry) |

## 관련 타입 (모델·상수)

파라미터와 콜백에 쓰이는 영역(Area)·Capability·상수 타입은 공식 API Reference 패키지 페이지에서 확인합니다.

- [api.model.area (DoorArea, SeatArea 등 영역 타입)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.area/entry)
- [api.model.capability (CapabilityConfig, AccessType, AreaBounds)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.capability/entry)
- [api.model.common (공통 모델)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.common/entry)
- [api.model.telltale (경고등 모델)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.telltale/entry)
- [constant (PleosPermission 등 상수)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.constant/entry)
- [constant.area / constant.control / constant.unit / constant.telltale](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.constant.area/entry)
- [callback.exception (VehicleException, 에러 코드)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.callback.exception/entry)
