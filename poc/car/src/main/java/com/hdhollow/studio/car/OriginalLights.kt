package com.hdhollow.studio.car

import android.content.SharedPreferences
import org.json.JSONException
import org.json.JSONObject

/**
 * 시작할 때 읽은 조명의 원래 값. 비정상 종료(강제 종료·크래시)로 복원을 못 하면 다음 실행이 이어받아 되돌린다.
 * 키는 [SeatArea.key] 문자열, 값은 SDK 의 Int(밝기·ARGB) 또는 LightSwitch 이름. (2026-11-09 결정, A안)
 */
data class Originals(
    val ambientBrightness: Map<String, Int> = emptyMap(),
    val ambientColor: Map<String, Int> = emptyMap(),
    val ambientSwitch: Map<String, String> = emptyMap(),
    val cabin: String? = null,
    val reading: Map<String, String> = emptyMap(),
) {
    val isEmpty: Boolean
        get() = ambientBrightness.isEmpty() && ambientColor.isEmpty() && ambientSwitch.isEmpty() && cabin == null && reading.isEmpty()

    fun toJson(): JSONObject = JSONObject()
        .put("ambientBrightness", JSONObject(ambientBrightness))
        .put("ambientColor", JSONObject(ambientColor))
        .put("ambientSwitch", JSONObject(ambientSwitch))
        .put("cabin", cabin ?: JSONObject.NULL)
        .put("reading", JSONObject(reading))

    companion object {
        fun fromJson(text: String): Originals? = try {
            val o = JSONObject(text)
            Originals(
                ambientBrightness = o.optJSONObject("ambientBrightness").ints(),
                ambientColor = o.optJSONObject("ambientColor").ints(),
                ambientSwitch = o.optJSONObject("ambientSwitch").strings(),
                cabin = if (o.isNull("cabin")) null else o.getString("cabin"),
                reading = o.optJSONObject("reading").strings(),
            )
        } catch (_: JSONException) {
            null
        }

        private fun JSONObject?.ints(): Map<String, Int> =
            this?.let { o -> o.keys().asSequence().associateWith { o.getInt(it) } } ?: emptyMap()

        private fun JSONObject?.strings(): Map<String, String> =
            this?.let { o -> o.keys().asSequence().associateWith { o.getString(it) } } ?: emptyMap()
    }
}

/**
 * [Originals] 저장소(SharedPreferences). 조명을 처음 바꿀 때 dirty 로 표시하고 복원을 마치면 지운다.
 * 시작 때 dirty 가 남아 있으면 지난 실행이 복원 못 하고 죽은 것이므로 그 값을 이번 세션의 원래 값으로 쓴다.
 */
class OriginalStore(private val prefs: SharedPreferences) {
    /** 지난 실행이 남긴 미복원 원래 값. 없으면 null. */
    fun pending(): Originals? =
        if (prefs.getBoolean(KEY_DIRTY, false)) prefs.getString(KEY_JSON, null)?.let(Originals::fromJson) else null

    fun save(o: Originals) {
        prefs.edit().putString(KEY_JSON, o.toJson().toString()).apply()
    }

    fun markDirty() {
        prefs.edit().putBoolean(KEY_DIRTY, true).apply()
    }

    fun clear() {
        prefs.edit().remove(KEY_DIRTY).remove(KEY_JSON).apply()
    }

    companion object {
        const val KEY_DIRTY = "dirty"
        const val KEY_JSON = "originals"
    }
}
