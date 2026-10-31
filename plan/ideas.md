# 아이디어 후보

> 작성일: 2026-10-28  
> 전제: [../contest/goal.md](../contest/goal.md) 심사 기준, [../sdk/overview.md](../sdk/overview.md)의 SDK 제공 상태(2026-10-28 확인)  
> 상태: 후보 선정 전. 이 문서에서 2~3개를 골라 각각 1페이지로 디벨롭한 뒤 1개를 확정한다.

## 전제와 제약

- 12/3 PT까지 에뮬레이터에서 동작해야 한다. 핵심 기능은 **Vehicle State(조회)** 와 **NaviHelper** 로만 구성한다.
- Vehicle Control(set), TextToSpeech, SpeechToText, FusedLocation은 "준비 중"이다. 기획서에는 확장 계획으로 넣고, 데모는 여기에 의존하지 않는다.
- 차량 속도는 Pleos SDK에 없다(`VehicleSpeedListener` 삭제). AAOS `CarPropertyManager`의 `PERF_VEHICLE_SPEED`로 읽는다. 에뮬레이터에서 값을 주입할 수 있는지 Hello World 단계에서 확인한다.
- 공식 예시 3개(음성 메모, 안전운전 점수, 배달 기사 앱)는 지원자가 몰릴 방향이다. 겹치는 방향은 피하거나 비튼다.
- 에뮬레이터에서 NaviHelper의 주행 중 콜백(`onCurrentRoadSpeedLimit`, `onCautionPointInfo`, `onTBTInfo`)이 실제로 들어오는지 공식 navi 샘플로 확인한다. 안 들어오면 데모용 mock 경로 데이터 레이어를 둔다.
- CarInfo 도메인은 VIN 조회만 제공한다. 차량 치수(높이·길이)는 SDK로 못 읽으므로 치수 기반 아이디어는 제외했다.

## 평가 축

공식 심사 기준 4개에 우리 제약 2개를 더한다.

| 축 | 질문 |
|---|---|
| 차량특화성 | 모바일 앱으로는 안 되는가. 주행 중/정차 중 맥락이 갈리는가 |
| 창의성 | 페인포인트가 선명한가. 기존에 없던 해법인가 |
| 기술활용성 | Pleos SDK가 핵심 기능에 들어가는가. 몇 개 도메인을 쓰는가 |
| 사업성 | 타깃이 명확한가. MAU·리텐션 서사가 되는가 |
| 12/3 구현 | Vehicle State + NaviHelper만으로 핵심 데모가 되는가 |
| 차별성 | 현대차 기본 기능, 타사 기능, 공식 예시와 겹치지 않는가 |

## 후보

### 1. 동승자 멀미 예보·완화 코치

분야: 패밀리·동승자 케어 / 스마트 드라이빙·안전

- **페인포인트.** 뒷좌석 아이·동승자가 멀미한다. 운전자는 뒷좌석 상태를 모르고, 동승자는 굽은 길이 언제 끝나는지 모른다. 멀미 원인은 가감속·회전·실내 공기·시선 고정(스마트폰)이다.
- **핵심 기능 (MVP).**
  1. 멀미 위험 지수(0~100): 브레이크 페달 빈도, 조향각 변화량, 앞으로 나올 주의구간·회전, 공조 내기순환·실내 온도를 가중합.
  2. 동승자 화면: "굽은 길 1.2km 남음, 그 뒤 직선" 예보, 먼 곳 보기 안내, 도착까지 남은 시간.
  3. 운전자 팁: "외기순환으로 바꾸세요", "다음 구간 급커브, 부드럽게".
- **SDK 매핑.**

  | 구분 | 사용 |
  |---|---|
  | 지금 가능 | Brake `registerBrakePedalStatus` · SteeringWheel 조향각 · HVAC `getHvacRecirculationState`, `getHvacTemperatureCurrent` · Seat `getCurrentSeatOccupancy`(뒷좌석 탑승 감지) · NaviHelper `onCautionPointInfo`, `onTBTInfo`, `getDestinationInfo`(remainTime), `onCurrentRoadSpeedLimit` |
  | 확장 | TextToSpeech로 동승자 음성 안내 · Vehicle Control로 외기순환 자동 전환, 시트 각도 · SpeechToText로 "멀미나요" 음성 신고 |

