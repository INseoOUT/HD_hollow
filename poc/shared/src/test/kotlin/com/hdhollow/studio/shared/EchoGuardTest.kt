package com.hdhollow.studio.shared

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EchoGuardTest {

    private val guard = EchoGuard<LightingParams> { a, b -> a.sameLight(b) }
    private val daily = LightingParams(kelvin = 7000, level = 0.7f)
    private val warm = LightingParams(kelvin = 3200, level = 0.85f)

    @Test
    fun `받은 적이 없으면 내 값은 보낸다`() {
        assertTrue(guard.shouldSend(daily))
        assertTrue(guard.shouldSend(daily))
    }

    @Test
    fun `받은 값이 되돌아온 것은 보내지 않는다`() {
        guard.received(daily.copy(src = Protocol.ROLE_PHONE, ts = 5L))
        assertFalse(guard.shouldSend(daily.copy(src = Protocol.ROLE_CAR)))
        assertFalse("같은 값이 다시 흘러와도(재전송 계기) 되돌이다", guard.shouldSend(daily))
    }

    @Test
    fun `받은 값에서 다른 값으로 갔다가 돌아오면 다시 보낸다`() {
        guard.received(daily)
        assertFalse(guard.shouldSend(daily))
        assertTrue(guard.shouldSend(warm))
        assertTrue("A 받음 → B → A: 마지막 A 는 내가 바꾼 것이다", guard.shouldSend(daily))
    }

    @Test
    fun `끊기면 잊는다`() {
        guard.received(daily)
        guard.reset()
        assertTrue(guard.shouldSend(daily))
    }

    @Test
    fun `앱 상태도 같은 규칙`() {
        val state = EchoGuard<StudioState> { a, b -> a.sameState(b) }
        val on = StudioState(fullLight = true, src = Protocol.ROLE_PHONE)
        val off = StudioState(fullLight = false, src = Protocol.ROLE_CAR)
        state.received(on)
        assertFalse(state.shouldSend(on.copy(src = Protocol.ROLE_CAR)))
        assertTrue(state.shouldSend(off))
        assertTrue(state.shouldSend(on.copy(src = Protocol.ROLE_CAR)))
    }
}
