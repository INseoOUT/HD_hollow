# FusedLocation SDK

> 마지막 확인일: 2026-10-28  
> 기준 버전: FusedLocation SDK 1.1.3  
> 원본:
> - [FusedLocation SDK 소개](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/intro)
> - [SDK 가져오기](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/project-setup)
> - [권한 선언](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/declaring-permissions)
> - [초기화와 해제](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/initialization-and-release)
> - [위치 데이터 접근](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/location-data-access)
> - [오류 처리](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/error-handling)
> - [FusedLocation API Reference](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/entry)
> - [LocationData API Reference](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/fused-location/ai.pleos.playground.location.fused.model/location-data/entry)
> - [SDK 릴리즈 노트 2026-10-19](https://document.pleos.ai/docs/pleos-only/release-note/sdk-261015)
> - [API 호환성 및 가용성](https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/api-compatibility-availability)
> - [프로젝트 생성 및 SDK 사용 신청](https://document.pleos.ai/docs/pleos-only/create-my-project)
> - [공식 샘플 앱 pleos-sample-fusedlocation-app](https://github.com/Pleos-Playground/pleos-sample-fusedlocation-app)
>
> 함수 전체 목록: [api.md](api.md)

## 설명

FusedLocation SDK는 앱이 차량 위치 데이터를 받을 수 있게 하는 API입니다. 앱은 이 SDK를 통해 Pleos Connect의 FusedLocation Service와 연동됩니다. 마지막 위치 1회 조회, 위치 업데이트 구독, 특정 영역 진입·이탈 알림(Proximity Alert)을 제공하며, 진입점은 `FusedLocation` 클래스입니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/intro)

| 항목 | 내용 |
|---|---|
| 버전 | 1.1.3 (2026-10-19 릴리즈) |
| Playground 제공 상태 | **준비 중** (`Comming Soon` 배지, 신청 버튼 없음). 문서 기준 2026-10-28이며 본인 프로젝트의 SDK 메뉴에서 재확인 필요 |
| 차량 호환성 | 모델에 따라 다름. `isSupported()`로 확인 |
| 의존성 | `implementation("ai.pleos.playground:FusedLocation:1.1.3")` |
| Maven 저장소 | `https://nexus-playground.pleos.ai/repository/maven-releases/` (인증 불필요) |

[원본: 제공 상태](https://document.pleos.ai/docs/pleos-only/create-my-project) · [원본: 호환성](https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/api-compatibility-availability) · [원본: 의존성](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/project-setup)

**권한.** 퍼미션은 `pleos.car.permission.FUSED_LOCATION` 하나이며 보호 수준이 `dangerous`입니다. 매니페스트에 선언하고, 권한이 허용되지 않은 상태에서 호출하면 API가 정상 동작하지 않을 수 있으므로 권한 요청과 결과 처리를 앱의 권한 처리 흐름에 맞게 구현합니다. 공식 샘플 앱은 권한이 허용된 뒤에 SDK를 초기화합니다. Android의 `ACCESS_FINE_LOCATION` 등 표준 위치 권한 정책은 이 SDK 문서가 보장하지 않습니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/declaring-permissions) · [원본](https://github.com/Pleos-Playground/pleos-sample-fusedlocation-app)

| 권한 | 보호 수준 | 사용 API |
|---|---|---|
| `pleos.car.permission.FUSED_LOCATION` | `dangerous` | `getLastKnownLocation`, `registerFusedLocationCallback`, `unregisterFusedLocationCallback` (API Reference 기준 `addProximityAlert`, `removeProximityAlert`도 동일 권한) |

```xml
<uses-permission android:name="pleos.car.permission.FUSED_LOCATION" />
```

**동작 전제.** API를 호출하기 전에 `isSupported()`로 현재 실행 환경의 지원 여부를 확인해야 하고, 지원되지 않으면 위치 기능을 제공하지 않도록 처리합니다. `getLastKnownLocation()`은 새 위치를 계산하지 않고 사용 가능한 마지막 위치를 돌려주므로 `null`이 올 수 있습니다. 위치 결과 `LocationData`는 위도, 경도, 고도, 진행 방향, 신뢰도, 타임스탬프를 담습니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/intro)

**에러 처리.** 오류는 `FusedLocationListener.onError(Exception)`으로 전달됩니다. `onChanged(LocationData?)`의 인자는 `null`일 수 있으므로 사용 전 확인하고, `null`을 유효한 위치로 처리하지 않습니다. 차량 개인정보 설정으로 위치 접근이 차단된 경우 `showLocationAccessBlockedPopup()`으로 차단 상태를 안내할 수 있지만, 이 API가 접근을 허용해 주지는 않습니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/error-handling) · [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/declaring-permissions)

## 기능

함수 9개와 콜백 5개로 구성됩니다. 함수별 시그니처와 개별 원본 링크는 [api.md](api.md)에 있습니다.

| 기능 그룹 | 함수 | 결과를 받는 콜백 |
|---|---|---|
| 지원 확인 | `isSupported(): Boolean` | 반환값 |
| 생명주기 | `initialize()`, `release()` | - |
| 마지막 위치 1회 조회 | `getLastKnownLocation(callback)` | `FusedLocationListener.onChanged`, `onError` |
| 위치 업데이트 수신·중지 | `registerFusedLocationCallback(listener)`, `unregisterFusedLocationCallback(listener)` | `FusedLocationListener.onChanged`, `onError` |
| 영역 진입·이탈 알림 | `addProximityAlert(request, listener)`, `removeProximityAlert(alertId)` | `ProximityAlertListener.onRegistered`, `onTransition`, `onError` |
| 접근 차단 안내 | `showLocationAccessBlockedPopup()` | - |

[원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/intro)

| 리스너 | 콜백 | 시점 |
|---|---|---|
| `FusedLocationListener` | `onChanged(location: LocationData?)` | 위치 업데이트가 있을 때 (`null` 가능) |
| `FusedLocationListener` | `onError(exception: Exception)` | 오류 발생 시. 1.1.3부터 구현 필수 |
| `ProximityAlertListener` | `onRegistered(alertId: String)` | 알림 등록 성공 시. 이 `alertId`로 `removeProximityAlert` 호출 |
| `ProximityAlertListener` | `onTransition(event: ProximityAlertEvent)` | 설정한 영역 진입·이탈이 감지될 때 |
| `ProximityAlertListener` | `onError(exception: Exception)` | 알림을 등록·처리할 수 없을 때 |

[원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/location-data-access) · [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/fused-location/ai.pleos.playground.location.fused.listener/proximity-alert-listener/entry)

## 활용

**기본 흐름.** 객체 생성 → `isSupported()` 확인 → `initialize()` → 위치 조회 또는 업데이트 등록 → 콜백 처리 → `unregisterFusedLocationCallback()` → `release()` 순서입니다. `unregister`는 리스너 해제, `release`는 SDK 해제이므로 서로 대체할 수 없고, `initialize()`를 호출했다면 끝날 때 반드시 `release()`를 호출합니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/initialization-and-release)

