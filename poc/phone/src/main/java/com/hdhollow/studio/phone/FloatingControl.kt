package com.hdhollow.studio.phone

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import com.hdhollow.studio.shared.ColorTemp
import com.hdhollow.studio.shared.LightingParams
import kotlin.math.abs

/**
 * 다른 앱 위에 뜨는 작은 조절판(P12). 촬영 앱을 쓰는 중에도 밝기·색온도를 바꾼다.
 * SYSTEM_ALERT_WINDOW 권한이 있어야 한다([canDraw]). 전체 화면을 공유하면 차 화면에도 같이 비치므로
 * "앱 하나만 공유"와 함께 쓰라고 안내한다. Compose 는 오버레이 창에 생명주기 소유자가 필요해 일반 View 로 만든다.
 */
class FloatingControl(private val context: Context) {

    private val wm = context.getSystemService(WindowManager::class.java)
    private var root: View? = null
    private var swatch: View? = null
    private var label: TextView? = null

    val isShowing: Boolean get() = root != null

    fun show() {
        if (root != null || !canDraw(context)) return
        val params = WindowManager.LayoutParams(
            dp(PANEL_WIDTH_DP), // 버튼 글자('따뜻하게')가 줄바꿈되지 않게 폭 고정
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = dp(12)
            y = dp(120)
        }
        val view = build(params)
        runCatching { wm.addView(view, params) }
            .onSuccess { root = view }
    }

    fun hide() {
        root?.let { runCatching { wm.removeView(it) } }
        root = null
        swatch = null
        label = null
    }

    /** 현재 값 표시(색 견본, 라벨). 서비스가 조명 값이 바뀔 때 부른다. */
    fun render(p: LightingParams) {
        (swatch?.background as? GradientDrawable)?.setColor(ColorTemp.colorFor(p))
        label?.text = "${(p.level * 100).toInt()}% · ${p.preset?.let(::presetLabel) ?: "${p.kelvin}K"}"
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun build(params: WindowManager.LayoutParams): View {
        val column = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply { cornerRadius = dp(14).toFloat(); setColor(0xE6141618.toInt()) }
            setPadding(dp(10), dp(8), dp(10), dp(8))
        }
        val head = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        swatch = View(context).apply {
            background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(Color.WHITE) }
            layoutParams = LinearLayout.LayoutParams(dp(18), dp(18)).apply { marginEnd = dp(8) }
        }
        label = TextView(context).apply { setTextColor(Color.WHITE); setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f); isSingleLine = true }
        head.addView(swatch)
        head.addView(label)
        column.addView(head)
        column.addView(buttonRow("따뜻하게" to { kelvin(-LightingParams.KELVIN_NOTIFICATION_STEP) }, "차갑게" to { kelvin(+LightingParams.KELVIN_NOTIFICATION_STEP) }))
        column.addView(buttonRow("어둡게" to { level(-LightingParams.LEVEL_DIAL_STEP * 2) }, "밝게" to { level(+LightingParams.LEVEL_DIAL_STEP * 2) }))

        // 드래그로 옮긴다. 조금만 움직인 터치는 버튼 클릭으로 둔다
        var downX = 0f; var downY = 0f; var startX = 0; var startY = 0
        head.setOnTouchListener { v, e ->
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> { downX = e.rawX; downY = e.rawY; startX = params.x; startY = params.y; true }
                MotionEvent.ACTION_MOVE -> {
                    params.x = startX + (e.rawX - downX).toInt()
                    params.y = startY + (e.rawY - downY).toInt()
                    root?.let { runCatching { wm.updateViewLayout(it, params) } }
                    true
                }
                MotionEvent.ACTION_UP -> { if (abs(e.rawX - downX) < dp(4) && abs(e.rawY - downY) < dp(4)) v.performClick(); true }
                else -> false
            }
        }
        render(MirrorService.lighting.value)
        return column
    }

    private fun buttonRow(vararg items: Pair<String, () -> Unit>): View {
        val row = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
        items.forEach { (text, onClick) ->
            row.addView(TextView(context).apply {
                this.text = text
                setTextColor(Color.WHITE)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                isSingleLine = true
                background = GradientDrawable().apply { cornerRadius = dp(999).toFloat(); setColor(0xFF2A2E36.toInt()) }
                setPadding(dp(12), dp(6), dp(12), dp(6))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(dp(2), dp(4), dp(2), 0)
                }
                gravity = Gravity.CENTER
                setOnClickListener { onClick() }
            })
        }
        return row
    }

    private fun kelvin(delta: Int) = MirrorService.updateLighting { it.copy(kelvin = it.kelvin + delta, preset = null) }
    private fun level(delta: Float) = MirrorService.updateLighting { it.copy(level = it.level + delta) }

    private fun dp(v: Int): Int = (v * context.resources.displayMetrics.density).toInt()

    companion object {
        private const val PANEL_WIDTH_DP = 190

        fun canDraw(context: Context): Boolean = Settings.canDrawOverlays(context)

        fun presetLabel(name: String): String = when (name) {
            "cyan" -> "시안"
            "blue" -> "블루"
            "purple" -> "퍼플"
            else -> name
        }
    }
}
