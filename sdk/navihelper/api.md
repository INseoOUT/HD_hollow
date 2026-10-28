# NaviHelper SDK 함수 목록

> 마지막 확인일: 2026-10-28  
> 기준 버전: NaviHelper SDK 3.0.5  
> 원본:
> - [NaviHelper API Reference](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/entry)
> - [소개](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/intro)
> - [SDK 가져오기](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/importing-sdk)
> - [권한 선언](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/declaring-permissions)
> - [초기화와 해제](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/initialization-and-release)
> - [이벤트 리스너](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/event-listeners)
> - [목적지](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/destinations)
> - [경로](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/routes)
> - [경유지 설정](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/waypoint-settings)
> - [경유지 포함 경로 요청](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/route-with-waypoints)
> - [에러 처리](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/error-handling)

설명은 공식 KDoc 원문(영문)입니다. 요청 함수의 결과는 `NaviHelperEventListener` 콜백으로 돌아옵니다.

## NaviHelper 클래스

The entry point of the NaviHelper SDK. Create a NaviHelper instance with an Android Context to call NaviHelper APIs directly and register event-listener callbacks.

원본: [NaviHelper API Reference](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper/navi-helper/entry)

| 함수 | 파라미터 | 설명 | 권한 | 원본 |
|---|---|---|---|---|
| `initialize` | `()` | Initializes NaviHelper SDK. When the app starts or is created, it should call the initialization of SDK. | - | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper/navi-helper/initialize) |
| `initialize` | `onConnected: () -> Unit, onFailed: (Throwable) -> Unit` | Initializes NaviHelper SDK with connection callbacks. Success is called when the first service connection is established. Failure is called when the initial connection retries are exhausted. | - | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper/navi-helper/initialize) |
| `release` | `()` | Releases NaviHelper SDK. When the app terminates or is destroyed, it should call the release of SDK | - | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper/navi-helper/release) |
| `addListener` | `listener: NaviHelperEventListener` | Adds a listener. Registers a listener for NaviHelper callback function | - | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper/navi-helper/add-listener) |
| `removeListener` | `listener: NaviHelperEventListener` | Removes a listener. | - | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper/navi-helper/remove-listener) |
| `requestRoute` | `routeInfo: RouteInfo` | Requests route. | `NAVI_ROUTE_SEARCH` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper/navi-helper/request-route) |
| `requestRouteWithWaypoints` | `request: RouteWithWaypointsRequest` | Requests a route with waypoints. | `NAVI_ROUTE_SEARCH` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper/navi-helper/request-route-with-waypoints) |
| `cancelRoute` | `()` | Cancels route. | `NAVI_ROUTE_SEARCH` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper/navi-helper/cancel-route) |
| `addWaypoint` | `requestWaypointInfo: RequestWaypointInfo` | Adds waypoint. | `NAVI_ROUTE_SEARCH` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper/navi-helper/add-waypoint) |
| `removeWaypoint` | `waypointIndex: WaypointIndex` | Removes waypoint. | `NAVI_ROUTE_SEARCH` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper/navi-helper/remove-waypoint) |
| `changeRouteOption` | `routeOption: RouteOption` | Changes route option. | `NAVI_ROUTE_SEARCH` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper/navi-helper/change-route-option) |
| `requestReRoute` | `()` | Requests re-route. | `NAVI_ROUTE_SEARCH` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper/navi-helper/request-re-route) |
| `getBookmarkInfo` | `()` | Gets bookmark info. | `NAVI_ROUTE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper/navi-helper/get-bookmark-info) |
| `getRecentDestinationInfo` | `()` | Gets recent destination info. | `NAVI_ROUTE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper/navi-helper/get-recent-destination-info) |
| `getRouteStateInfo` | `()` | Gets route state info. | `NAVI_ROUTE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper/navi-helper/get-route-state-info) |
| `getCurrentLocationInfo` | `()` | Gets current location info. | `NAVI_ROUTE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper/navi-helper/get-current-location-info) |
| `getDestinationInfo` | `()` | Gets destination info. | `NAVI_ROUTE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper/navi-helper/get-destination-info) |
| `getWaypointInfo` | `()` | Gets waypoint info. | `NAVI_ROUTE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper/navi-helper/get-waypoint-info) |
| `getTBTInfo` | `()` | Gets TBT info. | `NAVI_ROUTE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper/navi-helper/get-t-b-t-info) |
| `getChargerOperatorInfo` | `()` | Gets Charger Operator info. | `NAVI_ROUTE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper/navi-helper/get-charger-operator-info) |
| `requestNavigationScreen` | `requestScreen: NavigationScreenRequest` | Requests navigation screen. | `NAVI_CUSTOM_MAP` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper/navi-helper/request-navigation-screen) |
| `requestTrip` | `tripRequest: TripRequest` | Requests trip. | `NAVI_ROUTE` | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper/navi-helper/request-trip) |

## NaviHelperEventListener (리스너)

Interface definition for callback data received from the Navi application.

