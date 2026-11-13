# Selene — 차 안 스튜디오

> 작성일: 2026-11-09 · 다시 씀: 2026-11-10 · 앱명 반영: 2026-11-12
> 근거: 이 폴더의 코드와 [verification.md](verification.md)의 확인 결과. 기획서 [../plan/submission-draft.md](../plan/submission-draft.md) · 명세 [../plan/spec.md](../plan/spec.md) · 남은 일 [../plan/work-plan.md](../plan/work-plan.md)
> 상태: 기능 구현을 마치고 에뮬레이터 두 대에서 확인했다(브랜치 `design/hyundio`). 실기기·실차는 미확인이고 release APK 는 아직 만들지 않았다. **⚠️ 표시는 제출 전에 채울 곳.**

## 1. 앱 이름·한 줄 소개

**Selene**(셀레네) — 주차된 차의 디스플레이와 실내 조명을 스튜디오 조명으로, 거치한 폰의 화면을 모니터로, 다이얼을 컨트롤러로 묶어, 쓰던 카메라·라이브 앱 그대로 차 안에서 촬영하게 하는 앱.

이름은 그리스 신화의 달의 여신에서 왔다. 사용자를 늘 비춰 주는 달빛이라는 뜻이다.

폰 화면을 차 화면 가운데에 띄우고(모니터), 차 화면 가장자리와 실내 조명을 스튜디오 조명으로 쓰며, 다이얼·폰 어디서든 색온도·밝기를 맞춘다. 촬영·저장·라이브 송출은 사용자가 쓰던 폰 앱(기본 카메라·틱톡·유튜브 라이브)이 그대로 한다.

차량 앱(`car/`, Pleos Connect)과 폰 동반 앱(`phone/`) 두 개로 이루어진다. 기어 P·N 에서만 동작하고, 끝나면 차량 조명을 원래 값으로 되돌린다. 서버·저장·위치 수집이 없다.

## 2. 실행 방법

차량 에뮬레이터(Pleos Connect 2.1.0, 콜드 부팅, 기어 P)와 폰(에뮬레이터 또는 실기기)을 준비한다.

**설치.** release APK 는 4절, 직접 빌드한 debug APK 는 아래 경로.

```sh
adb -s <pleos> install -r car/build/outputs/apk/debug/car-debug.apk
adb -s <phone> install -r phone/build/outputs/apk/debug/phone-debug.apk
```

**폰 → 차 경로(데모).** 차량 앱은 `10.0.2.2:8080`(에뮬레이터에서 본 Mac)으로 접속한다. 폰이 실기기면 `adb -s <phone> forward tcp:8080 tcp:8080`, 폰 에뮬레이터면 콘솔로 `redir add tcp:8080:8080`. adb 서버가 자주 죽는 Mac 에서는 콘솔 redir 이 안정적이다.

```sh
printf 'auth %s\nredir add tcp:8080:8080\nquit\n' "$(cat ~/.emulator_console_auth_token)" | nc -w 3 127.0.0.1 5554
```

**사용 순서.**

1. 차에서 Selene 를 연다(기어 P). 모니터 자리에 "폰 연결 대기 중" 안내가 뜬다.
2. 폰에서 Selene 를 열고 **화면 공유 시작** → 동의 화면에서 "앱 하나만 공유"로 쓰던 카메라·라이브 앱을 고른다.
3. 차 화면 가운데에 그 앱 화면이 뜬다. 다이얼·차 패널·폰(앱 화면, 알림 버튼, 플로팅 컨트롤) 어디서 바꿔도 조명 값이 같이 바뀐다.
4. 기어를 D·R 로 바꾸면 조명을 원래 값으로 되돌리고 경고 뒤 종료한다.

