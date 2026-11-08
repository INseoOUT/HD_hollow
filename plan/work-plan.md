# 작업 계획 — 차량 안 스튜디오

> 작성일: 2026-11-08 (토)  
> 근거: [submission-draft.md](submission-draft.md) 2절(기능·개발 계획), [../poc/README.md](../poc/README.md) 검증 결과, Vehicle SDK 2.3.2 바이너리에서 확인한 타입, [../contest/rules.md](../contest/rules.md) 제출 규격  
> 상태: 확정 전. 범위·계약·순서는 이 문서가 기준이고, 바뀌면 여기부터 고친다.  
> 두 흐름: **A = 차량 앱**(`poc/car`), **B = 모바일 앱**(`poc/phone`). 사람이 한 명이면 8절 일정의 A·B 열을 같은 날 순서대로 한다.  
> 기준선: `develop` 커밋 96a63e3(11/5 PoC). 11/8에 했던 범위 정리 코드는 폐기했고, 그 설계는 CR·C4 작업으로 다시 구현한다.

## 1. 범위

| 단계 | 기한 | 들어가는 것 | 들어가지 않는 것 |
|---|---|---|---|
| **1차 (접수)** | 11/15 (일) 23:59 | 폰 앱: 백그라운드 화면 미러링, 조명 값 조절(앱 화면·알림 버튼). 차량 앱: 스튜디오 모드(링라이트 + 미러링 모니터 + 조명 패널), 조명 상태 관리(앰비언트 밝기·색·켜짐, 실내등, 좌석별 독서등 조회·설정·복원), 색온도(2700~6500K)·밝기 양방향 동기화, 기어 P·N 게이트. 연결은 TCP(에뮬레이터 경로). 제출물: 기획서 DOCX, 시연 영상, GitHub 저장소(소스 + release APK + README) | 조명 씬, 풀 라이트 모드, 무선 모드, 실차 USB 전송(테더링·액세서리), 폰 플로팅 컨트롤, H.264, 실기기 라이브 앱 검증 |
| **2차 (PT)** | 12/3~4 | 조명 씬, 풀 라이트 모드, 무선 모드(Wi-Fi·폰 핫스팟), 실차 USB 전송(Pleos 회신에 따라 테더링 또는 액세서리 모드), 폰 플로팅 컨트롤, 실제 폰 연결·틱톡/유튜브 라이브 미러링 확인, 안정화, PT 자료, 리허설 | 역방향 입력(차 화면 터치로 폰 조작), STT 음성 조절 |
| 이후 | 12/18 선정 후 | 전시용 APK 구동 테스트, 영상 제작 지원 | 추가 개발(순위에 영향 없음) |

원칙: 1차는 **11/15에 영상·APK로 보여 줄 수 있는 것**만 넣는다. 통합 체크포인트(7절)를 넘기지 못한 기능은 2차로 보낸다.

## 2. 두 앱이 만나는 지점: 계약

두 앱은 **연결 하나**로만 만난다. 이 절만 먼저 정하면 A·B는 독립 작업이다.

### 2.1 전송 계층

HTTP는 TCP/IP 위의 포맷이라 유선·무선 자체와는 무관하다. 실차에서 폰을 USB 포트에 꽂았을 때 생길 수 있는 경로가 세 가지이고 어느 것이 열리는지는 Pleos 회신에 달려 있으므로, **메시지와 전송을 분리**해 어느 경로든 앱 로직이 같게 한다.

| 경로 | 생기는 것 | 우리 구현 | 조건 |
|---|---|---|---|
| TCP (에뮬레이터·Wi-Fi·USB 테더링) | IP 소켓 | `TcpTransport`. **1차** | 에뮬레이터: 폰 USB → Mac `adb forward` 또는 콘솔 `redir` → 차 `10.0.2.2:8080`. 테더링: 차가 USB 네트워크의 게이트웨이(폰)로 접속. Wi-Fi: 주소 입력·NSD |
| USB 액세서리 모드(AOA) | 바이트 파이프(bulk), IP 없음 | `AoaTransport`. **2차**, Pleos가 서드파티 앱에 USB host를 열어 준 경우만 | 차: `UsbManager` host API + 권한. 폰: `USB_ACCESSORY_ATTACHED` 필터 + `openAccessory` |
| ADB 포워딩 | Mac 중계 | 개발·데모 전용 | 실차와 무관 |

```kotlin
interface Transport { val input: InputStream; val output: OutputStream; fun close() }
```

연결 방향은 항상 **차 → 폰**(폰이 listen, 차가 connect). 에뮬레이터 NAT에서 바깥으로 나가는 접속만 되기 때문이고, 테더링에서도 폰이 게이트웨이라 자연스럽다. AOA는 USB 호스트(차)가 여는 구조라 방향 개념이 없고 메시지는 대칭이다.

### 2.2 메시지

전송 위에 길이 접두 프레임을 얹는다. 연결 하나로 **양방향**.

```
type(1B) | length(4B, big-endian) | payload
```

