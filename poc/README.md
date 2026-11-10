# 차량 안 스튜디오 — 앱 코드

> 작성일: 2026-11-09  
> 기획: [../plan/submission-draft.md](../plan/submission-draft.md) · 계약·작업 순서: [../plan/work-plan.md](../plan/work-plan.md)  
> 상태: 1차·2차 범위를 한 번에 구현한 상태(2026-11-09, 브랜치 `feat/pleos-hyundio`). 단위 테스트는 통과했고 에뮬레이터 확인은 아래 표 참고.  
> 2026-11-10 브랜치 `design/hyundio`: 화면 디자인 개편(`ui/` 모듈). 차 패널·모니터 안내·폰 화면을 같은 디자인 시스템으로 다시 그림.  
> 같은 날 추가: 테마(차는 실내 마감에 맞춘 어두운 세트 3개, 폰은 시스템 라이트·다크), 차 패널과 폰 화면이 같은 구역 컴포넌트를 쓰고, 스튜디오 앱 상태(프리셋·앰비언트 연동·풀 라이트·다이얼 대상·차 테마)를 양쪽에서 바꾼다(계약 STATE 0x06·COMMAND 0x07).

폰 화면을 차 화면 가운데에 띄우고(모니터), 차 화면 가장자리와 실내 조명을 스튜디오 조명으로 쓰며, 다이얼·폰 어디서든 색온도·밝기를 맞춘다. 촬영·저장·라이브 송출은 사용자가 쓰던 폰 앱(기본 카메라·틱톡·유튜브 라이브)이 그대로 한다.

## 구성

| 모듈 | 하는 일 |
|---|---|
| `shared/` | 폰↔차 계약. `Protocol`(포트 8080, 메시지 HELLO/FRAME/LIGHT/PING/STATUS/STATE/COMMAND, 상한·주기), `MessageCodec`(type 1B \| length 4B \| payload), `LightingParams`(색온도 2700~8000K·기본 7000K·밝기·무드 색 JSON), `ColorTemp`(켈빈→ARGB, 링과 앰비언트가 같은 함수), `Scene`·`Scenes`(프리셋: 기본 4종, 적용·일치·저장 JSON), `StudioState`(앰비언트 연동·풀 라이트·다이얼 대상·차 테마·사용자 프리셋. 차가 기준)·`StudioCommand`(폰이 차에 부탁하는 프리셋 적용·저장·삭제), `Transport`(TCP·스트림), `LinkSession`(세션: HELLO 교환, 최신 FRAME 만, PING, 무수신 15초), `Aoa`(USB 액세서리 상수) |
| `ui/` | 두 앱이 같이 쓰는 디자인 시스템. `StudioTheme`(색 토큰 `StudioPalette`: 차 테마 `CarThemes` 블랙·웜 그레이지·네이비는 모두 어두운 저채도, 폰은 시스템을 따르는 라이트·다크. 컴포저블에서는 `StudioColors` 로 지금 테마의 색을 읽는다. 차/폰 글자 크기), 두 앱이 똑같이 쓰는 구역 `RingLightSection`·`PresetSection`·`CarScreenSection`(차에서는 구분선, 폰에서는 카드로 묶임), 그라데이션 슬라이더(`LevelSlider`·`KelvinSlider`: 트랙이 실제 밝기·색온도 색, 손잡이 속이 지금 빛), `StudioChip`·`MoodDots`·`Segmented`·`StatusPill`·`LightReadout`(색 견본 + "7000K · 60%" + 느낌 이름)·`StudioCard`·`StepRow`, 조명 값 라벨(`LightLabels`). Compose 를 모르는 `shared` 와 전송을 모르는 `ui` 를 나눈다 |
| `car/` | Pleos Connect 차량 앱. 기어 P·N 게이트(D·R 이면 경고 3초 후 종료, 실행 시점에도 같음), 링라이트(테두리 두께 = 밝기, 색온도/무드 색), 폰 화면 모니터, 조명 패널(프리셋 기본 4종 + 사용자 저장·삭제, 앰비언트 연동; 실내등·독서등은 프리셋으로만 바뀌고 종료 시 원래 값 복원. 원래 값은 `OriginalStore` 에 저장해 비정상 종료 뒤 다음 실행이 이어받아 복원), 풀 라이트 모드, 공유 상태 안내(공유 앱이 가려지면 오버레이, 공유가 끝나면 문구), 다이얼(`DialTarget`), 폰 연결(`PhoneLink`: USB 액세서리 → TCP[테더링 게이트웨이 → NSD → 10.0.2.2]) |
| `phone/` | 폰 동반 앱. `MirrorService`(MediaProjection → 정사각 1200px 가상 디스플레이에서 폰 화면 영역(`MirrorFit`)만 잘라 15fps JPEG → 세션), `StudioLink`(TCP 8080 서버 + USB 액세서리 세션), 차 패널과 같은 구역(링라이트·프리셋·차 화면: 차가 붙어 있으면 다이얼 대상·앰비언트 연동·풀 라이트·차 테마를 바꾸고 프리셋을 저장·삭제)·알림 버튼(따뜻하게·차갑게·중지)·플로팅 컨트롤(항상 어두운 판), 공유 상태(STATUS: 앱 가려짐·공유 끝남) 전송과 '공유가 끝났어요' 알림(탭하면 재시작), NSD `_carstudio._tcp` 광고. 디버그 빌드에만 `DebugReceiver`(adb 로 조명 값·JPEG 품질·한 변 변경)와 `CodecBench`(같은 그림을 JPEG 와 H.264 로 인코드·디코드해 비교) |
| `tools/` | `mock_phone.py`(가짜 폰: 차량 앱 단독 검증), `mock_car.py`(가짜 차: 폰 앱 단독 검증). 표준 라이브러리만 |