**기어.** Virtual Console(`adb forward tcp:8088 tcp:8088` → http://127.0.0.1:8088) 또는 `PUT /vehicle/gear?gear=Drive`(콘솔을 브라우저로 열어 두지 않았으면 `emulator_inactive` 가 돌아온다. 먼저 `POST /browser/session?client=cli` 를 보낸다). P·N 이면 사용, D·R 이면 경고 후 종료.

**상대 앱 없이.**

```sh
python3 tools/mock_phone.py --jpeg sample.jpg --sweep     # 차량 앱만: 영상 반복 + 색온도 스윕, 차의 LIGHT 출력. stdin: k 3500 | l 0.6 | p cyan | q
python3 tools/mock_car.py --save last.jpg                 # 폰 앱만: FRAME fps·크기, LIGHT 출력. 폰 에뮬레이터면 먼저 redir
```

## 3. 테스트 환경·필요한 설정

| 항목 | 값 |
|---|---|
| 차량 | Pleos Connect 에뮬레이터 2.1.0(API 34, Automotive AVD, **콜드 부팅**, RAM 4GB) + Virtual Console(기어 변경, 조명 상태 확인) |
| 폰 | Android 9(API 28) 이상. 확인한 것은 폰 에뮬레이터 API 37(`-cores 4` 로 띄운다. 기본 AVD 는 1코어라 1fps). 실기기는 미확인 |
| 빌드 | JDK 17(Temurin), compileSdk 35, minSdk 28, AGP 8.8.2, Kotlin 2.0.0, Compose. 버전은 `gradle/libs.versions.toml` 에 고정 |
| SDK | Pleos Vehicle SDK 2.3.2(조명), AAOS Car API(기어), car-ui-lib 2.6.0(다이얼) |
| 차량 앱 권한 | `pleos.car.permission.CAR_LIGHTS`(조명 조회), `pleos.car.permission.CONTROL_CAR_LIGHTS`(조명 설정. 에뮬레이터 2.1.0 에서 필요), `android.car.permission.CAR_POWERTRAIN`(기어), `INTERNET`, `ACCESS_NETWORK_STATE` |
| 폰 앱 권한 | `FOREGROUND_SERVICE_MEDIA_PROJECTION` 과 화면 공유 동의(세션마다), `POST_NOTIFICATIONS`(알림 버튼), `SYSTEM_ALERT_WINDOW`(플로팅 컨트롤. 쓸 때만 허용) |
| 포트 | 폰 8080(영상·조명 값), Virtual Console 8088 |

## 4. APK 위치

release APK 는 `apk/` 에 둔다. ⚠️ 아직 없다([../plan/work-plan.md](../plan/work-plan.md) S2 에서 만든다).

| 파일 | 앱 |
|---|---|
| `apk/belle&wise_Selene_release.apk` | 차량 앱 |
| `apk/belle&wise_SeleneMirror_release.apk` | 폰 동반 앱 |

## 5. 구성

| 모듈 | 하는 일 |
|---|---|
| `shared/` | 폰↔차 계약. `Protocol`(포트 8080, 메시지 HELLO/FRAME/LIGHT/PING/STATUS/STATE/COMMAND, 상한·주기), `MessageCodec`(type 1B \| length 4B \| payload), `LightingParams`(색온도 2700~8000K·기본 7000K·밝기·무드 색 JSON), `ColorTemp`(켈빈→ARGB, 링과 앰비언트가 같은 함수), `Scene`·`Scenes`(프리셋: 기본 4종, 적용·일치·저장 JSON), `StudioState`(앰비언트 연동·풀 라이트·다이얼 대상·차 테마·사용자 프리셋. 차가 기준)·`StudioCommand`(폰이 차에 부탁하는 프리셋 적용·저장·삭제), `Transport`(TCP·스트림), `LinkSession`(세션: HELLO 교환, 최신 FRAME 만, PING, 무수신 15초), `Aoa`(USB 액세서리 상수) |
| `ui/` | 두 앱이 같이 쓰는 디자인 시스템. `StudioTheme`(색 토큰 `StudioPalette`: 차 테마 `CarThemes` 블랙·웜 그레이지·네이비는 모두 어두운 저채도, 폰은 시스템을 따르는 라이트·다크. 컴포저블에서는 `StudioColors` 로 지금 테마의 색을 읽는다. 차/폰 글자 크기), 두 앱이 똑같이 쓰는 구역 `RingLightSection`·`PresetSection`·`CarScreenSection`(차에서는 구분선, 폰에서는 카드로 묶임), 그라데이션 슬라이더(`LevelSlider`·`KelvinSlider`: 트랙이 실제 밝기·색온도 색, 손잡이 속이 지금 빛), `StudioChip`·`MoodDots`·`Segmented`·`StatusPill`·`LightReadout`(색 견본 + "7000K · 60%" + 느낌 이름)·`StudioCard`·`StepRow`, 조명 값 라벨(`LightLabels`). Compose 를 모르는 `shared` 와 전송을 모르는 `ui` 를 나눈다 |
| `car/` | Pleos Connect 차량 앱. 기어 P·N 게이트(D·R 이면 경고 3초 후 종료, 실행 시점에도 같음), 링라이트(테두리 두께 = 밝기, 색온도/무드 색), 폰 화면 모니터, 조명 패널(프리셋 기본 4종 + 사용자 저장·삭제, 앰비언트 연동; 실내등·독서등은 프리셋으로만 바뀌고 종료 시 원래 값 복원. 원래 값은 `OriginalStore` 에 저장해 비정상 종료 뒤 다음 실행이 이어받아 복원), 풀 라이트 모드, 공유 상태 안내(공유 앱이 가려지면 오버레이, 공유가 끝나면 문구), 다이얼(`DialTarget`), 폰 연결(`PhoneLink`: USB 액세서리 → TCP[테더링 게이트웨이 → NSD → 10.0.2.2]) |
| `phone/` | 폰 동반 앱. `MirrorService`(MediaProjection → 정사각 1200px 가상 디스플레이에서 폰 화면 영역(`MirrorFit`)만 잘라 15fps JPEG → 세션), `StudioLink`(TCP 8080 서버 + USB 액세서리 세션), 차 패널과 같은 구역(링라이트·프리셋·차 화면: 차가 붙어 있으면 다이얼 대상·앰비언트 연동·풀 라이트·차 테마를 바꾸고 프리셋을 저장·삭제)·알림 버튼(따뜻하게·차갑게·중지)·플로팅 컨트롤(항상 어두운 판), 공유 상태(STATUS: 앱 가려짐·공유 끝남) 전송과 '공유가 끝났어요' 알림(탭하면 재시작), NSD `_selene._tcp` 광고. 디버그 빌드에만 `DebugReceiver`(adb 로 조명 값·JPEG 품질·한 변 변경)와 `CodecBench`(같은 그림을 JPEG 와 H.264 로 인코드·디코드해 비교) |
| `tools/` | `mock_phone.py`(가짜 폰: 차량 앱 단독 검증), `mock_car.py`(가짜 차: 폰 앱 단독 검증). 표준 라이브러리만 |

연결 방향은 **차 → 폰**(폰이 listen). 에뮬레이터 NAT 에서 바깥으로 나가는 접속만 되고, USB 테더링에서도 폰이 게이트웨이라 자연스럽다. 메시지는 전송 계층과 분리돼 있어 TCP 든 USB 액세서리든 앱 로직이 같다. 계약과 동작 규칙의 기준은 [../plan/spec.md](../plan/spec.md).

## 6. 빌드·테스트

```sh
export JAVA_HOME=$(/usr/libexec/java_home -v 17)   # Temurin 17
./gradlew assembleDebug testDebugUnitTest            # 단위 테스트 shared 49 · ui 12 · car 13 · phone 5
./gradlew assembleRelease                            # keystore.properties 가 없으면 debug 키로 서명(keystore.properties.example)
```

## 7. 확인 상태

항목별 방법과 결과, fps 측정, 개발 메모(로그 태그·디버그 리시버·adb)는 [verification.md](verification.md).

| 영역 | 상태 |
|---|---|
| 단위 테스트 | ✅ 79개 |
| 차량 앱: 조명 읽기·설정·복원(기어 D 즉시 복원, 비정상 종료 뒤 복원), 기어 게이트 | ✅ Pleos 에뮬레이터 2.1.0 |
| 미러링(전체 화면·앱 하나, 회전, 화면 잠금, 재접속), 공유 상태 안내 | ✅ 폰 에뮬레이터 ↔ Pleos 에뮬레이터 |
| 조명 값·앱 상태 양방향 동기화(슬라이더·색상환·헥사·알림 버튼·플로팅·프리셋·테마) | ✅ 같음 |
| 프레임 속도 | ⚠️ 에뮬레이터에서 카메라 프리뷰 4~6fps. 병목이 에뮬레이터라 실기기에서 다시 잰다 |
| 실기기(실제 폰), 실제 라이브 앱 | ⬜ |
| 실차 연결 경로(USB 테더링, USB 액세서리, NSD) | ⬜ |

## 8. 알려진 제한

- 영상은 JPEG 프레임(15fps, 긴 변 1200px). H.264(`MediaCodec`)는 보류: 에뮬레이터 측정([verification.md](verification.md) 'fps 측정')으로는 근거가 없고, 실기기에서 [../plan/spec.md](../plan/spec.md) 4절 'H.264 보류' 기준에 걸리면 바꾼다. 라이선스는 걸림돌이 아니다(OS 내장 코덱만 쓰고, Via LA 기기 단위 요율은 기기 제조사 몫. 앱에 인코더를 번들하지 않는다).
- 기본 안내는 "앱 하나만 공유"(11/09 결정): 촬영 앱 화면만 차에 보이고 알림·다른 앱·플로팅 컨트롤은 안 비친다. 시스템 동의 화면에서 사용자가 고르며 앱이 강제하거나 앱을 대신 골라 줄 수 없다(API 37 에뮬레이터는 이 선택이 기본으로 떠 있었다).
- 조명 값은 양쪽이 같은 값을 들고 **마지막으로 바뀐 쪽(ts)** 이 기준이다. 같은 시각이면 폰, 기본값(ts 0)은 항상 진다. 기기 간 시계 차이는 HELLO 의 `now` 로 보정한다. 양쪽 다 마지막 값을 저장한다.
- `FLAG_SECURE` 를 건 앱(DRM·금융)은 검게 캡처된다. 카메라·라이브 UI 는 보통 해당 없음(실기기 확인 필요).
- Android 14+ 는 화면 공유 동의를 세션마다 받고 토큰이 1회용이다. 끊기면 "화면 공유 시작"을 다시 누른다(공유 앱 종료로 끝났으면 알림 탭 한 번).
- "앱 하나만 공유"는 Android 동작상 그 앱이 뒤로 가면 검은 프레임이 오고 그 앱이 종료되면 공유도 끝난다. 폰이 STATUS 로 알리고 차가 이유를 띄운다.
- 실내등·독서등은 스위치 변경 리스너가 없어 상태(켜짐/꺼짐) 리스너로 근사한다. 바깥에서 AUTOMATIC 으로 바꾸면 패널에는 ON 으로 보일 수 있다.
- 앰비언트 set 은 `CONTROL_CAR_LIGHTS` 권한이 필요하다(에뮬레이터 2.1.0 확인). Playground 의 Vehicle Control 은 "준비 중".
- 다이얼 누름·nudge 는 RotaryService 가 가져가서 앱에 오지 않는다. 밝기↔색온도 전환은 패널 칩으로. 터치 후 첫 회전 1칸은 포커스 복구에 쓰인다.
- 비정상 종료(강제 종료·크래시) 뒤에는 다음 실행이 저장된 원래 값을 이어받아 되돌린다. 다음 실행이 없으면 조명은 스튜디오 값으로 남는다.
- 연결은 에뮬레이터 경로(Mac 경유 TCP)만 확인했다. 실차에서 USB 테더링·USB 액세서리·Wi-Fi 중 무엇이 열리는지는 Pleos 확인 대상이다.