| type | 이름 | 방향 | payload | 규칙 |
|---|---|---|---|---|
| 0x01 | `HELLO` | 양방향, 연결 직후 1회 | JSON `{"v":1,"role":"phone"\|"car","screen":[w,h],"now":epoch_ms}` | `v`가 다르면 끊는다. `now`는 보낸 쪽 시계(LIGHT `ts` 보정용, 없으면 보정 없음). HELLO 를 받은 쪽은 자기 LIGHT 를 한 번 보낸다 |
| 0x02 | `FRAME` | 폰 → 차 | JPEG 바이트 | 긴 변 ≤ 1200px, ≤ 15fps. 폰은 **최신 프레임만** 보낸다(쓰는 중이면 건너뜀). 상한 1MB |
| 0x03 | `LIGHT` | 양방향 | JSON `LightingParams` | 아래 표. 받은 값이 내 값보다 최신(`supersedes`)일 때만 적용. `src`가 자기 것이면 무시. 받은 값은 자기 UI에만 반영하고 되돌려 보내지 않는다 |
| 0x04 | `PING` | 양방향 | 없음 | 5초마다. 15초 무수신이면 끊고, 차가 2초 뒤 재접속 |

`LightingParams`:

```json
{"v":1,"kelvin":3500,"level":0.8,"preset":null,"src":"phone","ts":1731000000000}
```

| 필드 | 타입·범위 | 의미 |
|---|---|---|
| `kelvin` | Int 2700~8000 (기본 7000, 푸른기 도는 흰색) | 색온도. `preset`이 null일 때 적용. 폰 슬라이더 100K, 차 다이얼 1칸 250K |
| `level` | Float 0.0~1.0 | 밝기. 다이얼 1칸 0.05 |
| `preset` | null \| `"cyan"` \| `"blue"` \| `"purple"` | 무드 색. null이면 색온도 모드 |
| `src` | `"phone"` \| `"car"` | 보낸 쪽 |
| `ts` | epoch ms | 사용자가 이 값을 **마지막으로 바꾼 시각**(0 = 한 번도 안 바꾼 기본값). 재접속·충돌 때 ts 가 큰 쪽이 기준, 같으면 폰, 기본값은 항상 진다(`LightingParams.supersedes`). 기기 간 시계 차이는 HELLO 의 `now` 로 받는 쪽 시계에 맞춰 보정한다(11/09 결정) |

동시 조작 충돌은 해결하지 않는다(마지막에 받은 값 적용, 양쪽 150ms 디바운스).

### 2.3 색 변환

켈빈 → ARGB는 흑체 복사 근사식(Tanner Helland) **구현 하나**를 `:shared`에 두고 양쪽이 같은 함수를 쓴다. 링라이트, 폰 미리보기, `setAmbientLightBrightnessColor`가 같은 값을 받는다. 참고값(근사식 실제 출력) 2700K ≈ (255,167,87), 4000K ≈ (255,206,166), 6500K ≈ (255,254,250), 7000K ≈ (243,242,255), 8000K ≈ (221,230,255). 무드 색 ARGB 3개도 `:shared` 상수. 실내등·독서등은 켜고 끄기만 되므로 색온도 대상이 아니다.

### 2.4 공유 모듈과 도구 (I0 전에, 담당 A)

| ID | 작업 | 내용 | 시간 |
|---|---|---|---|
| T0 | `:shared` 모듈 | Android library, 리소스·외부 의존성 없음(JSON은 플랫폼 `org.json`). `LightingParams.kt`(데이터 클래스, `toJson/fromJson`, 범위 클램프), `ColorTemp.kt`(`kelvinToArgb`, `MOOD_COLORS`), `Protocol.kt`(포트 8080, type 상수, 타임아웃), `MessageCodec.kt`(프레임 읽기·쓰기, `Transport` 인터페이스). `settings.gradle.kts`에 `include(":shared")`, 두 앱에 `implementation(project(":shared"))` | 1.5h |
| T1 | `tools/mock_phone.py` | Python 표준 라이브러리만. 8080 listen, HELLO, `--jpeg 파일`을 주면 15fps로 FRAME 반복 송신, `LIGHT`는 1초마다 2700→6500K 스윕 또는 stdin `k 3500` / `l 0.6`, 받은 LIGHT는 출력. **A가 폰 앱 없이 차량 앱을 검증하는 도구** | 1h |
| T2 | `tools/mock_car.py` | 127.0.0.1:8080에 접속, HELLO, FRAME 크기·fps 출력, 받은 LIGHT 출력, stdin으로 LIGHT 송신. **B가 차량 앱 없이 폰 앱을 검증하는 도구** | 0.5h |

T0 머지 뒤 `settings.gradle.kts`·`libs.versions.toml`은 동결한다(충돌 지점).

## 3. 모바일 앱 (B)

지금([poc/phone](../poc/phone), 11/5 커밋): CameraX 프레임 → JPEG → `MjpegServer`(HTTP multipart :8080). 화면에 Wi-Fi IP·접속 수·프레임 수 표시, `ACCESS_NETWORK_STATE` 권한. 바꿀 것은 **프레임 소스**(카메라 → 화면)와 **서버**(HTTP → 프레임 메시지 + 조명 값)다.

