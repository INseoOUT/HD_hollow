# 차량 안 스튜디오 — 앱 코드

> 작성일: 2026-11-09  
> 기획: [../plan/submission-draft.md](../plan/submission-draft.md) · 계약·작업 순서: [../plan/work-plan.md](../plan/work-plan.md)  
> 상태: 1차·2차 범위를 한 번에 구현한 상태(2026-11-09, 브랜치 `feat/pleos-hyundio`). 단위 테스트는 통과했고 에뮬레이터 확인은 아래 표 참고.

폰 화면을 차 화면 가운데에 띄우고(모니터), 차 화면 가장자리와 실내 조명을 스튜디오 조명으로 쓰며, 다이얼·폰 어디서든 색온도·밝기를 맞춘다. 촬영·저장·라이브 송출은 사용자가 쓰던 폰 앱(기본 카메라·틱톡·유튜브 라이브)이 그대로 한다.

## 구성

| 모듈 | 하는 일 |
|---|---|
| `shared/` | 폰↔차 계약. `Protocol`(포트 8080, 메시지 HELLO/FRAME/LIGHT/PING, 상한·주기), `MessageCodec`(type 1B \| length 4B \| payload), `LightingParams`(색온도 2700~8000K·기본 7000K·밝기·무드 색 JSON), `ColorTemp`(켈빈→ARGB, 링과 앰비언트가 같은 함수), `Transport`(TCP·스트림), `LinkSession`(세션: HELLO 교환, 최신 FRAME 만, PING, 무수신 15초), `Aoa`(USB 액세서리 상수) |
| `car/` | Pleos Connect 차량 앱. 기어 P·N 게이트(D·R 이면 경고 3초 후 종료, 실행 시점에도 같음), 링라이트(테두리 두께 = 밝기, 색온도/무드 색), 폰 화면 모니터, 조명 패널(프리셋 기본 4종 + 사용자 저장·삭제, 앰비언트 연동; 실내등·독서등은 프리셋으로만 바뀌고 종료 시 원래 값 복원), 풀 라이트 모드, 다이얼(`DialTarget`), 폰 연결(`PhoneLink`: USB 액세서리 → TCP[테더링 게이트웨이 → NSD → 10.0.2.2]) |
| `phone/` | 폰 동반 앱. `MirrorService`(MediaProjection 화면 캡처 → 긴 변 1200px·15fps JPEG → 세션), `StudioLink`(TCP 8080 서버 + USB 액세서리 세션), 조명 조절 화면·알림 버튼(따뜻하게·차갑게·중지)·플로팅 컨트롤, NSD `_carstudio._tcp` 광고 |
| `tools/` | `mock_phone.py`(가짜 폰: 차량 앱 단독 검증), `mock_car.py`(가짜 차: 폰 앱 단독 검증). 표준 라이브러리만 |

연결 방향은 **차 → 폰**(폰이 listen). 에뮬레이터 NAT 에서 바깥으로 나가는 접속만 되고, USB 테더링에서도 폰이 게이트웨이라 자연스럽다. 메시지는 전송 계층과 분리돼 있어 TCP 든 USB 액세서리든 앱 로직이 같다.

## 빌드·테스트

```sh
export JAVA_HOME=$(/usr/libexec/java_home -v 17)   # Temurin 17
./gradlew assembleDebug testDebugUnitTest            # shared 25 · car 7 단위 테스트
./gradlew assembleRelease                            # keystore.properties 가 없으면 debug 키로 서명(keystore.properties.example)
```

## 실행

차량 에뮬레이터(Pleos Connect 2.1.0, 콜드 부팅, 기어 P)와 폰(에뮬레이터 또는 실기기)을 준비한다.

```sh
adb -s <pleos> install -r car/build/outputs/apk/debug/car-debug.apk
adb -s <phone> install -r phone/build/outputs/apk/debug/phone-debug.apk
```

**폰 → 차 경로(데모).** 차량 앱은 `10.0.2.2:8080`(에뮬레이터에서 본 Mac)으로 접속한다. 폰이 실기기면 `adb -s <phone> forward tcp:8080 tcp:8080`, 폰 에뮬레이터면 콘솔로 `redir add tcp:8080:8080`. adb 서버가 자주 죽는 Mac 에서는 콘솔 redir 이 안정적이다.

```sh
printf 'auth %s\nredir add tcp:8080:8080\nquit\n' "$(cat ~/.emulator_console_auth_token)" | nc -w 3 127.0.0.1 5554
```

폰 앱에서 **화면 공유 시작** → 동의 → 홈으로 나가 카메라·라이브 앱을 연다. 차 화면 가운데에 폰 화면이 뜨고, 다이얼·패널·폰 어디서 바꿔도 조명 값이 같이 바뀐다.

**상대 앱 없이.**

