# TextToSpeech SDK

> 마지막 확인일: 2026-10-28  
> 기준 버전: TextToSpeech SDK 3.3.1 (2026-10-19 릴리즈)  
> 원본:
> - [소개](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/intro)
> - [SDK 가져오기](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/importing-sdk)
> - [권한 선언](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/declaring-permissions)
> - [초기화와 해제](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/initialization-and-release)
> - [이벤트 리스너](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/event-listeners)
> - [로케일 설정](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/set-locale)
> - [앱 등록](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/app-registration)
> - [음성 합성 시작과 중지](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/start-and-stop)
> - [에러 코드](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/error-code)
> - [TextToSpeech API Reference](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/entry)
> - [SDK 릴리즈 노트 2026-10-19](https://document.pleos.ai/docs/pleos-only/release-note/sdk-261015)
> - [프로젝트 생성 및 SDK 사용 신청](https://document.pleos.ai/docs/pleos-only/create-my-project)

함수 전체 목록은 [api.md](api.md)에 있습니다.

## 설명

TextToSpeech SDK는 텍스트를 음성으로 합성해 차량 오디오 시스템으로 재생하는 API를 제공합니다. 앱이 합성을 요청하면 Pleos Connect의 Speech Service가 처리하고 재생합니다. 진입점은 `ai.pleos.playground.tts.TextToSpeech` 클래스입니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/intro)

**버전과 제공 상태**

| 항목 | 값 |
|---|---|
| SDK 버전 | 3.3.1 |
| Maven 좌표 | `ai.pleos.playground:TextToSpeech:3.3.1` |
| Playground 상태 | 준비 중 (`Coming Soon`, 신청 버튼 없음) |
| 상태 확인 기준 | 공식 문서, 2026-10-28 |

Playground 상태는 바뀔 수 있으므로 본인 프로젝트의 **프로젝트 정보 > SDK** 메뉴에서 다시 확인해야 합니다. 준비 중 상태에서는 사용 신청 자체가 불가능합니다. [원본](https://document.pleos.ai/docs/pleos-only/create-my-project)

**의존성**

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://nexus-playground.pleos.ai/repository/maven-releases/")
    }
}

