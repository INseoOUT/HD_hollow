# Vehicle SDK

> 마지막 확인일: 2026-10-28  
> 기준 버전: Vehicle SDK 2.3.2 (Vehicle Service 2.3.21 기준 가이드, 샘플 앱은 2.3.22)  
> 원본:
> - [Vehicle SDK 소개 (Introduction)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/intro)
> - [Importing Vehicle SDK (프로젝트 설정)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/project-setup)
> - [Obtaining a domain object (도메인 객체 획득)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/obtain-domain-object)
> - [Capability](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/check-capability)
> - [Get](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/get-property)
> - [Set](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/set-property)
> - [Callback](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/callback)
> - [Declaring permission (권한 선언)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-permission)
> - [Error code](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-error-code)
> - [Vehicle SDK API Reference](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/entry)
> - [Vehicle SDK Sample App 사용 가이드](https://document.pleos.ai/docs/pleos-only/tutorial/vehicle-sdk-sample-app) · [샘플 소스 (GitHub)](https://github.com/Pleos-Playground/pleos-sample-vehicle-app)
> - [Pleos Connect SDK 릴리즈 노트 (2026-10-19)](https://document.pleos.ai/docs/pleos-only/release-note/sdk-261015)
> - [API 호환성 및 가용성](https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/api-compatibility-availability)
> - [프로젝트 생성 및 SDK 사용 신청 (제공 상태)](https://document.pleos.ai/docs/pleos-only/create-my-project)

## 설명

Vehicle SDK는 차량 정보 조회와 제어를 위한 API입니다. 앱은 이 SDK를 통해 Pleos Connect의 Vehicle Service에 연결하고, Vehicle Service는 Android Automotive OS 위에서 요청을 처리합니다. 차량 통신은 IPC이므로 모든 조회·제어·capability 결과는 즉시 반환되지 않고 `onComplete`·`onFailed` 비동기 람다로 전달됩니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/intro)

**제공 상태.** 공식 문서는 Vehicle State(조회)를 "사용 가능, 신청 불필요", Vehicle Control(제어)을 "준비 중"으로 표시합니다(2026-10-28 확인). 어떤 함수가 Vehicle Control에 속하는지 문서에 명시된 대응표는 없으므로, `set*` 계열과 `CONTROL_CAR_*` 권한이 필요한 API는 본인 Playground 콘솔(Workspace > My Project > SDK)에서 상태를 확인한 뒤 데모에 포함할지 결정합니다. [원본](https://document.pleos.ai/docs/pleos-only/create-my-project)

**의존성.** `implementation("ai.pleos.playground:Vehicle:2.3.2")`. Maven 저장소 등록은 [../overview.md](../overview.md)의 공통 설정을 참고합니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/project-setup)

**구조.** 진입점 `Vehicle` 인스턴스에서 `getDoor()`, `getBrake()` 같은 `getXxx()`로 도메인 객체를 얻고, 도메인 객체에서 네 가지 타입의 API를 호출합니다. 도메인 객체는 직접 생성하지 않습니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/obtain-domain-object)

| 타입 | 하는 일 | 결과 전달 | 함수 이름 패턴 |
|---|---|---|---|
| Capability | 기능 지원 여부·접근 수준·허용 값 범위 확인 | `onComplete(config, areaMap)` / `onFailed` | `checkXxxCapabilities` |
| Get | 현재 값 조회 | `onComplete(값)` / `onFailed` | `getCurrentXxx`, `getXxx` |
| Set | 값 설정 | `onFailed` (실패 시) | `setXxx`, `moveXxx` |
| Callback | 상태 변화 구독 | 리스너 `onXxxUpdated` / `onFailed` | `registerXxx` / `unregisterXxx` |

### 권한 체계

Vehicle SDK API는 매니페스트에 Pleos 차량 권한을 선언해야 호출할 수 있습니다. 상수는 접두사로 구분되며 매니페스트에는 `pleos.car.permission.{상수명}` 형식으로 적습니다. 이 권한은 실행 중 승인 창을 띄우는 방식이 아니라 매니페스트 선언으로 처리됩니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-permission)

