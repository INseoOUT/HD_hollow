# 개발 환경

> 마지막 확인일: 2026-10-28  
> 원본:
> 1. [공모전 개발 환경 안내](https://appdevchallenge.auto-hmg.io/guide/development)
> 2. [자주 묻는 질문](https://appdevchallenge.auto-hmg.io/faq)
> 3. [Pleos Connect Emulator 설치 및 실행](https://document.pleos.ai/docs/connect/guide/getting-started/application-development/setup-connect-sdk-emulator)
> 4. [프로젝트 생성 및 SDK 사용 신청](https://document.pleos.ai/docs/pleos-only/create-my-project)
> 5. [Pleos Connect 앱 개발 시작하기](https://document.pleos.ai/docs/pleos-only/getting-started)
> 6. [Pleos Connect Emulator 설정](https://document.pleos.ai/docs/connect/guide/getting-started/application-development/configure-pleos-connect-emulator)
> 7. [Pleos Connect Virtual Console 셋업 가이드](https://document.pleos.ai/docs/connect/guide/getting-started/application-development/use-vehicle-emulator)
> 8. [Pleos Connect Virtual Console 사용 가이드](https://document.pleos.ai/docs/pleos-only/app-test-guide/virtual-console-guide)
> 9. [Pleos Connect SDK 릴리즈 노트 (2026-10-19)](https://document.pleos.ai/docs/pleos-only/release-note/sdk-261015)
> 10. [Pleos Connect Emulator 정식 릴리즈 (2026-10-19)](https://document.pleos.ai/docs/pleos-only/release-note/emulator-261015)
> 11. [Pleos Playground 개편 (2026-10-19)](https://document.pleos.ai/docs/pleos-only/release-note/playground-261015)
> 12. [API 호환성 및 가용성](https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/api-compatibility-availability)
> 13. [Pleos Connect SDK Overview](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos)
> 14. [공모전 참가 및 출품 유의사항 안내](https://appdevchallenge.auto-hmg.io/notice/c0a1a000-2026-4923-8000-000000000006)

## 공모전이 지정한 요건

| 구분 | 요건 |
|---|---|
| Minimum Android SDK | Android API 28 이상 (차량 사이버 보안 요건: 사이닝 키 로테이션·리니지 지원) |
| Gradle | 8.0 이상 |
| 개발 언어 | Java 또는 Kotlin |
| Pleos Connect Emulator | 2.1.0 |
| Virtual Console 지원 최소 에뮬레이터 | 2.1.0 |
| 사용 가능 SDK | Vehicle, NaviHelper, TextToSpeech, SpeechToText, FusedLocation |

[1][2][3]

- 지원 범위와 버전은 공식 문서(Pleos Playground)를 기준으로 한다. [1]
- Pleos는 현대자동차그룹의 소프트웨어 중심 모빌리티 브랜드, Pleos Connect는 차량용 인포테인먼트 플랫폼, Pleos Playground는 Pleos의 개발자 포털이다. [1]
- 개발 순서(FAQ): Pleos Connect 환경을 이해하고, Pleos Playground 개발 문서로 아이디어를 기획한 뒤, Pleos Connect SDK로 개발한다. [2]
- 2차 PT 시연은 에뮬레이터 구동 화면 또는 기기 시연으로 진행한다. 에뮬레이터 등 개발 도구로 구동 검증과 보완을 진행할 수 있다. [1][14]

## Pleos Connect SDK 5종

Pleos Connect SDK는 Android Automotive OS(AAOS) 기반 인포테인먼트 앱 개발용 통합 도구이며, 아래 5개 모듈로 구성된다. [13]

| SDK | 역할 | 버전 (2026-10-19 릴리즈) | Playground 제공 상태 | 차량 모델별 호환성 |
|---|---|---|---|---|
| Vehicle SDK | 차량 상태 조회와 차량 제어 API | v2.3.2 | 상태 조회(Vehicle State): 사용 가능, 신청 불필요. 제어(Vehicle Control): 준비 중 | 모델에 따라 다름. 도메인별 `checkXXXCapabilities`로 확인 |
| NaviHelper SDK | 경로 안내 제어와 내비게이션 정보 조회 API | v3.0.5 | 사용 가능, 신청 불필요 | 내비게이션 탑재 차량에 한해 전체 호환 |
| TextToSpeech SDK | 음성 합성 API | v3.3.1 | 준비 중 (Client ID/Secret 필요) | 전체 호환 |
| SpeechToText SDK | 음성 인식 API | v3.3.1 | 준비 중 (Client ID/Secret 필요) | 전체 호환 |
| FusedLocation SDK | 차량 위치 정보 API | v1.1.3 | 준비 중 | 모델에 따라 다름. `isSupported`로 확인 |

제공 상태의 확인 출처는 공식 문서 "프로젝트 생성 및 SDK 사용 신청" 페이지(2026-10-28 확인)이며, 본인 Playground 프로젝트의 SDK 화면에서 다시 확인해야 한다. [4][9][12][13]

- Vehicle SDK API 사용 시 필요한 퍼미션을 앱에 선언해야 하며, 퍼미션은 Pleos App Market과 사전 협의 및 승인된 범위 내에서만 사용할 수 있다. 승인되지 않은 퍼미션을 선언하거나 해당 API를 호출하면 앱 심사를 통과할 수 없다. [13]
- Pleos Connect SDK API가 제공하지 않는 특수 프로퍼티가 필요한 경우, 직접 구현 전 기술 지원팀에 검토를 요청해야 한다. 차량 모델명 기준 분기 처리는 하지 않는다. [12]

## Pleos Playground 계정·프로젝트·SDK 신청

회원 가입: [4]
1. https://pleos.ai/playground 접속.
2. 우측 상단 **로그인** 선택.
3. 로그인 화면 하단 **Pleos 계정 만들기** 선택.
4. 이용약관 동의 후 계정 정보 입력.
5. 가입 완료 후 Playground 로그인.

프로젝트 생성: [4]
1. 상단 GNB에서 **Workspace** > **My Project** 선택.
2. **프로젝트 생성** 클릭.
3. 프로젝트 이름(나중에 변경 가능), 개발사명, 카테고리(앱 개발 과정에서 필수 정보이므로 먼저 정의 권장), 애플리케이션 설명 입력.
4. **생성** 클릭. 생성 후 팀 멤버 추가 등 팀 관리 기능과 SDK 사용 권한 신청을 할 수 있다.

프로젝트 정보 화면의 키 값: [4]

| 항목 | 용도 |
|---|---|
| 키 정보 > 프로젝트 ID(Key) | 앱 심사 요청 시 Pleos App Market Console에 입력 |
| SDK > 클라이언트 ID | 서버 연결을 요구하는 SDK(TextToSpeech, SpeechToText)의 앱 등록에 필요 |
| SDK > 클라이언트 시크릿 | 위와 동일 |
| 테스트 정보 > CRN | Pleos Connect Emulator 2.1.0 이후부터는 입력 불필요 |

SDK 사용 신청과 상태값: [4]
1. 프로젝트 정보 화면 좌측 사이드바에서 **SDK** 선택.
2. 하위 메뉴에서 사용할 SDK 선택.
3. 각 기능의 상태 값 확인.

| 상태 | 의미 |
|---|---|
| 준비 중 | API 제공 준비 중. 신청 버튼이 없고 `Comming Soon` 배지 표시. 신청 필요 상태로 바뀐 뒤 진행. FusedLocation, TextToSpeech, SpeechToText, Vehicle Control SDK가 해당 |
| 사용 가능 | 신청 없이 사용 가능하거나 신청이 승인된 상태. Vehicle State, NaviHelper SDK는 신청 없이 바로 사용 가능 |
| 신청 필요 | 신청해야 사용 가능한데 아직 신청하지 않은 상태. **신청** 클릭 후 정보 입력·제출. 승인 전까지 **검토 중**, 승인되면 **사용 가능**, 반려되면 **반려됨** |
| 제휴 계약 필요 | 웹 사이트 신청 불가. 서비스 계약 담당자에게 별도 문의하여 제휴 계약 후 사용 |

## 에뮬레이터 2.1.0 설치

사전 준비: Android Studio 최신 버전, Android SDK API 34, Android Emulator 시스템 요구 사항 충족. 이전 버전의 Pleos Connect Emulator가 설치되어 있다면 삭제 후 진행. [3][5]

시스템 이미지 추가: [3]
1. Android Studio **Tools** > **SDK Manager** > **SDK Update Sites** 탭 > **+** 클릭.
2. 아래 정보로 사이트 추가.
   - Name: `Pleos Connect System Image`
   - URL (Mac): `https://nexus-playground.pleos.ai/repository/raw-releases/connect-avd/2.x/crp-arm64-sys-img.xml`
   - URL (Windows/Linux): `https://nexus-playground.pleos.ai/repository/raw-releases/connect-avd/2.x/crp-sys-img.xml`
3. **OK** 후 **SDK Platforms** 탭에서 Android API Level 34의 Pleos Connect system image 체크, **Show Package Details** 활성화, **OK**로 설치.
4. 설치 완료 후 **Finish**, Android Studio 재시작.

AVD 생성: [3]
1. **Tools** > **Device Manager** > **Create device**.
2. Form Factor에서 `Automotive` 선택, `Pleos Connect v2.1.0` 선택 후 **Next**.
3. **Advanced Settings**에서 **Startup** > **Default boot**를 `Cold`로 설정. (`quick boot`로 설정하면 VHAL을 이용할 수 없다.) [6]
4. **RAM** Size를 `4 GB`로 설정.
5. **Device** 탭에서 **API** `34` 선택, `Pleos Connect v2.1.0 APIs` 선택 후 **Finish**.
6. Device Manager에서 생성한 AVD 실행.

에뮬레이터 설정 참고: [6]
- 그래픽 카드 필수. 권장 NVIDIA RTX 4060/5060 Laptop 8GB 이상 또는 AMD Radeon RX 7700S/RX 7600M XT 8GB급 이상. 최소 NVIDIA RTX 3060 Laptop 6GB 이상 또는 AMD RX 6600M 8GB / RX 6700S 8GB 이상.
- 기본 언어는 한글. 영어로 바꾸려면 **차량 설정** > **일반 설정** > **언어/Language**에서 `English` 선택.
- SpeechToText 테스트는 에뮬레이터 **Extended Controls** > **Microphone**에서 오디오 입력 수신 설정 필요.
- 이 버전의 에뮬레이터는 Bluetooth 동글 연동을 지원하지 않는다.

## Virtual Console

Pleos Connect Virtual Console은 에뮬레이터 환경에서 차량 핵심 기능을 재현하고 시뮬레이션하는 개발·테스트 도구다. 차량 상태 변경, 주행·차량 이벤트 시나리오 재현, 앱 화면의 실시간 반영 확인, 차량 제어 명령 동작 확인, 반복 테스트 및 데모 시나리오 실행이 가능하다. 에뮬레이터 2.1.0 이상이 필요하다. [7]

실행 방법: [7]
1. 터미널에서 `adb forward tcp:8088 tcp:8088` 실행.
2. PC 브라우저에서 `http://127.0.0.1:8088/` 접속.

Emulation Controls 도메인: [7][8]

| Domain | 시뮬레이션 항목 |
|---|---|
| Vehicle | 차량 알림 표시·숨김 |
| Motion | 기어(P/R/N/D), 브레이크 페달 0~100%, 주행 거리, 스티어링 각도 -540~540도, 스티어링 휠 열선 |
| ADAS | 크루즈 제어 상태, 목표 속도 |
| Convenience | ETCS 전원·시나리오, 도어 위치·잠금, 트렁크/프렁크, 실내등·독서등, 앰비언트 밝기·색상, 외부 조명, 상향등, 비상등, 제동등, 방향지시등, 창문 위치·잠금, 좌석 탑승 여부, 와이퍼, 글로브 박스 |
| Safety | 경고등(주차 브레이크, 안전벨트, ABS, TPMS 등) Off/On/Blink |
| Energy | 주행 가능 거리, 배터리 에너지·SOC·용량, 충전 전류·목표 충전량, 충전 스위치, 충전 포트 |
| Climate | 공조 전원, 에어컨, Auto, Dual, 내기순환, 풍량 0~8, 송풍 방향, 성에 제거, 목표 온도 16~30°C, 좌석 열선·통풍, 공기 청정 |

접속이 안 되면 에뮬레이터 실행 여부, 이미지 버전, `adb devices` 연결, 포트 포워딩 재실행, 8088 포트 점유 여부를 순서대로 확인한다. [7]

## 2026-10-19 릴리즈 요점

- SDK 전 모듈 버전 갱신: Vehicle v2.3.2, NaviHelper v3.0.5, TextToSpeech v3.3.1, SpeechToText v3.3.1, FusedLocation v1.1.3. Breaking change가 있으므로 각 SDK 문서의 변경 내역을 확인해야 한다. [9]
- 에뮬레이터 v2.1.0 정식 릴리즈: Virtual Console 제공, 폼팩터 테스트 기능 추가, 탑재 SDK 버전 업데이트. [10]
- 에뮬레이터에서 제거된 기능: LLM SDK 제공 종료, CRN 제공 종료, 블루투스 동글 연동 제공 종료. [10]
- Playground v2.6.0 개편: Developer Journey 가이드 추가, 문서 검색 기능 추가, 개발자 가이드를 개발 흐름 순서로 재구성. LLM SDK 제공 종료(Gleo AI 연동은 Gleo Interface 이용). [11]

## 다운로드 자료

| 자료 | 위치 | 비고 |
|---|---|---|
| Maven 저장소 | `https://nexus-playground.pleos.ai/repository/maven-releases/` | `settings.gradle.kts`의 `dependencyResolutionManagement.repositories`에 추가. 인증 불필요 |
| Vehicle SDK 의존성 | `implementation("ai.pleos.playground:Vehicle:2.3.2")` | 시작하기 문서 예제는 2.3.1, 릴리즈 노트 최신은 2.3.2 |
| 샘플 앱: Vehicle | https://github.com/Pleos-Playground/pleos-sample-vehicle-app | |
| 샘플 앱: NaviHelper | https://github.com/Pleos-Playground/pleos-sample-navi-app | |
| 샘플 앱: FusedLocation | https://github.com/Pleos-Playground/pleos-sample-fusedlocation-app | |
| 에뮬레이터 이미지 | SDK Update Sites URL (위 설치 절차) | |

[5][9]

## 로컬 세팅 순서

공식 "시작하기" 문서의 순서를 따른 것이다. [4][5]

1. Pleos Playground 가입, 프로젝트 생성, SDK 화면에서 Vehicle State·NaviHelper가 "사용 가능"인지 확인. TTS/STT를 쓸 계획이면 상태가 "신청 필요"로 바뀌는지 주기적으로 확인하고, 클라이언트 ID/시크릿 위치를 파악해 둔다. [4]
2. Android Studio 최신 버전 설치, Android SDK API 34 설치. [5]
3. SDK Update Sites에 Pleos Connect System Image 추가, API 34 이미지 설치, 재시작. [3]
4. Automotive AVD 생성 (Pleos Connect v2.1.0, Cold boot, RAM 4 GB). [3]
5. Android 프로젝트 생성: **File > New > New Project**, Templates에서 **Car** 또는 **Automotive**, **Empty Activity**, 언어 Kotlin, Minimum SDK API 28 이상. `app` 모듈 빌드 확인. [5]
6. `settings.gradle.kts`에 Maven 저장소 추가, `app/build.gradle.kts`에 SDK 의존성 추가, Sync. [5]
7. `AndroidManifest.xml`에 사용할 도메인의 퍼미션 선언 (예: `pleos.car.permission.CAR_DOORS`). [5]
8. `Vehicle(context)` 생성 → `initialize()` → `getDoor()` → `getCurrentDoorPosition(...)` 호출로 첫 API 확인. `release()`는 `onDestroy()` 등 SDK를 더 이상 쓰지 않는 시점에 호출. [5]
9. 에뮬레이터에서 실행. 값을 읽지 못하면 minSdk 28 이상, 퍼미션 선언, 의존성 동기화, 에뮬레이터 실행 상태, `onFailed` 예외 메시지를 확인. [5]
10. `adb forward tcp:8088 tcp:8088` 후 Virtual Console로 차량 상태를 바꿔 가며 앱 반응 테스트. [7]
