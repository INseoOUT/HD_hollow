package com.hdhollow.studio.car

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * 디버그 빌드 전용 명령 수신기(테스트 자동화). 화면이 떠 있을 때만 동작한다(DebugHooks).
 *   adb shell am broadcast -n com.hdhollow.studio.car/.DebugReceiver --ei kelvin 3300 --ef level 0.9 --es preset cyan|none --es scene "웜 뷰티" --ez full true --es theme black|greige|navy
 */
class DebugReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val light = DebugHooks.light ?: return Unit.also { Log.w(TAG, "화면이 없다") }
        val e = intent.extras ?: return
        e.getString("scene")?.let { name -> Log.d(TAG, "프리셋 '$name' → ${DebugHooks.applyScene?.invoke(name)}") }
        if (e.containsKey("kelvin")) light.chooseKelvin(e.getInt("kelvin"))
        if (e.containsKey("level")) light.level = e.getFloat("level")
        if (e.containsKey("preset")) light.choosePreset(e.getString("preset")?.takeIf { it != "none" })
        if (e.containsKey("full")) light.fullLight = e.getBoolean("full")
        e.getString("theme")?.let { light.theme = it }
        Log.d(TAG, "적용 ${light.params("car")} full=${light.fullLight} theme=${light.theme}")
    }

    companion object {
        private const val TAG = "DebugReceiver"
    }
}
