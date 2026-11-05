package com.hdhollow.studio.car

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction
import com.android.car.ui.utils.DirectManipulationHelper

/**
 * 다이얼(CCP) 입력을 받는 투명 뷰.
 *
 * AAOS RotaryService는 회전을 앱에 직접 보내지 않는다. 포커스된 뷰가 car-ui `FocusArea` 안에 있고
 * rotate-directly로 표시돼 있으면, 다이얼을 한 번 눌러 직접 조작 모드에 들어간 뒤부터
 * 회전을 ACTION_SCROLL_FORWARD/BACKWARD로 보낸다(누르기 전 회전은 포커스 이동).
 * 위/아래 nudge = 밝기 ↔ 색 전환. 키보드(←/→/↑/↓/Enter)는 에뮬레이터 폴백.
 */
@SuppressLint("ViewConstructor")
class DialTarget(
    context: Context,
    private val onRotate: (Int) -> Unit,
    private val onPress: () -> Unit,
) : View(context) {

    init {
        isFocusable = true
        isFocusableInTouchMode = true // 터치로 시작해도 키보드 폴백이 바로 먹도록
        defaultFocusHighlightEnabled = false // 전체 화면 회색 포커스 표시 끔
        DirectManipulationHelper.setSupportsRotateDirectly(this, true)
    }

    // 다이얼을 먼저 누르지 않아도 회전이 바로 먹도록, 포커스를 받으면 직접 조작 모드를 요청한다
    override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: android.graphics.Rect?) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
        if (gainFocus) DirectManipulationHelper.enableDirectManipulationMode(this, true)
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        if (hasWindowFocus && isFocused) DirectManipulationHelper.enableDirectManipulationMode(this, true)
    }

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        info.isScrollable = true
        info.addAction(AccessibilityAction.ACTION_SCROLL_FORWARD)
        info.addAction(AccessibilityAction.ACTION_SCROLL_BACKWARD)
    }

    override fun performAccessibilityAction(action: Int, arguments: Bundle?): Boolean {
        Log.d("DialTarget", "a11y action=$action")
        return when (action) {
            AccessibilityNodeInfo.ACTION_SCROLL_FORWARD -> { onRotate(+1); true }
            AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD -> { onRotate(-1); true }
            else -> super.performAccessibilityAction(action, arguments)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        Log.d("DialTarget", "key=${KeyEvent.keyCodeToString(keyCode)}")
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_EQUALS -> onRotate(+1)
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_MINUS -> onRotate(-1)
            // 직접 조작 모드에서는 가운데 누름을 RotaryService가 모드 종료에 쓰므로, 상하 nudge로 전환
            KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> onPress()
            else -> return super.onKeyDown(keyCode, event)
        }
        return true
    }
}