```kotlin
import ai.pleos.playground.location.fused.FusedLocation
import ai.pleos.playground.location.fused.listener.FusedLocationListener
import ai.pleos.playground.location.fused.model.LocationData

class MyApplication : Application() {
    lateinit var fusedLocation: FusedLocation
        private set

    override fun onCreate() {
        super.onCreate()
        // 1. 인스턴스 생성
        fusedLocation = FusedLocation(this)

        // 2. 지원 여부 확인 후 초기화 (권한 허용 이후에 초기화하는 흐름 권장)
        if (fusedLocation.isSupported()) {
            fusedLocation.initialize()
        } else {
            // 위치 관련 UI와 기능을 비활성화
        }
    }

    fun shutdownFusedLocation(listener: FusedLocationListener) {
        // 3. 등록한 리스너를 먼저 해제
        fusedLocation.unregisterFusedLocationCallback(listener)
        // 4. SDK 해제. 이후 같은 인스턴스로 호출 금지
        fusedLocation.release()
    }
}

// 위치 업데이트 수신
val listener = object : FusedLocationListener {
    override fun onChanged(location: LocationData?) {
        location ?: return      // null은 유효한 위치가 아님
        // location 사용
    }

    override fun onError(exception: Exception) {
        // 기록하고 사용자에게 위치 사용 불가 안내 또는 기능 제한
    }
}
fusedLocation.registerFusedLocationCallback(listener)

// 마지막 위치 1회 조회 (같은 리스너 인터페이스로 수신)
fusedLocation.getLastKnownLocation(listener)

// 수신 중지: 등록할 때 쓴 동일 리스너를 전달
fusedLocation.unregisterFusedLocationCallback(listener)
```