- **에뮬레이터 데모.** Virtual Console에서 뒷좌석 착석 → 모드 자동 켜짐. 브레이크 반복, 내기순환 ON → 지수 상승과 팁. 내비 목적지 설정 → 주의구간 예보 카드.
- **차별성.** 멀미를 다루는 차량 앱은 사실상 없다. 현대차 기본 기능도 아니고 공식 예시와 안 겹친다. 자율주행·후석 스크린 사용이 늘수록 멀미가 커진다는 연구(재규어랜드로버, 미시간대 UMTRI)가 있어 확장 서사가 된다.
- **사업성.** 자녀 동반 가정, 택시·라이드헤일링(승객 만족), 제약(멀미약)·보험 제휴.
- **리스크.** 지수 모델의 근거. 가속도 기반 멀미 지표(MSDV) 연구를 인용해 단순 가중합으로 설계한다. 조향각·주의구간을 에뮬레이터에서 주입할 수 있는지 확인 필요.

### 2. EV 도착 배터리 예측·전비 코치

분야: 모빌리티·라이프 편의 / 스마트 드라이빙

- **페인포인트.** 목적지 도착 시 배터리가 얼마나 남을지 불안하다. 겨울·고속·히터에서 전비가 급락하는데, 내비는 거리 기준으로만 말한다.
- **핵심 기능 (MVP).**
  1. 도착 SoC 예측: 현재 SoC × 최근 전비(since charge, instantaneous) × 남은 거리 + 공조·주행모드 보정.
  2. 행동별 효과 수치화: "히터 1도 내리면 +1.8%", "에코 모드 +3%", "제한속도 준수 +2%"를 선택지로 제시.
  3. 부족 예측 시 충전소 경유지 자동 제안.
- **SDK 매핑.**

  | 구분 | 사용 |
  |---|---|
  | 지금 가능 | EvBattery `getEvBatteryStateOfCharge`, 효율 4종(`Instantaneous`/`SinceCharge`/`Cumulative`/`Current`), `getEvChargeSwitchState` · HVAC power·temperature·AC, Seat 열선·통풍, SteeringWheel 열선 · DrivingMode `getCurrentDrivingModeState`, 크루즈 · NaviHelper `getDestinationInfo`(remainDistance), `getChargerOperatorInfo`, `addWaypoint`, `onCurrentRoadSpeedLimit` |
  | 확장 | Vehicle Control로 공조·드라이브모드 원터치 적용 · TextToSpeech 안내 |

- **에뮬레이터 데모.** Virtual Console에서 SoC 35%, 전비 낮춤, 히터 ON → 도착 SoC 4% 경고 → "충전소 경유 추가" → 내비 경유지 반영 → 예측 갱신.
- **차별성.** 현대차 EV 내비에 "도착 시 배터리" 표시는 이미 있다. 차별은 "원인 분해 + 행동별 효과 수치화 + 경유지 자동화"다. 공식 예시와 안 겹친다.
- **사업성.** 현대차그룹 EV 전 차종이 타깃이라 MAU 서사가 가장 쉽다. 충전 사업자 제휴.
- **리스크.** 예측 모델은 단순화해야 한다. "기본 내비와 뭐가 다르냐"는 질문에 답을 준비해야 한다.

### 3. 장거리 출발 전 점검 + 자동 경유지

분야: 스마트 드라이빙·안전 / 여행

- **페인포인트.** 장거리 가는 길에 타이어 경고, 워셔액 부족, 연료 부족을 발견한다. 출발 전엔 아무도 경고등을 안 본다.
- **핵심 기능 (MVP).**
  1. 내비 목적지가 거리 임계치를 넘으면 자동 점검: 타이어 공기압, 엔진오일·브레이크액·워셔액, 텔테일, 연료/SoC 대 남은 거리.
  2. 결과 카드: 초록/노랑/빨강과 조치.
  3. 연료·충전 부족이면 주유소·충전소 경유지 추가, 워셔액 부족이면 다음 휴게소 경유지.
- **SDK 매핑.**

  | 구분 | 사용 |
  |---|---|
  | 지금 가능 | Tire 공기압 · Safety `getEngineOilLevelState`, `getBrakeFluidLevelState`, `hasLowWasherFluidWarning`, `getTelltalesInfo` · Fuel `getFuelPercent` / EvBattery SoC · Odometer `getTripSinceRefillDrivingDistance` · NaviHelper `getDestinationInfo`, `addWaypoint`, `getChargerOperatorInfo` |
  | 확장 | TextToSpeech 출발 브리핑 · FusedLocation으로 귀가·반납 시 재점검 |

