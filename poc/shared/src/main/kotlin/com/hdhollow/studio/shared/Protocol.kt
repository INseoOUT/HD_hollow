package com.hdhollow.studio.shared

/**
 * 폰↔차 계약 상수. plan/spec.md 2절.
 *
 * 연결 하나(차 → 폰 접속) 위에 길이 접두 메시지가 양방향으로 흐른다.
 * 전송 계층(TCP, USB 액세서리)은 [Transport] 로 갈아 끼우고 메시지는 [MessageCodec] 이 다룬다.
 */
object Protocol {
    const val VERSION = 1
    const val PORT = 8080

    /** 메시지 type(1바이트). 연결 직후 양쪽이 HELLO 를 한 번 보낸다. */
    const val TYPE_HELLO = 0x01
    const val TYPE_FRAME = 0x02 // 폰 → 차. JPEG
    const val TYPE_LIGHT = 0x03 // 양방향. LightingParams JSON
    const val TYPE_PING = 0x04 // 양방향. payload 없음
    const val TYPE_STATUS = 0x05 // 폰 → 차. ShareStatus JSON(공유 앱이 가려짐·공유 끝남). 차가 검은 화면 대신 안내를 띄운다
    const val TYPE_STATE = 0x06 // 양방향. StudioState JSON(앰비언트 연동·풀 라이트·다이얼 대상·차 테마·사용자 프리셋). 차가 기준
    const val TYPE_COMMAND = 0x07 // 폰 → 차. StudioCommand JSON(프리셋 적용·저장·삭제)

    const val PING_INTERVAL_MS = 5_000L
    const val IDLE_TIMEOUT_MS = 15_000L // 이만큼 아무것도 안 오면 끊는다
    const val RECONNECT_DELAY_MS = 2_000L // 차가 다시 접속하기까지

    const val FRAME_MAX = 1 shl 20 // 1 MiB
    const val LIGHT_MAX = 4 shl 10 // 4 KiB
    const val STATE_MAX = 64 shl 10 // 64 KiB. 사용자 프리셋 목록이 들어간다
    const val SMALL_MAX = 4 shl 10 // HELLO·PING 등 그 밖의 메시지

    /** 폰이 보내는 JPEG 의 긴 변 상한(px)과 초당 프레임 상한. 차 앱 영역 높이(약 1210px)에 맞춘 값. */
    const val FRAME_MAX_SIDE = 1200
    const val FRAME_MAX_FPS = 15

    /** 무선 모드(2차)에서 폰이 광고하고 차가 찾는 NSD 서비스 타입. */
    const val NSD_SERVICE_TYPE = "_selene._tcp."
    const val NSD_SERVICE_NAME = "Selene"

    const val ROLE_PHONE = "phone"
    const val ROLE_CAR = "car"

    fun maxPayload(type: Int): Int = when (type) {
        TYPE_FRAME -> FRAME_MAX
        TYPE_LIGHT -> LIGHT_MAX
        TYPE_STATE -> STATE_MAX
        else -> SMALL_MAX
    }

    fun typeName(type: Int): String = when (type) {
        TYPE_HELLO -> "HELLO"
        TYPE_FRAME -> "FRAME"
        TYPE_LIGHT -> "LIGHT"
        TYPE_PING -> "PING"
        TYPE_STATUS -> "STATUS"
        TYPE_STATE -> "STATE"
        TYPE_COMMAND -> "COMMAND"
        else -> "0x%02x".format(type)
    }
}
