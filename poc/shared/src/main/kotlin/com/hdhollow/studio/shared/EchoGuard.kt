package com.hdhollow.studio.shared

/**
 * 상대에게서 받은 값을 내 상태에 넣으면 내 상태가 바뀌었다고 다시 보내게 된다(되돌이). 그 한 번을 걸러 낸다.
 *
 * 받은 값과 다른 값을 보내는 순간 받은 값은 잊는다. 잊지 않으면 A(받음) → B(내가 바꿈) → A(내가 다시 바꿈)에서
 * 마지막 A 를 되돌이로 잘못 보고 보내지 않아 상대가 B 에 머문다(2026-11-13 에뮬레이터에서 확인:
 * 차에서 데일리 → 웜 뷰티 → 네온 → 데일리로 바꿨을 때 폰이 네온에 남았다).
 * [same] 은 보낸 쪽·시각처럼 내용이 아닌 것을 빼고 비교한다([LightingParams.sameLight], [StudioState.sameState]).
 */
class EchoGuard<T>(private val same: (T, T) -> Boolean) {
    private var received: T? = null

    /** 상대에게서 [value] 를 받아 내 상태에 넣기 직전에 부른다. */
    @Synchronized
    fun received(value: T) {
        received = value
    }

    /** 내 값 [value] 를 상대에게 보내야 하는가. 방금 받은 값이 되돌아온 것이면 false. */
    @Synchronized
    fun shouldSend(value: T): Boolean {
        val echo = received?.let { same(it, value) } == true
        if (!echo) received = null
        return !echo
    }

    /** 연결이 끊겼을 때 부른다. 다시 붙으면 무조건 한 번 보내도록 받은 값을 잊는다. */
    @Synchronized
    fun reset() {
        received = null
    }
}
