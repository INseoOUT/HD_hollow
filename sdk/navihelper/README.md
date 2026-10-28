# NaviHelper SDK

> 마지막 확인일: 2026-10-28  
> 기준 버전: NaviHelper SDK 3.0.5  
> 원본:
> - [NaviHelper SDK 소개](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/intro)
> - [SDK 가져오기](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/importing-sdk)
> - [권한 선언](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/declaring-permissions)
> - [초기화와 해제](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/initialization-and-release)
> - [이벤트 리스너](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/event-listeners)
> - [목적지](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/destinations)
> - [경로](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/routes)
> - [경유지 설정](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/waypoint-settings)
> - [경유지 포함 경로 요청](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/route-with-waypoints)
> - [에러 처리](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/error-handling)
> - [NaviHelper API Reference](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/entry)
> - [SDK 릴리즈 노트 2026-10-19](https://document.pleos.ai/docs/pleos-only/release-note/sdk-261015)
> - [API 호환성 및 가용성](https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/api-compatibility-availability)
> - [프로젝트 생성 및 SDK 사용 신청](https://document.pleos.ai/docs/pleos-only/create-my-project)
> - [공식 샘플 앱 pleos-sample-navi-app](https://github.com/Pleos-Playground/pleos-sample-navi-app)
>
> 함수 전체 목록: [api.md](api.md)

## 설명

NaviHelper SDK는 경로 안내 제어와 내비게이션 정보 조회를 위한 API를 제공합니다. 앱이 목적지·경유지 설정, 경로 변경, 정보 조회, Navigation 화면 표시를 요청하면 Pleos Connect의 Navi Service가 처리하고, 처리 결과와 주행 상태 변화를 이벤트로 돌려줍니다. 진입점은 `NaviHelper` 클래스 하나이고, 모든 결과는 `NaviHelperEventListener` 콜백으로 받습니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/intro)

| 항목 | 내용 |
|---|---|
| 버전 | 3.0.5 (2026-10-19 릴리즈) |
| Playground 제공 상태 | **사용 가능**. 신청 없이 바로 사용. 문서 기준 2026-10-28이며 본인 프로젝트의 SDK 메뉴에서 재확인 필요 |
| 차량 호환성 | 내비게이션이 탑재된 차량에 한해 전체 호환 |
| 의존성 | `implementation("ai.pleos.playground:NaviHelper:3.0.5")` |
| Maven 저장소 | `https://nexus-playground.pleos.ai/repository/maven-releases/` (인증 불필요) |

[원본: 제공 상태](https://document.pleos.ai/docs/pleos-only/create-my-project) · [원본: 호환성](https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/api-compatibility-availability) · [원본: 의존성](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/importing-sdk)

**권한.** 기능별로 세 가지 Android 퍼미션을 `AndroidManifest.xml`에 선언합니다. 함수별 정확한 요구 퍼미션은 API Reference의 `@RequiresPermission`에서 확인합니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/declaring-permissions)

| 퍼미션 | 주요 API |
|---|---|
| `pleos.car.permission.NAVI_ROUTE` | `get*Info()`, `requestTrip()` |
| `pleos.car.permission.NAVI_ROUTE_SEARCH` | `requestRoute()`, `requestRouteWithWaypoints()`, `cancelRoute()`, `requestReRoute()`, `addWaypoint()`, `removeWaypoint()`, `changeRouteOption()` |
| `pleos.car.permission.NAVI_CUSTOM_MAP` | `requestNavigationScreen()` |

```xml
<uses-permission android:name="pleos.car.permission.NAVI_ROUTE" />
<uses-permission android:name="pleos.car.permission.NAVI_ROUTE_SEARCH" />
<uses-permission android:name="pleos.car.permission.NAVI_CUSTOM_MAP" />
```

**동작 전제.** 요청은 기본 내비게이션 앱에 전달되고, 결과와 이벤트는 그 앱의 기능과 현재 상태에 따라 달라집니다. `requestRoute()` 호출이 정상이어도 경로 탐색 완료를 뜻하지 않으며, 정보 조회 API는 값을 직접 반환하지 않고 대응하는 콜백으로만 결과를 줍니다. 경로 옵션 변경과 재탐색은 활성 경로가 있을 때 요청합니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/destinations) · [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/routes)

**에러 처리.** 초기 서비스 연결 실패는 `initialize(onConnected, onFailed)`의 `onFailed(Throwable)`로, 내비게이션 앱의 요청 처리 오류는 `NaviHelperEventListener.onError(NaviErrorCode)`로 서로 다른 경로로 전달됩니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/error-handling)

