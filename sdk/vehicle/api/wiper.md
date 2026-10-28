# Wiper (와이퍼)

> 마지막 확인일: 2026-10-28  
> 기준 버전: Vehicle SDK 2.3.2  
> 원본:
> - [Wiper API Reference](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/wiper/entry)
> - [Vehicle.getWiper()](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/get-wiper)
> - [Vehicle SDK 소개](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/intro)
> - [프로젝트 설정](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/project-setup)
> - [도메인 객체 획득](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/obtain-domain-object)
> - [Capability 확인](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/check-capability)
> - [Get](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/get-property)
> - [Set](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/set-property)
> - [Callback](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/callback)
> - [권한 선언](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-permission)
> - [에러 코드](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-error-code)

Provides APIs for wiper state and switch.

- 획득: `vehicle.getWiper()` ([원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/get-wiper))
- 권한: 조회 `pleos.car.permission.CAR_WINDOWS` · 제어 `pleos.car.permission.CONTROL_CAR_WINDOWS`
- 함수 9개 (deprecated 2개 포함), 리스너 1개

## 기능 요약

| 기능 | Capability | Get | Set | Callback |
|---|:-:|:-:|:-:|:-:|
| WiperState | ✓ | ✓ |  | ✓ |
| WiperSwitch | ✓ | ✓ | ✓ |  |

## 함수 목록

설명은 공식 KDoc 원문(영문)입니다. 괄호 안은 파라미터, → 뒤는 반환 타입입니다.

| 함수 | 파라미터 | 설명 | 권한 | 원본 |
|---|---|---|---|---|
| `checkWiperStateCapabilities` | `onComplete: (Map<WindowArea, List<WiperState>>) -> Unit, onFailed: (Exception) -> Unit` | ⚠️ Deprecated. Use the new checkWiperStateCapabilities API that returns CapabilityConfig. | `CAR_WINDOWS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/wiper/check-wiper-state-capabilities) |
| `checkWiperStateCapabilities` | `onComplete: (CapabilityConfig, Map<WindowArea, List<WiperState>>) -> Unit, onFailed: (Exception) -> Unit` | Gets wiper state capabilities. | `CAR_WINDOWS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/wiper/check-wiper-state-capabilities) |
| `checkWiperSwitchCapabilities` | `onComplete: (Map<WindowArea, List<WiperSwitch>>) -> Unit, onFailed: (Exception) -> Unit` | ⚠️ Deprecated. Use the new checkWiperSwitchCapabilities API that returns CapabilityConfig. | `CAR_WINDOWS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/wiper/check-wiper-switch-capabilities) |
| `checkWiperSwitchCapabilities` | `onComplete: (CapabilityConfig, Map<WindowArea, List<WiperSwitch>>) -> Unit, onFailed: (Exception) -> Unit` | Gets wiper switch capabilities. | `CAR_WINDOWS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/wiper/check-wiper-switch-capabilities) |
| `getCurrentWiperState` | `area: WindowArea, onComplete: (WiperState?) -> Unit, onFailed: (Exception) -> Unit` | Gets the current wiper mode state. | `CAR_WINDOWS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/wiper/get-current-wiper-state) |
| `getCurrentWiperSwitch` | `area: WindowArea, onComplete: (WiperSwitch?) -> Unit, onFailed: (Exception) -> Unit` | Gets the current Wiper Switch state for the specified window area. | `CAR_WINDOWS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/wiper/get-current-wiper-switch) |
| `setWiperSwitch` | `area: WindowArea, value: WiperSwitch, onComplete: (WindowArea, WiperSwitch?) -> Unit = { _, _ -> }, onFailed: (Exception) -> Unit = {}` | Sets the current wiper switch. | `CONTROL_CAR_WINDOWS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/wiper/set-wiper-switch) |
| `registerWiperState` | `area: WindowArea, listener: WiperStateListener` | Registers the wiper state to get property updates. | `CAR_WINDOWS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/wiper/register-wiper-state) |
| `unregisterWiperState` | `area: WindowArea, listener: WiperStateListener` | Stops getting property updates for the given wiper state. | `CAR_WINDOWS` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/wiper/unregister-wiper-state) |

## 리스너 (Callback)

`register*` 함수에 넘기는 리스너와 콜백 시그니처입니다.

| 리스너 | 콜백 | 설명 | 원본 |
|---|---|---|---|
| `WiperStateListener` | `onWiperStateUpdated(state: WiperState?)` | Callback invoked when WiperState updates. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.listener/wiper-state-listener/entry) |

## 관련 타입 (모델·상수)

파라미터와 콜백에 쓰이는 영역(Area)·Capability·상수 타입은 공식 API Reference 패키지 페이지에서 확인합니다.

- [api.model.area (DoorArea, SeatArea 등 영역 타입)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.area/entry)
- [api.model.capability (CapabilityConfig, AccessType, AreaBounds)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.capability/entry)
- [api.model.common (공통 모델)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.common/entry)
- [api.model.telltale (경고등 모델)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.telltale/entry)
- [constant (PleosPermission 등 상수)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.constant/entry)
- [constant.area / constant.control / constant.unit / constant.telltale](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.constant.area/entry)
- [callback.exception (VehicleException, 에러 코드)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.callback.exception/entry)