| 접두사 | 예시 | 용도 |
|---|---|---|
| `CAR_*` | `CAR_DOORS` → `pleos.car.permission.CAR_DOORS` | 도메인 데이터 조회(get) |
| `CONTROL_CAR_*` | `CONTROL_CAR_DOORS` | 도메인 제어(set) |

도메인별 대표 조회 권한은 아래와 같습니다. 세부 API마다 요구 권한이 다를 수 있으므로 호출 전 각 함수의 `@RequiresPermission`(api 파일의 권한 열)을 확인합니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-permission)

| 도메인 | PleosPermission 상수 | 매니페스트 `android:name` |
|---|---|---|
| Door | `CAR_DOORS` | `pleos.car.permission.CAR_DOORS` |
| Window, Wiper | `CAR_WINDOWS` | `pleos.car.permission.CAR_WINDOWS` |
| HVAC | `CAR_CLIMATE` | `pleos.car.permission.CAR_CLIMATE` |
| DrivingMode, Brake, Odometer | `CAR_DRIVE` | `pleos.car.permission.CAR_DRIVE` |
| EvBattery, Fuel | `CAR_ENERGY` | `pleos.car.permission.CAR_ENERGY` |
| CarInfo, Display | `CAR_INFO` | `pleos.car.permission.CAR_INFO` |
| Light, TurnSignal | `CAR_LIGHTS` | `pleos.car.permission.CAR_LIGHTS` |
| SideMirror | `CAR_MIRRORS` | `pleos.car.permission.CAR_MIRRORS` |
| Safety, Tire | `CAR_SAFETY` | `pleos.car.permission.CAR_SAFETY` |
| Seat | `CAR_SEATS` | `pleos.car.permission.CAR_SEATS` |
| Toll | `CAR_TOLL` | `pleos.car.permission.CAR_TOLL` |

```xml
<!-- AndroidManifest.xml: 사용하는 도메인에 맞는 권한만 선언 -->
<uses-permission android:name="pleos.car.permission.CAR_DOORS" />
<uses-permission android:name="pleos.car.permission.CAR_INFO" />
```

권한은 Pleos App Market과 사전 협의·승인된 범위 안에서만 사용할 수 있고, 승인되지 않은 권한을 선언하거나 호출하면 앱 심사를 통과하지 못합니다. 제품 앱은 샘플 앱의 전체 권한 목록을 복사하지 말고 실제 사용하는 API의 최소 권한만 선언합니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/)

### 에러 코드

`onFailed`로 받은 `Exception`이 `VehicleException`이면 `errorCode`로 `ErrorCode`(`ai.pleos.playground.vehicle.constant`) 값을 확인합니다. 모든 `onFailed`가 `VehicleException`은 아닙니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-error-code)

| ErrorCode | 설명 | 권장 처리 |
|---|---|---|
| `ERROR_NOT_SUPPORTED_PROPERTY` | 해당 속성(기능) 미지원 | UI 비활성·대체 흐름 |
| `ERROR_NOT_SUPPORTED_AREA` | 해당 영역 미지원 | 영역 선택 변경 |
| `ERROR_NOT_SUPPORTED_VALUE_TYPE` | 지원하지 않는 값 타입 | 파라미터·타입 재확인 |
| `ERROR_SECURITY_PERMISSION` | 권한 부족 | 매니페스트 권한·`@RequiresPermission` 확인 |
| `ERROR_SERVICE_CONNECTION_FAILURE` | Vehicle 서비스 연결 실패 | `initialize()`·에뮬레이터 상태 확인 |
| `ERROR_INTERNAL_SERVICE` | 내부 서비스 오류 | 재시도·일시 오류 안내 |
| `ERROR_DRIVING_RESTRICTED` | 주행 중 기능 제한 | 주행 제한 안내 |
| `ERROR_EMPTY_RESULT` | 결과 없음 | 빈 상태 UI 처리 |
| `ERROR_UNDEFINED` | 정의되지 않은 오류 | 로그·재시도 |

### 호환성

API 지원 여부는 차량 모델에 따라 다릅니다. 모델명으로 분기하지 말고 도메인별 `checkXXXCapabilities`로 확인합니다. 샘플 앱 가이드도 "기능 전체 지원"과 "특정 영역 지원"을 구분하라고 안내합니다(예: 기능은 Supported인데 `ROW_1_LEFT` 영역 조회는 `area ID: 0x1 not supported`로 실패). [원본](https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/api-compatibility-availability)