| 순서 | ID | 작업 | 파일 | 내용 | 완료 기준 | 차량 앱 없이 확인 |
|---|---|---|---|---|---|---|
| 1 | P1 | 의존성·권한 | `phone/build.gradle.kts`, `libs.versions.toml`, `AndroidManifest.xml` | CameraX 2종·`CAMERA` 권한·`uses-feature camera`·`ACCESS_NETWORK_STATE` 제거. 추가 `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PROJECTION`(API 34+), `POST_NOTIFICATIONS`(API 33+). `<service android:name=".MirrorService" android:foregroundServiceType="mediaProjection" android:exported="false"/>`. `:shared` 의존 | 빌드 통과 | — |
| 2 | P2 | `MirrorService` 뼈대 + 알림 | 새 `MirrorService.kt` | 포그라운드 서비스. **순서 고정**(API 34+): ① `startForeground(id, 알림, FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)` → ② `getMediaProjection(resultCode, data)` → ③ `projection.registerCallback(cb, handler)` → ④ `ImageReader.newInstance(vw, vh, RGBA_8888, 2)` → ⑤ `createVirtualDisplay("CarStudio", vw, vh, dpi, VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, reader.surface, null, handler)`. 알림 채널 1개, "차량으로 화면 송출 중" + 중지 | 예외 없이 가상 디스플레이 생성. 알림 표시 | 로그 |
| 3 | P3 | 캡처 → JPEG | `MirrorService.kt` | `HandlerThread`에서 `acquireLatestImage()`. 15fps 제한(직전 전송 후 66ms 미만이면 버림). RGBA 평면 `rowStride/pixelStride` 패딩 보정 후 폭만큼 잘라 JPEG 60~70. 가상 디스플레이는 폰 해상도를 긴 변 1200px 이하로 축소(1080×2400 → 540×1200) | `mock_car.py`에 FRAME이 10~15fps로 들어오고 JPEG 크기가 540×1200 | T2 |
| 4 | P4 | `StudioLink` 서버 | `MjpegServer.kt` → `StudioLink.kt` | TCP 8080 accept → `TcpTransport` → `MessageCodec`. HELLO 교환, FRAME 최신만 송신, LIGHT 송수신, PING 5초. `StateFlow<LightingParams>`를 서비스가 소유. 액티비티가 죽어도 송출 유지 | mock_car에서 HELLO·FRAME·PING 수신, stdin LIGHT → 폰 상태 갱신 | T2 |
| 5 | P5 | 액티비티 개편 | `MainActivity.kt` | 카메라 코드·Wi-Fi IP 표시·프레임 카운트 삭제. "화면 공유 시작" → `createScreenCaptureIntent()`를 `StartActivityForResult`로, OK면 `startForegroundService`. "중지". 상태 3줄(송출, 차량 연결, 안내 "연결 → 화면 공유 시작 → 쓰던 카메라·라이브 앱을 여세요"). Android 14 동의 화면의 "앱 하나만 공유 / 전체 화면" 안내 한 줄 | 시작·중지 3회 반복 정상 | 화면 |
| 6 | P6 | 종료 처리 | `MirrorService.kt` | `MediaProjection.Callback.onStop`(상태바에서 중지) → `stopSelf`. `onDestroy`: `virtualDisplay.release()`, `reader.close()`, `projection.stop()`, 서버 종료. 토큰은 1회용이라 재시작 시 동의 다시 | 상태바 중지 후 리소스 정리, 재시작 가능 | mock_car 연결 끊김 확인 |
| 7 | P7 | 회전 대응 | `MirrorService.kt`, `MirrorFit.kt` | 가상 디스플레이는 정사각형(한 변 1200) 하나를 한 번만 만든다. AUTO_MIRROR 가 화면을 비율대로 가운데 맞추므로 `onDisplayChanged` 때(와 300ms 뒤) 내용 영역만 다시 계산해 그 부분을 잘라 보낸다. `resize`+surface 교체 경로는 회전 직후 작게 보이는 문제가 있어 버림(11/09) | 회전 시 FRAME 크기가 540×1200 ↔ 1200×540 | T2 출력 |
| **I1** | | 통합 1 (11/9 저녁) | | **폰 기본 카메라 앱 화면이 차 화면 가운데에 세로로 뜬다.** 홈으로 나가도 유지 | | A와 |
| 8 | P8 | 폰 조명 UI | `MainActivity.kt` | 밝기 슬라이더, 색온도 슬라이더(100K 단위), 무드 색 칩 3개. 값은 서비스 `StateFlow`; 차에서 온 LIGHT도 여기 반영. 미리보기 색은 `:shared` 변환 | 슬라이더 조작 → LIGHT 송신(mock_car 출력), mock_car LIGHT → UI 갱신 | T2 |
| 9 | P9 | 알림 액션 | `MirrorService.kt` | 버튼 3개: 따뜻하게(-300K) · 차갑게(+300K) · 중지. 알림 액션은 최대 3개라 밝기는 차 다이얼·폰 화면에서 | 카메라 앱이 앞에 있을 때 버튼 → LIGHT 송신 | T2 |
| **I2** | | 통합 2 (11/11 저녁) | | 폰 ↔ 차 양방향 동기화 | | A와 |
| 10 | P10 | 실기기 | — | 실제 폰 USB + `adb forward tcp:8080 tcp:8080` → 차 에뮬레이터. 기본 카메라 앱 미러링. Android 14+면 "앱 하나만 공유" 확인 | 시나리오 1회 | adb 교체 선행(10절) |
| 11 | P11 | release 서명·APK | `phone/build.gradle.kts` | S1 키로 `assembleRelease`, `isMinifyEnabled = false` | 새로 설치해 동작 | — |
| 2차 | P12 | 플로팅 컨트롤 | | `SYSTEM_ALERT_WINDOW` 오버레이 색온도·밝기 조절판. 전체 화면 공유면 차에도 비치므로 "앱 하나만 공유"와 함께 안내 | | |
| 2차 | P13 | AOA 전송 | | `USB_ACCESSORY_ATTACHED` 필터, `openAccessory` → `AoaTransport`. Pleos 회신이 "가능"일 때만 | | |
| 2차 | P14 | 무선·테더링 안내 | | 핫스팟/USB 테더링 설정 바로가기, NSD 광고 | | |

