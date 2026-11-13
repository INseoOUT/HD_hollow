# 개발 환경 정의

> 작성일: 2026-10-29  
> 근거: 공식 Vehicle 샘플 앱의 Gradle 설정(2026-10-29 GitHub `main` 기준), [../../contest/environment.md](../../contest/environment.md), [../../sdk/overview.md](../../sdk/overview.md), Android Studio·AGP 호환표, Gradle·Java 호환표  
> 상태: **종료(2026-11-10 보관).** '머신 환경' 절(JDK 17, Pleos 이미지, 콜드 부팅 AVD)은 지금도 맞다. 단, adb 는 platform-tools 35.0.2 를 쓴다(37.0.1 서버가 죽는다). '프로젝트 구조와 규칙'(단일 모듈 `:app`, NaviHelper, Fake 소스), '저장소·브랜치', '세팅 순서' 6~9, '확인 필요'는 실제와 다르다. 실제 구성과 실행 방법은 [../../poc/README.md](../../poc/README.md), 버전은 `poc/gradle/libs.versions.toml`, 남은 일은 [../work-plan.md](../work-plan.md). 아래는 2026-10-29 당시 내용 그대로다.

## 원칙

1. **검증된 조합을 기준선으로.** 공식 Vehicle 샘플 앱이 쓰는 Gradle·AGP·Kotlin·Compose 조합을 그대로 쓴다. Pleos SDK와 함께 동작하는 것이 확인된 유일한 조합이다. 버전을 올리는 것은 Hello World가 통과한 뒤, 필요할 때만 한다.
2. **전원 같은 버전.** 버전은 전부 파일로 고정한다(`gradle-wrapper.properties`, `gradle/libs.versions.toml`). 머신마다 다를 수 있는 것은 JDK와 Android Studio뿐이며 아래 표대로 맞춘다.
3. **컨테이너는 쓰지 않는다.** Mac 로컬에 Android Studio와 Pleos 에뮬레이터를 직접 설치한다. 이유는 Docker 컨테이너에 `/dev/kvm`이 없어 에뮬레이터를 돌릴 수 없고, 빌드만 컨테이너로 돌리는 것은 18일 일정에서 이득이 작기 때문이다.
4. **AS 새 프로젝트 템플릿을 쓰지 않는다.** Android Studio 2026.2의 템플릿은 AGP 9.x·Kotlin 2.3을 깔아 기준선과 어긋난다. 샘플의 Gradle 파일을 복사해서 시작한다.

## 머신 환경

이 Mac(2026-10-29 확인)의 현재 상태와 맞춰야 할 목표다. 팀원도 같은 목표에 맞춘다.

| 항목 | 현재 | 목표 | 할 일 |
|---|---|---|---|
| OS · 칩 | macOS 27.0.1 · Apple M6 (arm64) | 그대로 | - |
| Android Studio | 2026.2.1 (Rabbit 1). AGP 7.1~9.4 지원 | 2026.2.x | 팀원도 2026.x 설치 |
| Gradle JDK | 번들 JBR 25만 있음. **Gradle 8.10.2는 JDK 25에서 안 돈다**(JDK 25는 Gradle 9.1부터) | **Temurin 17** | `brew install --cask temurin@17` 후 AS **Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK**를 17로. 셸에 `JAVA_HOME` |
| SDK Platform | android-37만 설치 | **API 35**(compileSdk) + **API 34**(에뮬레이터 이미지) | SDK Manager에서 Android 15 (API 35) 설치. AGP가 자동 설치하기도 하지만 미리 깐다 |
| SDK Build-Tools | 36.0.0 | AGP 8.8.2 기본값 35.0.0 | AGP가 자동 설치 |
| Android Emulator | 37.2.12 | 유지 | - |
| Platform-Tools (adb) | 37.0.1. PATH에 없음 | 유지, PATH 추가 | 셸에 `ANDROID_HOME`, `PATH` |
| Pleos 시스템 이미지 | 없음 | **Pleos Connect v2.1.0 APIs, API 34, arm64-v8a** (4.7 GB) | SDK Update Sites에 `https://nexus-playground.pleos.ai/repository/raw-releases/connect-avd/2.x/crp-arm64-sys-img.xml` 추가 → SDK Platforms에서 설치 → AS 재시작 |
| AVD | 없음 | Automotive · Pleos Connect v2.1.0 · **Cold boot** · RAM **4 GB** | Device Manager. Quick boot면 VHAL이 안 된다 |
| Homebrew | 7.0.11 | 유지 | - |
| git | 2.55.0 | 유지 | - |
| GitHub CLI | 없음 | 선택 | `brew install gh` |