- **에뮬레이터 데모.** 내비 목적지 300km 설정 → 점검 화면 자동 등장 → Virtual Console에서 타이어 저압·워셔액 부족 토글 → 빨강 전환 → 경유지 추가.
- **차별성.** 경고등은 있지만 "여정 기준으로 묶어 출발 전 브리핑"은 없다. 안전운전 점수 예시와 다르다.
- **사업성.** 렌터카·카셰어 출발·반납 점검, 보험 안전 할인, 정비 네트워크 연결. 다만 MAU가 장거리 운행 빈도에 묶인다.
- **리스크.** 창의성 점수가 후보 중 가장 낮을 수 있다. 1번 또는 7번과 묶어 "여정 코치"로 키우는 선택지가 있다.

### 4. 뒷좌석 패밀리 케어

분야: 패밀리·동승자 케어

- **페인포인트.** 아이가 주행 중 벨트를 푼다. 운전자는 뒷좌석을 못 본다. 도착 전 아이를 깨워야 하고, 하차 시 뒷좌석에 아이·물건을 두고 내린다.
- **핵심 기능 (MVP).**
  1. 좌석별 착석·벨트 상태 카드, 주행 중 벨트 해제 시 즉시 알림.
  2. 아이용 "얼마나 남았어?" 화면: 남은 시간을 캐릭터 진행바로, 도착 N분 전 알림.
  3. 하차 체크: 주차 브레이크 ON + 운전석 도어 열림 + 뒷좌석 착석 유지 → 경고.
- **SDK 매핑.**

  | 구분 | 사용 |
  |---|---|
  | 지금 가능 | Seat `getCurrentSeatOccupancy`, `getCurrentSeatBeltBuckledState`(좌석별) · Safety `hasSeatBeltNotBuckledWarning` · Brake 주차 브레이크 · Door `getCurrentDoorPosition`, `getDoorLockState` · NaviHelper remainTime, `onDestinationArrived` |
  | 확장 | TextToSpeech로 아이에게 안내 · Vehicle Control로 뒷좌석 공조·창문 잠금 |

- **에뮬레이터 데모.** 뒷좌석 착석+벨트 → 주행 중 벨트 해제 → 알림. 도착 → 하차 체크.
- **차별성.** 현대차 기본 ROA(후석 승객 알림)와 벨트 경고가 이미 있다. 차별은 "여정과 결합한 동승자 경험"이어야 한다. 패밀리 분야는 지원자가 몰릴 가능성이 크다.
- **사업성.** 영유아 가정, 카시트 제조사·키즈 콘텐츠 제휴.
- **리스크.** 기본 기능 중복 지적. 창의성 보강이 필요하다.

### 5. 반려동물 동승 모드

분야: 패밀리·동승자 케어 / 라이프

- **페인포인트.** 반려동물을 태우고 잠깐 내릴 때 실내 온도가 걱정된다. 주행 중 뒷좌석 상태를 모른다. 산책 가능한 휴게소를 모른다.
- **핵심 기능 (MVP).**
  1. 주행 중: 뒷좌석 온도·환기·창문 상태 모니터, 급정거 빈도 → 쉬어가기 제안.
  2. 정차 중 펫 모드 화면: 외부인에게 "반려동물 있음, 실내 OO도, 공조 작동 중" 표시, 온도 임계 경고.
  3. 반려동물 동반 가능 휴게소·공원 경유지 추가.
- **SDK 매핑.**

  | 구분 | 사용 |
  |---|---|
  | 지금 가능 | HVAC `getHvacTemperatureCurrent`, `getHvacPowerState` · Window `getCurrentWindowPosition` · Door `getDoorLockState` · Seat occupancy · Brake 주차 브레이크 · NaviHelper `addWaypoint` |
  | 확장 | Vehicle Control로 공조 자동 유지(핵심 가치) · FusedLocation 지오펜스 · TextToSpeech |

- **에뮬레이터 데모.** 주차 브레이크 ON + 펫 모드 → 대형 안내 화면. Virtual Console에서 실내 온도 올림 → 경고.
- **차별성.** 테슬라 Dog Mode가 유명하지만 현대차엔 없다. 공식 예시와 안 겹친다.
- **사업성.** 반려 인구가 크다. 펫 보험·용품 제휴.
- **리스크.** 핵심 가치(공조 유지)가 Vehicle Control에 의존해 데모에선 모니터링만 보여준다. 보호자 외부 알림은 폰 푸시 백엔드가 필요하다.

### 6. 남은 주행시간 맞춤 오디오

분야: 인카 엔터테인먼트