B가 1차에서 손대지 않는 것: `poc/car/*`, `:shared`(수정 필요하면 A에게), `poc/README.md`(통합 때 A가 갱신).

## 4. 차량 앱 (A)

지금([poc/car](../poc/car), 11/5 커밋): `StudioVehicle`이 앰비언트 밝기·색 set(원래 값 읽기·복원 없음), 공조 풍량·창문 구독과 소음 경고, 기어 D·R 감지. `MjpegClient`가 HTTP multipart 파싱. 화면은 링 + 가운데 영상 + 좌상단 상태 패널(기어·풍량·fps·로그) + 하단 바(주소 입력란·연결 버튼·앰비언트 스위치·슬라이더·색 5개).

| 순서 | ID | 작업 | 파일 | 내용 | 완료 기준 | 폰 앱 없이 확인 |
|---|---|---|---|---|---|---|
| 1 | T0·T1 | 공유 모듈, mock | 2.4절 | | 머지, mock 실행 | — |
| 2 | CR | 범위 정리 | `StudioVehicle.kt`, `MainActivity.kt`, `AndroidManifest.xml` | 풍량·창문 구독과 `CAR_CLIMATE`·`CAR_WINDOWS` 권한 제거. 상태 패널·fps·주소 입력란·연결 버튼·앰비언트 스위치 제거, 폰 주소는 상수. 기어 게이트를 `usable: Boolean?`(P·N → true, 그 외 → false, 미확인 → null)로, D·R을 한 번이라도 보면 경고 3초 후 `finish()`, 실행 시점에 D·R이어도 같은 경로. 감시는 Compose가 아니라 `lifecycleScope`에서(Pleos가 앱을 가려 onStop이 와도 동작). 11/8에 같은 설계로 구현·검증했던 내용 | 기어 P 동작, D·R 전환 시 3초 뒤 종료, D에서 실행 시 즉시 종료 | Virtual Console 기어 |
| 3 | C0 | 색온도 모델 | `MainActivity.kt`(`Light`) | `Light`를 `level`, `kelvin`(다이얼 1칸 250K), `preset`(null이면 색온도)으로. 링 색과 `syncAmbient` ARGB는 `:shared` `kelvinToArgb` 한 함수에서. 기존 5색 중 화이트·웜은 색온도로 흡수, 시안·블루·퍼플만 무드 색 | 다이얼·슬라이더로 2700~6500K 이동, 링 색이 변환값과 일치 | 영상 없이 |
| 4 | C1 | `StudioLinkClient` | `MjpegClient.kt` → `StudioLinkClient.kt` | TCP connect(1차 주소 상수 `10.0.2.2:8080`) → `TcpTransport` → `MessageCodec`. HELLO, FRAME → Bitmap, LIGHT → `Light` 적용(`src=car` 무시), `Light` 변경 시 150ms 디바운스 후 LIGHT 송신, PING. 끊기면 2초 뒤 재접속. `usable && !exiting`일 때만(지금 영상 루프와 같은 조건) | mock_phone의 FRAME 표시, 켈빈 스윕에 링·앰비언트가 따라감, 다이얼 조작이 mock 터미널에 출력, mock 껐다 켜면 복귀 | T1 |
| 5 | C2 | 문구 | `MainActivity.kt` | 대기 문구 "폰 화면 연결 대기 중\n폰을 연결하고 Car Studio에서 화면 공유를 시작해 주세요" | | |
| **I1** | | 통합 1 (11/9 저녁) | | 새 폰 앱 영상이 뜬다 | | B와 |
| 6 | C3 | 조명 상태 모델 | `StudioVehicle.kt` | `CarState`에 `ambient: Map<SeatArea, AmbientArea(brightness, bounds, color, switch)>`, `cabin: LightSwitch?`, `reading: Map<SeatArea, LightSwitch?>`. 좌석 목록은 enum이 아니라 **capability 콜백의 Map 키**(`SeatArea`는 `Area`를 상속한 클래스). 스위치 값 `LightSwitch.OFF/ON/AUTOMATIC/DAYTIME_RUNNING` 중 capability가 돌려준 것만 노출 | 모델 컴파일, 로그에 읽힌 값 | — |
| 7 | C4 | 읽기·복원 일반화 | `StudioVehicle.kt` | 시작 시 원래 값을 읽어 두고 종료 시 되돌리는 **복원 동작 목록** `restore: List<(done) -> Unit>`. 읽기 성공 항목마다 되돌리는 클로저 추가, `stop()`은 전부 실행한 뒤 콜백 수를 세어 `release()`(콜백이 안 오면 1초 뒤). 먼저 해제하면 되돌리는 set이 버려지므로 순서가 중요. 읽기 `getAmbientLightBrightness`, `getAmbientLightBrightnessColor`, `getAmbientLightSwitch`, `getCabinLightSwitch`, `getReadingLightSwitch(area)`. capability `checkAmbientLightBrightnessCapabilities`, `checkAmbientLightBrightnessColorCapabilities`, `checkAmbientLightSwitchCapabilities`, `checkCabinLightSwitchCapabilities`, `checkReadingLightSwitchCapabilities`. `syncAmbient`는 원래 값을 읽은 영역만 바꾸고, `stopped` 가드로 복원 뒤 늦게 도착한 set을 막는다. **비정상 종료 대비(11/09)**: 읽은 원래 값을 `OriginalStore`(SharedPreferences)에 저장하고 처음 바꿀 때 dirty, 복원을 마치면 지운다. 다음 실행이 dirty 를 보면 저장된 값을 원래 값으로 채택하고 다른 값은 즉시 되돌린다. 기어 D 를 보면 `restoreNow()`로 먼저 되돌리고 3초 뒤 종료한다. 복원 순서 앰비언트 → 실내등 → 독서등 | 기어 D → 즉시 앰비언트·실내등·독서등 원래 값. 강제 종료 뒤 재실행도 원래 값 복원 | Virtual Console |
| 8 | C5 | 설정 API | `StudioVehicle.kt` | `setAmbientLightSwitch`, `setCabinLightSwitch`, `setReadingLightSwitch(area, …)`. `syncAmbient`는 "연동" 토글이 켜진 동안만. `stopped` 가드 유지 | 패널 조작 → Virtual Console 반영 | Virtual Console(`GET /vehicle/state`로 항목 이름 먼저 확인) |
| 9 | C6 | 외부 변경 추적 | `StudioVehicle.kt` | `registerCabinLightState`, `registerReadingLightState(area)`, `registerAmbientLightBrightnessListener/ColorListener(area)`. `stop()`에서 전부 unregister | Virtual Console에서 바꾸면 패널이 따라옴 | Virtual Console |
| 10 | C7 | 레이아웃 | `MainActivity.kt` | 링 안쪽을 `Row`로: 왼쪽 **모니터**(세로 영상 `ContentScale.Fit`, 남는 폭 검정), 오른쪽 **조명 패널**(약 380dp). 섹션 ① 링라이트(밝기·색온도 슬라이더, 무드 색 칩 3, 다이얼 대상) ② 앰비언트(연동 토글, 켜짐, 현재 값) ③ 실내등 ④ 독서등(좌석별 칩). capability 없는 섹션은 숨김 | 앱 영역(약 1680×1210)에 들어감, 좌우 링 폭 충분 | 에뮬레이터 |
| 11 | C8 | 다이얼 | `DialTarget.kt`, `MainActivity.kt` | 회전 대상 밝기 ⇄ 색온도. 패널 터치 후 회전 1~2칸이 포커스 복구에 쓰이는 현상 그대로인지 확인 | 다이얼 동작 유지 | `adb shell cmd car_service inject-rotary` |
| 12 | C9 | 매니페스트 주석 | `AndroidManifest.xml` | `CAR_LIGHTS` "앰비언트·실내등·독서등 조회", `CONTROL_CAR_LIGHTS` "조명 설정(종료 시 복원)" | | |
| **I2** | | 통합 2 (11/11 저녁) | | 실제 폰 앱과 양방향 | | B와 |
| 13 | C10 | 확인·CPU | — | 4.1 확인 항목 전체. `adb shell top -n 1 | grep studio` 정차 기준 25% 이하 | | 에뮬레이터 |
| 14 | C11 | Pleos 문의 | — | 9절 5건. B 검토 후 **11/9 발송** | | |
| 15 | S1~S4 | 서명·release·제출 저장소·README | 6절 | | | B의 APK 받아서 |
| 2차 | C12 | 조명 씬 | | `Scene(ring, ambient, cabin, reading)` 4종, 패널 맨 위 칩. 씬 적용도 복원 목록을 거침 | | |
| 2차 | C13 | 풀 라이트 모드 | | 모니터·패널 숨기고 앱 영역 전체를 링 색으로, 탭으로 복귀 | | |
| 2차 | C14 | 무선·테더링 주소 | | USB 전송 네트워크(`TRANSPORT_USB`)의 게이트웨이 탐지, Wi-Fi는 NSD 탐색·주소 입력, 상수 주소는 폴백 | | |
| 2차 | C15 | AOA 전송 | | `UsbManager` host API, 권한 요청, bulk → `AoaTransport`. Pleos 회신이 "가능"일 때만 | | |
| 2차 | C16 | H.264 | | 차량 CPU가 25%를 넘으면 폰 `MediaCodec` 인코딩 + 차 `MediaCodec` 디코딩 | | |

