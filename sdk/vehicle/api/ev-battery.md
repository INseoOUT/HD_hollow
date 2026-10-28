# EvBattery (EV 배터리·충전)

> 마지막 확인일: 2026-10-28  
> 기준 버전: Vehicle SDK 2.3.2  
> 원본:
> - [EvBattery API Reference](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/ev-battery/entry)
> - [Vehicle.getEvBattery()](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/get-ev-battery)
> - [Vehicle SDK 소개](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/intro)
> - [프로젝트 설정](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/project-setup)
> - [도메인 객체 획득](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/obtain-domain-object)
> - [Capability 확인](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/check-capability)
> - [Get](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/get-property)
> - [Set](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/set-property)
> - [Callback](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/callback)
> - [권한 선언](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-permission)
> - [에러 코드](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-error-code)

Provides APIs for EV battery efficiency.

- 획득: `vehicle.getEvBattery()` ([원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/get-ev-battery))
- 권한: 조회 `pleos.car.permission.CAR_ENERGY` · 제어 `pleos.car.permission.CONTROL_CAR_ENERGY`
- 함수 31개 (deprecated 1개 포함), 리스너 2개

## 기능 요약

| 기능 | Capability | Get | Set | Callback |
|---|:-:|:-:|:-:|:-:|
| InstantaneousEvEfficiency | ✓ | ✓ |  | ✓ |
| SinceChargeEvEfficiency | ✓ | ✓ |  | ✓ |
| CumulativeEvEfficiency | ✓ | ✓ |  | ✓ |
| CurrentEvEfficiency | ✓ |  |  | ✓ |
| EvBatteryStateOfCharge | ✓ | ✓ |  | ✓ |
| EvChargeCurrentDrawLimit | ✓ | ✓ | ✓ |  |
| EvChargePercentLimit | ✓ |  | ✓ |  |
| EvChargePortOpen | ✓ |  | ✓ |  |
| EvChargeSwitch | ✓ | ✓ | ✓ |  |

기타 함수: `getCurrentEvEfficiency`

## 함수 목록

설명은 공식 KDoc 원문(영문)입니다. 괄호 안은 파라미터, → 뒤는 반환 타입입니다.

