package com.hdhollow.studio.car

import com.hdhollow.studio.shared.LightingParams
import com.hdhollow.studio.shared.Protocol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StudioLightTest {

    @Test
    fun `사용자가 바꾸면 ts 가 찍히고 폰 값을 받으면 그 ts 를 따른다`() {
        var clock = 100L
        val light = StudioLight(now = { clock })
        assertEquals("기본값은 ts 0", 0L, light.params(Protocol.ROLE_CAR).ts)

        light.level = 0.3f
        assertEquals(100L, light.ts)
        clock = 200L
        light.chooseKelvin(3300)
        assertEquals(200L, light.ts)
        clock = 300L
        light.choosePreset("cyan")
        assertEquals(300L, light.ts)
        assertEquals(300L, light.params(Protocol.ROLE_CAR).ts)

        val fromPhone = LightingParams(kelvin = 4100, level = 0.9f, src = Protocol.ROLE_PHONE, ts = 250L)
        light.apply(fromPhone)
        assertEquals("받은 값의 ts 를 그대로", 250L, light.ts)
        assertEquals(4100, light.kelvin)
        assertNull(light.preset)

        // 더 오래된 폰 값은 지금 값을 대체하지 못하고 더 새 값은 대체한다
        assertFalse(fromPhone.copy(ts = 240L).supersedes(light.params(Protocol.ROLE_CAR)))
        assertTrue(fromPhone.copy(ts = 260L).supersedes(light.params(Protocol.ROLE_CAR)))
        // 같은 ts 면 폰이 이긴다(차 기본값이 폰 값을 덮지 않도록)
        assertTrue(fromPhone.supersedes(light.params(Protocol.ROLE_CAR)))
    }

    @Test
    fun `프리셋 적용도 사용자 변경이라 ts 가 찍힌다`() {
        val light = StudioLight(now = { 777L })
        Scenes.applyLight(Scenes.BUILT_IN[1], light)
        assertEquals(777L, light.ts)
    }
}
