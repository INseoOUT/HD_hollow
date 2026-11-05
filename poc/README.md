# 차량 안 스튜디오 PoC

> 작성일: 2026-11-04
> 목적: "가운데 화면 가장자리 = 링라이트, 가운데 = 폰 카메라 모니터, 다이얼로 밝기·색 조절" 컨셉이 Pleos 에뮬레이터에서 실제로 되는지 검증한다.

## 구성

| 모듈 | 하는 일 |
|---|---|
| `car/` | Pleos Connect 차량 앱. 가장자리 조명(테두리 두께 = 밝기), 폰 영상 표시, 앰비언트 라이트 연동, 공조 풍량·창문 소음 경고, 기어 D/R이면 차단, 다이얼(`DialTarget.kt`)·키보드 입력 |
| `phone/` | 폰 동반 앱. 카메라 → JPEG → MJPEG(HTTP, 포트 8080) 송출. 전면/후면 전환(전면만 좌우 반전), 가로·세로 회전(회전해도 송출 유지). 화면에 접속 주소 표시 |

연결 방향은 **차 → 폰**이다(차가 폰 주소로 접속). 에뮬레이터 NAT 안에서도 바깥으로 나가는 접속은 되기 때문이다.

## 검증 항목

| # | 항목 | 확인 방법 | 결과 (2026-11-04, Pleos Connect v2.1.0 에뮬레이터) |
|---|---|---|---|
| 1 | Vehicle SDK 연결, 공조 풍량·창문 구독 | Virtual Console에서 풍량·창문 변경 → 좌상단 패널·경고 반영 | ✅ 연결됨. 풍량 3 수신 → "녹음 소음 주의" 경고 표시. 창문 4개 영역 구독 |
| 2 | 앰비언트 밝기·색 set | 슬라이더·색 변경 → 패널 로그 | ⚠️ **`CAR_LIGHTS`만으로는 실패**(`[Pleos][SDK] setAmbientLightBrightness need Permission`). SDK 어노테이션과 달리 **`CONTROL_CAR_LIGHTS` 추가 시 성공**(`result = true`, 밝기 60·색 #FFFFFFFF). 지원 영역은 전 좌석 묶음 1개(0..100). Playground에서 Vehicle Control은 "준비 중"이라 배포 앱 사용 가능 여부는 별도 확인 필요 |
| 3 | 창 밝기 최대(`screenBrightness = 1f`) | 실제 차/에뮬레이터 화면 밝기 변화 | ➖ 에뮬레이터로는 판단 불가(실차 확인 필요). 오류는 없음 |
| 4 | 기어 읽기(AAOS `GEAR_SELECTION`) | Virtual Console 기어 D | ✅ P 읽힘. D로 바꾸면 **Pleos 시스템이 먼저 "주행 중에는 사용할 수 없습니다"로 앱을 가림**(DO 미선언 앱) |
| 5 | 폰 → 차 영상 스트리밍 | 폰 에뮬레이터(가상 카메라) → 호스트 → 차량 에뮬레이터 `10.0.2.2:8080` | ✅ **29.9fps** 수신·표시(2026-11-05 재확인: 29.7~29.8fps, 스트리밍 중 다이얼 색·밝기 조절 동시 동작). 지연 수치는 시계 오차로 무의미 → 실제 폰으로 육안 측정 필요 |
| 6 | 다이얼(회전) 입력 | `adb shell cmd car_service inject-rotary -c true` / `inject-key 23` | ✅ **회전 → 밝기·색 조절 동작.** 조건 3가지(car-ui-lib 2.6.0): ① 창에 `FocusParkingView`(FocusArea 밖, 맨 앞) ② 대상 뷰가 `FocusArea` 안 ③ 대상 뷰에 `setSupportsRotateDirectly` + **직접 조작 모드**. 직접 조작 모드는 다이얼을 눌러야 켜지지만, 포커스를 받을 때 `enableDirectManipulationMode(view, true)`를 호출하면 누르지 않아도 된다. 회전은 `ACTION_SCROLL_FORWARD/BACKWARD`로 들어온다. 터치 후 첫 회전 1칸은 포커스 복구에 쓰인다(AAOS 기본 동작). 주소 입력란을 터치한 뒤에는 2칸(입력란 복구 → 다이얼 뷰로 이동)이 쓰인다. 누름·nudge 키는 RotaryService가 가져가서 앱에 오지 않으므로, 밝기↔색 전환은 화면 터치로 처리(실차 CCP에서 재확인 필요) |

### 부수 발견

- **차량 속도 주입 가능.** Virtual Console 화면에는 속도 카드가 없지만, 내부 API `PUT /vehicle/controls?speed_kph=60`이 동작한다(Driving view에 60 km/h 표시). 브라우저 세션 하트비트(`POST /browser/session?client=...`, 1초 간격)가 없으면 `emulator_inactive`(503). 스크립트 예: 기어 `PUT /vehicle/gear?gear=Drive`.
- `adb shell cmd car_service inject-vhal-event`는 user 빌드라 불가 → 차량 값 조작은 Virtual Console API로.
- 에뮬레이터 설정: **`hw.audioInput=no` 필수**(켜 두면 부팅 직후 QEMU main loop가 멈춤, macOS 마이크 권한 문제로 추정). 에뮬레이터 출력을 `head`로 자르면 파이프가 닫혀 멈춘다. 비정상 종료 뒤에는 오류 보고 동의 창이 떠서 다음 부팅이 막히므로 `/tmp/android-$USER/emu-crash-*.db`를 지운다.
- Pleos 화면: 앱은 오른쪽 앱 영역(약 1680×1210px)에 뜬다. 왼쪽 Driving view는 가릴 수 없으므로 "화면 가장자리 조명"은 앱 영역 테두리까지만 가능.

## 실행

```sh
# 빌드 (JDK 17)
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
./gradlew :car:assembleDebug :phone:assembleDebug

# 차량 앱 설치 (Pleos 에뮬레이터)
adb -s <pleos-emulator> install -r car/build/outputs/apk/debug/car-debug.apk

# 폰 앱 설치 (실제 폰 또는 일반 폰 에뮬레이터)
adb -s <phone> install -r phone/build/outputs/apk/debug/phone-debug.apk
```

**실제 폰**: 폰과 Mac을 같은 Wi-Fi에 연결 → 폰 앱 화면의 `IP:8080`을 차량 앱에 입력.

**폰 에뮬레이터로 대체**: 폰 에뮬레이터는 Mac 웹캠을 카메라로 쓴다. 두 에뮬레이터는 서로 직접 못 붙으므로 호스트를 거친다.

```sh
adb -s <phone-emulator> forward tcp:8080 tcp:8080   # 호스트 8080 → 폰 에뮬레이터 8080
# 차량 앱 주소 입력: 10.0.2.2:8080  (에뮬레이터에서 본 호스트 주소)
```

## 단순화한 부분

- 영상은 MJPEG(표준 라이브러리만 사용). H.264 대비 대역폭이 커서 640×480·15fps 수준이 한계 → 화질이 문제면 RTSP/WebRTC로 교체.
- 지연은 폰·차 시계 차이를 보정하지 않은 근사값만 표시.
- 다이얼은 Compose가 아니라 투명 View(`DialTarget`)로 받는다. RotaryService는 접근성 노드에 동작을 수행하는데, Compose 가상 노드로는 이 경로를 검증하지 않았다.
- 다이얼 입력 확인용 로그: `adb logcat -s DialTarget`. RotaryService 상태: `adb shell dumpsys activity service com.android.car.rotary | grep DirectManipulation`.
