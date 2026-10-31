# HD_hollow

현대자동차그룹 차량용 앱 개발 공모전("Code your App into Pleos App Market") 참가 준비 레포입니다.
공모전 규정과 Pleos Connect SDK 공식 문서를 정리해 두어, 사이트를 열지 않고도 필요한 내용을 확인할 수 있게 합니다.

> 마지막 확인일: 2026-10-28  
> 접수 마감: 2026-11-15 (일) · 1차 결과: 11-23 · 2차 PT: 12-03~04 · 최종 발표: 12-18

## 폴더 안내

| 폴더 | 용도 | 언제 보나 |
|---|---|---|
| [contest/](contest/) | 대회 목표, 제약사항, 개발 환경 | 항상. 기획·개발 방향을 정할 때 |
| [sdk/](sdk/) | 공식 SDK 5종의 설명, 기능, 활용, 함수 목록 | 구현할 때 |
| [plan/](plan/) | 우리 기획: 아이디어 후보, 선정, 기획서 초안 | 기획할 때 |

### contest/ (항상 봐야 할 것)

| 파일 | 내용 |
|---|---|
| [goal.md](contest/goal.md) | 대회 목적, 공모 분야, 심사 기준 4개 + 가산점, 우리가 맞춰야 할 방향 |
| [rules.md](contest/rules.md) | 일정, 제출물 규격, 참가 규정, IP 조항, 상금 조건, 미공개 항목, 제출 체크리스트 |
| [environment.md](contest/environment.md) | 요구 버전, Playground 계정·프로젝트·SDK 신청, 에뮬레이터 2.1.0, SDK 제공 상태, 로컬 세팅 순서 |
| [forms/](contest/forms/) | 접수양식(기획서) + 개인정보 동의서 DOCX 원본 |

### plan/ (우리가 만드는 것)

| 파일 | 내용 |
|---|---|
| [ideas.md](plan/ideas.md) | 아이디어 후보 7개, SDK 매핑, 비교표, 추천 순위 |
| [dev-environment.md](plan/dev-environment.md) | 확정 개발 환경: 머신 세팅, 프로젝트 버전 고정표, 구조·규칙, 세팅 순서 |

### sdk/ (필요할 때 찾는 것)

| 파일 | 내용 |
|---|---|
| [overview.md](sdk/overview.md) | 5종 비교표, 공통 설정(Maven, 권한, 초기화 패턴), 선택 기준 |
| [vehicle/](sdk/vehicle/) | [README](sdk/vehicle/README.md) 개요 · [api/](sdk/vehicle/api/) 도메인별 함수 목록 19파일 |
| [navihelper/](sdk/navihelper/) | [README](sdk/navihelper/README.md) 개요 · [api.md](sdk/navihelper/api.md) 함수 목록 |
| [texttospeech/](sdk/texttospeech/) | [README](sdk/texttospeech/README.md) 개요 · [api.md](sdk/texttospeech/api.md) 함수 목록 |
| [speechtotext/](sdk/speechtotext/) | [README](sdk/speechtotext/README.md) 개요 · [api.md](sdk/speechtotext/api.md) 함수 목록 |
| [fusedlocation/](sdk/fusedlocation/) | [README](sdk/fusedlocation/README.md) 개요 · [api.md](sdk/fusedlocation/api.md) 함수 목록 |

## 문서 규칙

- 파일명은 영문, 내용은 한국어.
- 모든 문서는 상단 블록에 `마지막 확인일`, `기준 버전`(SDK 문서), `원본` 링크 목록을 둡니다.
- contest 문서는 원본을 번호로 매기고 본문에서 `[1]`처럼 인용합니다.
- SDK 함수 목록(api)은 행마다 공식 API Reference 페이지로 가는 `원본` 링크가 있습니다.
- 공식 문서에 있는 것만 싣습니다. LLM SDK, 비공개 도메인, 내부 클래스는 제외했습니다.
- SDK 함수 목록의 `설명` 열은 공식 KDoc 원문(영문)입니다. TextToSpeech, SpeechToText는 KDoc이 배포되지 않아 공식 가이드 내용을 한국어로 적었습니다.

## 공식 출처

| 출처 | 주소 |
|---|---|
| 공모전 사이트 | https://appdevchallenge.auto-hmg.io |
| Pleos Playground 포털 | https://pleos.ai/playground |
| Pleos 개발 문서 | https://document.pleos.ai |
| SDK Maven 저장소 | https://nexus-playground.pleos.ai/repository/maven-releases/ |
| 공식 샘플 앱 | https://github.com/Pleos-Playground |
| 문의 | appdevchallenge@hyundai-partners.com (공모전) · partnership@pleos.ai (Pleos) |

## 갱신 방법

공식 문서는 변경됩니다(SDK 전 모듈이 2026-10-19에 갱신됨). 접수 전과 2차 PT 전에 한 번씩 `마지막 확인일` 기준으로 원본을 다시 확인하고 날짜를 올립니다.