| 함수 | 파라미터 | 설명 | 권한 | 원본 |
|---|---|---|---|---|
| `checkInstantaneousEvEfficiencyCapabilities` | `onComplete: (CapabilityConfig) -> Unit, onFailed: (Exception) -> Unit` | Gets Instantaneous EV Efficiency capabilities. | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/ev-battery/check-instantaneous-ev-efficiency-capabilities) |
| `checkSinceChargeEvEfficiencyCapabilities` | `onComplete: (CapabilityConfig) -> Unit, onFailed: (Exception) -> Unit` | Gets EV Efficiency Since Charge capabilities. | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/ev-battery/check-since-charge-ev-efficiency-capabilities) |
| `checkCumulativeEvEfficiencyCapabilities` | `onComplete: (CapabilityConfig) -> Unit, onFailed: (Exception) -> Unit` | Gets Cumulative EV Efficiency capabilities. | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/ev-battery/check-cumulative-ev-efficiency-capabilities) |
| `checkCurrentEvEfficiencyCapabilities` | `onComplete: (CapabilityConfig) -> Unit, onFailed: (Exception) -> Unit` | Gets Current EV Efficiency capabilities. | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/ev-battery/check-current-ev-efficiency-capabilities) |
| `checkEvBatteryStateOfChargeCapabilities` | `onComplete: (CapabilityConfig) -> Unit, onFailed: (Exception) -> Unit` | Gets EV Battery State Of Charge capabilities. | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/ev-battery/check-ev-battery-state-of-charge-capabilities) |
| `checkEvChargeCurrentDrawLimitCapabilities` | `onComplete: (List<Float>) -> Unit, onFailed: (Exception) -> Unit` | ⚠️ Deprecated. Use the new checkEvChargeCurrentDrawLimitCapabilities API that returns CapabilityConfig. (Gets the supported EV charge current draw limits in amperes (A).) | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/ev-battery/check-ev-charge-current-draw-limit-capabilities) |
| `checkEvChargeCurrentDrawLimitCapabilities` | `onComplete: (CapabilityConfig, List<Float>) -> Unit, onFailed: (Exception) -> Unit` | Gets capabilities for EV charge current draw limits in amperes (A). | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/ev-battery/check-ev-charge-current-draw-limit-capabilities) |
| `checkEvChargePercentLimitCapabilities` | `onComplete: (CapabilityConfig, List<Float>) -> Unit, onFailed: (Exception) -> Unit` | Gets capabilities for EV charge limits, each expressed as a percentage (%). | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/ev-battery/check-ev-charge-percent-limit-capabilities) |
| `checkEvChargePortOpenCapabilities` | `onComplete: (CapabilityConfig) -> Unit, onFailed: (Exception) -> Unit` | Gets EvChargePortOpen capabilities. | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/ev-battery/check-ev-charge-port-open-capabilities) |
| `checkEvChargeSwitchCapabilities` | `onComplete: (CapabilityConfig) -> Unit, onFailed: (Exception) -> Unit` | Gets EvChargeSwitch capabilities. | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/ev-battery/check-ev-charge-switch-capabilities) |
| `getInstantaneousEvEfficiency` | `onComplete: (Float?) -> Unit, onFailed: (Exception) -> Unit` | Gets instantaneous EV efficiency. | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/ev-battery/get-instantaneous-ev-efficiency) |
| `getSinceChargeEvEfficiency` | `onComplete: (Float?) -> Unit, onFailed: (Exception) -> Unit` | Gets EV efficiency since charge. | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/ev-battery/get-since-charge-ev-efficiency) |
| `getCumulativeEvEfficiency` | `onComplete: (Float?) -> Unit, onFailed: (Exception) -> Unit` | Gets cumulative EV efficiency. | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/ev-battery/get-cumulative-ev-efficiency) |
| `getCurrentEvEfficiency` | `onComplete: (Float?) -> Unit, onFailed: (Exception) -> Unit` | Gets current EV efficiency. | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/ev-battery/get-current-ev-efficiency) |
| `getEvBatteryStateOfCharge` | `onComplete: (Int?) -> Unit, onFailed: (Exception) -> Unit` | Gets EV battery state of charge as a percentage (%). | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/ev-battery/get-ev-battery-state-of-charge) |
| `registerCurrentEvEfficiency` | `listener: EvBatteryEfficiencyListener` | Registers Current EV Efficiency listener. | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/ev-battery/register-current-ev-efficiency) |
| `unregisterCurrentEvEfficiency` | `listener: EvBatteryEfficiencyListener` | Unregisters Current EV Efficiency listener. | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/ev-battery/unregister-current-ev-efficiency) |
| `registerEvBatteryStateOfCharge` | `listener: EvBatteryStateOfChargeListener` | Registers a listener for EV battery state of charge updates expressed as a percentage (%). | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/ev-battery/register-ev-battery-state-of-charge) |
| `unregisterEvBatteryStateOfCharge` | `listener: EvBatteryStateOfChargeListener` | Unregisters EV Battery State Of Charge listener. | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/ev-battery/unregister-ev-battery-state-of-charge) |
| `setEvChargeCurrentDrawLimit` | `value: Float, onComplete: (Float?) -> Unit = {}, onFailed: (Exception) -> Unit = {}` | Sets the EV charge current draw limit in amperes (A). | `CONTROL_CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/ev-battery/set-ev-charge-current-draw-limit) |
| `getEvChargeCurrentDrawLimit` | `onComplete: (Float?) -> Unit, onFailed: (Exception) -> Unit` | Gets the EV charge current draw limit in amperes (A). | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/ev-battery/get-ev-charge-current-draw-limit) |
| `setEvChargePercentLimit` | `value: Float, onComplete: (Float?) -> Unit = {}, onFailed: (Exception) -> Unit = {}` | Sets the battery state-of-charge limit for EV charging, expressed as a percentage (%). | `CONTROL_CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/ev-battery/set-ev-charge-percent-limit) |
| `setEvChargePortOpen` | `value: Boolean, onComplete: (Boolean?) -> Unit = {}, onFailed: (Exception) -> Unit = {}` | Sets the EV charge port open state to On or Off. | `CONTROL_CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/ev-battery/set-ev-charge-port-open) |
| `setEvChargeSwitch` | `value: Boolean, onComplete: (Boolean?) -> Unit = {}, onFailed: (Exception) -> Unit = {}` | Sets whether to start or stop EV battery charging. | `CONTROL_CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/ev-battery/set-ev-charge-switch) |
| `getEvChargeSwitchState` | `onComplete: (Boolean?) -> Unit, onFailed: (Exception) -> Unit` | Gets the current EV battery charging state. | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/ev-battery/get-ev-charge-switch-state) |
| `registerInstantaneousEvEfficiency` | `listener: EvBatteryEfficiencyListener` | Registers Instantaneous EV Efficiency listener. | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/ev-battery/register-instantaneous-ev-efficiency) |
| `unregisterInstantaneousEvEfficiency` | `listener: EvBatteryEfficiencyListener` | Unregisters Instantaneous EV Efficiency listener. | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/ev-battery/unregister-instantaneous-ev-efficiency) |
| `registerSinceChargeEvEfficiency` | `listener: EvBatteryEfficiencyListener` | Registers EV Efficiency Since Charge listener. | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/ev-battery/register-since-charge-ev-efficiency) |
| `unregisterSinceChargeEvEfficiency` | `listener: EvBatteryEfficiencyListener` | Unregisters EV Efficiency Since Charge listener. | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/ev-battery/unregister-since-charge-ev-efficiency) |
| `registerCumulativeEvEfficiency` | `listener: EvBatteryEfficiencyListener` | Registers Cumulative EV Efficiency listener. | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/ev-battery/register-cumulative-ev-efficiency) |
| `unregisterCumulativeEvEfficiency` | `listener: EvBatteryEfficiencyListener` | Unregisters Cumulative EV Efficiency listener. | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/ev-battery/unregister-cumulative-ev-efficiency) |