### 4.1 차량 확인 항목 (Pleos 에뮬레이터 2.1.0 + Virtual Console)

- [ ] 시작 시 앰비언트·실내등·독서등 원래 값이 `adb logcat -s StudioVehicle`과 패널에
- [ ] 패널 실내등 ON/OFF, 독서등 좌석별 ON/OFF → Virtual Console 반영
- [ ] Virtual Console에서 실내등을 바꾸면 패널이 따라감
- [ ] 연동 토글 OFF → 링 색을 바꿔도 앰비언트 그대로
- [ ] 색온도 2700K·4000K·6500K에서 링 색과 Virtual Console 앰비언트 색이 같은 RGB
- [ ] 기어 D → 3초 뒤 종료 → 조명 전부 원래 값. D에서 실행 → 아무것도 안 바꾸고 종료
- [ ] 세로 미러링 + 패널이 앱 영역에 들어가고 좌우 링 폭이 넓다

A가 1차에서 손대지 않는 것: `poc/phone/*`.

## 5. 2차 공통

| 항목 | 내용 |
|---|---|
| 실차 USB 경로 | Pleos 회신에 따라: 테더링이면 C14만(전송 코드 변경 없음). 액세서리 모드면 P13·C15. 둘 다 불가면 무선(P14·C14)이 실차의 기본 경로가 됨 |
| 실기기 라이브 앱 | 틱톡·유튜브 라이브 미러링에서 `FLAG_SECURE`로 검게 나오는지. 지원 앱 목록을 README에 |
| PT 자료 | 기획서 흐름 그대로 10장 내외. 시연 대본은 S5 영상 대본 재사용 |

