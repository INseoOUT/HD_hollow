package com.hdhollow.studio.shared

import org.json.JSONException
import org.json.JSONObject

/**
 * 폰 → 차 공유 상태(STATUS). 차가 검은 화면 대신 이유를 안내하기 위해서다.
 * - [visible] "앱 하나만 공유"에서 그 앱이 앞에 없으면 false(검은 프레임만 온다). Android 14+ onCapturedContentVisibilityChanged.
 * - [stopped] 공유가 끝났다. [reason] "user"(사용자가 중지) 또는 "projection"(공유 앱 종료·시스템 중지).
 * 폰은 HELLO 를 받을 때 현재 상태를 한 번 보내고, 바뀔 때마다 보낸다.
 */
data class ShareStatus(val visible: Boolean = true, val stopped: Boolean = false, val reason: String? = null) {
    fun toJson(): String = JSONObject()
        .put("v", Protocol.VERSION).put("visible", visible).put("stopped", stopped).put("reason", reason ?: JSONObject.NULL)
        .toString()

    companion object {
        fun fromJson(text: String): ShareStatus? = try {
            val o = JSONObject(text)
            if (o.optInt("v", -1) != Protocol.VERSION) {
                null
            } else {
                ShareStatus(
                    visible = o.optBoolean("visible", true),
                    stopped = o.optBoolean("stopped", false),
                    reason = if (o.isNull("reason")) null else o.optString("reason"),
                )
            }
        } catch (_: JSONException) {
            null
        }
    }
}
