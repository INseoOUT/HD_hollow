package com.hdhollow.studio.shared

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * 조명 값([LightingParams]) 말고 차와 폰이 같이 보는 스튜디오 앱 상태(STATE). 차량 제어 값이 아니라 우리 앱의 화면·동작 상태다.
 *
 * - [ambientLinked] 링라이트 색·밝기를 앰비언트에도 넣는다.
 * - [fullLight] 풀 라이트(모니터·패널을 숨기고 차 화면 전체를 조명으로).
 * - [dialColor] 다이얼이 조절하는 대상. false=밝기, true=흰 빛 톤(색온도).
 * - [theme] 차 화면 테마([THEMES] 중 하나. 실내 마감에 맞춘 어두운 세트). 폰 화면의 라이트·다크와는 별개다.
 * - [userScenes] 사용자 프리셋. 차가 저장하고 폰은 받아서 보여 준다(폰이 보낸 목록은 차가 무시한다).
 * - [src] 보낸 쪽.
 *
 * 차가 기준이다: 차는 HELLO 를 받으면 지금 상태를 보내고 바뀔 때마다 보낸다. 폰은 사용자가 바꾼 순간에만 보낸다.
 * 프리셋 저장·삭제·적용은 [StudioCommand] 로 차에 부탁한다.
 */
data class StudioState(
    val ambientLinked: Boolean = true,
    val fullLight: Boolean = false,
    val dialColor: Boolean = false,
    val theme: String = THEME_DEFAULT,
    val userScenes: List<Scene> = emptyList(),
    val src: String = Protocol.ROLE_CAR,
) {
    /** 보낸 쪽을 빼고 비교. 되돌아온 자기 값을 걸러낼 때 쓴다. */
    fun sameState(other: StudioState): Boolean = copy(src = "") == other.copy(src = "")

    fun toJson(): String = JSONObject()
        .put("v", Protocol.VERSION)
        .put("ambientLinked", ambientLinked)
        .put("fullLight", fullLight)
        .put("dialColor", dialColor)
        .put("theme", theme)
        .put("scenes", JSONArray().also { a -> userScenes.forEach { a.put(Scenes.toJson(it)) } })
        .put("src", src)
        .toString()

    companion object {
        const val THEME_BLACK = "black"
        const val THEME_GREIGE = "greige"
        const val THEME_NAVY = "navy"
        const val THEME_DEFAULT = THEME_BLACK
        val THEMES: List<String> = listOf(THEME_BLACK, THEME_GREIGE, THEME_NAVY)

        /** v 가 다르면 null. 모르는 테마는 기본값으로, 읽지 못한 프리셋·기본 프리셋 id 는 버린다. */
        fun fromJson(text: String): StudioState? = try {
            val o = JSONObject(text)
            if (o.optInt("v", -1) != Protocol.VERSION) {
                null
            } else {
                val arr = o.optJSONArray("scenes") ?: JSONArray()
                StudioState(
                    ambientLinked = o.optBoolean("ambientLinked", true),
                    fullLight = o.optBoolean("fullLight", false),
                    dialColor = o.optBoolean("dialColor", false),
                    theme = o.optString("theme", THEME_DEFAULT).takeIf { it in THEMES } ?: THEME_DEFAULT,
                    userScenes = (0 until arr.length()).mapNotNull { i -> arr.optJSONObject(i)?.let(Scenes::fromJson) }.filterNot { it.builtIn },
                    src = o.optString("src", Protocol.ROLE_CAR),
                )
            }
        } catch (_: JSONException) {
            null
        }
    }
}

/**
 * 폰 → 차 명령(COMMAND). 프리셋은 차가 저장하고 실내등·독서등도 차가 다루므로 폰은 부탁만 한다.
 * [cmd] 는 [SCENE_APPLY](id 의 프리셋 적용), [SCENE_SAVE](지금 값을 새 프리셋으로), [SCENE_DELETE](id 의 사용자 프리셋 삭제).
 */
data class StudioCommand(val cmd: String, val id: String? = null) {
    fun toJson(): String = JSONObject().put("v", Protocol.VERSION).put("cmd", cmd).put("id", id ?: JSONObject.NULL).toString()

    companion object {
        const val SCENE_APPLY = "scene.apply"
        const val SCENE_SAVE = "scene.save"
        const val SCENE_DELETE = "scene.delete"

        fun fromJson(text: String): StudioCommand? = try {
            val o = JSONObject(text)
            val cmd = o.optString("cmd", "")
            if (o.optInt("v", -1) != Protocol.VERSION || cmd.isEmpty()) null
            else StudioCommand(cmd, if (o.isNull("id")) null else o.optString("id"))
        } catch (_: JSONException) {
            null
        }
    }
}