| 에러 코드 | 의미 |
|---|---|
| `CAUSE_LOW_BATTERY` | 배터리 부족 |
| `CAUSE_SEARCH_ROUTE_OVER_DISTANCE` | 경로 탐색 가능 거리 초과 |
| `CAUSE_SEARCH_ROUTE_TOO_SHORT_DISTANCE` | 현재 위치와 목적지가 너무 가까움 |
| `CAUSE_SEARCH_ROUTE` | 경로 탐색 서버 오류 |

오류가 나면 SDK 연결 완료 후 호출했는지, 퍼미션을 선언했는지, 좌표·POI ID·경유지 인덱스가 올바른지, 현재 내비 앱 상태에서 처리 가능한 요청인지를 확인합니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/error-handling)

## 기능

함수 21개와 콜백 25개로 구성됩니다. 함수별 시그니처와 개별 원본 링크는 [api.md](api.md)에 있습니다.

| 기능 그룹 | 함수 | 결과를 받는 콜백 |
|---|---|---|
| 생명주기 | `initialize()`, `initialize(onConnected, onFailed)`, `release()` | `onConnected` / `onFailed` (initialize 인자) |
| 리스너 관리 | `addListener(listener)`, `removeListener(listener)` | - |
| 목적지 설정·취소 | `requestRoute(routeInfo)`, `cancelRoute()` | `onRouteStarted`, `onRoutingCompleted`, `onDestinationChanged`, `onRouteCancelled`, `onError` |
| 경유지 포함 경로 요청 | `requestRouteWithWaypoints(request)` | `onRouteStarted`, `onRoutingCompleted`, `onError` |
| 경로 옵션·재탐색 | `changeRouteOption(routeOption)`, `requestReRoute()` | `onRouteOptionChanged`, `onReRouteEnded` |
| 경유지 추가·제거 | `addWaypoint(requestWaypointInfo)`, `removeWaypoint(waypointIndex)` | `onWayPointInfo`, `onWaypointChanged`, `onWayPointArrived` |
| 정보 조회 | `getDestinationInfo()`, `getWaypointInfo()`, `getRouteStateInfo()`, `getCurrentLocationInfo()`, `getTBTInfo()`, `getBookmarkInfo()`, `getRecentDestinationInfo()`, `getChargerOperatorInfo()` | `onDestinationInfo`, `onWayPointInfo`, `onRouteStateInfo`, `onCurrentLocationInfo`, `onTBTInfo`, `onBookmarkInfo`, `onRecentDestinationInfo`, `onChargerOperatorInfo` |
| 트립·화면 요청 | `requestTrip(tripRequest)`, `requestNavigationScreen(requestScreen)` | `onTripInfo` 등 (내비 앱 상태에 따라 다름) |

[원본: 기능 목록](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/intro) · [원본: 콜백 대응](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/destinations)

`NaviHelperEventListener` 콜백을 공식 분류대로 묶으면 다음과 같습니다. 필요한 콜백만 재정의하면 됩니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/event-listeners)

| 분류 | 콜백 |
|---|---|
| 경로 탐색 | `onRouteStarted(routeStartInfo)`, `onRoutingCompleted()`, `onRouteCancelled()`, `onReRouteEnded()`, `onRouteOptionChanged(routeOption)` |
| 목적지와 경유지 | `onDestinationChanged()`, `onDestinationArrived(destinationArrivedInfo)`, `onWaypointChanged(waypointChangedInfo)`, `onWayPointArrived(waypointArrivedInfo)` |
| 정보 조회 | `onBookmarkInfo(list)`, `onBookMarkChanged()`, `onRecentDestinationInfo(list)`, `onCurrentLocationInfo(info)`, `onDestinationInfo(info)`, `onWayPointInfo(list)`, `onRouteStateInfo(info)`, `onTBTInfo(list)`, `onChargerOperatorInfo(list)`, `onTripInfo(info)`, `onNavigationStatus(status)`, `onDrivingInfo(info)`, `onCurrentRoadSpeedLimit(speedLimit)`, `onCameraAlert(list)`, `onCautionPointInfo(list)` |
| 오류 | `onError(errorCode)` |