## 기능

19개 도메인입니다. 함수 수는 api 파일의 행 수(오버로드 포함, deprecated 포함)이고, 설명은 공식 KDoc 원문입니다. 도메인별 함수 전체 목록과 원본 링크는 각 api 파일에 있습니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/intro)

| 도메인 | 한글명 | 설명 (KDoc) | 함수 | 리스너 | 함수 목록 |
|---|---|---|---|---|---|
| Brake | 브레이크 | Provides APIs for brake pedal status. | 4 | 1 | [api/brake.md](api/brake.md) |
| CarInfo | 차량 정보 | Provides APIs for vehicle identification information and capability info. | 4 | 0 | [api/car-info.md](api/car-info.md) |
| Display | 디스플레이 | Provides APIs for temperature display unit. | 4 | 1 | [api/display.md](api/display.md) |
| Door | 도어 | Provides APIs for door lock, door position, and glove box. | 17 | 3 | [api/door.md](api/door.md) |
| DrivingMode | 주행 모드 | Provides APIs for driving mode and cruise control. | 12 | 3 | [api/driving-mode.md](api/driving-mode.md) |
| EvBattery | EV 배터리·충전 | Provides APIs for EV battery efficiency. | 31 | 2 | [api/ev-battery.md](api/ev-battery.md) |
| Fuel | 연료 | Provides APIs for fuel economy. | 20 | 2 | [api/fuel.md](api/fuel.md) |
| HVAC | 공조(HVAC) | Provides APIs for climate control (HVAC). | 86 | 12 | [api/hvac.md](api/hvac.md) |
| Light | 조명 | Provides APIs for exterior, cabin, and ambient lights. | 78 | 10 | [api/light.md](api/light.md) |
| Odometer | 주행거리·연비 | Provides APIs for odometer and trip data. | 35 | 2 | [api/odometer.md](api/odometer.md) |
| Safety | 안전 | Provides APIs for airbags, warnings, fluid levels, and telltales. | 39 | 7 | [api/safety.md](api/safety.md) |
| Seat | 시트 | Provides APIs for seat position, occupancy, seat belt, and seat climate. | 101 | 15 | [api/seat.md](api/seat.md) |
| SideMirror | 사이드미러 | Provides APIs for side mirror heat. | 6 | 1 | [api/side-mirror.md](api/side-mirror.md) |
| SteeringWheel | 스티어링 휠 | Provides APIs for the front-wheel steering angle and steering wheel heating. | 10 | 2 | [api/steering-wheel.md](api/steering-wheel.md) |
| Tire | 타이어 | Provides APIs for tire pressure. | 3 | 0 | [api/tire.md](api/tire.md) |
| Toll | 하이패스(ETCS) | Provides APIs for ETCS voice guidance volume. | 5 | 1 | [api/toll.md](api/toll.md) |
| TurnSignal | 방향지시등 | Provides APIs for turn signal state. | 4 | 1 | [api/turn-signal.md](api/turn-signal.md) |
| Window | 윈도우 | Provides APIs for window position, lock, blinds, and smart glass. | 31 | 4 | [api/window.md](api/window.md) |
| Wiper | 와이퍼 | Provides APIs for wiper state and switch. | 9 | 1 | [api/wiper.md](api/wiper.md) |

공식 소개 페이지의 도메인 설명(한국어)은 다음과 같습니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/intro)