// app/build.gradle.kts
dependencies {
    implementation("ai.pleos.playground:TextToSpeech:3.3.1")
}
```

Maven 저장소는 인증 없이 접근됩니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/importing-sdk)

**권한**

`AndroidManifest.xml`에 `pleos.car.permission.TTS_SERVICE`를 선언해야 합니다. 초기화, 리스너 등록, 앱 등록, 합성 요청 등 서비스에 접근하는 모든 API에 필요하며, 없으면 초기화나 호출이 실패하고 `PERMISSION_DENIED`가 전달됩니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/declaring-permissions)

```xml
<uses-permission android:name="pleos.car.permission.TTS_SERVICE" />
```

**음성 합성 모드**

생성자의 `Mode`로 고르며 생략하면 `HYBRID`입니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/intro)

| Mode | 동작 | 네트워크 | 앱 등록 |
|---|---|---|---|
| `HYBRID` (기본) | 서버 streaming 합성 우선, 네트워크 실패 시 On-device 엔진으로 자동 전환 | 선택 | 서버 합성을 쓰려면 필요 |
| `SERVER` | 서버 streaming 합성만 사용, 실패 시 전환 없이 오류 이벤트 | 필수 | 필수 |
| `ON_DEVICE` | 차량 On-device 엔진에서 합성 | 불필요 | 불필요 |

**서버 연결과 앱 등록**

서버 기반 합성(`SERVER`, 서버를 쓰는 `HYBRID`)은 Pleos Playground에서 My Project를 생성할 때 발급되는 `Client ID`와 `Client Secret`으로 앱을 등록해야 합니다. 두 값은 Playground의 프로젝트 정보 > SDK 메뉴에 있습니다. `initialize()` 완료 후 `registerApp()`을 호출하고, `OnServerConnectionListener.onConnected()`가 온 뒤에 `speak()`를 호출합니다. `ON_DEVICE`만 쓰면 등록이 필요 없습니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/app-registration)

**지원 로케일**

`ko-KR`, `en-US`, `fr-CA`, `es-US`, `es-MX`. `setLocale()`을 호출하지 않으면 같은 앱에서 이전에 설정한 값을, 그것도 없으면 시스템 locale을 사용합니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/set-locale)

**오류 타입**

오류는 `ErrorType` enum으로 `OnInitializeListener`, `OnServerConnectionListener`, `EventListener`에 전달됩니다. 값은 12개입니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/error-code)

| 분류 | 값 |
|---|---|
| 권한·인증 | `PERMISSION_DENIED`, `UNAUTHENTICATED` |
| 네트워크·서버 | `NETWORK_ERROR`, `TIMEOUT`, `SERVER_INTERNAL_ERROR` |
| 요청·지원 범위 | `INVALID_REQUEST_ERROR`, `NOT_SUPPORTED_API`, `UNAVAILABLE_LANGUAGE` |
| 오디오·엔진 | `AUDIO_ERROR`, `MIC_ERROR`, `ON_DEVICE_ENGINE_ERROR` |
| 기타 | `UNKNOWN` |

**3.3.1 변경점 (Breaking)**

오류 콜백 인자가 `String`에서 `ErrorType`으로 바뀌었습니다 (`EventListener.onError`, `OnServerConnectionListener.onFailed`). `initialize(listener: OnInitializeListener)`, `TextToSpeech.version`, `ErrorType`이 추가되었고 `ServerType` enum은 삭제되었습니다. [원본](https://document.pleos.ai/docs/pleos-only/release-note/sdk-261015)

**에뮬레이터**

Pleos Connect Emulator 설치와 AVD 생성 외에 마이크 접근 설정과 Cold boot 설정이 필요합니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/intro)

## 기능

| 기능 그룹 | 함수·콜백 | 상세 |
|---|---|---|
| 생성·초기화·해제 | `TextToSpeech(context, mode)`, `initialize()`, `initialize(listener)`, `release()`, `version` | [api.md](api.md#texttospeech-클래스) |
| 이벤트 리스너 | `addEventListener()`, `removeEventListener()` → `EventListener` 6개 콜백 | [api.md](api.md#eventlistener-리스너) |
| 로케일 | `setLocale(locale)` | [api.md](api.md#texttospeech-클래스) |
| 앱 등록 (서버 모드) | `registerApp()` 3개 오버로드 → `OnServerConnectionListener` | [api.md](api.md#onserverconnectionlistener-리스너) |
| 합성 시작·중지 | `speak(text, isFinal)`, `stop()` | [api.md](api.md#texttospeech-클래스) |
| 오디오 설정 (가이드 설명 없음) | `setSpeakerType()`, `setAudioAttributes()`, `setAudioZoneId()` | [api.md](api.md#texttospeech-클래스) |
| 상수 | `Mode` 3개, `ErrorType` 12개 | [api.md](api.md#관련-타입-상수enum) |

공식 가이드가 정한 호출 순서는 생성 → `initialize()` → `addEventListener()` → (필요 시) `setLocale()` → (서버 모드) `registerApp()` → `onConnected()` 이후 `speak()` → `stop()` → `removeEventListener()` → `release()`입니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/intro)

## 활용

**전체 흐름 (Kotlin)**

공식 가이드의 예제를 한 흐름으로 합친 것입니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/initialization-and-release) · [앱 등록](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/app-registration) · [시작과 중지](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/start-and-stop)

```kotlin
import ai.pleos.playground.tts.TextToSpeech
import ai.pleos.playground.tts.constant.ErrorType
import ai.pleos.playground.tts.constant.Mode
import ai.pleos.playground.tts.listener.EventListener
import ai.pleos.playground.tts.listener.OnInitializeListener
import ai.pleos.playground.tts.listener.OnServerConnectionListener
import java.util.Locale

val textToSpeech = TextToSpeech(applicationContext, Mode.HYBRID)

