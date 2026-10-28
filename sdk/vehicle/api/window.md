# Window (윈도우)

> 마지막 확인일: 2026-10-28  
> 기준 버전: Vehicle SDK 2.3.2  
> 원본:
> - [Window API Reference](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/window/entry)
> - [Vehicle.getWindow()](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/get-window)
> - [Vehicle SDK 소개](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/intro)
> - [프로젝트 설정](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/project-setup)
> - [도메인 객체 획득](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/obtain-domain-object)
> - [Capability 확인](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/check-capability)
> - [Get](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/get-property)
> - [Set](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/set-property)
> - [Callback](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/callback)
> - [권한 선언](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-permission)
> - [에러 코드](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-error-code)

Provides APIs for window position, lock, blinds, and smart glass.

- 획득: `vehicle.getWindow()` ([원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/get-window))
- 권한: 조회 `pleos.car.permission.CAR_WINDOWS` · 제어 `pleos.car.permission.CONTROL_CAR_WINDOWS`
- 함수 31개 (deprecated 2개 포함), 리스너 4개

## 기능 요약

| 기능 | Capability | Get | Set | Callback |
|---|:-:|:-:|:-:|:-:|
| MoveWindowBlind | ✓ |  |  |  |
| WindowBlindPosition | ✓ | ✓ |  | ✓ |
| SmartGlassState | ✓ | ✓ | ✓ | ✓ |
| SmartGlassZoneState | ✓ |  | ✓ |  |
| SmartGlassLevel | ✓ | ✓ | ✓ | ✓ |
| WindowLock | ✓ | ✓ | ✓ | ✓ |
| WindowPosition | ✓ | ✓ | ✓ | ✓ |

기타 함수: `moveWindowBlind`, `getSmartGlassConfig`

## 함수 목록

설명은 공식 KDoc 원문(영문)입니다. 괄호 안은 파라미터, → 뒤는 반환 타입입니다.