| 도메인 | 설명 |
|---|---|
| Brake | 브레이크 페달, 주차 브레이크 상태 조회 |
| CarInfo | 차량 기본 정보(외부 크기, 제조사, 연식 등) 조회 |
| Display | 인포테인먼트 표시 데이터 단위(온도 단위 등) 조회/설정 |
| Door | 도어 열림/닫힘, 잠금/해제 제어 및 상태 조회 |
| DrivingMode | 드라이브 모드, 크루즈 컨트롤 정보 조회 |
| EvBattery | EV 배터리·충전 상태 및 전비(효율) 조회 |
| Fuel | 연비 등 연료 관련 정보 조회 |
| HVAC | 난방·환기·공조(온도, 풍량, 습도 등) 제어 및 조회 |
| Light | 조명 상태 조회 및 앰비언트 라이트 등 제어 |
| Odometer | 총 주행 거리, 트립 주행 거리/시간, 주행 거리 단위 조회 |
| Safety | 차량 경고(Warning) 정보 조회 |
| Seat | 좌석 위치·각도·기능 제어 및 조회 |
| SideMirror | 사이드미러 열선 상태 조회/설정 |
| Steeringwheel | 핸들(스티어링휠) 상태, 열선 조회/제어 |
| Tire | 타이어 상태 조회 |
| Toll | ETCS(하이패스) 음성 안내 음량 등 조회/설정 |
| TurnSignal | 방향 지시등 상태 조회 |
| Window | 창문 제어 및 상태 조회 |
| Wiper | 와이퍼 제어 및 상태 조회 |

### Vehicle 진입점 함수

`ai.pleos.playground.vehicle.Vehicle` 클래스의 공개 함수 21개입니다. 설명은 공식 KDoc 원문입니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/entry)

| 함수 | 반환 | 설명 | 원본 |
|---|---|---|---|
| `initialize()` | - | Initialize sdk. It is called while the application is created. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/initialize) |
| `release()` | - | Release sdk. It is called while the application is destroyed. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/release) |
| `getBrake()` | `Brake` | Get the object of Brake to use Brake APIs. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/get-brake) |
| `getCarInfo()` | `CarInfo` | Get the object of CarInfo to use CarInfo APIs. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/get-car-info) |
| `getDisplay()` | `Display` | Get the object of Display to use Display APIs. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/get-display) |
| `getDoor()` | `Door` | Get the object of Door to use Door APIs. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/get-door) |
| `getDrivingMode()` | `DrivingMode` | Get the object of DrivingMode to use DrivingMode APIs. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/get-driving-mode) |
| `getEvBattery()` | `EvBattery` | Get the object of EvBattery to use EvBattery APIs. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/get-ev-battery) |
| `getFuel()` | `Fuel` | Get the object of Fuel to use Fuel APIs. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/get-fuel) |
| `getHVAC()` | `HVAC` | Get the object of HVAC to use HVAC APIs. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/get-h-v-a-c) |
| `getLight()` | `Light` | Get the object of Light to use Light APIs. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/get-light) |
| `getOdometer()` | `Odometer` | Get the object of Odometer to use Odometer APIs. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/get-odometer) |
| `getSafety()` | `Safety` | Get the object of Safety to use Safety APIs. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/get-safety) |
| `getSeat()` | `Seat` | Get the object of Seat to use Seat APIs. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/get-seat) |
| `getSideMirror()` | `SideMirror` | Get the object of SideMirror to use SideMirror APIs. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/get-side-mirror) |
| `getSteeringWheel()` | `SteeringWheel` | Get the object of SteeringWheel to use SteeringWheel APIs. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/get-steering-wheel) |
| `getTire()` | `Tire` | Get the object of Tire to use Tire APIs. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/get-tire) |
| `getToll()` | `Toll` | Get the object of Toll to use Toll APIs. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/get-toll) |
| `getTurnSignal()` | `TurnSignal` | Get the object of TurnSignal to use TurnSignal APIs. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/get-turn-signal) |
| `getWindow()` | `Window` | Get the object of Window to use Window APIs. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/get-window) |
| `getWiper()` | `Wiper` | Get the object of Wiper to use Wiper APIs. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle/vehicle/get-wiper) |

### Capability 반환 타입

최신 capability API는 `CapabilityConfig`와 영역별 범위를 함께 돌려줍니다. 이전 형태(`List<Area>` 또는 `Map<Area, AreaBounds>`만 받는 콜백)는 2026-10-19 릴리즈에서 deprecated(WARNING)되었고 삭제된 것은 아닙니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/check-capability)

| 타입 | 설명 |
|---|---|
| `CapabilityConfig` | 기능 단위 접근 수준(`AccessType`: `NONE`, `READ`, `WRITE`, `READ_WRITE`) |
| `AreaBounds` | 영역별 허용 값 범위(`minValue`, `maxValue`) |
| `Map<Area, AreaBounds>` | 영역(예: `DoorArea`)별 `AreaBounds` 맵 |

