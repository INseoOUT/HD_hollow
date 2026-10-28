# SteeringWheel (스티어링 휠)

> 마지막 확인일: 2026-10-28  
> 기준 버전: Vehicle SDK 2.3.2  
> 원본:
> - [SteeringWheel API Reference](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/steering-wheel/entry)
> - [Vehicle.getSteeringWheel()](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/get-steering-wheel)
> - [Vehicle SDK 소개](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/intro)
> - [프로젝트 설정](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/project-setup)
> - [도메인 객체 획득](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/obtain-domain-object)
> - [Capability 확인](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/check-capability)
> - [Get](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/get-property)
> - [Set](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/set-property)
> - [Callback](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/callback)
> - [권한 선언](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-permission)
> - [에러 코드](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-error-code)

Provides APIs for the front-wheel steering angle and steering wheel heating.

- 획득: `vehicle.getSteeringWheel()` ([원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/get-steering-wheel))
- 권한: 조회 `pleos.car.permission.CAR_CLIMATE`, `pleos.car.permission.CAR_DRIVE` · 제어 `pleos.car.permission.CONTROL_CAR_CLIMATE`
- 함수 10개 (deprecated 1개 포함), 리스너 2개

## 기능 요약

| 기능 | Capability | Get | Set | Callback |
|---|:-:|:-:|:-:|:-:|
| HvacSteeringWheelHeat | ✓ | ✓ | ✓ | ✓ |
| SteeringWheelAngle | ✓ | ✓ |  | ✓ |

## 함수 목록

설명은 공식 KDoc 원문(영문)입니다. 괄호 안은 파라미터, → 뒤는 반환 타입입니다.

| 함수 | 파라미터 | 설명 | 권한 | 원본 |
|---|---|---|---|---|
| `checkHvacSteeringWheelHeatCapabilities` | `onComplete: (AreaBounds<Int>) -> Unit, onFailed: (Exception) -> Unit` | ⚠️ Deprecated. Use the new checkHvacSteeringWheelHeatCapabilities API that returns CapabilityConfig. | `CAR_CLIMATE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/steering-wheel/check-hvac-steering-wheel-heat-capabilities) |
| `checkHvacSteeringWheelHeatCapabilities` | `onComplete: (CapabilityConfig, AreaBounds<Int>) -> Unit, onFailed: (Exception) -> Unit` | Gets hvac steering wheel heat capabilities. | `CAR_CLIMATE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/steering-wheel/check-hvac-steering-wheel-heat-capabilities) |
| `checkSteeringWheelAngleCapabilities` | `onComplete: (CapabilityConfig, AreaBounds<Float>) -> Unit, onFailed: (Exception) -> Unit` | Gets the capabilities for the front-wheel steering angle. | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/steering-wheel/check-steering-wheel-angle-capabilities) |
| `getHvacSteeringWheelHeatState` | `onComplete: (Int?) -> Unit, onFailed: (Exception) -> Unit` | Gets the current SteeringWheel heating/cooling state. | `CAR_CLIMATE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/steering-wheel/get-hvac-steering-wheel-heat-state) |
| `setHvacSteeringWheelHeat` | `value: Int, onComplete: (Int?) -> Unit = {}, onFailed: (Exception) -> Unit = {}` | Sets SteeringWheel heating/cooling temperature. | `CONTROL_CAR_CLIMATE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/steering-wheel/set-hvac-steering-wheel-heat) |
| `getCurrentSteeringWheelAngle` | `onComplete: (Float?) -> Unit, onFailed: (Exception) -> Unit` | Gets the current front-wheel steering angle in degrees. | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/steering-wheel/get-current-steering-wheel-angle) |
| `registerSteeringWheelAngle` | `listener: SteeringWheelAngleListener` | Registers a listener to receive front-wheel steering angle updates. | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/steering-wheel/register-steering-wheel-angle) |
| `unregisterSteeringWheelAngle` | `listener: SteeringWheelAngleListener` | Unregisters the given listener from front-wheel steering angle updates. | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/steering-wheel/unregister-steering-wheel-angle) |
| `registerHvacSteeringWheelHeatState` | `listener: HvacSteeringWheelHeatStateListener` | Registers Hvac SteeringWheel heat state to get property updates. | `CAR_CLIMATE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/steering-wheel/register-hvac-steering-wheel-heat-state) |
| `unregisterHvacSteeringWheelHeatState` | `listener: HvacSteeringWheelHeatStateListener` | Stops getting property updates for the given Hvac SteeringWheel heat state. | `CAR_CLIMATE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/steering-wheel/unregister-hvac-steering-wheel-heat-state) |

## 리스너 (Callback)

`register*` 함수에 넘기는 리스너와 콜백 시그니처입니다.

| 리스너 | 콜백 | 설명 | 원본 |
|---|---|---|---|
| `SteeringWheelAngleListener` | `onSteeringWheelAngleUpdated(angle: Float?)` | Called when a front-wheel steering angle update is available. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.listener/steering-wheel-angle-listener/entry) |
| `HvacSteeringWheelHeatStateListener` | `onHvacSteeringWheelHeatStateUpdated(state: Int?)` | Callback invoked when the HvacSteeringWheelHeatState updates. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.listener/hvac-steering-wheel-heat-state-listener/entry) |

## 관련 타입 (모델·상수)

파라미터와 콜백에 쓰이는 영역(Area)·Capability·상수 타입은 공식 API Reference 패키지 페이지에서 확인합니다.

- [api.model.area (DoorArea, SeatArea 등 영역 타입)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.area/entry)
- [api.model.capability (CapabilityConfig, AccessType, AreaBounds)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.capability/entry)
- [api.model.common (공통 모델)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.common/entry)
- [api.model.telltale (경고등 모델)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.telltale/entry)
- [constant (PleosPermission 등 상수)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.constant/entry)
- [constant.area / constant.control / constant.unit / constant.telltale](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.constant.area/entry)
- [callback.exception (VehicleException, 에러 코드)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.callback.exception/entry)
