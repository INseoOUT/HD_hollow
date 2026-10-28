# SpeechToText SDK 함수 목록

> 마지막 확인일: 2026-10-28  
> 기준 버전: SpeechToText SDK 3.3.1  
> 원본:
> - [SpeechToText API Reference](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/entry)
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

이 모듈은 sources.jar가 배포되지 않아, 시그니처는 Maven AAR(classes.jar)의 공개 메서드에서 확인했고 설명은 공식 가이드 페이지에서 가져왔습니다. 파라미터 이름은 가이드에 나온 경우에만 적었고, 그 외에는 타입만 적었습니다. 모든 SDK API 호출에는 `pleos.car.permission.STT_SERVICE` 권한 선언이 필요합니다.

## SpeechToText 클래스

음성을 텍스트로 변환하는 SDK의 진입점입니다. 생성자에 애플리케이션 `Context`와 인식 `Mode`를 전달하며, `Mode`를 생략하면 `HYBRID`가 적용됩니다. 발화를 텍스트로 돌려줄 뿐 음성 명령을 실행하지는 않습니다.

원본: [SpeechToText API Reference](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt/speech-to-text/entry)

| 함수 | 파라미터 | 설명 | 권한 | 원본 |
|---|---|---|---|---|
| `SpeechToText` (생성자) | `context: Context, mode: Mode = Mode.HYBRID` | 객체를 생성합니다. `Mode` 생략 시 `HYBRID`. 생성 후 `initialize()`를 호출해야 API를 쓸 수 있습니다. | - | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt/speech-to-text/-speech-to-text) |
| `version` (companion 속성) | - | SDK 버전 조회 속성. 3.3.1에서 추가되었습니다. | - | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt/speech-to-text/companion/version) |
| `initialize` | `()` | SDK를 초기화합니다. 결과 콜백이 필요 없을 때 인자 없이 호출합니다. | `STT_SERVICE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt/speech-to-text/initialize) |
| `initialize` | `listener: OnInitializeListener` | SDK를 초기화하고 결과를 `onSuccess()` / `onFailure(errorType)`로 받습니다. 3.3.1에서 추가. | `STT_SERVICE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt/speech-to-text/initialize) |
| `release` | `()` | 연결과 내부 리소스를 해제합니다. 등록한 결과 리스너를 제거한 뒤 호출하며, 이후 다시 쓰려면 새 객체를 만들어 초기화부터 다시 합니다. | `STT_SERVICE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt/speech-to-text/release) |
| `addListener` | `listener: ResultListener` | 인식 준비·시작·중간/최종 결과·음성 구간·입력 크기·오류 이벤트를 받을 `ResultListener`를 등록합니다. `request()`나 `sendAudio()` 전에 등록해야 이벤트를 놓치지 않습니다. | `STT_SERVICE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt/speech-to-text/add-listener) |
| `removeListener` | `listener: ResultListener` | 등록할 때 사용한 동일한 인스턴스를 제거합니다. 리스너가 더 이상 필요 없거나 `release()` 전에 호출합니다. | `STT_SERVICE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt/speech-to-text/remove-listener) |
| `setLocale` | `locale: Locale` | 음성 인식 언어/지역을 설정합니다. `request()` 전에 설정한 값이 해당 인식 세션의 기준이 됩니다. 지원: `ko-KR`, `en-US`, `fr-CA`, `es-US`, `es-MX`. 호출하지 않으면 같은 앱의 이전 설정값, 없으면 시스템 locale을 사용합니다. | `STT_SERVICE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt/speech-to-text/set-locale) |
| `registerApp` | `clientID: String, clientSecret: String, listener: OnServerConnectionListener` | Pleos Playground My Project의 Client ID/Secret으로 앱을 등록합니다. `SERVER` 모드와 서버 인식을 쓰는 `HYBRID` 모드에서 필요하며, 초기화 완료 후 호출하고 `onConnected()` 이후 서버 기반 요청을 시작합니다. | `STT_SERVICE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt/speech-to-text/register-app) |
| `registerApp` | `String, String, String, () -> Unit, (String) -> Unit` | 가이드 설명 없음 (시그니처만 확인). 성공·실패 람다를 받는 오버로드입니다. | `STT_SERVICE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt/speech-to-text/register-app) |
| `registerApp` | `String, () -> Unit, (String) -> Unit` | 가이드 설명 없음 (시그니처만 확인). 성공·실패 람다를 받는 오버로드입니다. | `STT_SERVICE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt/speech-to-text/register-app) |
| `request` | `()` | 차량 마이크를 활성화하고 음성 인식을 시작합니다. 초기화와 리스너 등록을 마친 뒤, 서버 모드에서는 앱 등록(또는 UA 인증)까지 끝낸 뒤 호출합니다. | `STT_SERVICE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt/speech-to-text/request) |
| `stop` | `()` | 진행 중인 음성 인식을 중지하고 마이크 사용을 종료합니다. 종료되면 `ResultListener.onEndedRecognition()`이 호출됩니다. | `STT_SERVICE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt/speech-to-text/stop) |
| `sendAudio` | `ByteArray` | 차량 마이크 대신 앱이 가진 PCM 오디오를 전달해 인식합니다. `ON_DEVICE` 모드에서만 사용할 수 있으며, 전체를 한 번에 보내거나 청크로 나눠 보낼 수 있습니다 (예: 16 kHz, 16-bit, mono PCM은 3,200-byte 청크를 100ms 간격으로). | `STT_SERVICE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt/speech-to-text/send-audio) |
| `completeAudioSend` | `()` | 마지막 오디오 데이터를 보낸 뒤 반드시 호출해 입력이 끝났음을 알립니다. 결과는 `ResultListener`로 전달됩니다. | `STT_SERVICE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt/speech-to-text/complete-audio-send) |

## ResultListener (리스너)

`addListener()`로 등록하며, 음성 인식의 준비·시작·중간/최종 결과·음성 구간·입력 크기·오류 이벤트를 받습니다.

원본: [ResultListener API Reference](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt.listener/result-listener/entry)

| 콜백 | 파라미터 | 설명 | 원본 |
|---|---|---|---|
| `onReady` | `()` | 음성 인식 요청을 처리할 준비가 되었을 때 호출됩니다. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt.listener/result-listener/on-ready) |
| `onStartedRecognition` | `()` | 음성 인식이 시작될 때 호출됩니다. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt.listener/result-listener/on-started-recognition) |
| `onUpdated` | `stt: String, completed: Boolean` | 인식 텍스트가 갱신될 때 호출됩니다. `completed`가 `true`이면 해당 인식 결과가 완료된 것입니다. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt.listener/result-listener/on-updated) |
| `onUpdatedEpdData` | `on: Long, off: Long` | EPD(End Point Detection)가 감지한 음성 시작(`on`)과 종료(`off`) 위치가 갱신될 때 호출됩니다. 값은 입력 오디오 버퍼의 byte offset입니다. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt.listener/result-listener/on-updated-epd-data) |
| `onUpdatedRms` | `rms: Double` | 입력 음성 신호의 RMS(Root Mean Square) 값이 갱신될 때 호출됩니다. 신호 크기 값이며 dB가 아닙니다. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt.listener/result-listener/on-updated-rms) |
| `onEndedRecognition` | `()` | 음성 인식이 종료될 때 호출됩니다. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt.listener/result-listener/on-ended-recognition) |
| `onError` | `errorType: ErrorType` | 처리 중 오류가 발생하면 호출됩니다. 3.3.1부터 인자가 `String`에서 `ErrorType`으로 바뀌었습니다. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt.listener/result-listener/on-error) |

## OnInitializeListener (리스너)

`initialize(listener)`에 전달해 초기화 결과를 받습니다. 3.3.1에서 추가되었습니다.

원본: [OnInitializeListener API Reference](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt.listener/on-initialize-listener/entry)

| 콜백 | 파라미터 | 설명 | 원본 |
|---|---|---|---|
| `onSuccess` | `()` | 초기화가 완료되어 SpeechToText API를 사용할 수 있을 때 호출됩니다. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt.listener/on-initialize-listener/on-success) |
| `onFailure` | `errorType: ErrorType` | 초기화에 실패했을 때 원인과 함께 호출됩니다. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt.listener/on-initialize-listener/on-failure) |

## OnServerConnectionListener (리스너)

`registerApp()`에 전달해 앱 등록과 서버 연결 결과를 받습니다.

원본: [OnServerConnectionListener API Reference](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt.listener/on-server-connection-listener/entry)

| 콜백 | 파라미터 | 설명 | 원본 |
|---|---|---|---|
| `onConnected` | `()` | 등록과 서버 연결이 완료되어 서버 기반 음성 인식을 요청할 수 있을 때 호출됩니다. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt.listener/on-server-connection-listener/on-connected) |
| `onFailed` | `errorType: ErrorType` | 등록 또는 서버 연결에 실패했을 때 호출됩니다. 3.3.1부터 인자가 `String`에서 `ErrorType`으로 바뀌었습니다. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt.listener/on-server-connection-listener/on-failed) |

## 관련 타입 (상수·Enum)

패키지: [ai.pleos.playground.stt.constant](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt.constant/entry) · [ai.pleos.playground.stt.data](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt.data/entry) · [ai.pleos.playground.stt.listener](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt.listener/entry)

### Mode

생성자에 전달하는 음성 인식 모드입니다. 원본: [Mode](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt.constant/mode/entry)

| 값 | 설명 | 원본 |
|---|---|---|
| `HYBRID` | 기본 모드. 서버 기반 인식을 우선 사용하고, 네트워크 실패 시 On-device 엔진으로 자동 전환합니다. 서버 인식을 쓰려면 앱 등록이 필요합니다. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt.constant/mode/h-y-b-r-i-d/entry) |
| `SERVER` | 음성 데이터를 원격 Speech Service로 보내 인식합니다. 네트워크와 앱 등록이 필요하며, 네트워크 실패 시 On-device로 전환하지 않습니다. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt.constant/mode/s-e-r-v-e-r/entry) |
| `ON_DEVICE` | 차량 On-device 엔진에서 인식합니다. 네트워크와 앱 등록이 필요 없고, `sendAudio()`는 이 모드에서만 쓸 수 있습니다. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt.constant/mode/o-n_-d-e-v-i-c-e/entry) |

### InferenceSource

인식 결과가 어느 엔진에서 나왔는지를 나타내는 enum으로 3.3.1에서 추가되었습니다. 공식 `ResultListener` 콜백은 이 타입을 직접 전달하지 않습니다. 원본: [InferenceSource](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt.data/inference-source/entry) · [릴리즈 노트](https://document.pleos.ai/docs/pleos-only/release-note/sdk-261015)

| 값 | 설명 | 원본 |
|---|---|---|
| `ON_DEVICE` | On-device 엔진 추론 | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt.data/inference-source/o-n_-d-e-v-i-c-e/entry) |
| `SERVER` | 서버 추론 | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt.data/inference-source/s-e-r-v-e-r/entry) |
| `Companion.DEFAULT` | 기본값 상수 (가이드 설명 없음) | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt.data/inference-source/companion/-d-e-f-a-u-l-t) |

### ErrorType

`OnInitializeListener`, `OnServerConnectionListener`, `ResultListener`의 오류 콜백으로 전달됩니다. 새 타입이 추가될 수 있으므로 `when`에 기본 분기를 두는 것이 권장됩니다. 원본: [ErrorType](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt.constant/error-type/entry) · [에러 코드 가이드](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/error-code)

| 값 | 설명 | 권장 조치 | 원본 |
|---|---|---|---|
| `AUDIO_ERROR` | 입력 오디오 데이터 처리 실패 | 입력 데이터와 오디오 형식 확인 후 재요청 | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt.constant/error-type/a-u-d-i-o_-e-r-r-o-r/entry) |
| `INVALID_REQUEST_ERROR` | 요청 값이나 요청 상태가 올바르지 않음 | 호출 순서, 중복 요청, 입력 값 확인 | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt.constant/error-type/i-n-v-a-l-i-d_-r-e-q-u-e-s-t_-e-r-r-o-r/entry) |
| `MIC_ERROR` | 마이크 입력 시작·처리 중 오류 | 마이크 상태와 센서 개인정보 보호 설정 확인 | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt.constant/error-type/m-i-c_-e-r-r-o-r/entry) |
| `NETWORK_ERROR` | 네트워크 통신 실패 | 네트워크 연결 확인 후 재요청 | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt.constant/error-type/n-e-t-w-o-r-k_-e-r-r-o-r/entry) |
| `NOT_SUPPORTED_API` | 현재 모드 또는 실행 환경에서 지원하지 않는 API | 선택한 `Mode`와 플랫폼 지원 범위 확인 | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt.constant/error-type/n-o-t_-s-u-p-p-o-r-t-e-d_-a-p-i/entry) |
| `ON_DEVICE_ENGINE_ERROR` | On-device 음성 인식 엔진 오류 | 엔진 상태 확인 또는 `HYBRID`/`SERVER` 모드 검토 | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt.constant/error-type/o-n_-d-e-v-i-c-e_-e-n-g-i-n-e_-e-r-r-o-r/entry) |
| `PERMISSION_DENIED` | SpeechToText 서비스 접근 권한 없음 | manifest의 `STT_SERVICE` 권한과 실행 환경의 권한 상태 확인 | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt.constant/error-type/p-e-r-m-i-s-s-i-o-n_-d-e-n-i-e-d/entry) |
| `SERVER_INTERNAL_ERROR` | Speech Service 내부 오류 | 잠시 후 재요청, 반복되면 서비스 관리자 문의 | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt.constant/error-type/s-e-r-v-e-r_-i-n-t-e-r-n-a-l_-e-r-r-o-r/entry) |
| `TIMEOUT` | 제한 시간 안에 요청 미완료 | 네트워크와 서버 상태 확인 후 재요청 | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt.constant/error-type/t-i-m-e-o-u-t/entry) |
| `UNAUTHENTICATED` | 앱 인증 실패 | Pleos Playground My Project 자격 증명 또는 UA 앱 정보와 접속 환경 확인 | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt.constant/error-type/u-n-a-u-t-h-e-n-t-i-c-a-t-e-d/entry) |
| `UNAVAILABLE_LANGUAGE` | 요청한 언어 사용 불가 | 지원되는 `Locale`로 변경 | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt.constant/error-type/u-n-a-v-a-i-l-a-b-l-e_-l-a-n-g-u-a-g-e/entry) |
| `UNKNOWN` | 분류되지 않은 오류 | 호출 시점과 환경 정보를 수집해 서비스 관리자 문의 | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt.constant/error-type/u-n-k-n-o-w-n/entry) |

### 참고: AAR에만 있는 결과 모델

아래 클래스는 AAR의 `ai.pleos.playground.stt.data` 패키지에 들어 있지만 공식 API Reference에 개별 페이지가 없고, 공식 `ResultListener` 콜백(`onUpdated(stt: String, completed: Boolean)`)은 이 타입을 전달하지 않습니다. 필드만 참고로 적습니다. 패키지 페이지: [ai.pleos.playground.stt.data](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/SpeechToText/speech-to-text/ai.pleos.playground.stt.data/entry)

| 클래스 | 필드 (getter 기준) |
|---|---|
| `RecognitionResult` | `alternatives: List<Alternative>`, `languageCode: String`, `speaker: Speaker`, `isWakeUpWord: Boolean` |
| `RecognitionFinalResult` | `epdOn: Long`, `epdOff: Long`, `results: List<RecognitionResult>`, `speaker: Speaker`, `inferenceSource: InferenceSource` |
| `Alternative` | `transcript: String`, `confidence: Float`, `tokenSequence: String` |
| `Speaker` | `speakerId: String` |