## 활용

### 기본 흐름 (Kotlin)

공식 가이드의 예제를 한 흐름으로 이었습니다. 초기화 → 도메인 객체 획득 → capability 확인 → get → set → 리스너 등록 → 해제 순서입니다. 시그니처는 Vehicle SDK 2.3.2 기준(API Reference)이며, Callback 가이드 페이지의 예제는 `area` 인자가 없는 이전 형태를 보여 주므로 [api/door.md](api/door.md)의 시그니처를 우선합니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/obtain-domain-object)

```kotlin
import ai.pleos.playground.vehicle.Vehicle
import ai.pleos.playground.vehicle.api.model.area.DoorArea
import ai.pleos.playground.vehicle.api.model.capability.AccessType
import ai.pleos.playground.vehicle.callback.exception.VehicleException
import ai.pleos.playground.vehicle.constant.ErrorCode
import ai.pleos.playground.vehicle.constant.area.DoorValue
import ai.pleos.playground.vehicle.listener.DoorPositionListener

// 1. 초기화 (앱 생성 시)
val vehicle = Vehicle(context)
vehicle.initialize()

// 2. 도메인 객체 획득 (직접 생성하지 않음)
val door = vehicle.getDoor()
val area = DoorArea(DoorValue.ROW_1_LEFT)

// 3. Capability: 지원 여부·접근 수준·값 범위 확인
door.checkDoorPositionCapabilities(
    onComplete = { config, areaMap ->
        when (config.accessType) {
            AccessType.READ, AccessType.READ_WRITE -> {
                val bounds = areaMap[area]
                // bounds?.minValue / bounds?.maxValue 확인 후 getCurrentDoorPosition 호출
            }
            else -> { /* NONE 또는 WRITE만 지원: UI 비활성 또는 대체 흐름 */ }
        }
    },
    onFailed = { e -> if (e is VehicleException) { /* e.errorCode */ } }
)

// 4. Get: 현재 값 조회
door.getCurrentDoorPosition(
    area = area,
    onComplete = { pos: Int? -> /* 값 사용 */ },
    onFailed = { e ->
        if (e is VehicleException) {
            when (e.errorCode) {
                ErrorCode.ERROR_NOT_SUPPORTED_PROPERTY,
                ErrorCode.ERROR_NOT_SUPPORTED_AREA -> { /* UI 비활성 */ }
                ErrorCode.ERROR_SECURITY_PERMISSION -> { /* 매니페스트 권한 확인 */ }
                ErrorCode.ERROR_SERVICE_CONNECTION_FAILURE,
                ErrorCode.ERROR_INTERNAL_SERVICE -> { /* initialize·재시도 */ }
                else -> { /* 로그 */ }
            }
        }
    }
)

// 5. Set: 값 설정 (CONTROL_CAR_DOORS 권한 필요, 제어 SDK 제공 상태 확인)
door.setDoorPosition(
    area = area,
    value = 1,
    onComplete = { _, _ -> /* 요청 완료. 실제 상태는 다시 조회하거나 리스너로 확인 */ },
    onFailed = { e -> /* 처리 */ }
)

// 6. Callback: 상태 변화 구독
val listener = object : DoorPositionListener {
    override fun onPositionUpdated(pos: Int?) { /* 갱신 */ }
    override fun onFailed(e: Exception) { /* 처리 */ }
}
door.registerDoorPosition(area = area, listener = listener)

// 7. 해제: 등록에 쓴 같은 area와 listener로 해제한 뒤 release (앱 종료 시)
door.unregisterDoorPosition(area = area, listener = listener)
vehicle.release()
```

### 주의사항

