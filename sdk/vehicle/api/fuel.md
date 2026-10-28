# Fuel (연료)

> 마지막 확인일: 2026-10-28  
> 기준 버전: Vehicle SDK 2.3.2  
> 원본:
> - [Fuel API Reference](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/fuel/entry)
> - [Vehicle.getFuel()](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/get-fuel)
> - [Vehicle SDK 소개](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/intro)
> - [프로젝트 설정](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/project-setup)
> - [도메인 객체 획득](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/obtain-domain-object)
> - [Capability 확인](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/check-capability)
> - [Get](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/get-property)
> - [Set](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/set-property)
> - [Callback](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/callback)
> - [권한 선언](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-permission)
> - [에러 코드](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-error-code)

Provides APIs for fuel economy.

- 획득: `vehicle.getFuel()` ([원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/get-fuel))
- 권한: 조회 `pleos.car.permission.CAR_ENERGY`
- 함수 20개 (deprecated 0개 포함), 리스너 2개

## 기능 요약

| 기능 | Capability | Get | Set | Callback |
|---|:-:|:-:|:-:|:-:|
| InstantaneousFuelEconomy | ✓ | ✓ |  | ✓ |
| SinceChargeFuelEconomy | ✓ | ✓ |  | ✓ |
| CumulativeFuelEconomy | ✓ | ✓ |  | ✓ |
| CurrentFuelEconomy | ✓ |  |  | ✓ |
| FuelPercent | ✓ | ✓ |  | ✓ |

기타 함수: `getCurrentFuelEconomy`

## 함수 목록

설명은 공식 KDoc 원문(영문)입니다. 괄호 안은 파라미터, → 뒤는 반환 타입입니다.

| 함수 | 파라미터 | 설명 | 권한 | 원본 |
|---|---|---|---|---|
| `checkInstantaneousFuelEconomyCapabilities` | `onComplete: (CapabilityConfig) -> Unit, onFailed: (Exception) -> Unit` | Gets Instantaneous Fuel Economy capabilities. | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/fuel/check-instantaneous-fuel-economy-capabilities) |
| `checkSinceChargeFuelEconomyCapabilities` | `onComplete: (CapabilityConfig) -> Unit, onFailed: (Exception) -> Unit` | Gets Fuel Economy Since Refuel capabilities. | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/fuel/check-since-charge-fuel-economy-capabilities) |
| `checkCumulativeFuelEconomyCapabilities` | `onComplete: (CapabilityConfig) -> Unit, onFailed: (Exception) -> Unit` | Gets Cumulative Fuel Economy capabilities. | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/fuel/check-cumulative-fuel-economy-capabilities) |
| `checkCurrentFuelEconomyCapabilities` | `onComplete: (CapabilityConfig) -> Unit, onFailed: (Exception) -> Unit` | Gets Current Fuel Economy capabilities. | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/fuel/check-current-fuel-economy-capabilities) |
| `checkFuelPercentCapabilities` | `onComplete: (CapabilityConfig) -> Unit, onFailed: (Exception) -> Unit` | Gets Fuel Percent capabilities. | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/fuel/check-fuel-percent-capabilities) |
| `getInstantaneousFuelEconomy` | `onComplete: (Float?) -> Unit, onFailed: (Exception) -> Unit` | Gets instantaneous fuel economy. | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/fuel/get-instantaneous-fuel-economy) |
| `getSinceChargeFuelEconomy` | `onComplete: (Float?) -> Unit, onFailed: (Exception) -> Unit` | Gets fuel economy since refuel. | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/fuel/get-since-charge-fuel-economy) |
| `getCumulativeFuelEconomy` | `onComplete: (Float?) -> Unit, onFailed: (Exception) -> Unit` | Gets cumulative fuel economy. | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/fuel/get-cumulative-fuel-economy) |
| `getCurrentFuelEconomy` | `onComplete: (Float?) -> Unit, onFailed: (Exception) -> Unit` | Gets current fuel economy. | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/fuel/get-current-fuel-economy) |
| `getFuelPercent` | `onComplete: (Int?) -> Unit, onFailed: (Exception) -> Unit` | Gets the fuel level as a percentage (%). | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/fuel/get-fuel-percent) |
| `registerInstantaneousFuelEconomy` | `listener: FuelEconomyListener` | Registers Instantaneous Fuel Economy listener. | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/fuel/register-instantaneous-fuel-economy) |
| `unregisterInstantaneousFuelEconomy` | `listener: FuelEconomyListener` | Unregisters Instantaneous Fuel Economy listener. | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/fuel/unregister-instantaneous-fuel-economy) |
| `registerSinceChargeFuelEconomy` | `listener: FuelEconomyListener` | Registers Fuel Economy Since Refuel listener. | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/fuel/register-since-charge-fuel-economy) |
| `unregisterSinceChargeFuelEconomy` | `listener: FuelEconomyListener` | Unregisters Fuel Economy Since Refuel listener. | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/fuel/unregister-since-charge-fuel-economy) |
| `registerCumulativeFuelEconomy` | `listener: FuelEconomyListener` | Registers Cumulative Fuel Economy listener. | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/fuel/register-cumulative-fuel-economy) |
| `unregisterCumulativeFuelEconomy` | `listener: FuelEconomyListener` | Unregisters Cumulative Fuel Economy listener. | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/fuel/unregister-cumulative-fuel-economy) |
| `registerCurrentFuelEconomy` | `listener: FuelEconomyListener` | Registers Current Fuel Economy listener. | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/fuel/register-current-fuel-economy) |
| `unregisterCurrentFuelEconomy` | `listener: FuelEconomyListener` | Unregisters Current Fuel Economy listener. | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/fuel/unregister-current-fuel-economy) |
| `registerFuelPercent` | `listener: FuelPercentListener` | Registers a listener for fuel level updates expressed as a percentage (%). | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/fuel/register-fuel-percent) |
| `unregisterFuelPercent` | `listener: FuelPercentListener` | Unregisters Fuel Percent listener. | `CAR_ENERGY` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/fuel/unregister-fuel-percent) |

## 리스너 (Callback)

`register*` 함수에 넘기는 리스너와 콜백 시그니처입니다.

| 리스너 | 콜백 | 설명 | 원본 |
|---|---|---|---|
| `FuelEconomyListener` | `onFuelEconomyUpdated(efficiency: Float?)` | Callback invoked when fuel economy updates. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.listener/fuel-economy-listener/entry) |
| `FuelPercentListener` | `onFuelPercentUpdated(value: Int?)` | Callback invoked with the updated fuel level as a percentage (%). | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.listener/fuel-percent-listener/entry) |

## 관련 타입 (모델·상수)

파라미터와 콜백에 쓰이는 영역(Area)·Capability·상수 타입은 공식 API Reference 패키지 페이지에서 확인합니다.

- [api.model.area (DoorArea, SeatArea 등 영역 타입)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.area/entry)
- [api.model.capability (CapabilityConfig, AccessType, AreaBounds)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.capability/entry)
- [api.model.common (공통 모델)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.common/entry)
- [api.model.telltale (경고등 모델)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.telltale/entry)
- [constant (PleosPermission 등 상수)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.constant/entry)
- [constant.area / constant.control / constant.unit / constant.telltale](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.constant.area/entry)
- [callback.exception (VehicleException, 에러 코드)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.callback.exception/entry)