## 6. 제출물

| ID | 작업 | 내용 |
|---|---|---|
| S1 | 서명 키 | `keytool -genkeypair -keystore carstudio-release.jks -alias carstudio -keyalg RSA -keysize 2048 -validity 10000`. 저장소 밖 보관, `keystore.properties`(gitignore). `signingConfigs.release { enableV2Signing = true; enableV3Signing = true }` — 공모전 요건 "사이닝 키 로테이션·리니지"는 v3 서명 체계 |
| S2 | release 빌드 | `./gradlew :car:assembleRelease :phone:assembleRelease`, `isMinifyEnabled = false`(Pleos SDK AIDL 보호). `apk/팀명_CarStudio_release.apk`(차량), `apk/팀명_CarStudioMirror_release.apk`(폰). 규격은 차량 앱 1개지만 폰 앱도 넣고 README에 설명 |
| S3 | 제출용 저장소 | 새 저장소, 이력 없음. `poc/`를 루트로 + `apk/` + README. 작성자 중립(`git -c user.name="Car Studio Team" -c user.email="carstudio@users.noreply.github.com"`). 비공개면 심사용 계정 Collaborator 초대 후 접근·쓰기 권한 확인. `plan/`·`contest/`는 넣지 않음 |
| S4 | README 4항목 | ① 앱 이름·한 줄 소개 ② 실행 방법(Pleos 에뮬레이터 2.1.0, 기어 P, APK 설치, 폰 `adb forward tcp:8080 tcp:8080` 또는 폰 에뮬레이터 콘솔 `redir`) ③ 테스트 환경·설정(JDK 17, Pleos 이미지, Virtual Console, `CONTROL_CAR_LIGHTS` 필요) ④ APK 위치 |
| S5 | 시연 영상 | 1~2분, 에뮬레이터 두 창 나란히, **macOS 화면 녹화**(adb `screenrecord`는 adb 크래시로 불안정). 대본: 차 앱 실행(P) → 폰 "화면 공유 시작" → 기본 카메라 앱 → 미러링 → 다이얼 밝기·색온도 → 폰 알림 '차갑게' → 패널 실내등 → Virtual Console 기어 D → 경고 → 종료·복원 |
| S6 | 스크린샷(별지) | 스튜디오 화면, 조명 패널, 색 변경, 종료 안내. 4장 |
| S7 | 기획서 DOCX | [submission-draft.md](submission-draft.md) 2절을 02 섹션 입력란에, 01·04 섹션 팀 정보·서명 |
| S8 | 제출 메일 | 제목 `[차량용 앱 개발 공모전] 팀명_대표자명`, 팀원 전원 CC, DOCX + 영상(또는 링크) + 저장소 링크. **11/15 오전** |

## 7. 통합 체크포인트

| 체크포인트 | 시점 | 통과 기준 | 미통과 시 |
|---|---|---|---|
| **I0** 계약·공유 모듈 | 11/9 오전 | 2절 계약에 둘 다 동의, `:shared`·mock 머지, 두 앱 빌드 통과 | 이 뒤로 계약 변경은 둘이 같이, 같은 날 머지 |
| **I1** 미러링 | 11/9 저녁 | 폰 카메라 앱 화면 → 차 화면. 홈으로 나가도 유지. 회전 반영 | 11/10 오전까지 B 수정. A는 mock으로 계속 |
| **I2** 동기화 | 11/11 저녁 | 폰 슬라이더 → 차 링·앰비언트 0.3초 내. 차 다이얼 → 폰 UI. 알림 '따뜻하게' 동작 | **P9(알림 버튼)부터 뺀다.** 그래도 안 되면 폰 조절 전체를 2차로, 1차는 "차에서만 조절 + 미러링" |
| **I3** 리허설 | 11/12 | S5 대본을 처음부터 끝까지 끊김 없이. 실기기 1회 | 영상은 폰 에뮬레이터로 |
| **I4** release 검증 | 11/13 | release APK 2개를 **새로 설치**해 I3 재실행 | 디버그와 차이 나면 서명·minify 설정부터 |

