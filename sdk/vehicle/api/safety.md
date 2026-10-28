# Safety (안전)

> 마지막 확인일: 2026-10-28  
> 기준 버전: Vehicle SDK 2.3.2  
> 원본:
> - [Safety API Reference](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/safety/entry)
> - [Vehicle.getSafety()](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/get-safety)
> - [Vehicle SDK 소개](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/intro)
> - [프로젝트 설정](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/project-setup)
> - [도메인 객체 획득](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/obtain-domain-object)
> - [Capability 확인](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/check-capability)
> - [Get](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/get-property)
> - [Set](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/set-property)
> - [Callback](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/callback)
> - [권한 선언](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-permission)
> - [에러 코드](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-error-code)

Provides APIs for airbags, warnings, fluid levels, and telltales.

- 획득: `vehicle.getSafety()` ([원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/get-safety))
- 권한: 조회 `pleos.car.permission.CAR_SAFETY` · 기타 `pleos.car.permission.PRIVATE_CAR_SAFETY`
- 함수 39개 (deprecated 2개 포함), 리스너 7개

## 기능 요약

| 기능 | Capability | Get | Set | Callback |
|---|:-:|:-:|:-:|:-:|
| Airbag | ✓ |  |  |  |
| AirbagDeployed | ✓ |  |  |  |
| SeatBuckled | ✓ |  |  |  |
| SeatNotBuckledWarning | ✓ |  |  |  |
| EngineOilLevelState | ✓ | ✓ |  | ✓ |
| BrakeFluidLevelState | ✓ | ✓ |  | ✓ |
| TransmissionOilLevelState | ✓ | ✓ |  | ✓ |
| LowWasherFluidWarningState | ✓ |  |  |  |
| PowerDownWarningIndicator | ✓ | ✓ |  | ✓ |
| Telltales | ✓ | ✓ |  | ✓ |

기타 함수: `hasLowWasherFluidWarning`, `hasSeatAirbagBeenDeployed`, `hasSeatAirbagEnabled`, `hasSeatBeltNotBuckledWarning`, `registerSeatAirbagEnabled`, `unregisterSeatAirbagEnabled`, `registerSeatBeltNotBuckledWarning`, `unregisterSeatBeltNotBuckledWarning`, `registerLowWasherFluidWaringState`, `unregisterLowWasherFluidWaringState`, `registerSeatAirbagDeployed`, `unregisterSeatAirbagDeployed`

## 함수 목록

설명은 공식 KDoc 원문(영문)입니다. 괄호 안은 파라미터, → 뒤는 반환 타입입니다.

