package com.hdhollow.studio.shared

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ShareStatusTest {

    @Test
    fun `JSON 왕복이 같다`() {
        listOf(ShareStatus(), ShareStatus(visible = false), ShareStatus(stopped = true, reason = "user"), ShareStatus(false, true, "projection"))
            .forEach { assertEquals(it, ShareStatus.fromJson(it.toJson())) }
    }

    @Test
    fun `빠진 키는 기본값이고 버전이 다르거나 깨지면 null`() {
        assertEquals(ShareStatus(), ShareStatus.fromJson("""{"v":1}"""))
        assertNull(ShareStatus.fromJson("""{"v":2,"visible":false}"""))
        assertNull(ShareStatus.fromJson("nope"))
    }
}
