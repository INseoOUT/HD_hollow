# CarInfo (차량 정보)

> 마지막 확인일: 2026-10-28  
> 기준 버전: Vehicle SDK 2.3.2  
> 원본:
> - [CarInfo API Reference](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/car-info/entry)
> - [Vehicle.getCarInfo()](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/get-car-info)
> - [Vehicle SDK 소개](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/intro)
> - [프로젝트 설정](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/project-setup)
> - [도메인 객체 획득](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/obtain-domain-object)
> - [Capability 확인](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/check-capability)
> - [Get](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/get-property)
> - [Set](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/set-property)
> - [Callback](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/callback)
> - [권한 선언](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-permission)
> - [에러 코드](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-error-code)

Provides APIs for vehicle identification information and capability info.

- 획득: `vehicle.getCarInfo()` ([원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/get-car-info))
- 권한: 조회 `pleos.car.permission.CAR_INFO`
- 함수 4개 (deprecated 2개 포함), 리스너 0개

## 기능 요약

| 기능 | Capability | Get | Set | Callback |
|---|:-:|:-:|:-:|:-:|
| CRNInfo | ✓ | ✓ |  |  |
| VINInfo | ✓ | ✓ |  |  |

## 함수 목록

설명은 공식 KDoc 원문(영문)입니다. 괄호 안은 파라미터, → 뒤는 반환 타입입니다.

| 함수 | 파라미터 | 설명 | 권한 | 원본 |
|---|---|---|---|---|
| `checkCRNInfoCapabilities` | `onComplete: (CapabilityConfig) -> Unit, onFailed: (Exception) -> Unit` | ⚠️ Deprecated. Use checkVINInfoCapabilities to check VIN capability. (Gets CRN info capabilities.) | `CAR_INFO` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/car-info/check-c-r-n-info-capabilities) |
| `checkVINInfoCapabilities` | `onComplete: (CapabilityConfig) -> Unit, onFailed: (Exception) -> Unit` | Gets VIN info capabilities. | `CAR_INFO` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/car-info/check-v-i-n-info-capabilities) |
| `getVINInfo` | `onComplete: (String?) -> Unit, onFailed: (Exception) -> Unit` | Gets the Vehicle Identification Number (VIN). | `CAR_INFO` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/car-info/get-v-i-n-info) |
| `getCRNInfo` | `onComplete: (String?) -> Unit, onFailed: (Exception) -> Unit` | ⚠️ Deprecated. Use getVINInfo to retrieve the Vehicle Identification Number (VIN). (Gets the Connected Resource Name (CRN) Info.) | `CAR_INFO` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/car-info/get-c-r-n-info) |

## 관련 타입 (모델·상수)

파라미터와 콜백에 쓰이는 영역(Area)·Capability·상수 타입은 공식 API Reference 패키지 페이지에서 확인합니다.

- [api.model.area (DoorArea, SeatArea 등 영역 타입)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.area/entry)
- [api.model.capability (CapabilityConfig, AccessType, AreaBounds)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.capability/entry)
- [api.model.common (공통 모델)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.common/entry)
- [api.model.telltale (경고등 모델)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.telltale/entry)
- [constant (PleosPermission 등 상수)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.constant/entry)
- [constant.area / constant.control / constant.unit / constant.telltale](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.constant.area/entry)
- [callback.exception (VehicleException, 에러 코드)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.callback.exception/entry)
