# HD_hollow 작업 안내

현대자동차그룹 차량용 앱 공모전 "차 안 스튜디오" 저장소. 기획은 [plan/submission-draft.md](plan/submission-draft.md), 구현 범위·계약·작업 순서는 [plan/work-plan.md](plan/work-plan.md)를 따른다.

## 코드

- Android 프로젝트는 `poc/`(Gradle 멀티모듈 `:car` 차량 앱, `:phone` 폰 앱, 추가 예정 `:shared`). JDK 17, compileSdk 35, minSdk 28, Kotlin 2.0, Compose. 버전은 `poc/gradle/libs.versions.toml`에 고정.
- 빌드·테스트는 **오프라인으로**: `cd poc && ./gradlew --offline assembleDebug testDebugUnitTest`. Sprintify 컨테이너에는 바깥 네트워크가 없고 의존성은 이미지에 미리 들어 있다.
- **새 의존성을 더하지 않는다**(`libs.versions.toml`, `build.gradle.kts`의 dependencies). 이미지를 다시 빌드해야 하므로, 필요하면 question 으로 묻는다.
- 단위 테스트는 JUnit 4. 수용 기준 표식은 테스트 이름에 넣는다: `` fun `켈빈 2700 은 따뜻한 주황 [T-001/1]`() ``.
- 에뮬레이터·실기기는 없다. 실행해 봐야 아는 기준은 manual 로 두고, 코드로 검증할 수 있는 것(색 변환, JSON, 프레임 코덱, 상태 계산)은 단위 테스트로 쓴다.
- `poc/car`와 `poc/phone`은 각각 다른 사람이 맡는다. 티켓 scope 밖은 고치지 않는다.

## 문서

- 파일명은 영문, 내용은 한국어. 문서 상단에 작성일·근거·상태를 둔다.
- `.sprintify/`는 사람이 관리한다. 고치지 않는다.