## 8. 일정

### 1차: 11/8 ~ 11/15

| 날짜 | A 차량 앱 | B 모바일 앱 | 같이 |
|---|---|---|---|
| 11/8 (토) | T0·T1 시작 | P1, P2 | 계약 합의 |
| 11/9 (일) | T0·T1·T2 머지(오전) → CR → C0 → C1 → C2 | P3 → P4 → P5 → P6 → P7 | **I0 오전, I1 저녁.** Pleos 문의 발송 |
| 11/10 (월) | C3 → C4 → C5 → C6 | P8 | — |
| 11/11 (화) | C7 → C8 → C9 | P9 | **I2 저녁** |
| 11/12 (수) | C10, 버그 | P10, 버그 | **I3** |
| 11/13 (목) | S1 → S2 → S3 → S4, `poc/README.md` 갱신 | P11 → APK를 A에게 | **I4** |
| 11/14 (금) | S5 영상, S6 스크린샷 | S7 DOCX 입력, 01·04 섹션 | 최종 검토 |
| 11/15 (일) | — | — | **S8 오전 제출** |

### 2차: 11/16 ~ 12/4

| 주 | A | B | 같이 |
|---|---|---|---|
| 11/16~22 | C12 씬, C13 풀 라이트, C14 무선·테더링 주소 | P12 플로팅, P14 무선·테더링 안내. 실기기 라이브 앱 확인 | Pleos 회신 반영(P13·C15 여부), C16 판단 |
| 11/23 (월) | | | 1차 결과 |
| 11/23~29 | 안정화 | 안정화 | PT 자료, 시연 대본, 실기기 리허설. 2차 심사 기준 공지 확인 |
| 11/30~12/2 | | | 리허설 2회, 최종 결과물 제출(공지 방식) |
| 12/3~4 | | | PT. 에뮬레이터 + 실제 폰 |

## 9. 리스크와 문의

| 리스크 | 영향 | 대응 | 어디서 |
|---|---|---|---|
| adb 37.0.1 서버가 수 초마다 죽음(macOS 27) | 실기기 `adb forward` 끊김. 11/12 테스트와 12/3 시연 | Android Studio SDK Manager에서 platform-tools 교체(**11/12 전**). 에뮬레이터는 콘솔 `redir`로 우회 중 | 머신 소유자 |
| 실차에서 서드파티 앱의 폰 USB 연결 가능 여부 미확인 | 기획서 "유선" 전제, 2차 실차 경로 | 전송 계층 분리(2.1). Pleos 문의 ①. 불가면 무선이 기본 경로 | C11 |
| Vehicle Control(`CONTROL_CAR_LIGHTS`) "준비 중" | 배포 앱에서 조명 설정이 막힐 수 있음 | 조회·패널 표시는 Vehicle State만으로 동작. 문의 ② | C11 |
| 화면 밝기 최대(`screenBrightness = 1f`) 실차 허용 | 링라이트 광량 | 문의 ③ | C11 |
| 폰 화면 미러링 앱마켓 정책 | 2차 이후 입점 | 정차 전용·원복으로 설명. 문의 ④ | C11 |
| 차량 Wi-Fi·핫스팟을 서드파티 앱이 쓸 수 있는지 | 2차 무선 모드 | 문의 ⑤. 1차는 TCP 에뮬레이터 경로로 완결 | C11 |
| `FLAG_SECURE` 앱 검은 화면 | 일부 앱 미지원 | 카메라·라이브 UI는 보통 해당 없음. 실기기 확인 후 README에 | 2차 |
| Android 14+ 세션마다 동의, 토큰 1회용 | 시작 흐름 한 탭 추가 | P5 안내 문구 | P5 |
| 알림 액션 3개 제한 | 백그라운드에서 밝기 조절 불가 | 1차는 색온도만. 2차 P12 | P9 |
| 차량 MJPEG 디코드 CPU | 정차 ≤25% 목표 | 축소·15fps. 넘으면 C16 | C10 |
| 동시 조작 값 튐 | 조명이 잠깐 왔다 갔다 | 마지막 수신 적용, 150ms 디바운스 | 2.2 |
| Virtual Console이 실내등·독서등을 반영하지 않음 | C5·C6 검증 불가 | 앰비언트만으로 1차 제출, 실내등·독서등은 "조회만" 표시. 기획서 5번 문구 수정 | A |
| B의 미러링이 11/9에 안 끝남 | I1 지연 | A는 영향 없음(mock). I1을 11/10 오전으로, P8·P9를 하루씩 민다. 그래도 밀리면 P9 제외 | 7절 |

