# SpeechToText SDK

> 마지막 확인일: 2026-10-28  
> 기준 버전: SpeechToText SDK 3.3.1 (2026-10-19 릴리즈)  
> 원본:
> - [소개](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/intro)
> - [SDK 가져오기](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/importing-sdk)
> - [권한 선언](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/declaring-permissions)
> - [초기화와 해제](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/initialization-and-release)
> - [결과 리스너](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/result-listeners)
> - [로케일 설정](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/set-locale)
> - [앱 등록](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/app-registration)
> - [음성 인식 시작과 중지](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/start-and-stop)
> - [오디오 데이터 전송](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/sending-audio-data)
> - [에러 코드](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/error-code)
> - [SpeechToText API Reference](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/entry)
> - [SDK 릴리즈 노트 2026-10-19](https://document.pleos.ai/docs/pleos-only/release-note/sdk-261015)
> - [프로젝트 생성 및 SDK 사용 신청](https://document.pleos.ai/docs/pleos-only/create-my-project)

함수 전체 목록은 [api.md](api.md)에 있습니다.

## 설명

SpeechToText SDK는 음성을 텍스트로 변환하는 API를 제공합니다. 앱이 인식을 요청하면 Pleos Connect의 Speech Service가 음성을 텍스트로 바꿔 돌려줍니다. 진입점은 `ai.pleos.playground.stt.SpeechToText` 클래스입니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/intro)

음성 명령 기능은 제공하지 않습니다. "음악 틀어줘" 같은 발화를 텍스트로 돌려줄 뿐 실제 동작은 실행하지 않으므로, 인식한 명령으로 앱 기능을 실행하려면 Gleo Interface를 봐야 합니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/intro) · [Gleo Interface](https://document.pleos.ai/docs/pleos-only/app-integration-guide/guide/core-concepts)

**버전과 제공 상태**

| 항목 | 값 |
|---|---|
| SDK 버전 | 3.3.1 |
| Maven 좌표 | `ai.pleos.playground:SpeechToText:3.3.1` |
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
    implementation("ai.pleos.playground:SpeechToText:3.3.1")
}
```

Maven 저장소는 인증 없이 접근됩니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/importing-sdk)

**권한**

`AndroidManifest.xml`에 `pleos.car.permission.STT_SERVICE`를 선언해야 합니다. 초기화, 리스너 등록, 앱 등록, 인식 요청, 오디오 전송 등 서비스에 접근하는 모든 API에 필요하며, 없으면 초기화나 호출이 실패하고 `PERMISSION_DENIED`가 전달됩니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/declaring-permissions)

```xml
<uses-permission android:name="pleos.car.permission.STT_SERVICE" />
```

**음성 인식 모드**

생성자의 `Mode`로 고르며 생략하면 `HYBRID`입니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/intro)

| Mode | 동작 | 네트워크 | 앱 등록 | `sendAudio()` |
|---|---|---|---|---|
| `HYBRID` (기본) | 서버 인식 우선, 네트워크 실패 시 On-device 엔진으로 자동 전환 | 선택 | 서버 인식을 쓰려면 필요 | 불가 |
| `SERVER` | 음성을 원격 Speech Service로 보내 인식, 실패 시 전환 없음 | 필수 | 필수 | 불가 |
| `ON_DEVICE` | 차량 On-device 엔진에서 인식 | 불필요 | 불필요 | 가능 |

**서버 연결과 앱 등록**

서버 기반 인식(`SERVER`, 서버를 쓰는 `HYBRID`)은 Pleos Playground에서 My Project를 생성할 때 발급되는 `Client ID`와 `Client Secret`으로 앱을 등록해야 합니다. 두 값은 Playground의 프로젝트 정보 > SDK 메뉴에 있습니다. `initialize()` 완료 후 `registerApp()`을 호출하고, `OnServerConnectionListener.onConnected()`가 온 뒤에 `request()`를 호출합니다. `ON_DEVICE`만 쓰면 등록이 필요 없습니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/app-registration)

**입력 경로 두 가지**

- 차량 마이크: `request()`가 마이크를 켜고 인식을 시작합니다. `stop()`으로 끝냅니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/start-and-stop)
- 앱이 가진 PCM 오디오: `sendAudio(ByteArray)`로 보내고 마지막에 `completeAudioSend()`를 호출합니다. `ON_DEVICE` 모드 전용입니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/sending-audio-data)

**지원 로케일**

`ko-KR`, `en-US`, `fr-CA`, `es-US`, `es-MX`. `setLocale()`을 호출하지 않으면 같은 앱에서 이전에 설정한 값을, 그것도 없으면 시스템 locale을 사용합니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/set-locale)

**오류 타입**

오류는 `ErrorType` enum으로 `OnInitializeListener`, `OnServerConnectionListener`, `ResultListener`에 전달됩니다. 값은 12개입니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/error-code)

| 분류 | 값 |
|---|---|
| 권한·인증 | `PERMISSION_DENIED`, `UNAUTHENTICATED` |
| 네트워크·서버 | `NETWORK_ERROR`, `TIMEOUT`, `SERVER_INTERNAL_ERROR` |
| 요청·지원 범위 | `INVALID_REQUEST_ERROR`, `NOT_SUPPORTED_API`, `UNAVAILABLE_LANGUAGE` |
| 오디오·엔진 | `AUDIO_ERROR`, `MIC_ERROR`, `ON_DEVICE_ENGINE_ERROR` |
| 기타 | `UNKNOWN` |

**3.3.1 변경점 (Breaking)**

오류 콜백 인자가 `String`에서 `ErrorType`으로 바뀌었습니다 (`ResultListener.onError`, `OnServerConnectionListener.onFailed`). `initialize(listener: OnInitializeListener)`, `SpeechToText.version`, `ErrorType`, `InferenceSource`가 추가되었고 `DevelopmentOnly` annotation은 삭제되었습니다. [원본](https://document.pleos.ai/docs/pleos-only/release-note/sdk-261015)

**에뮬레이터**

Pleos Connect Emulator 설치와 AVD 생성 외에 마이크 접근 설정과 Cold boot 설정이 필요합니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/intro)

## 기능

| 기능 그룹 | 함수·콜백 | 상세 |
|---|---|---|
| 생성·초기화·해제 | `SpeechToText(context, mode)`, `initialize()`, `initialize(listener)`, `release()`, `version` | [api.md](api.md#speechtotext-클래스) |
| 결과 리스너 | `addListener()`, `removeListener()` → `ResultListener` 7개 콜백 | [api.md](api.md#resultlistener-리스너) |
| 로케일 | `setLocale(locale)` | [api.md](api.md#speechtotext-클래스) |
| 앱 등록 (서버 모드) | `registerApp()` 3개 오버로드 → `OnServerConnectionListener` | [api.md](api.md#onserverconnectionlistener-리스너) |
| 마이크 인식 시작·중지 | `request()`, `stop()` | [api.md](api.md#speechtotext-클래스) |
| 오디오 데이터 전송 (ON_DEVICE) | `sendAudio(ByteArray)`, `completeAudioSend()` | [api.md](api.md#speechtotext-클래스) |
| 상수 | `Mode` 3개, `ErrorType` 12개, `InferenceSource` 2개 | [api.md](api.md#관련-타입-상수enum) |

공식 가이드가 정한 호출 순서는 생성 → `initialize()` → `addListener()` → (필요 시) `setLocale()` → (서버 모드) `registerApp()` → `request()` → `stop()` → `removeListener()` → `release()`입니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/intro)

## 활용

**전체 흐름 (Kotlin, 차량 마이크)**

공식 가이드의 예제를 한 흐름으로 합친 것입니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/initialization-and-release) · [앱 등록](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/app-registration) · [시작과 중지](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/start-and-stop) · [결과 리스너](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/result-listeners)

```kotlin
import ai.pleos.playground.stt.SpeechToText
import ai.pleos.playground.stt.constant.ErrorType
import ai.pleos.playground.stt.constant.Mode
import ai.pleos.playground.stt.listener.OnInitializeListener
import ai.pleos.playground.stt.listener.OnServerConnectionListener
import ai.pleos.playground.stt.listener.ResultListener
import java.util.Locale