- **콜백 완료 전 `release()` 금지.** 결과는 비동기로 오므로 콜백이 끝나기 전에 `release()`를 호출하면 안 됩니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/Vehicle/intro)
- **같은 listener와 area로 해제.** 등록할 때 쓴 동일 객체와 영역으로 `unregister*`를 호출합니다. 다른 영역에서 해제를 요청하면 등록 당시 영역을 선택하라는 오류가 날 수 있습니다. 화면 이동이나 영역 선택 변경만으로는 구독이 해제되지 않습니다. [원본](https://document.pleos.ai/docs/pleos-only/tutorial/vehicle-sdk-sample-app)
- **Set 완료 ≠ 값 변경.** `set*`의 완료 콜백은 요청이 처리됐다는 뜻이고 실제 차량 상태는 다시 조회하거나 리스너 알림으로 확인합니다. [원본](https://document.pleos.ai/docs/pleos-only/tutorial/vehicle-sdk-sample-app)
- **`initialize()` 반환 ≠ 연결 완료.** 초기화 호출이 예외 없이 끝났다는 표시만으로 Vehicle Service 연결이나 개별 기능 사용 가능을 판단하지 않습니다. [원본](https://document.pleos.ai/docs/pleos-only/tutorial/vehicle-sdk-sample-app)
- **Capability 먼저.** 지원 확인이 실패하거나 영역 범위를 모르면 변경 요청을 보내지 않습니다. `Unsupported`에는 실제 미지원뿐 아니라 지원 확인 요청 실패도 포함될 수 있습니다. [원본](https://document.pleos.ai/docs/pleos-only/tutorial/vehicle-sdk-sample-app)
- **최소 권한.** 실제 사용하는 typed API의 권한만 선언합니다. `PRIVATE_*` 권한은 공개 예제에 포함되지 않습니다. [원본](https://document.pleos.ai/docs/pleos-only/tutorial/vehicle-sdk-sample-app)

### 샘플 앱 빌드

공식 Vehicle SDK Sample App은 도메인을 고르고 기능별로 capability 확인, 조회, 변경, 알림 구독·해제를 실행해 보는 예제입니다. Pleos Connect 전용 AVD(`Pleos_Connect_v2.1`, Android 14)에서 실행해야 하며 일반 휴대전화나 기본 에뮬레이터 이미지에서는 동작하지 않습니다. [원본](https://document.pleos.ai/docs/pleos-only/tutorial/vehicle-sdk-sample-app)

```bash
git clone https://github.com/Pleos-Playground/pleos-sample-vehicle-app
cd pleos-sample-vehicle-app
./gradlew assembleDebug
./gradlew installDebug   # 실행 중인 전용 AVD에 설치
```

APK는 `app/build/outputs/apk/debug/`에 생성되고, 런처에서 **Vehicle SDK Sample**로 실행합니다. 샘플은 Vehicle SDK 버전을 `gradle/libs.versions.toml`로 관리하며 minSdk는 23이지만, Playground에 배포할 제품 앱은 API 28 이상이어야 합니다. 샘플의 Door Position 화면은 `checkDoorPositionCapabilities` → `getCurrentDoorPosition` → `registerDoorPosition` → `setDoorPosition` → `unregisterDoorPosition` 흐름을 그대로 보여 주는 기준 예제입니다. [원본](https://document.pleos.ai/docs/pleos-only/tutorial/vehicle-sdk-sample-app)

### 공모전 아이디어에 쓸 만한 도메인 조합 (예시)

아래는 공식 문서가 아니라 팀 참고용 예시입니다. 조회(`CAR_*`) 위주로 구성하면 현재 "사용 가능" 상태인 Vehicle State 범위 안에서 데모할 수 있습니다.

- **예시 1. 안전운전 피드백**: DrivingMode(크루즈 컨트롤 상태·목표 속도) + Brake(브레이크 페달 상태) + SteeringWheel(조향각) + TurnSignal(방향지시등) + Odometer(트립 거리·시간)를 구독해 주행 습관을 점수화. 속도 자체는 AAOS Car API로 조회.
- **예시 2. EV 장거리·충전 도우미**: EvBattery(충전율, 충전 포트, 전비) + Odometer(주행 가능 거리 관련 데이터) + HVAC(공조 상태)를 묶어 충전 타이밍과 전비 절감 팁을 제시. NaviHelper와 결합하면 충전소 경유지 요청까지 연결.
- **예시 3. 패밀리·동승자 케어**: Seat(착좌·안전벨트 체결) + Safety(안전벨트 경고, 에어백 상태) + Door(잠금) + Window(윈도우 위치)를 조합해 뒷좌석 탑승 상태를 운전자에게 요약. 제어가 필요한 부분은 Vehicle Control 상태 확인 후 결정.