셸 설정(`~/.zshrc`):

```sh
export JAVA_HOME="$(/usr/libexec/java_home -v 17)"
export ANDROID_HOME="$HOME/Library/Android/sdk"
export PATH="$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator"
```

Windows·Linux 팀원은 x86_64 이미지 URL(`crp-sys-img.xml`, 4.1 GB)을 쓰고, 공식 문서의 GPU 요건(최소 RTX 3060 Laptop 6 GB급)을 확인한다. [../../contest/environment.md](../../contest/environment.md#에뮬레이터-210-설치)

## 프로젝트 버전

`gradle/libs.versions.toml`과 `gradle-wrapper.properties`에 고정한다.

| 항목 | 값 | 근거 |
|---|---|---|
| Gradle wrapper | **8.10.2** | 샘플. JDK 17~23에서 동작 |
| AGP | **8.8.2** | 샘플. AS 2026.2 지원 범위(7.1~9.4) 안. 최소 JDK 17 |
| Kotlin | **2.0.0** | 샘플. Compose 컴파일러는 `org.jetbrains.kotlin.plugin.compose` 같은 버전 |
| JDK · jvmTarget | **17** | AGP 최소값, 샘플과 동일 |
| compileSdk · targetSdk | **35** | 샘플 |
| minSdk | **28** | Playground 요구(서명 키 로테이션·lineage). 샘플은 Vehicle 23, Navi 33이지만 공식 요건은 28 |
| Compose BOM | **2024.04.01** | 샘플 |
| Material3 | BOM 버전 | 샘플 |
| androidx core-ktx · lifecycle · activity-compose | 1.16.0 · 2.9.0 · 1.10.1 | 샘플 |
| kotlinx-coroutines-android | **1.8.1** | SDK 콜백을 `callbackFlow`로 감싸기 위해 명시 |
| Vehicle SDK | **2.3.2** | 릴리즈 노트 최신. 샘플 toml은 2.3.1이라 올린다 |
| NaviHelper SDK | **3.0.5** | 릴리즈 노트 최신, Navi 샘플과 동일 |
| TextToSpeech · SpeechToText · FusedLocation | **의존성 추가 안 함** | 준비 중. 상태가 바뀌면 3.3.1 · 3.3.1 · 1.1.3 추가 |
| DI | **없음 (수동 조립)** | Navi 샘플의 Hilt+kapt는 Kotlin 1.9 기준이고 18일 일정에 불필요 |
| 단위 테스트 | JUnit 4.13.2 · kotlinx-coroutines-test 1.8.1 | 도메인 로직(지수 계산, 예측식) 대상 |
| 계측 테스트 | androidx.test.ext:junit 1.2.1 · espresso 3.6.1 | 샘플 기본값. 적극적으로 쓰지는 않음 |
| 코드 스타일 | AS 기본 Kotlin 스타일 + `.editorconfig` | ktlint·detekt는 넣지 않음 |

`settings.gradle.kts`는 샘플을 그대로 쓴다. `pluginManagement`의 Google 저장소 content filter, `RepositoriesMode.FAIL_ON_PROJECT_REPOS`, Pleos Maven(`https://nexus-playground.pleos.ai/repository/maven-releases/`, 인증 불필요)이 들어 있다.

## 프로젝트 구조와 규칙

- **단일 모듈 `:app`.** 모듈 분리는 하지 않는다.
- **패키지.** `<base>.sdk`(Vehicle·NaviHelper 래퍼, `callbackFlow`, Fake 구현) · `<base>.domain`(순수 Kotlin 로직, 단위 테스트 대상) · `<base>.ui`(Compose 화면·ViewModel) · `<base>.di`(수동 조립).
- **SDK 래퍼.** `VehicleSource`, `NaviSource` 인터페이스를 두고 `Real`과 `Fake` 두 구현을 만든다. Fake는 에뮬레이터가 주입하지 못하는 값(예: 속도)과 PT 데모 시나리오 재생에 쓴다. 디버그 메뉴에서 런타임 전환하며, 빌드 플레이버는 만들지 않는다.
- **생명주기.** `initialize()`는 Application 또는 최상위 ViewModel에서 한 번, `release()`는 종료 시. 콜백이 끝나기 전 `release()` 금지. [../../sdk/overview.md](../../sdk/overview.md#공통-패턴)
- **매니페스트 권한.** 실제 호출하는 API의 권한만 선언한다. 권한은 각 도메인 api 파일의 권한 열 기준. 샘플처럼 전체 권한을 복사하면 앱 심사에서 탈락한다.
- **속도.** Pleos SDK에 없다. 필요하면 AAOS `CarPropertyManager`(`useLibrary("android.car")`)로 읽되, 에뮬레이터에서 주입 가능한지 확인한 뒤 결정한다.
- **UI.** Compose + Material3. 차량 디스플레이 가로 화면 기준. 주행 중 화면은 글자 크게, 터치 타깃 크게, 한 화면에 정보 3개 이하.

## 저장소·브랜치·서명·산출물

- **앱 저장소는 별도로 만든다.** 이름은 앱 이름. 이 저장소(HD_hollow)는 문서·기획을 유지하고 앱 저장소 README에서 링크한다. 이유: 심사용 README 요건(앱 소개, 실행 방법, 테스트 환경, APK 위치)이 루트 README에 있어야 하고, Android Studio가 루트를 바로 열어야 하며, 비공개 저장소라면 심사용 계정 Collaborator 초대 범위를 앱만으로 좁힐 수 있다.
- **브랜치.** `main` + `feat/*`. PR로 머지하되 셀프 머지 허용. 커밋 메시지는 한국어, 이 저장소 관례를 따른다.
- **서명.** release keystore 1개를 만들어 팀 비밀 채널로 공유한다. `keystore.properties`와 `*.jks`는 `.gitignore`. `signingConfigs.release`는 properties 파일이 없으면 debug 키로 fallback해서 팀원·CI 빌드가 깨지지 않게 한다.
- **APK.** `apk/팀명_앱이름_release.apk`. `isMinifyEnabled = false` 유지(심사 측 실행 안정성이 용량보다 중요). GitHub 100 MB를 넘으면 GitHub Release에 올리고 링크 제출. [../../contest/rules.md](../../contest/rules.md#github-저장소-제출-규격)
- **CI.** 선택. 여유가 생기면 GitHub Actions(ubuntu, temurin 17)로 `assembleRelease`를 돌려 APK를 아티팩트로 남긴다. Pleos Maven이 익명이라 추가 설정 없이 된다.

## 세팅 순서

1. [ ] `brew install --cask temurin@17`. `~/.zshrc`에 위 셸 설정 추가 후 `java -version`이 17, `adb version`이 뜨는지 확인.
2. [ ] Android Studio: Gradle JDK를 17로. SDK Manager에서 Android 15 (API 35) 플랫폼 설치.
3. [ ] SDK Update Sites에 Pleos 이미지 사이트(arm64) 추가 → SDK Platforms에서 API 34의 Pleos Connect 이미지 설치 → AS 재시작.
4. [ ] AVD 생성(Automotive, Pleos Connect v2.1.0, Cold boot, 4 GB) → 부팅 확인 → `adb forward tcp:8088 tcp:8088` → `http://127.0.0.1:8088/` Virtual Console 접속 확인.
5. [ ] Pleos Playground 가입 → 프로젝트 생성 → SDK 화면에서 Vehicle State·NaviHelper "사용 가능" 확인, 나머지 상태 기록.
6. [ ] 앱 저장소 생성 → Vehicle 샘플의 `gradle/wrapper/*`, `gradle/libs.versions.toml`, `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts` 복사 → 위 버전 표대로 조정(minSdk 28, Vehicle 2.3.2, NaviHelper·coroutines 추가, 패키지명) → 빈 Compose 화면 → 빌드 통과.
7. [ ] Hello World: `Vehicle.initialize()` → `getDoor()` → 값 표시. `NaviHelper.initialize()` → `getDestinationInfo()`. Virtual Console로 값을 바꿔 화면 반영 확인.
8. [ ] 확인 항목 기록(아래) → [ideas.md](ideas.md) 후보 확정에 반영.
9. [ ] keystore 생성 → release 빌드 1회 → `apk/` 규격으로 저장해 전 과정 리허설.

## 확인 필요

Hello World 단계에서 답을 내고 이 문서에 결과를 적는다.

- [ ] 에뮬레이터 내비에서 `onCurrentRoadSpeedLimit`, `onCautionPointInfo`, `onTBTInfo`가 실제로 들어오는가. 안 들어오면 Fake로 대체.
- [ ] 차량 속도를 에뮬레이터에 주입할 수 있는가(VHAL `PERF_VEHICLE_SPEED`). Virtual Console 항목에는 속도가 없다.
- [ ] Virtual Console로 바꿀 수 있는 항목이 공식 표와 같은가(조향각, 브레이크, 좌석 탑승, 공조, SOC, 타이어 경고등).
- [ ] 팀명·앱 이름(APK 파일명과 Playground 프로젝트명에 필요).
- [ ] Windows 팀원이 있다면 GPU 요건 충족 여부.
