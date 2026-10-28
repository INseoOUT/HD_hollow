# Toll (하이패스(ETCS))

> 마지막 확인일: 2026-10-28  
> 기준 버전: Vehicle SDK 2.3.2  
> 원본:
> - [Toll API Reference](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/toll/entry)
> - [Vehicle.getToll()](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/get-toll)
> - [Vehicle SDK 소개](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/intro)
> - [프로젝트 설정](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/project-setup)
> - [도메인 객체 획득](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/obtain-domain-object)
> - [Capability 확인](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/check-capability)
> - [Get](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/get-property)
> - [Set](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/set-property)
> - [Callback](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/callback)
> - [권한 선언](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-permission)
> - [에러 코드](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-error-code)

Provides APIs for ETCS voice guidance volume.

- 획득: `vehicle.getToll()` ([원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/get-toll))
- 권한: 조회 `pleos.car.permission.CAR_TOLL` · 제어 `pleos.car.permission.CONTROL_CAR_TOLL`
- 함수 5개 (deprecated 0개 포함), 리스너 1개

## 기능 요약

| 기능 | Capability | Get | Set | Callback |
|---|:-:|:-:|:-:|:-:|
| EtcsVoiceGuidanceVolume | ✓ | ✓ | ✓ | ✓ |

## 함수 목록

설명은 공식 KDoc 원문(영문)입니다. 괄호 안은 파라미터, → 뒤는 반환 타입입니다.

| 함수 | 파라미터 | 설명 | 권한 | 원본 |
|---|---|---|---|---|
| `checkEtcsVoiceGuidanceVolumeCapabilities` | `onComplete: (CapabilityConfig, AreaBounds<Int>) -> Unit, onFailed: (Exception) -> Unit` | Gets ETCS voice guidance volume capabilities. | `CAR_TOLL` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/toll/check-etcs-voice-guidance-volume-capabilities) |
| `getCurrentEtcsVoiceGuidanceVolume` | `onComplete: (Int?) -> Unit, onFailed: (Exception) -> Unit` | Gets current ETCS voice guidance volume. | `CAR_TOLL` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/toll/get-current-etcs-voice-guidance-volume) |
| `setEtcsVoiceGuidanceVolume` | `value: Int, onComplete: () -> Unit, onFailed: (Exception) -> Unit` | Sets ETCS voice guidance volume. | `CONTROL_CAR_TOLL` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/toll/set-etcs-voice-guidance-volume) |
| `registerEtcsVoiceGuidanceVolume` | `listener: EtcsVoiceGuidanceVolumeListener` | Registers ETCS voice guidance volume updates. | `CAR_TOLL` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/toll/register-etcs-voice-guidance-volume) |
| `unregisterEtcsVoiceGuidanceVolume` | `listener: EtcsVoiceGuidanceVolumeListener` | Stops ETCS voice guidance volume updates. | `CAR_TOLL` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/toll/unregister-etcs-voice-guidance-volume) |

## 리스너 (Callback)

`register*` 함수에 넘기는 리스너와 콜백 시그니처입니다.

| 리스너 | 콜백 | 설명 | 원본 |
|---|---|---|---|
| `EtcsVoiceGuidanceVolumeListener` | `onEtcsVoiceGuidanceVolumeUpdated(volume: Int?)` | Callback invoked when ETCS voice guidance volume updates. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.listener/etcs-voice-guidance-volume-listener/entry) |

## 관련 타입 (모델·상수)

파라미터와 콜백에 쓰이는 영역(Area)·Capability·상수 타입은 공식 API Reference 패키지 페이지에서 확인합니다.

- [api.model.area (DoorArea, SeatArea 등 영역 타입)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.area/entry)
- [api.model.capability (CapabilityConfig, AccessType, AreaBounds)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.capability/entry)
- [api.model.common (공통 모델)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.common/entry)
- [api.model.telltale (경고등 모델)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api.model.telltale/entry)
- [constant (PleosPermission 등 상수)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.constant/entry)
- [constant.area / constant.control / constant.unit / constant.telltale](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.constant.area/entry)
- [callback.exception (VehicleException, 에러 코드)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.callback.exception/entry)
