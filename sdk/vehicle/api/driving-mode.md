# DrivingMode (주행 모드)

> 마지막 확인일: 2026-10-28  
> 기준 버전: Vehicle SDK 2.3.2  
> 원본:
> - [DrivingMode API Reference](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/driving-mode/entry)
> - [Vehicle.getDrivingMode()](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/get-driving-mode)
> - [Vehicle SDK 소개](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/intro)
> - [프로젝트 설정](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/project-setup)
> - [도메인 객체 획득](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/obtain-domain-object)
> - [Capability 확인](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/check-capability)
> - [Get](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/get-property)
> - [Set](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/set-property)
> - [Callback](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/callback)
> - [권한 선언](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-permission)
> - [에러 코드](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-error-code)

Provides APIs for driving mode and cruise control.

- 획득: `vehicle.getDrivingMode()` ([원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/get-driving-mode))
- 권한: 조회 `pleos.car.permission.CAR_DRIVE`
- 함수 12개 (deprecated 0개 포함), 리스너 3개

## 기능 요약

| 기능 | Capability | Get | Set | Callback |
|---|:-:|:-:|:-:|:-:|
| CruiseControlTargetSpeed | ✓ | ✓ |  | ✓ |
| CruiseControlState | ✓ | ✓ |  | ✓ |
| DrivingModeState | ✓ | ✓ |  | ✓ |

## 함수 목록

설명은 공식 KDoc 원문(영문)입니다. 괄호 안은 파라미터, → 뒤는 반환 타입입니다.

| 함수 | 파라미터 | 설명 | 권한 | 원본 |
|---|---|---|---|---|
| `checkCruiseControlTargetSpeedCapabilities` | `onComplete: (CapabilityConfig) -> Unit, onFailed: (Exception) -> Unit` | Gets CruiseControlTargetSpeed capabilities. | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/driving-mode/check-cruise-control-target-speed-capabilities) |
| `checkCruiseControlStateCapabilities` | `onComplete: (CapabilityConfig) -> Unit, onFailed: (Exception) -> Unit` | Gets CruiseControlState capabilities. | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/driving-mode/check-cruise-control-state-capabilities) |
| `checkDrivingModeStateCapabilities` | `onComplete: (CapabilityConfig) -> Unit, onFailed: (Exception) -> Unit` | Gets DrivingModeState capabilities. | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/driving-mode/check-driving-mode-state-capabilities) |
| `getCruiseControlTargetSpeed` | `onComplete: (Float?) -> Unit, onFailed: (Exception) -> Unit` | Gets the current cruise control target speed in meters per second (m/s). | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/driving-mode/get-cruise-control-target-speed) |
| `getCruiseControlState` | `onComplete: (CruiseControl?) -> Unit, onFailed: (Exception) -> Unit` | Gets the current cruise control state. | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/driving-mode/get-cruise-control-state) |
| `getCurrentDrivingModeState` | `onComplete: (DrivingModeType?) -> Unit, onFailed: (Exception) -> Unit` | Gets current driving mode state. | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/driving-mode/get-current-driving-mode-state) |
| `registerCruiseControlTargetSpeed` | `listener: CruiseControlTargetSpeedListener` | Registers a listener for cruise control target speed updates in meters per second (m/s). | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/driving-mode/register-cruise-control-target-speed) |
| `unregisterCruiseControlTargetSpeed` | `listener: CruiseControlTargetSpeedListener` | Stops getting property updates for the given CruiseControlTargetSpeed. | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/driving-mode/unregister-cruise-control-target-speed) |
| `registerCruiseControlState` | `listener: CruiseControlStateListener` | Registers CruiseControlState to get property updates. | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/driving-mode/register-cruise-control-state) |
| `unregisterCruiseControlState` | `listener: CruiseControlStateListener` | Stops getting property updates for the given CruiseControlState. | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/driving-mode/unregister-cruise-control-state) |
| `registerDrivingModeState` | `listener: DrivingModeStateListener` | Registers DrivingModeState to get property updates. | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/driving-mode/register-driving-mode-state) |
| `unregisterDrivingModeState` | `listener: DrivingModeStateListener` | Stops getting property updates for the given DrivingModeState. | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/driving-mode/unregister-driving-mode-state) |

## 리스너 (Callback)

`register*` 함수에 넘기는 리스너와 콜백 시그니처입니다.

| 리스너 | 콜백 | 설명 | 원본 |
|---|---|---|---|
| `CruiseControlTargetSpeedListener` | `onCruiseControlTargetSpeedUpdated(speed: Float?)` | Called with the updated cruise control target speed in meters per second (m/s). | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.listener/cruise-control-target-speed-listener/entry) |
| `CruiseControlStateListener` | `onCruiseControlStateUpdated(state: CruiseControl?)` | Callback invoked when the CruiseControlState updates. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.listener/cruise-control-state-listener/entry) |
| `DrivingModeStateListener` | `onDrivingModeStateUpdated(state: DrivingModeType?)` | Callback invoked when the DrivingModeState updates. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.listener/driving-mode-state-listener/entry) |

## 관련 타입 (모델·상수)

파라미터와 콜백에 쓰이는 영역(Area)·Capability·상수 타입은 공식 API Reference 패키지 페이지에서 확인합니다.

- [api.model.area (DoorArea, SeatArea 등 영역 타입)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.area/entry)
- [api.model.capability (CapabilityConfig, AccessType, AreaBounds)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.capability/entry)
- [api.model.common (공통 모델)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.common/entry)
- [api.model.telltale (경고등 모델)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.telltale/entry)
- [constant (PleosPermission 등 상수)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.constant/entry)
- [constant.area / constant.control / constant.unit / constant.telltale](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.constant.area/entry)
- [callback.exception (VehicleException, 에러 코드)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.callback.exception/entry)
