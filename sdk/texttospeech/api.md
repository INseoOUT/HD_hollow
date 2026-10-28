# TextToSpeech SDK 함수 목록

> 마지막 확인일: 2026-10-28  
> 기준 버전: TextToSpeech SDK 3.3.1  
> 원본:
> - [TextToSpeech API Reference](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/entry)
> - [소개](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/intro)
> - [SDK 가져오기](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/importing-sdk)
> - [권한 선언](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/declaring-permissions)
> - [초기화와 해제](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/initialization-and-release)
> - [이벤트 리스너](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/event-listeners)
> - [로케일 설정](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/set-locale)
> - [앱 등록](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/app-registration)
> - [음성 합성 시작과 중지](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/start-and-stop)
> - [에러 코드](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/error-code)

이 모듈은 sources.jar가 배포되지 않아, 시그니처는 Maven AAR(classes.jar)의 공개 메서드에서 확인했고 설명은 공식 가이드 페이지에서 가져왔습니다. 파라미터 이름은 가이드에 나온 경우에만 적었고, 그 외에는 타입만 적었습니다. 모든 SDK API 호출에는 `pleos.car.permission.TTS_SERVICE` 권한 선언이 필요합니다.

## TextToSpeech 클래스

텍스트를 음성으로 합성하고 차량 오디오로 재생하는 SDK의 진입점입니다. 생성자에 애플리케이션 `Context`와 실행 `Mode`를 전달하며, `Mode`를 생략하면 `HYBRID`가 적용됩니다.

원본: [TextToSpeech API Reference](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts/text-to-speech/entry)

| 함수 | 파라미터 | 설명 | 권한 | 원본 |
|---|---|---|---|---|
| `TextToSpeech` (생성자) | `context: Context, mode: Mode = Mode.HYBRID` | 객체를 생성합니다. `Mode` 생략 시 `HYBRID`. 생성 후 `initialize()`를 호출해야 API를 쓸 수 있습니다. | - | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts/text-to-speech/-text-to-speech) |
| `version` (companion 속성) | - | SDK 버전 조회 속성. 3.3.1에서 추가되었습니다. | - | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts/text-to-speech/companion/version) |
| `initialize` | `()` | SDK를 초기화합니다. 결과 콜백이 필요 없을 때 인자 없이 호출합니다. | `TTS_SERVICE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts/text-to-speech/initialize) |
| `initialize` | `listener: OnInitializeListener` | SDK를 초기화하고 결과를 `onSuccess()` / `onFailure(errorType)`로 받습니다. 3.3.1에서 추가. | `TTS_SERVICE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts/text-to-speech/initialize) |
| `release` | `()` | 연결과 내부 리소스를 해제합니다. 리스너를 먼저 제거한 뒤 호출을 권장하며, 이후 다시 쓰려면 새 객체를 만들어 초기화부터 다시 합니다. | `TTS_SERVICE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts/text-to-speech/release) |
| `addEventListener` | `listener: EventListener` | 합성 준비·시작·완료·중지·음량·오류 이벤트를 받을 `EventListener`를 등록합니다. | `TTS_SERVICE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts/text-to-speech/add-event-listener) |
| `removeEventListener` | `listener: EventListener` | 등록할 때 사용한 동일한 인스턴스를 제거합니다. 리스너가 더 이상 필요 없거나 `release()` 전에 호출합니다. | `TTS_SERVICE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts/text-to-speech/remove-event-listener) |
| `setLocale` | `locale: Locale` | 음성 합성 언어/지역을 설정합니다. `speak()` 전에 설정한 값이 해당 요청의 기준이 됩니다. 지원: `ko-KR`, `en-US`, `fr-CA`, `es-US`, `es-MX`. 호출하지 않으면 같은 앱의 이전 설정값, 없으면 시스템 locale을 사용합니다. | `TTS_SERVICE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts/text-to-speech/set-locale) |
| `registerApp` | `clientID: String, clientSecret: String, listener: OnServerConnectionListener` | Pleos Playground My Project의 Client ID/Secret으로 앱을 등록합니다. `SERVER` 모드와 서버 합성을 쓰는 `HYBRID` 모드에서 필요하며, 초기화 완료 후 호출하고 `onConnected()` 이후 서버 기반 요청을 시작합니다. | `TTS_SERVICE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts/text-to-speech/register-app) |
| `registerApp` | `String, String, String, () -> Unit, (String) -> Unit` | 가이드 설명 없음 (시그니처만 확인). 성공·실패 람다를 받는 오버로드입니다. | `TTS_SERVICE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts/text-to-speech/register-app) |
| `registerApp` | `String, () -> Unit, (String) -> Unit` | 가이드 설명 없음 (시그니처만 확인). 성공·실패 람다를 받는 오버로드입니다. | `TTS_SERVICE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts/text-to-speech/register-app) |
| `speak` | `text: String, isFinal: Boolean = true` | 텍스트의 음성 합성과 재생을 시작합니다. 기본값 `isFinal = true`는 텍스트를 하나의 완성된 요청으로 처리하고, 여러 조각을 한 발화로 보낼 때는 마지막 조각만 `true`로 둡니다. 앱 등록이 필요한 모드에서는 `onConnected()` 이후 호출합니다. | `TTS_SERVICE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts/text-to-speech/speak) |
| `stop` | `()` | 진행 중인 음성 합성과 재생을 중지합니다. 중지가 완료되면 `EventListener.onStop()`이 호출됩니다. | `TTS_SERVICE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts/text-to-speech/stop) |
| `setSpeakerType` | `String` | 가이드 설명 없음 (시그니처만 확인). | `TTS_SERVICE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts/text-to-speech/set-speaker-type) |
| `setAudioAttributes` | `AudioAttributes` | 가이드 설명 없음 (시그니처만 확인). `android.media.AudioAttributes`를 받습니다. | `TTS_SERVICE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts/text-to-speech/set-audio-attributes) |
| `setAudioZoneId` | `Int` | 가이드 설명 없음 (시그니처만 확인). | `TTS_SERVICE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts/text-to-speech/set-audio-zone-id) |

