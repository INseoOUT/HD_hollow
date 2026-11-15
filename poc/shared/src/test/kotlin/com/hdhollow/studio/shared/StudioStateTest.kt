package com.hdhollow.studio.shared

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StudioStateTest {

    private val userScene = Scene("user-7", "내 프리셋 1", 0.4f, kelvin = 4300, ambientLinked = false)

    @Test
    fun `STATE 는 JSON 왕복이 같다`() {
        val s = StudioState(
            ambientLinked = false, fullLight = true, dialColor = true, theme = StudioState.THEME_NAVY,
            userScenes = listOf(userScene), src = Protocol.ROLE_PHONE,
        )
        assertEquals(s, StudioState.fromJson(s.toJson()))
        assertEquals(StudioState(), StudioState.fromJson(StudioState().toJson()))
    }

    @Test
    fun `버전이 다르거나 JSON 이 아니면 null, 모르는 테마는 기본값, 기본 프리셋 id 는 버린다`() {
        assertNull(StudioState.fromJson("""{"v":99,"theme":"navy"}"""))
        assertNull(StudioState.fromJson("not json"))
        assertEquals(StudioState.THEME_DEFAULT, StudioState.fromJson("""{"v":1,"theme":"pink"}""")!!.theme)
        val withBuiltIn = """{"v":1,"scenes":[{"id":"daily","name":"x","level":0.5,"kelvin":5000,"preset":null},{"id":"user-1","name":"y","level":0.5,"kelvin":5000,"preset":null},{"bad":1}]}"""
        assertEquals(listOf("user-1"), StudioState.fromJson(withBuiltIn)!!.userScenes.map { it.id })
    }

    @Test
    fun `sameState 는 보낸 쪽만 빼고 비교한다`() {
        val car = StudioState(theme = StudioState.THEME_GREIGE, userScenes = listOf(userScene), src = Protocol.ROLE_CAR)
        assertTrue(car.sameState(car.copy(src = Protocol.ROLE_PHONE)))
        assertFalse(car.sameState(car.copy(fullLight = true)))
        assertFalse(car.sameState(car.copy(userScenes = emptyList())))
    }

    @Test
    fun `테마 목록에 기본 테마가 있고 중복이 없다`() {
        assertTrue(StudioState.THEME_DEFAULT in StudioState.THEMES)
        assertEquals(StudioState.THEMES.size, StudioState.THEMES.toSet().size)
    }

    @Test
    fun `COMMAND 는 JSON 왕복이 같고 cmd 가 없으면 null`() {
        val apply = StudioCommand(StudioCommand.SCENE_APPLY, "warm")
        val save = StudioCommand(StudioCommand.SCENE_SAVE)
        assertEquals(apply, StudioCommand.fromJson(apply.toJson()))
        assertEquals(save, StudioCommand.fromJson(save.toJson()))
        assertNull(StudioCommand.fromJson("""{"v":1}"""))
        assertNull(StudioCommand.fromJson("""{"v":2,"cmd":"scene.save"}"""))
    }

    @Test
    fun `STATE 는 프리셋 목록 때문에 상한이 크고 COMMAND 는 작은 메시지다`() {
        assertEquals(Protocol.STATE_MAX, Protocol.maxPayload(Protocol.TYPE_STATE))
        assertEquals(Protocol.SMALL_MAX, Protocol.maxPayload(Protocol.TYPE_COMMAND))
        val many = StudioState(userScenes = (1..200).map { Scene("user-$it", "내 프리셋 $it", 0.5f, kelvin = 5000) })
        assertTrue(many.toJson().toByteArray().size in (Protocol.SMALL_MAX + 1)..Protocol.STATE_MAX)
    }
}
