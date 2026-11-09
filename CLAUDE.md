# HD_hollow 작업 안내

현대자동차그룹 차량용 앱 공모전 "차 안 스튜디오" 저장소. 기획은 [plan/submission-draft.md](plan/submission-draft.md), 구현 범위·계약·작업 순서는 [plan/work-plan.md](plan/work-plan.md)를 따른다. 코드 상태와 확인 결과는 [poc/README.md](poc/README.md).

## 코드

- Android 프로젝트는 `poc/`(Gradle 멀티모듈 `:shared` 계약, `:ui` 디자인 시스템, `:car` 차량 앱, `:phone` 폰 앱, `tools/` 모의 스크립트). JDK 17, compileSdk 35, minSdk 28, Kotlin 2.0, Compose. 버전은 `poc/gradle/libs.versions.toml`에 고정.
- 빌드·테스트: `cd poc && ./gradlew assembleDebug testDebugUnitTest`.
- 폰↔차 계약(메시지·전송 계층·색 변환)은 `poc/shared` 한 곳에만 둔다. 양쪽 앱이 같은 함수를 쓴다.
- 단위 테스트는 JUnit 4. 코드로 검증할 수 있는 것(색 변환, JSON, 프레임 코덱, 상태 계산, 다이얼 단계, 프리셋)은 단위 테스트로 쓴다. 실행해 봐야 아는 것은 에뮬레이터로 확인하고 poc/README.md 의 확인 상태 표에 적는다.
- 화면의 색·글자 크기·칩·슬라이더·상태 표시·조명 값 문구는 `poc/ui`(`StudioTheme`, `StudioColors`, `LightLabels`, 공용 컴포넌트)를 쓴다. 두 앱에 같은 컴포넌트를 따로 만들거나 색을 직접 적지 않는다. 테마는 항상 어두운 무채색이고 채도 있는 색은 상태 표시에만 쓴다.
- 조명 기본값은 푸른기 도는 흰색(7000K). 실내등·독서등은 패널에서 직접 만지지 않고 프리셋으로만 바꾼다.

## 문서

- 파일명은 영문, 내용은 한국어. 문서 상단에 작성일·근거·상태를 둔다.