| 함수 | 파라미터 | 설명 | 권한 | 원본 |
|---|---|---|---|---|
| `checkMoveWindowBlindCapabilities` | `onComplete: (CapabilityConfig, List<WindowArea>) -> Unit, onFailed: (Exception) -> Unit` | Gets move window blind capabilities. | `CAR_WINDOWS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/window/check-move-window-blind-capabilities) |
| `checkWindowBlindPositionCapabilities` | `onComplete: (CapabilityConfig, Map<WindowArea, AreaBounds<Int>>) -> Unit, onFailed: (Exception) -> Unit` | Gets window blind position capabilities. | `CAR_WINDOWS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/window/check-window-blind-position-capabilities) |
| `checkSmartGlassStateCapabilities` | `onComplete: (CapabilityConfig) -> Unit, onFailed: (Exception) -> Unit` | Gets smart glass state capabilities. | `CAR_WINDOWS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/window/check-smart-glass-state-capabilities) |
| `checkSmartGlassZoneStateCapabilities` | `onComplete: (CapabilityConfig, List<WindowArea>) -> Unit, onFailed: (Exception) -> Unit` | Gets smart glass zone state capabilities. | `CAR_WINDOWS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/window/check-smart-glass-zone-state-capabilities) |
| `checkSmartGlassLevelCapabilities` | `onComplete: (CapabilityConfig, Map<WindowArea, AreaBounds<Int>>) -> Unit, onFailed: (Exception) -> Unit` | Gets smart glass level capabilities. | `CAR_WINDOWS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/window/check-smart-glass-level-capabilities) |
| `checkWindowLockCapabilities` | `onComplete: (List<WindowArea>) -> Unit, onFailed: (Exception) -> Unit` | ⚠️ Deprecated. Use the new checkWindowLockCapabilities API that returns CapabilityConfig. | `CAR_WINDOWS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/window/check-window-lock-capabilities) |
| `checkWindowLockCapabilities` | `onComplete: (CapabilityConfig, List<WindowArea>) -> Unit, onFailed: (Exception) -> Unit` | Gets window lock capabilities. | `CAR_WINDOWS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/window/check-window-lock-capabilities) |
| `checkWindowPositionCapabilities` | `onComplete: (Map<WindowArea, AreaBounds<Int>>) -> Unit, onFailed: (Exception) -> Unit` | ⚠️ Deprecated. Use the new checkWindowPositionCapabilities API that returns CapabilityConfig. | `CAR_WINDOWS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/window/check-window-position-capabilities) |
| `checkWindowPositionCapabilities` | `onComplete: (CapabilityConfig, Map<WindowArea, AreaBounds<Int>>) -> Unit, onFailed: (Exception) -> Unit` | Gets window position capabilities. | `CAR_WINDOWS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/window/check-window-position-capabilities) |
| `setWindowLock` | `area: WindowArea, value: Boolean, onComplete: (WindowArea, Boolean?) -> Unit = { _, _ -> }, onFailed: (Exception) -> Unit = {}` | Sets the current window lock state. | `CONTROL_CAR_WINDOWS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/window/set-window-lock) |
| `getCurrentWindowLockState` | `area: WindowArea, onComplete: (Boolean?) -> Unit, onFailed: (Exception) -> Unit` | Gets the current window lock state. | `CAR_WINDOWS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/window/get-current-window-lock-state) |
| `getCurrentWindowPosition` | `area: WindowArea, onComplete: (Int?) -> Unit, onFailed: (Exception) -> Unit` | Gets the current window position. | `CAR_WINDOWS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/window/get-current-window-position) |
| `setWindowPosition` | `area: WindowArea, value: Int, onComplete: (WindowArea, Int?) -> Unit = { _, _ -> }, onFailed: (Exception) -> Unit = {}` | Sets the current window position state. | `CONTROL_CAR_WINDOWS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/window/set-window-position) |
| `getCurrentWindowBlindPosition` | `area: WindowArea, onComplete: (Int?) -> Unit, onFailed: (Exception) -> Unit` | Gets the current Window Blind Position for the specified window area. | `CAR_WINDOWS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/window/get-current-window-blind-position) |
| `moveWindowBlind` | `area: WindowArea, value: MoveControl, onComplete: (WindowArea, MoveControl?) -> Unit, onFailed: (Exception) -> Unit` | Moves the Window Blind for the specified window area. | `CONTROL_CAR_WINDOWS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/window/move-window-blind) |
| `getCurrentSmartGlassState` | `onComplete: (Boolean?) -> Unit, onFailed: (Exception) -> Unit` | Gets the current Smart Glass state. | `CAR_WINDOWS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/window/get-current-smart-glass-state) |
| `setSmartGlassState` | `value: Boolean, onComplete: (Boolean?) -> Unit = { _ -> }, onFailed: (Exception) -> Unit = {}` | Sets the Smart Glass state. | `CONTROL_CAR_WINDOWS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/window/set-smart-glass-state) |
| `setSmartGlassZoneState` | `area: WindowArea, value: List<SmartGlassZoneCommand>, onComplete: (WindowArea, List<SmartGlassZoneCommand>?) -> Unit = { _, _ -> }, onFailed: (Exception) -> Unit = {}` | Sets the Smart Glass zone state by submitting one command per smart glass panel for the specified window area. | `CONTROL_CAR_WINDOWS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/window/set-smart-glass-zone-state) |
| `getSmartGlassConfig` | `area: WindowArea, onComplete: (SmartGlassConfig?) -> Unit, onFailed: (Exception) -> Unit` | Gets the Smart Glass configuration for the specified window area. | `CAR_WINDOWS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/window/get-smart-glass-config) |
| `getCurrentSmartGlassLevel` | `area: WindowArea, onComplete: (Int?) -> Unit, onFailed: (Exception) -> Unit` | Gets the current Smart Glass Level for the specified window area. | `CAR_WINDOWS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/window/get-current-smart-glass-level) |
| `setSmartGlassLevel` | `area: WindowArea, value: Int, onComplete: (WindowArea, Int?) -> Unit = { _, _ -> }, onFailed: (Exception) -> Unit = {}` | Sets the Smart Glass Level for the specified window area. | `CONTROL_CAR_WINDOWS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/window/set-smart-glass-level) |
| `registerWindowLockState` | `area: WindowArea, listener: WindowLockStateListener` | Registers the window lock state to get property updates. | `CAR_WINDOWS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/window/register-window-lock-state) |
| `unregisterWindowLockState` | `area: WindowArea, listener: WindowLockStateListener` | Stops getting property updates for the given window lock state. | `CAR_WINDOWS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/window/unregister-window-lock-state) |
| `registerWindowPosition` | `area: WindowArea, listener: WindowPositionListener` | Registers the window area to get property updates. | `CAR_WINDOWS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/window/register-window-position) |
| `unregisterWindowPosition` | `area: WindowArea, listener: WindowPositionListener` | Stops getting property updates for the given window position. | `CAR_WINDOWS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/window/unregister-window-position) |
| `registerWindowBlindPosition` | `area: WindowArea, listener: WindowPositionListener` | Registers a listener to receive Window Blind Position updates for the specified window area. | `CAR_WINDOWS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/window/register-window-blind-position) |
| `unregisterWindowBlindPosition` | `area: WindowArea, listener: WindowPositionListener` | Unregisters a previously registered Window Blind Position listener for the specified window area. | `CAR_WINDOWS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/window/unregister-window-blind-position) |
| `registerSmartGlassLevel` | `area: WindowArea, listener: SmartGlassLevelListener` | Registers a listener to receive Smart Glass Level updates for the specified window area. | `CAR_WINDOWS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/window/register-smart-glass-level) |
| `unregisterSmartGlassLevel` | `area: WindowArea, listener: SmartGlassLevelListener` | Unregisters a previously registered Smart Glass Level listener for the specified window area. | `CAR_WINDOWS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/window/unregister-smart-glass-level) |
| `registerSmartGlassState` | `listener: SmartGlassStateListener` | Registers Smart Glass state listener. | `CAR_WINDOWS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/window/register-smart-glass-state) |
| `unregisterSmartGlassState` | `listener: SmartGlassStateListener` | Unregisters Smart Glass state listener. | `CAR_WINDOWS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/window/unregister-smart-glass-state) |

## 리스너 (Callback)

`register*` 함수에 넘기는 리스너와 콜백 시그니처입니다.

| 리스너 | 콜백 | 설명 | 원본 |
|---|---|---|---|
| `WindowLockStateListener` | `onWindowLockStateUpdated(state: Boolean?)` | Callback invoked when WindowLockState updates. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.listener/window-lock-state-listener/entry) |
| `WindowPositionListener` | `onWindowPositionUpdated(position: Int?)` | Callback invoked when WindowPosition updates. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.listener/window-position-listener/entry) |
| `SmartGlassLevelListener` | `onSmartGlassLevelUpdated(level: Int?)` | Callback invoked when SmartGlass Level updates. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.listener/smart-glass-level-listener/entry) |
| `SmartGlassStateListener` | `onSmartGlassStateUpdated(state: Boolean?)` | Callback invoked when SmartGlassState updates. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.listener/smart-glass-state-listener/entry) |

## 관련 타입 (모델·상수)

파라미터와 콜백에 쓰이는 영역(Area)·Capability·상수 타입은 공식 API Reference 패키지 페이지에서 확인합니다.

- [api.model.area (DoorArea, SeatArea 등 영역 타입)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.area/entry)
- [api.model.capability (CapabilityConfig, AccessType, AreaBounds)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.capability/entry)
- [api.model.common (공통 모델)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.common/entry)
- [api.model.telltale (경고등 모델)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.telltale/entry)
- [constant (PleosPermission 등 상수)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.constant/entry)
- [constant.area / constant.control / constant.unit / constant.telltale](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.constant.area/entry)
- [callback.exception (VehicleException, 에러 코드)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.callback.exception/entry)