원본: [NaviHelperEventListener API Reference](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper.listener/navi-helper-event-listener/entry)

| 콜백 | 파라미터 | 설명 | 원본 |
|---|---|---|---|
| `onRouteStarted` | `routeStartInfo: RouteStartInfo` | Called when navigation route search begins. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper.listener/navi-helper-event-listener/on-route-started) |
| `onRoutingCompleted` | `()` | Called when navigation route search is completed. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper.listener/navi-helper-event-listener/on-routing-completed) |
| `onReRouteEnded` | `()` | Called when navigation re-route is completed. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper.listener/navi-helper-event-listener/on-re-route-ended) |
| `onRouteOptionChanged` | `routeOption: RouteOption` | Called when the route type is changed. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper.listener/navi-helper-event-listener/on-route-option-changed) |
| `onRouteCancelled` | `()` | Called when the current route is canceled. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper.listener/navi-helper-event-listener/on-route-cancelled) |
| `onWaypointChanged` | `waypointChangedInfo: WaypointChangedInfo` | Called when arriving at or removing the waypoint. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper.listener/navi-helper-event-listener/on-waypoint-changed) |
| `onDestinationArrived` | `destinationArrivedInfo: DestinationArrivedInfo` | Called when arriving at the destination. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper.listener/navi-helper-event-listener/on-destination-arrived) |
| `onWayPointArrived` | `waypointArrivedInfo: WaypointArrivedInfo` | Called when arriving at the waypoint. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper.listener/navi-helper-event-listener/on-way-point-arrived) |
| `onBookMarkChanged` | `()` | Called when arriving at or removing the bookmark. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper.listener/navi-helper-event-listener/on-book-mark-changed) |
| `onDestinationChanged` | `()` | Called when arriving at or removing the destination. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper.listener/navi-helper-event-listener/on-destination-changed) |
| `onError` | `errorCode: NaviErrorCode` | Called when a navigation error occurs. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper.listener/navi-helper-event-listener/on-error) |
| `onCurrentLocationInfo` | `currentLocationInfo: CurrentLocationInfo` | Called when the current location information is changed. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper.listener/navi-helper-event-listener/on-current-location-info) |
| `onRouteStateInfo` | `routeStateInfo: RouteStateInfo` | Called when the route state information is changed. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper.listener/navi-helper-event-listener/on-route-state-info) |
| `onDestinationInfo` | `destinationInfo: DestinationInfo` | Called when the destination information is changed. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper.listener/navi-helper-event-listener/on-destination-info) |
| `onWayPointInfo` | `waypointInfoList: List<WaypointInfo>` | Called when the waypoint information is changed. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper.listener/navi-helper-event-listener/on-way-point-info) |
| `onRecentDestinationInfo` | `recentDestinationInfoList: List<RecentDestinationInfo>` | Called when the recent destination information is changed. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper.listener/navi-helper-event-listener/on-recent-destination-info) |
| `onBookmarkInfo` | `bookmarkInfoList: List<BookmarkInfo>` | Called when the bookmark information is changed. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper.listener/navi-helper-event-listener/on-bookmark-info) |
| `onCurrentRoadSpeedLimit` | `speedLimit: Int` | Called when the road speed limit is notified. Received every second after the event listener registration. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper.listener/navi-helper-event-listener/on-current-road-speed-limit) |
| `onCameraAlert` | `cameraAlertList: List<CameraAlert>` | Called when camera alerts are notified. Received when speeding camera exists. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper.listener/navi-helper-event-listener/on-camera-alert) |
| `onDrivingInfo` | `drivingInfo: DrivingInfo` | Called when driving information is notified. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper.listener/navi-helper-event-listener/on-driving-info) |
| `onNavigationStatus` | `status: NavigationStatus` | Called when the navigation status is notified. Received whenever the driving status changes | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper.listener/navi-helper-event-listener/on-navigation-status) |
| `onTBTInfo` | `tbtInfoList: List<TBTInfo>` | Called when the TBT information is notified. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper.listener/navi-helper-event-listener/on-t-b-t-info) |
| `onChargerOperatorInfo` | `chargerOperatorList: List<ChargerOperator>` | Called when charger operator information is notified. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper.listener/navi-helper-event-listener/on-charger-operator-info) |
| `onCautionPointInfo` | `cautionPointList: List<CautionPoint>` | Called when caution point information is notified. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper.listener/navi-helper-event-listener/on-caution-point-info) |
| `onTripInfo` | `tripInfo: TripInfo` | Called when trip information is notified. | [원본](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.helper.listener/navi-helper-event-listener/on-trip-info) |

## 관련 타입 (데이터 모델·상수)

함수 파라미터와 콜백에 쓰이는 타입은 공식 API Reference의 패키지 페이지에서 확인합니다.

- [ai.pleos.playground.navi.data (요청·응답 모델)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.data/entry)
- [ai.pleos.playground.navi.constants (상수·Enum)](https://document.pleos.ai/docs/pleos-only/connect-sdk-pleos/NaviHelper/navi-helper/ai.pleos.playground.navi.constants/entry)
