# HD_hollow 작업 안내

현대자동차그룹 차량용 앱 공모전 출품작 "Selene"(셀레네, 차 안 스튜디오) 저장소. 팀명은 belle&wise. 기획서는 [plan/submission-draft.md](plan/submission-draft.md), 폰↔차 계약·동작 규칙·결정 기록은 [plan/spec.md](plan/spec.md), 남은 일·리스크·미결정은 [plan/work-plan.md](plan/work-plan.md)를 따른다. 실행 방법과 구성은 [poc/README.md](poc/README.md), 확인 결과는 [poc/verification.md](poc/verification.md).

## 코드

- Android 프로젝트는 `poc/`(Gradle 멀티모듈 `:shared` 계약, `:ui` 디자인 시스템, `:car` 차량 앱, `:phone` 폰 앱, `tools/` 모의 스크립트). JDK 17, compileSdk 35, minSdk 28, Kotlin 2.0, Compose. 버전은 `poc/gradle/libs.versions.toml`에 고정.
- 빌드·테스트: `cd poc && ./gradlew assembleDebug testDebugUnitTest`.
- 폰↔차 계약(메시지·전송 계층·색 변환)은 `poc/shared` 한 곳에만 둔다. 양쪽 앱이 같은 함수를 쓴다.
- 단위 테스트는 JUnit 4. 코드로 검증할 수 있는 것(색 변환, JSON, 프레임 코덱, 상태 계산, 다이얼 단계, 프리셋)은 단위 테스트로 쓴다. 실행해 봐야 아는 것은 에뮬레이터로 확인하고 poc/verification.md 의 표에 적는다.
- 화면의 색·글자 크기·칩·슬라이더·상태 표시·조명 값 문구는 `poc/ui`(`StudioTheme`, `StudioColors`, `LightLabels`, 공용 컴포넌트)를 쓴다. 두 앱에 같은 컴포넌트를 따로 만들거나 색을 직접 적지 않는다. 차 패널과 폰 화면에 같이 나오는 구역(링라이트·프리셋·차 화면)은 `poc/ui` 의 구역 컴포넌트 하나를 양쪽이 쓴다.
- 테마: 차는 실내 마감에 맞춘 어두운 저채도 세트(`CarThemes`: 블랙·웜 그레이지·네이비)만 쓰고 밝은 테마를 두지 않는다(차 화면 자체가 조명). 폰은 시스템 설정을 따르는 라이트·다크. 채도 있는 색은 실제 조명 색과 상태 표시에만 쓴다.
- 스튜디오 앱 상태(프리셋·앰비언트 연동·풀 라이트·다이얼 대상·차 테마)는 차가 기준이고 폰에서도 바꿀 수 있다(STATE·COMMAND). 폰에 차량 제어를 직접 넣지 않는다.
- 조명 기본값은 푸른기 도는 흰색(7000K). 실내등·독서등은 패널에서 직접 만지지 않고 프리셋으로만 바꾼다.

## 문서

- 파일명은 영문, 내용은 한국어. 문서 상단에 작성일·근거·상태를 둔다.
- 끝난 문서는 `plan/archive/` 에 있다(아이디어 후보, 초기 개발 환경 정의).
