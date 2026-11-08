# HD_hollow 작업 안내

현대자동차그룹 차량용 앱 공모전 "차 안 스튜디오" 저장소. 기획은 [plan/submission-draft.md](plan/submission-draft.md), 구현 범위·계약·작업 순서는 [plan/work-plan.md](plan/work-plan.md)를 따른다.

## 코드

- Android 프로젝트는 `poc/`(Gradle 멀티모듈 `:car` 차량 앱, `:phone` 폰 앱, 추가 예정 `:shared`). JDK 17, compileSdk 35, minSdk 28, Kotlin 2.0, Compose. 버전은 `poc/gradle/libs.versions.toml`에 고정.
- 빌드·테스트: `cd poc && ./gradlew assembleDebug testDebugUnitTest`. Sprintify 컨테이너에서 돌 때만 `--offline`(네트워크 없음, 의존성은 이미지에).
- 의존성을 더하면 `.sprintify/agent.Dockerfile` 이미지를 다시 빌드해야 한다. Sprintify 에이전트로 돌 때는 더하지 말고 question 으로 묻는다.
- 단위 테스트는 JUnit 4. 코드로 검증할 수 있는 것(색 변환, JSON, 프레임 코덱, 상태 계산, 다이얼 단계, 씬)은 단위 테스트로 쓴다. 실행해 봐야 아는 것은 에뮬레이터로 확인하고 poc/README.md 의 확인 상태 표에 적는다.
- 폰↔차 계약(메시지·전송 계층)은 `poc/shared` 한 곳에만 둔다. 양쪽 앱이 같은 함수를 쓴다.

## 문서

- 파일명은 영문, 내용은 한국어. 문서 상단에 작성일·근거·상태를 둔다.
- `.sprintify/`는 Sprintify 연동용(2026-11-08 보류). 사람이 관리한다.