## 리스너 (Callback)

`register*` 함수에 넘기는 리스너와 콜백 시그니처입니다.

| 리스너 | 콜백 | 설명 | 원본 |
|---|---|---|---|
| `EvBatteryEfficiencyListener` | `onEvBatteryEfficiencyUpdated(efficiency: Float?)` | Callback invoked when EV battery efficiency updates. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.listener/ev-battery-efficiency-listener/entry) |
| `EvBatteryStateOfChargeListener` | `onEvBatteryStateOfChargeUpdated(value: Int?)` | Callback invoked with the updated EV battery state of charge as a percentage (%). | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.listener/ev-battery-state-of-charge-listener/entry) |

## 관련 타입 (모델·상수)

파라미터와 콜백에 쓰이는 영역(Area)·Capability·상수 타입은 공식 API Reference 패키지 페이지에서 확인합니다.

- [api.model.area (DoorArea, SeatArea 등 영역 타입)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.area/entry)
- [api.model.capability (CapabilityConfig, AccessType, AreaBounds)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.capability/entry)
- [api.model.common (공통 모델)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.common/entry)
- [api.model.telltale (경고등 모델)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.telltale/entry)
- [constant (PleosPermission 등 상수)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.constant/entry)
- [constant.area / constant.control / constant.unit / constant.telltale](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.constant.area/entry)
- [callback.exception (VehicleException, 에러 코드)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.callback.exception/entry)