**Pleos 문의 (11/9 발송, `partnership@pleos.ai` 또는 공모전 메일, FAQ 먼저 확인):**
① 실차 헤드유닛 USB 포트에 꽂은 폰을 서드파티 차량 앱이 쓸 수 있는가 — (a) 폰 USB 테더링 시 차에 네트워크 인터페이스가 생기는지, (b) USB 액세서리 모드(AOA)를 서드파티 앱이 USB host API로 열 수 있는지, (c) 둘 다 아니면 서드파티 앱이 쓸 수 있는 폰 연결 수단은 무엇인지
② Vehicle Control(`CONTROL_CAR_LIGHTS`) 제공 시점과 공모전 기간 허용 여부
③ 앱 창 `screenBrightness` 최대 설정 허용 여부
④ 정차 중 폰 화면 미러링 앱의 앱마켓 심사 기준
⑤ 차량 Wi-Fi 또는 폰 핫스팟 연결을 서드파티 앱이 쓸 수 있는지

## 10. 브랜치·환경

| 브랜치 | 담당 | 머지 |
|---|---|---|
| `develop` | 공용 | 기준선 96a63e3(11/5 PoC). 작업 트리에는 문서 두 편만 있다 |
| `feat/shared-protocol` | A | I0 |
| `feat/phone-mirror` | B | I1, 이후 작은 단위로 수시 |
| `feat/car-lighting` | A | I2 전 수시 |

충돌 지점은 `settings.gradle.kts`, `libs.versions.toml`(I0 뒤 동결), `poc/README.md`(A만) 세 파일. 제출용 저장소(S3)는 별개로 이력 없이 만든다.

| 환경 | A | B |
|---|---|---|
| JDK 17 | 필수 (`brew install --cask temurin@17`) | 필수 |
| Pleos 에뮬레이터 2.1.0 + Virtual Console | 필수 | 통합 때 A 머신, 또는 설치(이미지 4.7GB) |
| 폰 에뮬레이터 또는 실기기 | 통합용 | 필수. 실기기 Android 14+면 "앱 하나만 공유" 확인 가능 |
| adb | 11/12 전 platform-tools 교체 | 같음(실기기 `adb forward`) |
| 포워딩 | 차: `printf 'auth %s\nredir add tcp:8088:8088\nquit\n' "$(cat ~/.emulator_console_auth_token)" \| nc -w 3 127.0.0.1 5556` | 폰 에뮬레이터: 같은 명령에 `tcp:8080:8080`, 포트 5554 |

## 11. 결정할 것

| # | 결정 | 추천 | 기한 |
|---|---|---|---|
| 1 | 1차 전송 포맷: 프레임 메시지(2.2) vs HTTP(MJPEG) 유지 | **프레임 메시지.** 제어 채널을 아직 안 만든 지금이 바꾸기 가장 싸고, AOA가 와도 전송 계층만 추가하면 된다. HTTP 유지는 검증된 MJPEG 코드를 안 건드리는 대신 제어 채널까지 HTTP로 만들면 나중에 두 번 고친다 | I0 (11/9 오전) |
| 2 | N 기어: P·N 허용 vs P만 | 현재(P·N) 유지. Pleos가 N에서도 가리므로 실질 차이 없음 | 11/13 |
| 3 | 앱명·팀명 | — | 11/13 (서명·APK 파일명) |

기획서 쪽 결정(가정값, 제출 저장소, 숏폼)은 [submission-draft.md](submission-draft.md) 3절.

## 12. 확인한 사실

- Vehicle SDK 2.3.2 바이너리: `LightSwitch` = `OFF, ON, DAYTIME_RUNNING, AUTOMATIC`, `LightState` = `OFF, ON, DAYTIME_RUNNING`. `SeatArea`는 `Area`를 상속한 클래스(enum 아님) → 좌석 목록은 capability 콜백에서.
- 실내등·독서등 함수는 문서대로 바이너리에 있다: `get/setCabinLightSwitch`, `get/setReadingLightSwitch(area)`, `getCurrentCabinLightState`, `getCurrentReadingLightState(area)`, `registerCabinLightState`, `registerReadingLightState(area)`.
- Virtual Console Convenience 도메인에 실내등·독서등 항목이 있다([../contest/environment.md](../contest/environment.md)).
- 차량 앱 수신부는 프레임 소스가 카메라든 화면이든 구분하지 않는다.
- Pleos 문서(~130쪽 가이드)에서 USB 언급은 Android Auto(AOAP/USB) 용어 설명 하나뿐이다. 서드파티 앱의 USB 사용 여부는 문서에 없다.
- 켈빈→RGB 근사식은 표준 라이브러리만으로 구현된다.
- 11/8 범위 정리 코드(기어 P·N 게이트, 앰비언트 원래 값 복원, 풍량·창문·주소 입력란 제거)는 에뮬레이터에서 동작을 확인한 뒤 폐기했다. 설계는 CR·C4에 적어 두었다. 그때 확인된 것: P에서 동작, N에서도 Pleos가 DO 미선언 앱을 가림, D·R 전환 시 3초 뒤 종료·앰비언트 복원(30/`#00e5ff`), D에서 실행 시 즉시 종료.
