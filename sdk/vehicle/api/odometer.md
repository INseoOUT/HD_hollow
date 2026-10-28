# Odometer (주행거리·연비)

> 마지막 확인일: 2026-10-28  
> 기준 버전: Vehicle SDK 2.3.2  
> 원본:
> - [Odometer API Reference](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/odometer/entry)
> - [Vehicle.getOdometer()](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/get-odometer)
> - [Vehicle SDK 소개](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/intro)
> - [프로젝트 설정](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/project-setup)
> - [도메인 객체 획득](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/obtain-domain-object)
> - [Capability 확인](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/check-capability)
> - [Get](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/get-property)
> - [Set](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/set-property)
> - [Callback](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/callback)
> - [권한 선언](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-permission)
> - [에러 코드](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-error-code)

Provides APIs for odometer and trip data.

- 획득: `vehicle.getOdometer()` ([원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/get-odometer))
- 권한: 조회 `pleos.car.permission.CAR_DRIVE` · 제어 `pleos.car.permission.CONTROL_CAR_DRIVE`
- 함수 35개 (deprecated 5개 포함), 리스너 2개

## 기능 요약

| 기능 | Capability | Get | Set | Callback |
|---|:-:|:-:|:-:|:-:|
| CurrentDistanceDriven | ✓ |  |  |  |
| TripDrivingTime | ✓ |  |  |  |
| TripCurrentDrivingTime | ✓ | ✓ |  | ✓ |
| TripCumulativeDrivingTime | ✓ | ✓ |  | ✓ |
| TripSinceRefillDrivingTime | ✓ | ✓ |  | ✓ |
| TripDrivingDistance | ✓ |  |  |  |
| TripCurrentDrivingDistance | ✓ | ✓ |  | ✓ |
| TripInstantaneousDrivingDistance | ✓ | ✓ |  | ✓ |
| TripCumulativeDrivingDistance | ✓ | ✓ |  | ✓ |
| TripSinceRefillDrivingDistance | ✓ | ✓ |  | ✓ |

기타 함수: `getCurrentDistanceDriven`, `setRangeRemainingDistance`, `registerDistanceDriven`, `unregisterDistanceDriven`

## 함수 목록

설명은 공식 KDoc 원문(영문)입니다. 괄호 안은 파라미터, → 뒤는 반환 타입입니다.

