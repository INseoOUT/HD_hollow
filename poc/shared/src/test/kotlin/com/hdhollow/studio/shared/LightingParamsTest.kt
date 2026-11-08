package com.hdhollow.studio.shared

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LightingParamsTest {

    @Test
    fun `toJson 과 fromJson 은 왕복이 같다`() {
        val p = LightingParams(kelvin = 3500, level = 0.8f, preset = null, src = Protocol.ROLE_PHONE, ts = 1731000000000L)
        assertEquals(p, LightingParams.fromJson(p.toJson()))
        val q = LightingParams(kelvin = 5000, level = 0.25f, preset = "cyan", src = Protocol.ROLE_CAR, ts = 7L)
        assertEquals(q, LightingParams.fromJson(q.toJson()))
    }

    @Test
    fun `범위 밖 값은 경계로 맞춘다`() {
        val p = LightingParams.fromJson("""{"v":1,"kelvin":100,"level":2.5,"preset":null,"src":"car","ts":0}""")!!
        assertEquals(LightingParams.KELVIN_MIN, p.kelvin)
        assertEquals(1f, p.level)
        val q = LightingParams(kelvin = 12000, level = -1f).normalized()
        assertEquals(LightingParams.KELVIN_MAX, q.kelvin)
        assertEquals(0f, q.level)
    }

    @Test
    fun `모르는 키는 무시하고 빠진 키는 기본값`() {
        val p = LightingParams.fromJson("""{"v":1,"kelvin":4200,"future":"x"}""")!!
        assertEquals(4200, p.kelvin)
        assertEquals(LightingParams.LEVEL_DEFAULT, p.level)
        assertNull(p.preset)
    }

    @Test
    fun `버전이 다르면 null`() {
        assertNull(LightingParams.fromJson("""{"v":2,"kelvin":4000,"level":0.5}"""))
        assertNull(LightingParams.fromJson("""{"kelvin":4000}"""))
    }

    @Test
    fun `JSON 이 아니면 null`() {
        assertNull(LightingParams.fromJson("not json"))
        assertNull(LightingParams.fromJson(""))
    }

    @Test
    fun `모르는 preset 은 null 로`() {
        val p = LightingParams.fromJson("""{"v":1,"kelvin":4000,"level":0.5,"preset":"neon"}""")!!
        assertNull(p.preset)
        assertEquals("blue", LightingParams(preset = "blue").normalized().preset)
    }

    @Test
    fun `sameLight 는 src 와 ts 를 보지 않는다`() {
        val a = LightingParams(kelvin = 3000, level = 0.5f, src = "car", ts = 1)
        val b = a.copy(src = "phone", ts = 2)
        assertTrue(a.sameLight(b))
        assertFalse(a.sameLight(b.copy(kelvin = 3100)))
        assertFalse(a.sameLight(b.copy(preset = "cyan")))
    }
}
