# Pleos Connect SDK 개요

> 마지막 확인일: 2026-10-28  
> 기준 버전: Vehicle 2.3.2 · NaviHelper 3.0.5 · TextToSpeech 3.3.1 · SpeechToText 3.3.1 · FusedLocation 1.1.3 (2026-10-19 릴리즈)  
> 원본:
> - [Pleos Connect SDK Overview](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/)
> - [Pleos Connect SDK 릴리즈 노트 (2026-10-19)](https://document.pleos.ai/docs/pleos-only/release-note/sdk-261015)
> - [Pleos Connect Emulator 릴리즈 노트 (2026-10-19)](https://document.pleos.ai/docs/pleos-only/release-note/emulator-261015)
> - [프로젝트 생성 및 SDK 사용 신청](https://document.pleos.ai/docs/pleos-only/create-my-project)
> - [API 호환성 및 가용성](https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/api-compatibility-availability)
> - [SDK API 정책](https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/pleos-connect-sdk-api)
> - [Vehicle SDK 소개](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/intro) · [Importing Vehicle SDK](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/project-setup) · [Vehicle 권한 선언](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-permission)
> - [NaviHelper SDK 소개](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/intro) · [NaviHelper 권한 선언](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/declaring-permissions)
> - [TextToSpeech SDK 소개](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/intro) · [TextToSpeech 권한 선언](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/declaring-permissions) · [TextToSpeech 앱 등록](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/app-registration)
> - [SpeechToText SDK 소개](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/intro) · [SpeechToText 권한 선언](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/declaring-permissions)
> - [FusedLocation SDK 소개](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/intro) · [FusedLocation 권한 선언](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/declaring-permissions)
> - [Vehicle SDK Sample App 사용 가이드](https://document.pleos.ai/docs/pleos-only/tutorial/vehicle-sdk-sample-app)

Pleos Connect SDK는 Pleos Connect(현대자동차그룹 차량용 인포테인먼트 플랫폼) 위에서 동작하는 앱을 만들기 위한 통합 개발 도구입니다. Android Automotive OS(AAOS) 기반이며, 차량 하드웨어 연동 없이 표준화된 인터페이스로 차량 앱을 개발할 수 있습니다. 공식 모듈은 Vehicle, NaviHelper, TextToSpeech, SpeechToText, FusedLocation 5종입니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/)

## SDK 5종 비교

| SDK | 역할 | 버전 | Playground 제공 상태 | 의존성 (`ai.pleos.playground:`) | 권한 (`pleos.car.permission.`) | 서버 연결 | 함수 / 콜백 | 문서 |
|---|---|---|---|---|---|---|---|---|
| Vehicle | 차량 상태 조회·제어 (19개 도메인) | 2.3.2 | 조회(Vehicle State) **사용 가능**, 신청 불필요 · 제어(Vehicle Control) **준비 중** | `Vehicle:2.3.2` | `CAR_*` (조회), `CONTROL_CAR_*` (제어) | 불필요 | 458 + 진입점 21 / 67 | [vehicle/](vehicle/README.md) |
| NaviHelper | 경로 안내 제어, 내비게이션 정보 조회 | 3.0.5 | **사용 가능**, 신청 불필요 | `NaviHelper:3.0.5` | `NAVI_ROUTE`, `NAVI_ROUTE_SEARCH`, `NAVI_CUSTOM_MAP` | 불필요 | 21 / 25 | [navihelper/](navihelper/README.md) |
| TextToSpeech | 텍스트 → 음성 합성·재생 | 3.3.1 | **준비 중** | `TextToSpeech:3.3.1` | `TTS_SERVICE` | `HYBRID`·`SERVER` 모드는 Client ID/Secret으로 앱 등록 필요, `ON_DEVICE`는 불필요 | 11 / 10 | [texttospeech/](texttospeech/README.md) |
| SpeechToText | 음성 → 텍스트 변환 (명령 실행은 하지 않음) | 3.3.1 | **준비 중** | `SpeechToText:3.3.1` | `STT_SERVICE` | `HYBRID`·`SERVER` 모드는 앱 등록 필요, `ON_DEVICE`는 불필요 | 10 / 11 | [speechtotext/](speechtotext/README.md) |
| FusedLocation | 차량 위치 조회, 영역 진입·이탈 알림 | 1.1.3 | **준비 중** | `FusedLocation:1.1.3` | `FUSED_LOCATION` (`dangerous`, 런타임 권한 처리 필요) | 불필요 | 9 / 5 | [fusedlocation/](fusedlocation/README.md) |

함수 수는 공식 API Reference에 페이지가 있는 공개 함수 기준이고, 콜백 수는 리스너 인터페이스의 콜백 메서드 수입니다. 역할과 버전은 릴리즈 노트와 각 SDK 소개 페이지 기준입니다. [원본](https://document.pleos.ai/docs/pleos-only/release-note/sdk-261015)

**제공 상태 주의.** 위 상태는 2026-10-28에 공식 문서(프로젝트 생성 및 SDK 사용 신청 페이지)에서 확인한 값입니다. 문서는 "준비 중(Coming Soon)" 상태인 SDK로 FusedLocation, TextToSpeech, SpeechToText, Vehicle Control을, "사용 가능" 상태로 Vehicle State와 NaviHelper를 명시합니다. 상태는 바뀔 수 있으므로 본인 Playground 콘솔(Workspace > My Project > SDK)에서 다시 확인해야 합니다. 준비 중인 항목은 신청 버튼이 없고, "신청 필요"로 바뀌면 신청 → 검토 중 → 사용 가능 순서로 진행됩니다. "제휴 계약 필요" 항목은 웹에서 신청할 수 없습니다. [원본](https://document.pleos.ai/docs/pleos-only/create-my-project)

권한은 Pleos App Market과 사전 협의·승인된 범위 안에서만 사용할 수 있습니다. 승인되지 않은 권한을 선언하거나 해당 API를 호출하면 앱 심사를 통과하지 못합니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/)

### 차량 모델별 호환성

| SDK | 차량 모델별 호환성 | 확인 방법 |
|---|---|---|
| Vehicle | 모델에 따라 다름 | 도메인별 `checkXXXCapabilities` |
| NaviHelper | 내비게이션 탑재 차량에 한해 전체 호환 | - |
| TextToSpeech, SpeechToText | 전체 호환 | - |
| FusedLocation | 모델에 따라 다름 | `isSupported()` |

모델명을 기준으로 분기하지 말고 capability·`isSupported`로 실제 지원 여부를 확인해야 합니다. SDK가 제공하지 않는 특수 프로퍼티가 필요하면 직접 구현 전에 기술 지원팀 검토를 요청하라고 안내합니다. [원본](https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/api-compatibility-availability)

## 공통 설정

### 요구 환경

| 항목 | 값 |
|---|---|
| Android API | 28 이상 (Playground 배포 앱은 signing key rotation·lineage 지원을 위해 필수) |
| Gradle | 8.0 이상 |
| 언어 | Java 또는 Kotlin |
| 에뮬레이터 | Pleos Connect Emulator 2.1.0 (API 34 시스템 이미지, Automotive AVD) |
| 공식 샘플 기준 | JDK 17, Gradle wrapper 8.10.2, AGP 8.8.2, Kotlin 2.0.0, compileSdk/targetSdk 35 |

샘플 앱 기준 버전은 Vehicle SDK Sample App 가이드의 표에서 가져왔습니다. 에뮬레이터 설치 절차는 [../contest/environment.md](../contest/environment.md)에 정리되어 있습니다. [원본](https://document.pleos.ai/docs/pleos-only/tutorial/vehicle-sdk-sample-app)

### Maven 저장소와 의존성

Maven 저장소는 인증 없이 접근됩니다. `settings.gradle.kts`에 저장소를 등록하고 앱 모듈 `build.gradle.kts`에 필요한 모듈만 추가합니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/project-setup)

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://nexus-playground.pleos.ai/repository/maven-releases/")
        }
    }
}
```

```kotlin
// app/build.gradle.kts — 사용하는 모듈만 선언
dependencies {
    implementation("ai.pleos.playground:Vehicle:2.3.2")
    implementation("ai.pleos.playground:NaviHelper:3.0.5")
    implementation("ai.pleos.playground:TextToSpeech:3.3.1")
    implementation("ai.pleos.playground:SpeechToText:3.3.1")
    implementation("ai.pleos.playground:FusedLocation:1.1.3")
}
```

### 권한 선언

사용하는 API에 맞는 권한만 `AndroidManifest.xml`에 선언합니다. Vehicle 권한은 일반 Android 위험 권한처럼 실행 중 승인 창을 띄우지 않으며, FusedLocation 권한은 보호 수준이 `dangerous`라 앱의 권한 처리 흐름이 필요합니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/FusedLocation/declaring-permissions)

```xml
<!-- 예시: 도어 조회 + 경로 요청 + 위치 -->
<uses-permission android:name="pleos.car.permission.CAR_DOORS" />
<uses-permission android:name="pleos.car.permission.NAVI_ROUTE" />
<uses-permission android:name="pleos.car.permission.NAVI_ROUTE_SEARCH" />
<uses-permission android:name="pleos.car.permission.FUSED_LOCATION" />
```

## 공통 패턴

다섯 모듈 모두 같은 생명주기를 따릅니다. 진입점 객체를 만들고 `initialize()`로 서비스에 연결한 뒤 API를 호출하고, 끝나면 리스너를 해제하고 `release()`로 자원을 반환합니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/obtain-domain-object)

| 패턴 | 내용 | 해당 SDK |
|---|---|---|
| 초기화 → 사용 → 해제 | `initialize()` / `release()`. 앱 생성 시 초기화, 종료 시 해제 | 전체 |
| 비동기 결과 | 차량 통신은 IPC라 결과가 즉시 반환되지 않고 `onComplete`·`onFailed` 람다 또는 리스너 콜백으로 전달됨. 콜백이 끝나기 전에 `release()` 호출 금지 | Vehicle, NaviHelper, FusedLocation |
| Capability 먼저 | `checkXXXCapabilities`로 지원 여부·접근 수준(`AccessType`)·값 범위(`AreaBounds`)를 확인한 뒤 get/set 호출 | Vehicle |
| 지원 환경 확인 | `isSupported()` 결과가 false면 기능을 제공하지 않도록 처리 | FusedLocation |
| 서버 연결 앱 등록 | `initialize()` 후 `registerApp(clientId, clientSecret, listener)`. `onConnected()` 이후 `speak()`/`request()` 호출 | TextToSpeech, SpeechToText (`HYBRID`·`SERVER` 모드) |
| 리스너 등록·해제 | `register*`/`unregister*`, `addListener`/`removeListener`. 등록에 쓴 같은 객체와 영역으로 해제 | 전체 |
| 권한 | 사용하는 API의 최소 권한만 선언. 누락 시 `onFailed`에 `ERROR_SECURITY_PERMISSION` 또는 `PERMISSION_DENIED` | 전체 |

### AAOS와의 관계

Pleos Connect SDK는 AAOS에 동일·유사 API가 있으면 그 표준을 따르고 중복 기능을 제공하지 않습니다. AAOS 표준으로 접근할 수 없거나 제조사 고유 제어가 필요한 영역만 SDK 전용 API로 제공합니다. SDK 전용 기능이 나중에 AAOS 표준으로 편입되더라도 기존 SDK API는 최소 2개 메이저 버전 동안 하위 호환을 유지합니다. [원본](https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/pleos-connect-sdk-api)

| 구분 | 접근 경로 | 대상 기능 예시 |
|---|---|---|
| 표준 데이터 | AAOS Car API | 속도, 기어 상태, 외기 온도, 연료 레벨 등 |
| 특화 기능 | Pleos Connect SDK | 제조사 전용 시트 제어, 특화 라이팅, 고유 보안 인증 등 |

## 2026-10-19 릴리즈 Breaking change 요약

| 모듈 | Breaking change |
|---|---|
| Vehicle 2.3.2 | CruiseControl 상수 재구성(OFF/READY/ACTIVE 제거 → OTHER, ENABLED, ACTIVATED, USER_OVERRIDE, SUSPENDED, FORCED_DEACTIVATION_WARNING). `checkHvacFanSpeedCapabilities` 콜백 타입 `Map<SeatArea, List<Int>>` → `Map<SeatArea, AreaBounds<Int>>`. `FanDirectionType` 3개 → 8개로 확장, ordinal 변경. Area 클래스 6종에 내용 기반 `equals`/`hashCode` 추가. `getHvacTemperatureValueSuggestion`, `VehicleSpeedListener` 삭제 |
| NaviHelper 3.0.5 | `onNavigationStatus` 인자 `RouteDriving` → `NavigationStatus`. 좌표 생성자 인자 순서 (longitude, latitude) → (latitude, longitude). `DestinationInfo`/`WaypointInfo` 필드 `totalDistance`/`totalTime` → `remainDistance`/`remainTime`. `FindRouteResult`, `onUserDefinedRoute` 제거 |
| TextToSpeech 3.3.1 | 오류 콜백 인자 `String` → `ErrorType` (`EventListener.onError`, `OnServerConnectionListener.onFailed`). `ServerType` enum 삭제 |
| SpeechToText 3.3.1 | 오류 콜백 인자 `String` → `ErrorType`. `DevelopmentOnly` annotation 삭제 |
| FusedLocation 1.1.3 | `FusedLocationListener.onChanged` 인자 `LocationData` → `LocationData?`. `onError(exception: Exception)` 구현이 필수로 추가 |

같은 날 에뮬레이터 2.1.0이 나오면서 LLM SDK, CRN, 블루투스 동글 연동 제공이 종료되었습니다. 이 레포는 LLM SDK를 다루지 않습니다. [원본](https://document.pleos.ai/docs/pleos-only/release-note/emulator-261015)

## 어떤 SDK를 쓸지 판단 기준

공모전 2차 PT(2026-12-03~04)까지 실제 동작하는 앱이 필요하므로, 공식 상태표에서 "사용 가능"인 **Vehicle 조회(Vehicle State)** 와 **NaviHelper** 를 핵심 기능의 기반으로 삼는 것이 안전합니다. 두 SDK는 신청 없이 바로 쓸 수 있고 에뮬레이터와 Virtual Console로 검증할 수 있습니다. [원본](https://document.pleos.ai/docs/pleos-only/create-my-project)

- **Vehicle 제어(set), TextToSpeech, SpeechToText, FusedLocation**: 문서상 준비 중입니다. 기획서에는 활용 계획으로 넣되, 데모가 이 기능에 의존하지 않도록 설계합니다. 콘솔에서 "신청 필요"로 바뀌면 바로 신청합니다.
- **표준 데이터(속도, 기어 등)**: Pleos SDK가 아니라 AAOS Car API(`CarPropertyManager`)로 접근합니다. 모델별 지원 여부는 `VehiclePropertyIds`와 `CarPropertyManager`로 확인합니다. [원본](https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/api-compatibility-availability)
- **TextToSpeech / SpeechToText를 쓸 경우**: `ON_DEVICE` 모드는 앱 등록 없이 동작하므로 서버 연결보다 먼저 검토합니다. SpeechToText는 텍스트 변환만 하고 명령 실행은 하지 않으므로, 음성 명령 기능은 앱이 직접 해석해야 합니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/intro)

## 에뮬레이터

모든 SDK는 Pleos Connect Emulator 2.1.0에서 개발·검증합니다. Virtual Console로 도메인별 차량 상태와 이벤트를 바꿔 가며 앱 반응을 시험할 수 있고, 폼팩터 테스트 기능도 포함됩니다. 설치와 AVD 설정은 [../contest/environment.md](../contest/environment.md)를 참고합니다. [원본](https://document.pleos.ai/docs/pleos-only/release-note/emulator-261015)