private val eventListener = object : EventListener {
    override fun onReady() = Unit
    override fun onStart() = Unit
    override fun onUpdatedRms(rms: Double) = Unit
    override fun onDone() = Unit
    override fun onStop() = Unit
    override fun onError(errorType: ErrorType) {
        when (errorType) {
            ErrorType.PERMISSION_DENIED -> checkTtsPermission()
            ErrorType.UNAUTHENTICATED -> checkCredentials()
            ErrorType.NETWORK_ERROR,
            ErrorType.TIMEOUT -> retryWhenNetworkIsAvailable()
            else -> logError(errorType.name)
        }
    }
}

// 1. 초기화
textToSpeech.initialize(object : OnInitializeListener {
    override fun onSuccess() {
        // 2. 리스너 등록, 로케일 설정
        textToSpeech.addEventListener(eventListener)
        textToSpeech.setLocale(Locale.KOREA)

        // 3. 서버 모드(HYBRID, SERVER)는 앱 등록 후 onConnected()에서 요청
        textToSpeech.registerApp(
            clientID = clientId,          // Playground > 프로젝트 정보 > SDK
            clientSecret = clientSecret,
            listener = object : OnServerConnectionListener {
                override fun onConnected() {
                    // 4. 합성 요청
                    textToSpeech.speak("안녕하세요. 무엇을 도와드릴까요?")
                }
                override fun onFailed(errorType: ErrorType) {
                    // 등록 또는 서버 연결 실패 처리
                }
            }
        )
    }
    override fun onFailure(errorType: ErrorType) {
        // 초기화 실패 처리
    }
})

// 중지
textToSpeech.stop()   // 완료되면 onStop()

// 5. 해제 (예: onDestroy)
override fun onDestroy() {
    textToSpeech.removeEventListener(eventListener)
    textToSpeech.release()
    super.onDestroy()
}
```

**콜백 흐름**

정상 재생은 `onReady()` → `onStart()` → `onUpdatedRms(rms)` 반복 → `onDone()` 순서로 들어오고, `stop()`을 호출하면 `onStop()`, 오류는 `onError(errorType)`로 끝납니다. 여러 텍스트 조각을 한 발화로 읽히려면 마지막 조각만 `isFinal = true`로 보냅니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/event-listeners) · [시작과 중지](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/start-and-stop)

```kotlin
textToSpeech.speak("안녕하세요. ", isFinal = false)
textToSpeech.speak("TextToSpeech SDK입니다.", isFinal = true)
```

**주의사항**

- `removeEventListener()`에는 등록할 때 쓴 동일한 인스턴스를 넘겨야 합니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/event-listeners)
- `release()` 뒤에 다시 쓰려면 새 객체를 만들어 초기화부터 다시 합니다. 객체는 컴포넌트 수명주기에 맞춰 재사용합니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/initialization-and-release)
- `SERVER` 모드는 네트워크 실패 시 On-device로 넘어가지 않고 오류 이벤트만 옵니다. 네트워크가 불안정한 시연 환경이면 `HYBRID` 또는 `ON_DEVICE`가 안전합니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/intro)
- `ErrorType`은 값이 추가될 수 있으니 `when`에 `else` 분기를 둡니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/error-code)
- `setSpeakerType`, `setAudioAttributes`, `setAudioZoneId`는 공식 가이드에 설명이 없어 동작을 확인하지 못했습니다. 쓰기 전에 API Reference 페이지를 확인합니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/entry)
- Playground 상태가 "준비 중"인 동안은 실제 서비스 연결을 보장할 수 없습니다. 공모전 2차 PT(12/3~4)에 이 SDK를 핵심 기능으로 두려면 상태가 "사용 가능"으로 바뀌는지 먼저 확인해야 합니다. [원본](https://document.pleos.ai/docs/pleos-only/create-my-project)

**공모전 활용 예시**

- 예시 1: Vehicle SDK로 받은 차량 상태 변화(타이어 공기압 경고, 배터리 잔량 등)를 운전자가 화면을 보지 않아도 되도록 음성으로 안내. 주행 중 시각 정보 제한 정책과 맞물려 차량 특화 가치를 보여줄 수 있습니다.
- 예시 2: NaviHelper의 경유지 도착·주의 지점 이벤트를 받아 동승자용 안내나 배달 기사용 작업 지시를 읽어 주는 음성 브리핑. 텍스트 조각을 `isFinal`로 이어 붙여 긴 안내문도 한 발화로 처리할 수 있습니다.