주행 중 지속적으로 들어오는 콜백도 있습니다. `onCurrentRoadSpeedLimit`은 리스너 등록 후 매초 수신되고, `onCameraAlert`는 단속 카메라가 있을 때, `onNavigationStatus`는 주행 상태가 바뀔 때마다 수신됩니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper.listener/navi-helper-event-listener/entry)

경로 옵션 `RouteOption` 값: `RECOMMENDED`(추천), `SHORT_DISTANCE`(최단 거리), `SHORT_TIME`(최단 시간), `FREE`(무료 도로 우선), `HIGHWAY`(고속도로 우선), `COMFORTABLE`(편안한 경로), `SCHOOL_ZONE`(스쿨존 고려). 경유지는 최대 2개이며 `WaypointIndex.FIRST`, `WaypointIndex.SECOND`로 구분하고, `WaypointIndex.ALL`로 전부 제거합니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/routes) · [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/waypoint-settings)

## 활용

**기본 흐름.** 객체 생성 → `initialize()` → `addListener()` → 요청 API 호출 → 콜백 처리 → `removeListener()` → `release()` 순서입니다. 공식 가이드 예제를 하나로 이었습니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/intro)

```kotlin
import ai.pleos.playground.navi.constants.NaviErrorCode
import ai.pleos.playground.navi.data.RouteStartInfo
import ai.pleos.playground.navi.helper.NaviHelper
import ai.pleos.playground.navi.helper.listener.NaviHelperEventListener

private lateinit var naviHelper: NaviHelper

private val naviEventListener = object : NaviHelperEventListener {
    override fun onRouteStarted(routeStartInfo: RouteStartInfo) {
        // 경로 탐색 시작
    }
    override fun onRoutingCompleted() {
        // 경로 탐색 완료. 이 시점이 "목적지 설정 성공"이다.
    }
    override fun onDestinationInfo(destinationInfo: DestinationInfo) {
        // getDestinationInfo() 결과
    }
    override fun onError(errorCode: NaviErrorCode) {
        when (errorCode) {
            NaviErrorCode.CAUSE_LOW_BATTERY -> { /* 배터리 부족 안내 */ }
            NaviErrorCode.CAUSE_SEARCH_ROUTE_OVER_DISTANCE -> { /* 탐색 거리 초과 안내 */ }
            NaviErrorCode.CAUSE_SEARCH_ROUTE_TOO_SHORT_DISTANCE -> { /* 너무 가까움 안내 */ }
            NaviErrorCode.CAUSE_SEARCH_ROUTE -> { /* 서버 오류 처리 */ }
        }
    }
}

// 1. 초기화 (연결 결과 콜백이 있는 오버로드 권장)
naviHelper = NaviHelper(applicationContext)
naviHelper.initialize(
    onConnected = {
        // 2. 리스너 등록
        naviHelper.addListener(naviEventListener)

        // 3. 목적지 설정
        naviHelper.requestRoute(
            RouteInfo(
                latitude = 37.48544722,
                longitude = 127.03636666,
                displayLongitude = null,
                displayLatitude = null,
                poiName = "42dot",
                poiId = "36khp37hh8gqcm5nqhe5",
                poiSubId = "0",
                address = "서울시 강남구 남부순환로 2621"
            )
        )

        // 목적지 확인 요청 → onDestinationInfo 콜백으로 수신
        naviHelper.getDestinationInfo()
    },
    onFailed = { throwable ->
        // 초기 연결 재시도가 모두 실패
    }
)

// 4. 종료 시 정리
override fun onDestroy() {
    naviHelper.removeListener(naviEventListener)
    naviHelper.release()
    super.onDestroy()
}
```

[원본: 초기화·해제](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/initialization-and-release) · [원본: 리스너](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/event-listeners) · [원본: 목적지](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/destinations) · [원본: 에러](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/error-handling)

**경유지를 포함한 경로를 한 번에 요청.** 목적지는 `RouteInfo`, 경유지는 `RequestWaypointInfo`로 만들어 `RouteWithWaypointsRequest`에 담습니다. `NAVI_ROUTE_SEARCH` 퍼미션이 필요합니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/route-with-waypoints)

```kotlin
naviHelper.requestRouteWithWaypoints(
    RouteWithWaypointsRequest(
        destination = RouteInfo(
            latitude = 37.48544722, longitude = 127.03636666,
            poiName = "42dot", poiId = "36khp37hh8gqcm5nqhe5",
            address = "서울시 강남구 남부순환로 2621", poiSubId = "0"
        ),
        waypoints = listOf(
            RequestWaypointInfo(
                latitude = 37.41275833, longitude = 127.09481944,
                poiName = "판교 현대백화점", waypointIndex = WaypointIndex.FIRST,
                poiId = "36khp2hdj2cfcm5nqhe3",
                address = "경기도 성남시 수정구 창업로 0번길 6", poiSubId = "0"
            )
        )
    )
)
```