## EventListener (리스너)

`addEventListener()`로 등록하며, 음성 합성 요청의 준비·시작·완료·중지·음량 변화·오류 이벤트를 받습니다.

원본: [EventListener API Reference](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts.listener/event-listener/entry)

| 콜백 | 파라미터 | 설명 | 원본 |
|---|---|---|---|
| `onReady` | `()` | `speak()` 요청을 처리할 준비가 되었을 때 호출됩니다. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts.listener/event-listener/on-ready) |
| `onStart` | `()` | 음성 재생이 시작될 때 호출됩니다. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts.listener/event-listener/on-start) |
| `onUpdatedRms` | `rms: Double` | 재생 중 RMS(Root Mean Square) 값이 갱신될 때 호출됩니다. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts.listener/event-listener/on-updated-rms) |
| `onDone` | `()` | 요청한 음성 재생이 정상적으로 끝나면 호출됩니다. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts.listener/event-listener/on-done) |
| `onStop` | `()` | 진행 중인 재생이 중지되면 호출됩니다. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts.listener/event-listener/on-stop) |
| `onError` | `errorType: ErrorType` | 처리 중 오류가 발생하면 호출됩니다. 3.3.1부터 인자가 `String`에서 `ErrorType`으로 바뀌었습니다. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts.listener/event-listener/on-error) |

## OnInitializeListener (리스너)

`initialize(listener)`에 전달해 초기화 결과를 받습니다. 3.3.1에서 추가되었습니다.

원본: [OnInitializeListener API Reference](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts.listener/on-initialize-listener/entry)

| 콜백 | 파라미터 | 설명 | 원본 |
|---|---|---|---|
| `onSuccess` | `()` | 초기화가 완료되어 TextToSpeech API를 사용할 수 있을 때 호출됩니다. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts.listener/on-initialize-listener/on-success) |
| `onFailure` | `errorType: ErrorType` | 초기화에 실패했을 때 원인과 함께 호출됩니다. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts.listener/on-initialize-listener/on-failure) |

## OnServerConnectionListener (리스너)

`registerApp()`에 전달해 앱 등록과 서버 연결 결과를 받습니다.

원본: [OnServerConnectionListener API Reference](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts.listener/on-server-connection-listener/entry)

| 콜백 | 파라미터 | 설명 | 원본 |
|---|---|---|---|
| `onConnected` | `()` | 등록과 서버 연결이 완료되어 서버 기반 음성 합성을 요청할 수 있을 때 호출됩니다. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts.listener/on-server-connection-listener/on-connected) |
| `onFailed` | `errorType: ErrorType` | 등록 또는 서버 연결에 실패했을 때 호출됩니다. 3.3.1부터 인자가 `String`에서 `ErrorType`으로 바뀌었습니다. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts.listener/on-server-connection-listener/on-failed) |

## 관련 타입 (상수·Enum)

패키지: [ai.pleos.playground.tts.constant](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts.constant/entry) · [ai.pleos.playground.tts.listener](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts.listener/entry)

### Mode

생성자에 전달하는 음성 합성 모드입니다. 원본: [Mode](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts.constant/mode/entry)