[원본: 초기화·해제](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/initialization-and-release) · [원본: 위치 데이터 접근](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/location-data-access)

**Proximity Alert.** `ProximityAlertRequest`로 영역과 전환 종류를 지정해 `addProximityAlert`에 리스너와 함께 넘기고, `onRegistered(alertId)`로 받은 ID로 `removeProximityAlert(alertId)`를 호출합니다. 요청 모델의 필드는 [model 패키지 API Reference](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/fused-location/ai.pleos.playground.location.fused.model/entry)에서 확인합니다. 공식 샘플은 ENTER / EXIT 이벤트를 수신합니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/fused-location/ai.pleos.playground.location.fused/fused-location/add-proximity-alert) · [원본](https://github.com/Pleos-Playground/pleos-sample-fusedlocation-app)

**주의사항 (1.1.3 Breaking change 포함).** [원본](https://document.pleos.ai/docs/pleos-only/release-note/sdk-261015)

- `FusedLocationListener.onChanged`의 인자가 `LocationData`에서 `LocationData?`로 바뀌어 null 처리가 필요합니다.
- `FusedLocationListener.onError(exception: Exception)` 구현이 필수가 됐습니다.
- `addProximityAlert`, `removeProximityAlert`, `getLastKnownLocation`, `isSupported`, `showLocationAccessBlockedPopup`은 1.1.3에서 추가된 API입니다.
- 에뮬레이터 전제: Pleos Connect Emulator 설치, AVD 생성, Virtual Console 설정, 마이크 접근 설정, Cold boot 설정을 먼저 마칩니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/intro)

**공식 샘플 앱.** 서비스 가용성 확인·초기화·해제, 마지막 위치 조회와 연속 업데이트 시작·중지, Proximity Alert 등록·해제와 ENTER / EXIT 수신을 다룹니다. 앱은 `isSupported()`를 먼저 확인하고, 권한이 허용된 뒤 SDK를 초기화합니다. [원본](https://github.com/Pleos-Playground/pleos-sample-fusedlocation-app)

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n ai.pleos.playground.location.fused.sample/.MainActivity

# 에뮬레이터에 테스트 위치 주입 (경도, 위도 순서)
adb emu geo fix 127.0276 37.4979

# Proximity Alert 테스트용 가상 좌표 주입 전 mock location 허용
adb shell appops set --user current ai.pleos.playground.location.fused.sample android:mock_location allow
```

**예시: 공모전 아이디어에 쓸 조합.**

- 예시 1. 주차·픽업 도우미: 매장이나 주차장 영역을 `addProximityAlert`로 등록해 진입 시 자동으로 주문 상태를 알리거나 결제 화면을 띄움.
- 예시 2. 가족 동승 케어: 집·학교 등 지정 지점의 ENTER / EXIT 이벤트를 받아 도착 알림과 하차 체크리스트를 표시.
- 예시 3. 휴식 추천: `registerFusedLocationCallback`의 연속 위치와 Odometer 도메인의 주행거리를 합쳐 장시간 운행 시 근처 휴게 지점을 NaviHelper 경유지로 제안.