**경로 옵션 변경·재탐색·경유지 추가.** 활성 경로가 있는 상태에서 호출합니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/routes) · [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/waypoint-settings)

```kotlin
naviHelper.changeRouteOption(RouteOption.HIGHWAY)   // → onRouteOptionChanged
naviHelper.requestReRoute()                          // → onReRouteEnded
naviHelper.addWaypoint(
    RequestWaypointInfo(
        latitude = 37.41275833, longitude = 127.09481944,
        waypointIndex = WaypointIndex.FIRST,
        poiId = "36khp2hdj2cfcm5nqhe3", poiName = "소프트웨어드림센터",
        poiSubId = "0", address = "경기도 성남시 수정구 창업로40번길 6"
    )
)                                                    // → onWayPointInfo
naviHelper.removeWaypoint(WaypointIndex.FIRST)
naviHelper.cancelRoute()                             // → onRouteCancelled
```

**주의사항 (3.0.5 Breaking change 포함).** [원본](https://document.pleos.ai/docs/pleos-only/release-note/sdk-261015)

- 데이터 모델 생성자의 좌표 인자 순서가 `(longitude, latitude)`에서 `(latitude, longitude)`로 바뀌었습니다. 대상은 `BookmarkInfo`, `CurrentLocationInfo`, `DestinationInfo`, `RecentDestinationInfo`, `RequestWaypointInfo`, `RouteInfo`, `WaypointInfo`입니다. Kotlin에서는 named argument로 쓰면 안전합니다.
- `onNavigationStatus`의 인자 타입이 `RouteDriving`에서 `NavigationStatus`로 바뀌었습니다.
- `DestinationInfo`, `WaypointInfo`의 `totalDistance`/`totalTime`이 `remainDistance`/`remainTime`으로 바뀌었습니다.
- `onRouteEnded`는 deprecated이며 `onRoutingCompleted`를 씁니다. `FindRouteResult`, `onUserDefinedRoute`는 삭제됐습니다.
- `CameraType.SPEED_CAMERA_SECTION`이 `SPEED_CAMERA_SECTION_START`, `SECTION_SPEED_ALERT`, `SPEED_CAMERA_SECTION_END`로 세분화됐습니다.
- 리스너는 등록할 때 쓴 동일 인스턴스로 제거해야 하고, `release()` 뒤에 다시 쓰려면 객체를 새로 만들어 초기화합니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/initialization-and-release)
- 에뮬레이터 전제: Pleos Connect Emulator 설치, AVD 생성, Virtual Console 설정, 마이크 접근 설정, Cold boot 설정을 먼저 마칩니다. 기본 내비게이션 앱이 설치·설정된 환경이어야 응답이 옵니다. [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/intro)

**공식 샘플 앱.** `NaviDevApp`은 API를 골라 샘플 데이터로 실행하고 호출 로그와 콜백을 확인하는 앱입니다. 직접 호출 코드는 `app/src/main/java/ai/pleos/playground/navidevapp/screen/vm/NaviHelperViewModel.kt`, 샘플 요청 데이터는 `data/sample/`에 있습니다. 빌드는 `./gradlew assembleDebug`이며 저장소 토큰은 필요 없습니다. [원본](https://github.com/Pleos-Playground/pleos-sample-navi-app)

**예시: 공모전 아이디어에 쓸 조합.**

- 예시 1. 배달·방문 업무 앱: 방문지 목록을 `requestRouteWithWaypoints`로 한 번에 설정하고 `onWayPointArrived`로 도착 체크, `onTripInfo`로 남은 일정 표시.
- 예시 2. 안전운전 코치: `onCurrentRoadSpeedLimit`(매초)와 `onCameraAlert`, `onCautionPointInfo`를 Vehicle SDK 속도·상태 데이터와 결합해 실시간 피드백.
- 예시 3. 충전 계획 도우미: `getChargerOperatorInfo`와 `onDrivingInfo`를 EvBattery 도메인의 충전 상태와 함께 보여주고, 필요 시 `addWaypoint`로 충전소를 경유지에 추가.