| 함수 | 파라미터 | 설명 | 권한 | 원본 |
|---|---|---|---|---|
| `checkAirbagCapabilities` | `onComplete: (List<SeatArea>) -> Unit, onFailed: (Exception) -> Unit` | ⚠️ Deprecated. Use the new checkAirbagCapabilities API that returns CapabilityConfig. | `CAR_SAFETY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/safety/check-airbag-capabilities) |
| `checkAirbagCapabilities` | `onComplete: (CapabilityConfig, List<SeatArea>) -> Unit, onFailed: (Exception) -> Unit` | Gets airbag capabilities. | `CAR_SAFETY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/safety/check-airbag-capabilities) |
| `checkAirbagDeployedCapabilities` | `onComplete: (CapabilityConfig, List<SeatArea>) -> Unit, onFailed: (Exception) -> Unit` | Gets airbag deployed capabilities. | `CAR_SAFETY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/safety/check-airbag-deployed-capabilities) |
| `checkSeatBuckledCapabilities` | `onComplete: (List<SeatArea>) -> Unit, onFailed: (Exception) -> Unit` | ⚠️ Deprecated. Use the new checkSeatBuckledCapabilities API that returns CapabilityConfig. | `CAR_SAFETY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/safety/check-seat-buckled-capabilities) |
| `checkSeatBuckledCapabilities` | `onComplete: (CapabilityConfig, List<SeatArea>) -> Unit, onFailed: (Exception) -> Unit` | Gets seat buckled capabilities. | `CAR_SAFETY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/safety/check-seat-buckled-capabilities) |
| `checkSeatNotBuckledWarningCapabilities` | `onComplete: (CapabilityConfig, List<SeatArea>) -> Unit, onFailed: (Exception) -> Unit` | Gets seat not buckled warning capabilities. | `CAR_SAFETY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/safety/check-seat-not-buckled-warning-capabilities) |
| `checkEngineOilLevelStateCapabilities` | `onComplete: (CapabilityConfig, List<OilLevelState>) -> Unit, onFailed: (Exception) -> Unit` | Gets engine oil level state capabilities. | `CAR_SAFETY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/safety/check-engine-oil-level-state-capabilities) |
| `checkBrakeFluidLevelStateCapabilities` | `onComplete: (CapabilityConfig, List<OilLevelState>) -> Unit, onFailed: (Exception) -> Unit` | Gets brake fluid level state capabilities. | `CAR_SAFETY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/safety/check-brake-fluid-level-state-capabilities) |
| `checkTransmissionOilLevelStateCapabilities` | `onComplete: (CapabilityConfig, List<OilLevelState>) -> Unit, onFailed: (Exception) -> Unit` | Gets transmission oil level state capabilities. | `CAR_SAFETY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/safety/check-transmission-oil-level-state-capabilities) |
| `checkLowWasherFluidWarningStateCapabilities` | `onComplete: (CapabilityConfig) -> Unit, onFailed: (Exception) -> Unit` | Gets low washer fluid warning state capabilities. | `CAR_SAFETY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/safety/check-low-washer-fluid-warning-state-capabilities) |
| `checkPowerDownWarningIndicatorCapabilities` | `onComplete: (CapabilityConfig) -> Unit, onFailed: (Exception) -> Unit` | Gets power down warning indicator capabilities. | `CAR_SAFETY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/safety/check-power-down-warning-indicator-capabilities) |
| `checkTelltalesCapabilities` | `onComplete: (CapabilityConfig) -> Unit, onFailed: (Exception) -> Unit` | Gets telltales capabilities. | `PRIVATE_CAR_SAFETY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/safety/check-telltales-capabilities) |
| `getEngineOilLevelState` | `onComplete: (OilLevelState?) -> Unit, onFailed: (Exception) -> Unit` | Gets the current engine oil level. | `CAR_SAFETY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/safety/get-engine-oil-level-state) |
| `getBrakeFluidLevelState` | `onComplete: (OilLevelState?) -> Unit, onFailed: (Exception) -> Unit` | Gets the current brake fluid level. | `CAR_SAFETY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/safety/get-brake-fluid-level-state) |
| `getTransmissionOilLevelState` | `onComplete: (OilLevelState?) -> Unit, onFailed: (Exception) -> Unit` | Gets the current transmission oil level. | `CAR_SAFETY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/safety/get-transmission-oil-level-state) |
| `hasLowWasherFluidWarning` | `onComplete: (Boolean?) -> Unit, onFailed: (Exception) -> Unit` | Checks the current washer fluid low warning status. If the status is true, there is an issue with the washer fluid | `CAR_SAFETY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/safety/has-low-washer-fluid-warning) |
| `hasSeatAirbagBeenDeployed` | `area: SeatArea, onComplete: (Boolean?) -> Unit, onFailed: (Exception) -> Unit` | Gets the current airbag deployment status. If the state is true, the airbag is deployed. | `CAR_SAFETY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/safety/has-seat-airbag-been-deployed) |
| `hasPowerDownWarningIndicator` | `onComplete: (Boolean?) -> Unit, onFailed: (Exception) -> Unit` | Gets the current power down warning indicator state. If the state is true, there is a problem with the engine. | `CAR_SAFETY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/safety/has-power-down-warning-indicator) |
| `hasSeatAirbagEnabled` | `area: SeatArea, onComplete: (Boolean?) -> Unit, onFailed: (Exception) -> Unit` | Gets the seat airbag enabled state. If the state is true, there is a problem with the seat airbag in the `area`. | `CAR_SAFETY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/safety/has-seat-airbag-enabled) |
| `hasSeatBeltNotBuckledWarning` | `area: SeatArea, onComplete: (Boolean?) -> Unit, onFailed: (Exception) -> Unit` | Gets the seat belt buckled warning state. If the state is true, the seat belt in the `area` is not buckled. | `CAR_SAFETY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/safety/has-seat-belt-not-buckled-warning) |
| `getTelltalesInfo` | `onComplete: (telltales: Telltales) -> Unit, onFailed: (Exception) -> Unit` | Gets telltales info. | `CAR_SAFETY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/safety/get-telltales-info) |
| `registerPowerDownWarningIndicator` | `listener: PowerDownWarningIndicatorListener` | Registers the PowerDownWarningIndicator to get property updates. | `CAR_SAFETY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/safety/register-power-down-warning-indicator) |
| `unregisterPowerDownWarningIndicator` | `listener: PowerDownWarningIndicatorListener` | Stops getting property updates for the given PowerDownWarningIndicator. | `CAR_SAFETY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/safety/unregister-power-down-warning-indicator) |
| `registerSeatAirbagEnabled` | `area: SeatArea, listener: SeatAirbagEnabledListener` | Registers the SeatAirbagEnabled to get property updates. | `CAR_SAFETY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/safety/register-seat-airbag-enabled) |
| `unregisterSeatAirbagEnabled` | `area: SeatArea, listener: SeatAirbagEnabledListener` | Stops getting property updates for the given SeatAirbagEnabled. | `CAR_SAFETY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/safety/unregister-seat-airbag-enabled) |
| `registerSeatBeltNotBuckledWarning` | `area: SeatArea, listener: SeatBeltNotBuckledWarningListener` | Registers the SeatBeltNotBuckledWarning to get property updates. | `CAR_SAFETY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/safety/register-seat-belt-not-buckled-warning) |
| `unregisterSeatBeltNotBuckledWarning` | `area: SeatArea, listener: SeatBeltNotBuckledWarningListener` | Stops getting property updates for the given SeatBeltNotBuckledWarning. | `CAR_SAFETY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/safety/unregister-seat-belt-not-buckled-warning) |
| `registerLowWasherFluidWaringState` | `listener: LowWasherFluidWaringListener` | Registers the LowWasherFluidWaringState to get property updates. | `CAR_SAFETY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/safety/register-low-washer-fluid-waring-state) |
| `unregisterLowWasherFluidWaringState` | `listener: LowWasherFluidWaringListener` | Stops getting property updates for the given LowWasherFluidWaringState. | `CAR_SAFETY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/safety/unregister-low-washer-fluid-waring-state) |
| `registerBrakeFluidLevelState` | `listener: OilLevelStateListener` | Registers the BrakeFluidLevelState to get property updates. | `CAR_SAFETY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/safety/register-brake-fluid-level-state) |
| `unregisterBrakeFluidLevelState` | `listener: OilLevelStateListener` | Stops getting property updates for the given BrakeFluidLevelState. | `CAR_SAFETY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/safety/unregister-brake-fluid-level-state) |
| `registerTransmissionOilLevelState` | `listener: OilLevelStateListener` | Registers the TransmissionOilLevel to get property updates. | `CAR_SAFETY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/safety/register-transmission-oil-level-state) |
| `unregisterTransmissionOilLevelState` | `listener: OilLevelStateListener` | Stops getting property updates for the given TransmissionOilLevel. | `CAR_SAFETY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/safety/unregister-transmission-oil-level-state) |
| `registerEngineOilLevelState` | `listener: OilLevelStateListener` | Registers the EngineOilLevelState to get property updates. | `CAR_SAFETY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/safety/register-engine-oil-level-state) |
| `unregisterEngineOilLevelState` | `listener: OilLevelStateListener` | Stops getting property updates for the given EngineOilLevelState. | `CAR_SAFETY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/safety/unregister-engine-oil-level-state) |
| `registerSeatAirbagDeployed` | `area: SeatArea, listener: SeatAirbagDeployedListener` | Registers the SeatAirbagDeployed to get property updates. | `CAR_SAFETY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/safety/register-seat-airbag-deployed) |
| `unregisterSeatAirbagDeployed` | `area: SeatArea, listener: SeatAirbagDeployedListener` | Stops getting property updates for the given SeatAirbagDeployed. | `CAR_SAFETY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/safety/unregister-seat-airbag-deployed) |
| `registerTelltales` | `listener: TelltaleInfoListener` | Registers a Telltales listener to receive telltale information updates. | `PRIVATE_CAR_SAFETY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/safety/register-telltales) |
| `unregisterTelltales` | `listener: TelltaleInfoListener` | Unregisters a previously registered Telltales listener. | `PRIVATE_CAR_SAFETY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/safety/unregister-telltales) |

## 리스너 (Callback)

`register*` 함수에 넘기는 리스너와 콜백 시그니처입니다.

| 리스너 | 콜백 | 설명 | 원본 |
|---|---|---|---|
| `PowerDownWarningIndicatorListener` | `onPowerDownWarningIndicatorUpdated(state: Boolean?)` | Callback invoked when the PowerDownWarningIndicator updates. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.listener/power-down-warning-indicator-listener/entry) |
| `SeatAirbagEnabledListener` | `onSeatAirbagEnabledUpdated(enabled: Boolean?)` | Callback invoked when SeatAirbagEnabled updates. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.listener/seat-airbag-enabled-listener/entry) |
| `SeatBeltNotBuckledWarningListener` | `onSeatBeltNotBuckledWarningUpdated(state: Boolean?)` | Callback invoked when SeatBeltNotBuckledWarning updates. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.listener/seat-belt-not-buckled-warning-listener/entry) |
| `LowWasherFluidWaringListener` | `onLowWasherFluidWaringUpdated(state: Boolean?)` | Callback invoked when the LowWasherFluidWaring updates. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.listener/low-washer-fluid-waring-listener/entry) |
| `OilLevelStateListener` | `onOilLevelUpdated(level: OilLevelState?)` | Callback invoked when the OilLevelState updates. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.listener/oil-level-state-listener/entry) |
| `SeatAirbagDeployedListener` | `onSeatAirbagDeployUpdated(deployed: Boolean?)` | Callback invoked when SeatAirbagDeployed updates. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.listener/seat-airbag-deployed-listener/entry) |
| `TelltaleInfoListener` | `onTelltaleInfoUpdated(telltales: Telltales?)` | Callback invoked when TelltaleInfo updates. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.listener/telltale-info-listener/entry) |

## 관련 타입 (모델·상수)

파라미터와 콜백에 쓰이는 영역(Area)·Capability·상수 타입은 공식 API Reference 패키지 페이지에서 확인합니다.

- [api.model.area (DoorArea, SeatArea 등 영역 타입)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.area/entry)
- [api.model.capability (CapabilityConfig, AccessType, AreaBounds)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.capability/entry)
- [api.model.common (공통 모델)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.common/entry)
- [api.model.telltale (경고등 모델)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.telltale/entry)
- [constant (PleosPermission 등 상수)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.constant/entry)
- [constant.area / constant.control / constant.unit / constant.telltale](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.constant.area/entry)
- [callback.exception (VehicleException, 에러 코드)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.callback.exception/entry)
