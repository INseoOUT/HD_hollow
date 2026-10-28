# FusedLocation SDK 함수 목록

> 마지막 확인일: 2026-10-28  
> 기준 버전: FusedLocation SDK 1.1.3  
> 원본:
> - [FusedLocation API Reference](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/entry)
> - [소개](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/intro)
> - [프로젝트 설정](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/project-setup)
> - [권한 선언](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/declaring-permissions)
> - [초기화와 해제](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/initialization-and-release)
> - [위치 데이터 접근](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/location-data-access)
> - [에러 처리](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/error-handling)

설명은 공식 KDoc 원문(영문)입니다. 권한은 `pleos.car.permission.FUSED_LOCATION` 하나이며 런타임 권한 요청이 필요합니다.

## FusedLocation 클래스

원본: [FusedLocation API Reference](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/fused-location/ai.pleos.playground.location.fused/fused-location/entry)

| 함수 | 파라미터 | 설명 | 권한 | 원본 |
|---|---|---|---|---|
| `initialize` | `()` | Initializes the SDK. | - | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/fused-location/ai.pleos.playground.location.fused/fused-location/initialize) |
| `release` | `()` | Releases the SDK. | - | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/fused-location/ai.pleos.playground.location.fused/fused-location/release) |
| `isSupported` | `()` → `Boolean` | Returns whether the Fused Location Service package is present on Pleos Connect. | - | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/fused-location/ai.pleos.playground.location.fused/fused-location/is-supported) |
| `getLastKnownLocation` | `callback: FusedLocationListener` | Gets the last known location. | `FUSED_LOCATION` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/fused-location/ai.pleos.playground.location.fused/fused-location/get-last-known-location) |
| `registerFusedLocationCallback` | `listener: FusedLocationListener` | Registers a callback to receive location updates. | `FUSED_LOCATION` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/fused-location/ai.pleos.playground.location.fused/fused-location/register-fused-location-callback) |
| `unregisterFusedLocationCallback` | `listener: FusedLocationListener` | Stops receiving location updates through the specified listener. | `FUSED_LOCATION` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/fused-location/ai.pleos.playground.location.fused/fused-location/unregister-fused-location-callback) |
| `addProximityAlert` | `request: ProximityAlertRequest, listener: ProximityAlertListener` | Registers a proximity alert for the specified area and transitions. | `FUSED_LOCATION` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/fused-location/ai.pleos.playground.location.fused/fused-location/add-proximity-alert) |
| `removeProximityAlert` | `alertId: String` | Removes a previously registered proximity alert. | `FUSED_LOCATION` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/fused-location/ai.pleos.playground.location.fused/fused-location/remove-proximity-alert) |
| `showLocationAccessBlockedPopup` | `()` | Displays a popup message informing the user that location access is blocked due to their current privacy settings. | - | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/fused-location/ai.pleos.playground.location.fused/fused-location/show-location-access-blocked-popup) |

## FusedLocationListener (리스너)

Defines the listener interface for receiving location updates.

원본: [FusedLocationListener API Reference](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/fused-location/ai.pleos.playground.location.fused.listener/fused-location-listener/entry)

| 콜백 | 파라미터 | 설명 | 원본 |
|---|---|---|---|
| `onChanged` | `location: LocationData?` | Invoked when a location update is available. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/fused-location/ai.pleos.playground.location.fused.listener/fused-location-listener/on-changed) |
| `onError` | `exception: Exception` | 문서 없음 | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/fused-location/ai.pleos.playground.location.fused.listener/fused-location-listener/on-error) |

## ProximityAlertListener (리스너)

Receives events and errors for a registered proximity alert.

원본: [ProximityAlertListener API Reference](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/fused-location/ai.pleos.playground.location.fused.listener/proximity-alert-listener/entry)

| 콜백 | 파라미터 | 설명 | 원본 |
|---|---|---|---|
| `onRegistered` | `alertId: String` | Invoked when the alert is registered successfully. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/fused-location/ai.pleos.playground.location.fused.listener/proximity-alert-listener/on-registered) |
| `onTransition` | `event: ProximityAlertEvent` | Invoked when a configured proximity transition is detected. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/fused-location/ai.pleos.playground.location.fused.listener/proximity-alert-listener/on-transition) |
| `onError` | `exception: Exception` | Invoked when the alert cannot be registered or processed. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/fused-location/ai.pleos.playground.location.fused.listener/proximity-alert-listener/on-error) |

## 관련 타입 (데이터 모델·상수)

함수 파라미터와 콜백에 쓰이는 타입은 공식 API Reference의 패키지 페이지에서 확인합니다.

- [ai.pleos.playground.location.fused.model (위치·Proximity 모델)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/fused-location/ai.pleos.playground.location.fused.model/entry)