연결 방향은 **차 → 폰**(폰이 listen). 에뮬레이터 NAT 에서 바깥으로 나가는 접속만 되고, USB 테더링에서도 폰이 게이트웨이라 자연스럽다. 메시지는 전송 계층과 분리돼 있어 TCP 든 USB 액세서리든 앱 로직이 같다.

## 빌드·테스트

```sh
export JAVA_HOME=$(/usr/libexec/java_home -v 17)   # Temurin 17
./gradlew assembleDebug testDebugUnitTest            # 단위 테스트 shared 44 · ui 7 · car 13 · phone 4
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
| 메시지 코덱·세션(시계 보정·닫기 전 큐 비우기, STATE·COMMAND 전달)·색 변환·조명 값 JSON·최신 값 규칙·공유 상태 JSON·프리셋(적용·일치·이름·JSON)·앱 상태·명령 JSON | 단위 테스트 44개 | ✅ |
| 테마: 차 테마가 계약 id 마다 하나, 모두 어둡고 저채도, 모든 테마(폰 라이트 포함)에서 글자 대비, 조명 값 라벨 | 단위 테스트 7개 | ✅ |
| 다이얼 단계·클램프, 프리셋의 차 적용(기본은 실내등·독서등 끔, 사용자는 안 건드림), 앱 상태 만들기·받기, 조명 ts, 원래 값 JSON | 단위 테스트 13개 | ✅ |
| 가상 디스플레이 안 화면 영역 계산(세로·가로·한 변) | 단위 테스트 4개 | ✅ |
| 차량 앱: 조명 원래 값 읽기(앰비언트 밝기·색·스위치, 실내등, 독서등 4석) | Pleos 에뮬레이터 2.1.0 | ✅ |
| 차량 앱: 기어 D → **즉시 조명 복원**(0.7초 안에 앰비언트 #ffffff·60 확인) → 약 3초 뒤 종료 | Virtual Console `PUT /vehicle/gear` + `/vehicle/state` | ✅ |
| 차량 앱: **비정상 종료 복원** — 강제 종료로 앰비언트 #00e5ff 가 남음 → 재실행이 저장된 원래 값을 이어받아 #ffffff 로 되돌림(로그 `이어받음`·`되돌림`) → 기어 D 에서 원래 값 복원, 저장소 비움 → 다음 실행은 이어받을 것 없음 | `am force-stop` + Virtual Console + `run-as cat shared_prefs` | ✅ |
| 차량 앱: 프리셋 적용, 풀 라이트·복귀, 패널 조작 | adb 탭 | ✅ |
| **폰 앱 → 차량 앱 미러링**(MediaProjection 전체 화면·앱 하나) | 폰 에뮬레이터(API 37) ↔ Pleos 에뮬레이터, Mac 경유 redir | ✅ 카메라 앱 프리뷰가 차 모니터에 뜸. "앱 하나만 공유"면 플로팅 컨트롤·다른 앱은 차에 안 비침 |
| **상태 동기화** 차→폰(프리셋 탭 → 폰 슬라이더 85%), 폰→차(무드 색·색온도 슬라이더 → 앰비언트 #00e5ff·2700K), 알림 버튼(차갑게 +300K), 플로팅(밝게 +10%·차갑게) | 같음 + Virtual Console 상태 | ✅ 에코 없음 |
| 회전 | 콘솔 `rotate` 4회 | ✅ 가상 디스플레이는 1200×1200 그대로, 내용 영역 (330,0,540×1200) ↔ (0,330,1200×540) 전환. 가로에서 차 모니터를 폭 가득 채움(이전의 '작게 보임' 재발 없음) |
| 화면 잠금(전원 버튼) 중 송출 | 폰 keyevent POWER | ✅ 끊기지 않음 |
| 재접속: 폰 중지 → 차 대기 → 폰 재시작(동의 다시) → 재연결. 차 앱 강제 종료 → 재실행 → 재연결 | 같음 | ✅ |
| **재접속 때 값 기준 = 마지막으로 바뀐 쪽**: 차가 시안을 저장한 채 죽고 폰에서 블루로 바꾼 뒤 차 재실행 → 차가 블루 채택, 폰 값 유지. 정상 종료 뒤 재실행은 저장값(블루) 복원, 폰과 같은 ts 라 충돌 없음 | 같음 | ✅ |
| 플로팅 컨트롤 폭 190dp 고정 | 폰 스크린샷 | ✅ '따뜻하게' 줄바꿈 없음 |
| **공유 상태 안내**: 카메라 앱 하나 공유 중 HOME → 차 모니터에 "공유한 앱이 폰에서 가려져 있습니다" 오버레이, 카메라 복귀 → 사라짐. 카메라 종료 → 공유 종료 → 차 "폰 화면 공유가 끝났습니다", 폰 알림 "화면 공유가 끝났어요" → 탭 → 바로 동의 화면 | 폰 HOME·`am force-stop com.android.camera2`, 차 UI 덤프, `dumpsys notification` | ✅ 끝남 STATUS 가 소켓이 닫히기 전에 도착 |
| **디자인 개편**(2026-11-10): 차 패널(이름·연결 상태 알약, 색 견본 + "7000K · 70%" + 느낌 이름, 색 점 달린 프리셋 칩, 트랙이 실제 빛 색인 밝기·색온도 슬라이더, 다이얼 분할 버튼, 풀 라이트 버튼), 모니터 안내(폰 윤곽 + 상태 문장 + 순서 알약), 풀 라이트 안내 알약, 폰 화면(상태 카드 + 큰 시작/중지 버튼, 사용 순서 카드, 조명 카드, 플로팅 행) | Pleos 에뮬레이터(2560×1440·160dpi, 앱 영역에 스크롤 없이 들어감) + 폰 에뮬레이터(1080×2400·420dpi) 스크린샷. 대기 → 데일리 프리셋 → 풀 라이트·복귀 → 전체 화면 공유 → 차 "송출 중"·폰 "차량 연결됨" → 차 '웜 뷰티' 칩 탭 → 폰 3200K·85% '따뜻한 전구색' | ✅ 크래시 없음. 폰 상태 바는 엣지 투 엣지(`enableEdgeToEdge`)로 어두운 배경 위 밝은 아이콘 |
| **테마**(2026-11-10): 차 블랙·웜 그레이지·네이비(바탕·패널·선의 색조만 다르고 모두 어두움), 폰은 시스템 라이트·다크를 따름(라이트에서 선택 칩은 검은 바탕·흰 글자, 색 점·슬라이더에 어두운 테두리). 차 테마는 다음 실행에도 유지 | 두 에뮬레이터 스크린샷, 폰 `cmd uimode night yes\|no` | ✅ |
| **앱 상태 양방향**(2026-11-10): 폰에서 테마 네이비·다이얼 색온도·프리셋 '쿨 시네마'·'지금 값 저장' → 차 패널에 반영되고 '내 프리셋 1' 이 양쪽 목록에 뜸(차 로그 `폰 명령 scene.apply`·`scene.save`). 차에서 테마 웜 그레이지·'웜 뷰티'·풀 라이트 켬 → 폰 화면에 반영. 폰에서 풀 라이트 끔 → 차 복귀. 차가 떨어져 있으면 폰의 '차 화면' 구역은 흐리게 잠기고 프리셋은 고르기만 됨 | 같음 + adb 탭·디버그 리시버(`--es theme greige`) | ✅ 에코 없음 |
| 디버그 리시버(탭 좌표 없이 조명 값·프리셋 변경) | `am broadcast -n …/.DebugReceiver` | ✅ 차 '웜 뷰티' → 85%·3200K·앰비언트 #ffb87b, 폰 3300K·40% → 차 반영. 릴리스 APK 에는 리시버 없음 |
| fps 단계별 시간(폰 복사·크롭/압축/전송, 차 디코드) | logcat 1초 평균, 폰 에뮬레이터 `-cores 4` | ⚠️ 아래 'fps 측정' 표. 홈 1~2fps(바뀔 때만), 스톱워치 7~8fps, 카메라 프리뷰 4~6fps. 전날의 1fps(복사 0.4~1.6초·압축 0.8~3.3초)는 폰 에뮬레이터가 1코어였기 때문. 에뮬레이터 수치라 실기기에서 다시 읽어야 한다 |
| 코덱 벤치: 같은 그림을 JPEG q60 과 H.264(`MediaCodec`, 소프트웨어 인코더)로, 540×1200 | 디버그 `CodecBench` 100장 | ✅ 합성 그림: JPEG 인코드 13~15ms·디코드 6ms·94KB, H.264 인코드 32~35ms·디코드 8~9ms·9KB(4Mbps). 카메라 화면: JPEG 8ms·3ms·12KB, H.264 30ms·8ms·1KB. 에뮬레이터엔 하드웨어 인코더가 없다 |
| 차량 측 CPU | `adb shell top` | 카메라 프리뷰 전체 화면 공유(4~6fps) 중 차 앱 36%(코어 하나 기준, 4코어의 9%). 폰 앱 30%, 에뮬레이터 카메라 HAL 53% |
| USB 테더링 게이트웨이, NSD 탐색·광고 | 실기기·실차 | ⬜ 미확인(에뮬레이터로 불가) |
| USB 액세서리(AOA) | 실차 | ⬜ 미확인. 서드파티 앱의 USB 호스트 사용 가능 여부 자체가 Pleos 확인 대상 |

에뮬레이터 확인에서 잡은 결함: 세션의 HELLO 를 메인 스레드에서 써 `NetworkOnMainThreadException` 으로 죽었다 → 쓰기 스레드로 수정(커밋 6b058c9).
비정상 종료(강제 종료·크래시) 뒤에는 다음 실행이 저장된 원래 값을 이어받아 되돌린다. 다음 실행이 없으면 조명은 스튜디오 값으로 남는다(남은 한계).

### fps 측정 (2026-11-09, 폰 에뮬레이터 API 37 `-cores 4` ↔ Pleos 에뮬레이터 4코어, Apple M6)

| 장면 | 폰 fps | 복사·크롭 | 압축 | 전송 | 프레임 | 차 디코드 |
|---|---|---|---|---|---|---|
| 홈 화면(정지), 전체 화면 공유 | 1~2(바뀔 때만) | 6ms | 10ms | 0ms | 26KB | 5~10ms |
| 스톱워치, 전체 화면 | 7~8 | 20~50ms | 25~80ms | 0~5ms | 24KB | 20~56ms |
| 카메라 프리뷰, 전체 화면 | 2~6 | 30~280ms | 35~370ms | 0~28ms | 11~13KB | 19~104ms |
| 카메라 프리뷰, 전체 화면, JPEG 품질 35 | 4~6 | 24~130ms | 45~125ms | 0~7ms | 10KB | 18~66ms |
| 카메라 앱 하나만 공유 | 4~6 | 26~180ms | 31~184ms | 0~37ms | 11~12KB | 13~50ms |
| 카메라 앱 하나만, 한 변 720(324×720) | 4~7 | 4~62ms | 17~80ms | 0~20ms | 6KB | 6~26ms |

읽기: 해상도를 2.8배 줄이거나 품질을 60→35 로 내려도 fps 가 그대로다. 병목은 JPEG 가 아니라 에뮬레이터의 가상 디스플레이 합성·GPU 버퍼 읽기와 에뮬레이터 카메라 HAL 과의 CPU 경쟁이다(같은 그림을 `CodecBench` 로 돌리면 압축 8~15ms 인데 송출 중에는 35~370ms). 540×1200 JPEG 는 이 CPU 에서 인코드 8~15ms·디코드 3~6ms 라 15fps 에 코어 하나의 1/4 이하. 소프트웨어 H.264 는 인코드가 JPEG 의 2.3배 느리고 크기만 10배 작다. H.264 가 유리한 건 하드웨어 인코더가 있는 실기기뿐이라 결정은 실기기 수치로 한다: 폰 한 대 연결 뒤 `adb logcat -s MirrorService` 1분. 기준은 work-plan C16(폰 압축+복사 30ms 초과, 차 디코드 CPU 25% 초과, 10fps 미만 중 하나).

## 알려진 제한·단순화

- 영상은 JPEG 프레임(15fps, 긴 변 1200px). H.264(`MediaCodec`)는 보류: 에뮬레이터 측정(위 'fps 측정')으로는 근거가 없고, 실기기에서 work-plan C16 기준에 걸리면 바꾼다. 라이선스는 걸림돌이 아니다(OS 내장 코덱만 쓰고, Via LA 기기 단위 요율은 기기 제조사 몫. 앱에 인코더를 번들하지 않는다).
- 기본 안내는 "앱 하나만 공유"(11/09 결정): 촬영 앱 화면만 차에 보이고 알림·다른 앱·플로팅 컨트롤은 안 비친다. 시스템 동의 화면에서 사용자가 고르며 앱이 강제하거나 앱을 대신 골라 줄 수 없다(API 37 에뮬레이터는 이 선택이 기본으로 떠 있었다).
- 조명 값은 양쪽이 같은 값을 들고 **마지막으로 바뀐 쪽(ts)** 이 기준이다. 같은 시각이면 폰, 기본값(ts 0)은 항상 진다. 기기 간 시계 차이는 HELLO 의 `now` 로 보정한다. 양쪽 다 마지막 값을 저장한다.
- `FLAG_SECURE` 를 건 앱(DRM·금융)은 검게 캡처된다. 카메라·라이브 UI 는 보통 해당 없음(실기기 확인 필요).
- Android 14+ 는 화면 공유 동의를 세션마다 받고 토큰이 1회용이다. 끊기면 "화면 공유 시작"을 다시 누른다(공유 앱 종료로 끝났으면 알림 탭 한 번).
- "앱 하나만 공유"는 Android 동작상 그 앱이 뒤로 가면 검은 프레임이 오고 그 앱이 종료되면 공유도 끝난다. 폰이 STATUS 로 알리고 차가 이유를 띄운다.
- 이 Mac 의 adb 37.0.1 서버는 몇 초마다 죽는다. platform-tools 35.0.2 를 따로 받아 그 adb 로 서버를 띄우면 안정적이었다(2026-11-09). 클라이언트와 서버 버전이 다르면 서로 죽이므로 한 버전만 쓴다.
- 실내등·독서등은 스위치 변경 리스너가 없어 상태(켜짐/꺼짐) 리스너로 근사한다. 바깥에서 AUTOMATIC 으로 바꾸면 패널에는 ON 으로 보일 수 있다.
- 앰비언트 set 은 `CONTROL_CAR_LIGHTS` 권한이 필요하다(에뮬레이터 2.1.0 확인). Playground 의 Vehicle Control 은 "준비 중".
- 다이얼 누름·nudge 는 RotaryService 가 가져가서 앱에 오지 않는다. 밝기↔색온도 전환은 패널 칩으로. 터치 후 첫 회전 1칸은 포커스 복구에 쓰인다.
- 로그: `adb logcat -s StudioVehicle StudioLink PhoneLink PhoneAddress UsbHostLink DialTarget DebugReceiver` (차), `adb logcat -s MirrorService StudioLink UsbAccessoryLink DebugReceiver` (폰). 폰은 1초마다 `Nfps 복사·크롭 Xms 압축 Yms 전송 Zms`, 차는 `디코드 N장 평균 Xms` 를 찍는다.
- 디버그 빌드 자동화: `adb shell am broadcast -n com.hdhollow.studio.phone/.DebugReceiver --ei kelvin 3300 --ef level 0.9 --es preset cyan --ez floating true`, `adb shell "am broadcast -n com.hdhollow.studio.car/.DebugReceiver --es scene '웜 뷰티' --ez full true"`. fps 측정 손잡이(폰): `--ei quality 35`(JPEG 품질, 즉시) `--ei side 720`(다음 공유부터) `--es bench <앱 외부 파일 디렉터리의 그림>` [`--ei n 100 --es dec c2.android.avc.decoder`] → `logcat -s CodecBench`. 폰 에뮬레이터는 `-cores 4` 로 띄워야 수치가 의미 있다(기본 AVD 는 1코어).