| 함수 | 파라미터 | 설명 | 권한 | 원본 |
|---|---|---|---|---|
| `checkCurrentDistanceDrivenCapabilities` | `onComplete: (CapabilityConfig) -> Unit, onFailed: (Exception) -> Unit` | Gets current distance driven capabilities. | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/odometer/check-current-distance-driven-capabilities) |
| `getCurrentDistanceDriven` | `onComplete: (Float?) -> Unit, onFailed: (Exception) -> Unit` | Current odometer value of the vehicle in kilometers (km). | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/odometer/get-current-distance-driven) |
| `checkTripDrivingTimeCapabilities` | `onComplete: (CapabilityConfig) -> Unit, onFailed: (Exception) -> Unit` | Gets Trip Driving Time capabilities. | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/odometer/check-trip-driving-time-capabilities) |
| `checkTripCurrentDrivingTimeCapabilities` | `onComplete: (CapabilityConfig) -> Unit, onFailed: (Exception) -> Unit` | Gets Trip Current Driving Time capabilities. | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/odometer/check-trip-current-driving-time-capabilities) |
| `checkTripCumulativeDrivingTimeCapabilities` | `onComplete: (CapabilityConfig) -> Unit, onFailed: (Exception) -> Unit` | Gets Trip Cumulative Driving Time capabilities. | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/odometer/check-trip-cumulative-driving-time-capabilities) |
| `checkTripSinceRefillDrivingTimeCapabilities` | `onComplete: (CapabilityConfig) -> Unit, onFailed: (Exception) -> Unit` | Gets Trip Since Refuel Driving Time capabilities. | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/odometer/check-trip-since-refill-driving-time-capabilities) |
| `getTripCurrentDrivingTime` | `onComplete: (Int?) -> Unit, onFailed: (Exception) -> Unit` | Gets trip current driving time. | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/odometer/get-trip-current-driving-time) |
| `getTripCumulativeDrivingTime` | `onComplete: (Int?) -> Unit, onFailed: (Exception) -> Unit` | Gets trip cumulative driving time. | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/odometer/get-trip-cumulative-driving-time) |
| `getTripSinceRefillDrivingTime` | `onComplete: (Int?) -> Unit, onFailed: (Exception) -> Unit` | Gets trip since refuel driving time. | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/odometer/get-trip-since-refill-driving-time) |
| `checkTripDrivingDistanceCapabilities` | `onComplete: (CapabilityConfig) -> Unit, onFailed: (Exception) -> Unit` | Gets Trip Driving Distance capabilities. | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/odometer/check-trip-driving-distance-capabilities) |
| `checkTripCurrentDrivingDistanceCapabilities` | `onComplete: (CapabilityConfig) -> Unit, onFailed: (Exception) -> Unit` | Gets Trip Current Driving Distance capabilities. | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/odometer/check-trip-current-driving-distance-capabilities) |
| `checkTripInstantaneousDrivingDistanceCapabilities` | `onComplete: (CapabilityConfig) -> Unit, onFailed: (Exception) -> Unit` | ⚠️ Deprecated. This API is deprecated because it uses TripStatisticType.INSTANTANEOUS. (Gets Trip Instantaneous Driving Distance capabilities.) | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/odometer/check-trip-instantaneous-driving-distance-capabilities) |
| `checkTripCumulativeDrivingDistanceCapabilities` | `onComplete: (CapabilityConfig) -> Unit, onFailed: (Exception) -> Unit` | Gets Trip Cumulative Driving Distance capabilities. | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/odometer/check-trip-cumulative-driving-distance-capabilities) |
| `checkTripSinceRefillDrivingDistanceCapabilities` | `onComplete: (CapabilityConfig) -> Unit, onFailed: (Exception) -> Unit` | Gets Trip Since Refuel Driving Distance capabilities. | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/odometer/check-trip-since-refill-driving-distance-capabilities) |
| `getTripCurrentDrivingDistance` | `onComplete: (Float?) -> Unit, onFailed: (Exception) -> Unit` | Gets trip current driving distance in meters (m). | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/odometer/get-trip-current-driving-distance) |
| `getTripInstantaneousDrivingDistance` | `onComplete: (Float?) -> Unit, onFailed: (Exception) -> Unit` | ⚠️ Deprecated. This API is deprecated because it uses TripStatisticType.INSTANTANEOUS. (Gets trip instantaneous driving distance in meters (m).) | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/odometer/get-trip-instantaneous-driving-distance) |
| `getTripCumulativeDrivingDistance` | `onComplete: (Float?) -> Unit, onFailed: (Exception) -> Unit` | Gets trip cumulative driving distance in meters (m). | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/odometer/get-trip-cumulative-driving-distance) |
| `getTripSinceRefillDrivingDistance` | `onComplete: (Float?) -> Unit, onFailed: (Exception) -> Unit` | Gets trip since refuel driving distance in meters (m). | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/odometer/get-trip-since-refill-driving-distance) |
| `setRangeRemainingDistance` | `value: Float, onComplete: (Float?) -> Unit = {}, onFailed: (Exception) -> Unit = {}` | ⚠️ Deprecated. This API is deprecated (Sets the estimated remaining range in meters (m).) | `CONTROL_CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/odometer/set-range-remaining-distance) |
| `registerDistanceDriven` | `listener: DistanceDrivenListener` | Registers a listener for odometer updates in kilometers (km). | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/odometer/register-distance-driven) |
| `unregisterDistanceDriven` | `listener: DistanceDrivenListener` | Stops getting property updates for the given DistanceDriven. | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/odometer/unregister-distance-driven) |
| `registerTripCurrentDrivingTime` | `listener: DrivingTimeListener` | Registers Trip Current Driving Time listener. | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/odometer/register-trip-current-driving-time) |
| `unregisterTripCurrentDrivingTime` | `listener: DrivingTimeListener` | Unregisters Trip Current Driving Time listener. | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/odometer/unregister-trip-current-driving-time) |
| `registerTripCumulativeDrivingTime` | `listener: DrivingTimeListener` | Registers Trip Cumulative Driving Time listener. | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/odometer/register-trip-cumulative-driving-time) |
| `unregisterTripCumulativeDrivingTime` | `listener: DrivingTimeListener` | Unregisters Trip Cumulative Driving Time listener. | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/odometer/unregister-trip-cumulative-driving-time) |
| `registerTripSinceRefillDrivingTime` | `listener: DrivingTimeListener` | Registers Trip Since Refuel Driving Time listener. | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/odometer/register-trip-since-refill-driving-time) |
| `unregisterTripSinceRefillDrivingTime` | `listener: DrivingTimeListener` | Unregisters Trip Since Refuel Driving Time listener. | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/odometer/unregister-trip-since-refill-driving-time) |
| `registerTripCurrentDrivingDistance` | `listener: DistanceDrivenListener` | Registers a listener for trip current driving distance updates in meters (m). | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/odometer/register-trip-current-driving-distance) |
| `unregisterTripCurrentDrivingDistance` | `listener: DistanceDrivenListener` | Unregisters Trip Current Driving Distance listener. | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/odometer/unregister-trip-current-driving-distance) |
| `registerTripInstantaneousDrivingDistance` | `listener: DistanceDrivenListener` | ⚠️ Deprecated. This API is deprecated because it uses TripStatisticType.INSTANTANEOUS. (Registers a listener for trip instantaneous driving distance updates in meters (m).) | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/odometer/register-trip-instantaneous-driving-distance) |
| `unregisterTripInstantaneousDrivingDistance` | `listener: DistanceDrivenListener` | ⚠️ Deprecated. This API is deprecated because it uses TripStatisticType.INSTANTANEOUS. (Unregisters Trip Instantaneous Driving Distance listener.) | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/odometer/unregister-trip-instantaneous-driving-distance) |
| `registerTripCumulativeDrivingDistance` | `listener: DistanceDrivenListener` | Registers a listener for trip cumulative driving distance updates in meters (m). | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/odometer/register-trip-cumulative-driving-distance) |
| `unregisterTripCumulativeDrivingDistance` | `listener: DistanceDrivenListener` | Unregisters Trip Cumulative Driving Distance listener. | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/odometer/unregister-trip-cumulative-driving-distance) |
| `registerTripSinceRefillDrivingDistance` | `listener: DistanceDrivenListener` | Registers a listener for trip since refuel driving distance updates in meters (m). | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/odometer/register-trip-since-refill-driving-distance) |
| `unregisterTripSinceRefillDrivingDistance` | `listener: DistanceDrivenListener` | Unregisters Trip Since Refuel Driving Distance listener. | `CAR_DRIVE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/odometer/unregister-trip-since-refill-driving-distance) |