- **페인포인트.** 목적지 5분 전에 끊기는 팟캐스트·오디오북. 운전 맥락을 무시하는 재생.
- **핵심 기능 (MVP).**
  1. 남은 주행 시간에 맞는 길이의 콘텐츠 자동 편성.
  2. TBT 안내·주의구간에서 자동 볼륨 다운·일시정지, 도착 시 북마크.
  3. 비(와이퍼), 야간(조명), 주행 모드에 따른 분위기 플레이리스트.
- **SDK 매핑.**

  | 구분 | 사용 |
  |---|---|
  | 지금 가능 | NaviHelper remainTime, `onTBTInfo`, `onCautionPointInfo`, `onDestinationArrived` · Wiper 상태 · Light 상태 · DrivingMode · HVAC |
  | 확장 | TextToSpeech로 뉴스 브리핑 합성 · SpeechToText 음성 제어 |

- **에뮬레이터 데모.** 로컬 오디오 파일 + 내비 목적지 → 편성표 생성, TBT 발생 시 덕킹.
- **차별성.** 구글·스포티파이와의 경쟁 구도를 설명해야 한다.
- **사업성.** 콘텐츠 제휴, 구독.
- **리스크.** 콘텐츠 라이선스. 기술 활용성이 NaviHelper 하나에 치우쳐 보일 수 있다.

### 7. 연속 운전 피로 관리

분야: 스마트 드라이빙·안전

- **페인포인트.** 2시간 넘게 쉬지 않는 장거리 운전, 졸음.
- **핵심 기능 (MVP).** 트립 연속 주행 시간, 야간 여부, 남은 시간 → 휴식 권고, 휴게소 경유지 자동 제안, 정차 중 스트레칭 가이드.
- **SDK 매핑.** 지금 가능: Odometer `getTripCurrentDrivingTime` · NaviHelper remainTime, `addWaypoint` · Light · Brake. 확장: TextToSpeech.
- **차별성.** 현대차 DAW(운전자 주의 경고)와 겹친다. 단독보다 1번 또는 3번에 병합하는 편이 낫다.

## 비교표

◎ 강함 · ○ 보통 · △ 약함

| 후보 | 차량특화성 | 창의성 | 기술활용성 | 사업성 | 12/3 구현 | 차별성 |
|---|---|---|---|---|---|---|
| 1 멀미 예보 | ◎ | ◎ | ◎ Vehicle 4도메인 + Navi | ○ | ○ | ◎ |
| 2 EV 도착 배터리 | ◎ | ○ | ◎ Vehicle 4도메인 + Navi | ◎ | ◎ | ○ |
| 3 출발 전 점검 | ◎ | △ | ◎ Vehicle 5도메인 + Navi | ○ | ◎ | ○ |
| 4 패밀리 케어 | ○ | △ | ○ | ○ | ◎ | △ |
| 5 펫 모드 | ○ | ○ | △ 핵심이 Control | ○ | ○ | ○ |
| 6 맞춤 오디오 | ○ | ○ | △ Navi 중심 | ○ | ○ | ○ |
| 7 피로 관리 | ○ | △ | ○ | △ | ◎ | △ |

## 추천

1. **1번 멀미 예보.** 창의성과 차별성이 가장 높고, Vehicle State 여러 도메인과 NaviHelper를 핵심 기능에 쓴다. 자율주행 서사로 확장된다. 리스크는 지수 모델 근거와 에뮬레이터 데이터 주입 여부.
2. **2번 EV 도착 배터리.** 사업성과 구현 안정성이 가장 높다. 기본 내비와의 차별만 잘 설명하면 안정적인 선택.
3. **3번 출발 전 점검.** 가장 확실하게 동작하는 데모. 창의성이 약하므로 1번 또는 7번과 묶어 "출발 전 점검 → 주행 중 코치 → 도착"의 여정 코치로 키울 수 있다.

병합안 "여정 동승 코치"(1+3+7)는 범위가 넓어 12/3까지 위험하다. PT는 핵심 하나로 하고, 나머지는 로드맵에 둔다.

## 다음 단계

- [ ] 1~3번 각각 1페이지로 디벨롭: 타깃, 주행 중/정차 중 시나리오, 화면 3장 스케치, SDK 호출 목록, MAU·리텐션 가정.
- [ ] Hello World 단계에서 확인: Virtual Console이 바꿀 수 있는 항목(조향각, 브레이크, 좌석, HVAC, SoC, 타이어), 에뮬레이터 내비에서 실제로 들어오는 콜백.
- [ ] 확인 결과를 반영해 1개 확정.
