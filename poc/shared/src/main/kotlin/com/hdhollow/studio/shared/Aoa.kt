package com.hdhollow.studio.shared

/**
 * Android Open Accessory(AOA) 상수. 차(USB 호스트)가 폰을 액세서리 모드로 바꿀 때 보내는 문자열과,
 * 폰이 액세서리 모드로 다시 열거될 때 쓰는 Google VID/PID. 폰 쪽 accessory_filter.xml 의 값과 같아야 한다.
 * plan/work-plan.md 2.1절 2차 경로. 실차에서 서드파티 앱이 USB 호스트 API 를 쓸 수 있는지는 Pleos 확인 전까지 모른다.
 */
object Aoa {
    const val MANUFACTURER = "HDhollow"
    const val MODEL = "Selene"
    const val DESCRIPTION = "Selene phone link"
    const val VERSION = "1"
    const val URI = "https://github.com/INseoOUT/HD_hollow"
    const val SERIAL = "0001"

    /** 액세서리 모드의 폰이 쓰는 Google VID 와 PID(0x2D00 accessory, 0x2D01 accessory+ADB). */
    const val GOOGLE_VID = 0x18D1
    val ACCESSORY_PIDS: IntRange = 0x2D00..0x2D05

    /** AOA 제어 요청. */
    const val REQ_GET_PROTOCOL = 51
    const val REQ_SEND_STRING = 52
    const val REQ_START = 53

    const val STR_MANUFACTURER = 0
    const val STR_MODEL = 1
    const val STR_DESCRIPTION = 2
    const val STR_VERSION = 3
    const val STR_URI = 4
    const val STR_SERIAL = 5
}