## 리스너 (Callback)

`register*` 함수에 넘기는 리스너와 콜백 시그니처입니다.

| 리스너 | 콜백 | 설명 | 원본 |
|---|---|---|---|
| `DistanceDrivenListener` | `onDistanceDrivenUpdated(value: Float?)` | Called with the updated distance in kilometers (km) or meters (m), depending on the registration API. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.listener/distance-driven-listener/entry) |
| `DrivingTimeListener` | `onDrivingTimeUpdated(value: Int?)` | Callback invoked when driving time updates. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.listener/driving-time-listener/entry) |

## 관련 타입 (모델·상수)

파라미터와 콜백에 쓰이는 영역(Area)·Capability·상수 타입은 공식 API Reference 패키지 페이지에서 확인합니다.

- [api.model.area (DoorArea, SeatArea 등 영역 타입)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.area/entry)
- [api.model.capability (CapabilityConfig, AccessType, AreaBounds)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.capability/entry)
- [api.model.common (공통 모델)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.common/entry)
- [api.model.telltale (경고등 모델)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.telltale/entry)
- [constant (PleosPermission 등 상수)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.constant/entry)
- [constant.area / constant.control / constant.unit / constant.telltale](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.constant.area/entry)
- [callback.exception (VehicleException, 에러 코드)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.callback.exception/entry)