| 값 | 설명 | 원본 |
|---|---|---|
| `HYBRID` | 기본 모드. 서버 기반 streaming 합성을 우선 사용하고, 네트워크 실패 시 On-device 엔진으로 자동 전환합니다. 서버 합성을 쓰려면 앱 등록이 필요합니다. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts.constant/mode/h-y-b-r-i-d/entry) |
| `SERVER` | 서버 기반 streaming 합성만 사용합니다. 네트워크와 앱 등록이 필요하며, 네트워크 실패 시 전환 없이 오류 이벤트를 전달합니다. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts.constant/mode/s-e-r-v-e-r/entry) |
| `ON_DEVICE` | 차량 On-device 엔진에서 합성합니다. 네트워크와 앱 등록이 필요 없습니다. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts.constant/mode/o-n_-d-e-v-i-c-e/entry) |

### ErrorType

`OnInitializeListener`, `OnServerConnectionListener`, `EventListener`의 오류 콜백으로 전달됩니다. 새 타입이 추가될 수 있으므로 `when`에 기본 분기를 두는 것이 권장됩니다. 원본: [ErrorType](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts.constant/error-type/entry) · [에러 코드 가이드](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/error-code)

| 값 | 설명 | 권장 조치 | 원본 |
|---|---|---|---|
| `AUDIO_ERROR` | 오디오 출력 또는 재생 처리 실패 | 오디오 설정과 출력 장치 상태 확인 후 재요청 | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts.constant/error-type/a-u-d-i-o_-e-r-r-o-r/entry) |
| `INVALID_REQUEST_ERROR` | 요청 값이나 요청 상태가 올바르지 않음 | 텍스트와 호출 순서 등 요청 조건 확인 | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts.constant/error-type/i-n-v-a-l-i-d_-r-e-q-u-e-s-t_-e-r-r-o-r/entry) |
| `MIC_ERROR` | 오디오 입력 장치 처리 중 오류 | 실행 환경의 오디오 장치 상태 확인 | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts.constant/error-type/m-i-c_-e-r-r-o-r/entry) |
| `NETWORK_ERROR` | 네트워크 통신 실패 | 네트워크 연결 확인 후 재요청 | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts.constant/error-type/n-e-t-w-o-r-k_-e-r-r-o-r/entry) |
| `NOT_SUPPORTED_API` | 현재 모드 또는 실행 환경에서 지원하지 않는 API | 선택한 `Mode`와 플랫폼 지원 범위 확인 | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts.constant/error-type/n-o-t_-s-u-p-p-o-r-t-e-d_-a-p-i/entry) |
| `ON_DEVICE_ENGINE_ERROR` | On-device 음성 합성 엔진 오류 | 엔진 상태 확인 또는 `HYBRID`/`SERVER` 모드 검토 | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts.constant/error-type/o-n_-d-e-v-i-c-e_-e-n-g-i-n-e_-e-r-r-o-r/entry) |
| `PERMISSION_DENIED` | TextToSpeech 서비스 접근 권한 없음 | manifest의 `TTS_SERVICE` 권한과 실행 환경의 권한 상태 확인 | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts.constant/error-type/p-e-r-m-i-s-s-i-o-n_-d-e-n-i-e-d/entry) |
| `SERVER_INTERNAL_ERROR` | Speech Service 내부 오류 | 잠시 후 재요청, 반복되면 서비스 관리자 문의 | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts.constant/error-type/s-e-r-v-e-r_-i-n-t-e-r-n-a-l_-e-r-r-o-r/entry) |
| `TIMEOUT` | 제한 시간 안에 요청 미완료 | 네트워크와 서버 상태 확인 후 재요청 | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts.constant/error-type/t-i-m-e-o-u-t/entry) |
| `UNAUTHENTICATED` | 앱 인증 실패 | Pleos Playground My Project 자격 증명 또는 UA 앱 정보와 접속 환경 확인 | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts.constant/error-type/u-n-a-u-t-h-e-n-t-i-c-a-t-e-d/entry) |
| `UNAVAILABLE_LANGUAGE` | 요청한 언어 사용 불가 | 지원되는 `Locale`로 변경 | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts.constant/error-type/u-n-a-v-a-i-l-a-b-l-e_-l-a-n-g-u-a-g-e/entry) |
| `UNKNOWN` | 분류되지 않은 오류 | 호출 시점과 환경 정보를 수집해 서비스 관리자 문의 | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/TextToSpeech/text-to-speech/ai.pleos.playground.tts.constant/error-type/u-n-k-n-o-w-n/entry) |