val speechToText = SpeechToText(applicationContext, Mode.HYBRID)

private val resultListener = object : ResultListener {
    override fun onReady() = Unit
    override fun onStartedRecognition() = Unit
    override fun onUpdated(stt: String, completed: Boolean) {
        // 중간 결과(completed = false)와 최종 결과(completed = true)를 처리
    }
    override fun onUpdatedEpdData(on: Long, off: Long) = Unit
    override fun onUpdatedRms(rms: Double) = Unit
    override fun onEndedRecognition() = Unit
    override fun onError(errorType: ErrorType) {
        when (errorType) {
            ErrorType.PERMISSION_DENIED -> checkSttPermission()
            ErrorType.UNAUTHENTICATED -> checkCredentials()
            ErrorType.NETWORK_ERROR,
            ErrorType.TIMEOUT -> retryWhenNetworkIsAvailable()
            else -> logError(errorType.name)
        }
    }
}

// 1. 초기화
speechToText.initialize(object : OnInitializeListener {
    override fun onSuccess() {
        // 2. 리스너 등록(요청 전에 반드시), 로케일 설정
        speechToText.addListener(resultListener)
        speechToText.setLocale(Locale.KOREA)

        // 3. 서버 모드(HYBRID, SERVER)는 앱 등록 후 onConnected()에서 요청
        speechToText.registerApp(
            clientID = clientId,          // Playground > 프로젝트 정보 > SDK
            clientSecret = clientSecret,
            listener = object : OnServerConnectionListener {
                override fun onConnected() {
                    // 4. 마이크 인식 시작
                    speechToText.request()
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
speechToText.stop()   // 끝나면 onEndedRecognition()

// 5. 해제 (예: onDestroy)
override fun onDestroy() {
    speechToText.removeListener(resultListener)
    speechToText.release()
    super.onDestroy()
}
```

**PCM 오디오를 직접 보내는 흐름 (ON_DEVICE 전용)**

16 kHz, 16-bit, mono PCM을 실시간 속도로 보내는 공식 예시입니다. 마지막에 `completeAudioSend()`를 꼭 호출합니다. 다른 형식이면 100ms 분량에 맞춰 청크 크기를 다시 계산합니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/sending-audio-data)

```kotlin
private const val BYTES_PER_100_MS = 3_200

val pcmData = applicationContext.assets
    .open("test_audio.pcm")
    .use { it.readBytes() }

pcmData.asList()
    .chunked(BYTES_PER_100_MS)
    .forEach { chunk ->
        speechToText.sendAudio(chunk.toByteArray())
        Thread.sleep(100L)
    }

speechToText.completeAudioSend()
```

**콜백 흐름**

마이크 인식은 `onReady()` → `onStartedRecognition()` → `onUpdated(stt, completed)` 반복(중간 결과는 `completed = false`, 완료 시 `true`) → `onEndedRecognition()` 순서로 들어옵니다. 그 사이 `onUpdatedEpdData(on, off)`로 음성 구간의 byte offset이, `onUpdatedRms(rms)`로 입력 크기가 갱신됩니다. 오류는 `onError(errorType)`입니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/result-listeners)

**주의사항**

- 리스너는 `request()`나 `sendAudio()`보다 먼저 등록합니다. 그렇지 않으면 인식 이벤트를 놓칩니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/result-listeners)
- `removeListener()`에는 등록할 때 쓴 동일한 인스턴스를 넘겨야 합니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/result-listeners)
- `release()` 뒤에 다시 쓰려면 새 객체를 만들어 초기화부터 다시 합니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/initialization-and-release)
- `sendAudio()`는 `ON_DEVICE` 모드에서만 됩니다. 녹음 파일로 시연하려면 모드를 맞춰야 합니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/sending-audio-data)
- 인식 결과로 앱 기능을 실행하는 "음성 명령"은 이 SDK 범위 밖입니다. 텍스트를 받은 뒤의 해석과 실행은 앱이 직접 구현하거나 Gleo Interface를 씁니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/intro)
- `ErrorType`은 값이 추가될 수 있으니 `when`에 `else` 분기를 둡니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/error-code)
- Playground 상태가 "준비 중"인 동안은 실제 서비스 연결을 보장할 수 없습니다. 공모전 2차 PT(12/3~4)에 이 SDK를 핵심 기능으로 두려면 상태가 "사용 가능"으로 바뀌는지 먼저 확인해야 합니다. [원본](https://document.pleos.ai/docs/pleos-only/create-my-project)

**공모전 활용 예시**

- 예시 1: 주행 중 음성 메모. `request()`로 운전자의 말을 받아 `onUpdated(stt, completed = true)` 텍스트를 저장하고, 정차 후 목록으로 보여 주는 앱. 공모전 사이트가 든 예시 시나리오(음성 메모 앱)와 같은 방향입니다.
- 예시 2: 음성으로 목적지 지정. 인식한 텍스트를 앱이 해석해 NaviHelper `requestRoute()`에 넘기고, 결과를 TextToSpeech로 읽어 주는 손 안 대는 내비 보조. 명령 해석은 앱 몫이라는 점을 기획서에 명시해야 합니다.
