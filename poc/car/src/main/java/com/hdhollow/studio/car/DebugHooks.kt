package com.hdhollow.studio.car

/**
 * 디버그 빌드의 DebugReceiver(테스트 자동화)가 실행 중인 화면의 조명 상태에 닿는 고리. MainActivity 가 살아 있는 동안만 채워진다.
 * 릴리스 빌드에는 리시버가 없어 아무도 읽지 않는다.
 */
object DebugHooks {
    @Volatile var light: StudioLight? = null
    /** 이름으로 프리셋 적용(기본 + 사용자). 없으면 false. */
    @Volatile var applyScene: ((String) -> Boolean)? = null
}