```sh
python3 tools/mock_phone.py --jpeg sample.jpg --sweep     # 차량 앱만: 영상 반복 + 색온도 스윕, 차의 LIGHT 출력. stdin: k 3500 | l 0.6 | p cyan | q
python3 tools/mock_car.py --save last.jpg                 # 폰 앱만: FRAME fps·크기, LIGHT 출력. 폰 에뮬레이터면 먼저 redir
```

**기어.** Virtual Console(`adb forward tcp:8088 tcp:8088` → http://127.0.0.1:8088) 또는 `PUT /vehicle/gear?gear=Drive`. P·N 이면 사용, D·R 이면 경고 후 종료.

## 확인 상태 (2026-11-09)

| 항목 | 방법 | 상태 |
|---|---|---|
| 메시지 코덱·세션·색 변환·조명 값 JSON | 단위 테스트 25개 | ✅ |
| 다이얼 단계·클램프, 씬 적용·일치 | 단위 테스트 7개 | ✅ |
| 차량 앱: 조명 원래 값 읽기(앰비언트 밝기 30·색 #ffffff·스위치 OFF, 실내등 OFF, 독서등 4석 OFF) | Pleos 에뮬레이터 2.1.0 + `mock_phone.py` | ✅ |
| 차량 앱: 폰 연결(HELLO 교환), 모니터에 FRAME 표시, 폰→차 LIGHT 가 링·앰비언트에 반영(색온도 스윕 추종), 차→폰 LIGHT 전송·에코 없음 | 같음 | ✅ |
| 차량 앱: 패널에서 실내등·독서등 켬, 씬(네온) 적용, 풀 라이트 모드·복귀 | 같음(adb 탭) | ✅ 실내등 ON 시 에뮬레이터가 독서등 4개도 ON 으로 보고(에뮬레이터 결합으로 추정) |
| 차량 앱: 기어 D → Pleos 가 가림 → 약 3초 뒤 앱 종료 → 앰비언트 30/#ffffff·실내등·독서등 OFF 로 복원 | Virtual Console `PUT /vehicle/gear` | ✅ |
| 폰 앱: MediaProjection 캡처·송출, 회전, 알림 버튼, 플로팅 컨트롤 | 폰 에뮬레이터/실기기 | ⬜ 미확인(빌드만) |
| 폰 앱 ↔ 차량 앱 실제 연결(에뮬레이터 둘) | 두 에뮬레이터 | ⬜ 미확인. 차 쪽은 mock_phone 으로, 폰 쪽은 mock_car 로 각각 확인할 수 있다 |
| USB 테더링 게이트웨이, NSD 탐색·광고 | 실기기·실차 | ⬜ 미확인(에뮬레이터로 불가) |
| USB 액세서리(AOA) | 실차 | ⬜ 미확인. 서드파티 앱의 USB 호스트 사용 가능 여부 자체가 Pleos 확인 대상 |
| 차량 측 CPU(정차 기준 ≤25%) | `adb shell top` | ⬜ 미확인 |

에뮬레이터 확인에서 잡은 결함: 세션의 HELLO 를 메인 스레드에서 써 `NetworkOnMainThreadException` 으로 죽었다 → 쓰기 스레드로 수정(커밋 6b058c9). 앱이 비정상 종료하면 조명 복원이 안 된다(종료 경로에서만 복원).

## 알려진 제한·단순화

- 영상은 JPEG 프레임(15fps, 긴 변 1200px). 차량 CPU 가 높으면 H.264(`MediaCodec`)로 교체한다.
- `FLAG_SECURE` 를 건 앱(DRM·금융)은 검게 캡처된다. 카메라·라이브 UI 는 보통 해당 없음(실기기 확인 필요).
- Android 14+ 는 화면 공유 동의를 세션마다 받고 토큰이 1회용이다. 끊기면 "화면 공유 시작"을 다시 누른다.
- 실내등·독서등은 스위치 변경 리스너가 없어 상태(켜짐/꺼짐) 리스너로 근사한다. 바깥에서 AUTOMATIC 으로 바꾸면 패널에는 ON 으로 보일 수 있다.
- 앰비언트 set 은 `CONTROL_CAR_LIGHTS` 권한이 필요하다(에뮬레이터 2.1.0 확인). Playground 의 Vehicle Control 은 "준비 중".
- 다이얼 누름·nudge 는 RotaryService 가 가져가서 앱에 오지 않는다. 밝기↔색온도 전환은 패널 칩으로. 터치 후 첫 회전 1칸은 포커스 복구에 쓰인다.
- 로그: `adb logcat -s StudioVehicle StudioLink PhoneLink PhoneAddress UsbHostLink DialTarget` (차), `adb logcat -s MirrorService StudioLink UsbAccessoryLink` (폰).
