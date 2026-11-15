package com.hdhollow.studio.phone

import com.hdhollow.studio.shared.Aoa
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Test

class AccessoryFilterTest {

    @Test
    fun `액세서리 필터는 차가 AOA 로 보내는 문자열과 같다`() {
        val xml = File("src/main/res/xml/accessory_filter.xml").readText()
        fun attr(name: String) = Regex("""$name="([^"]*)"""").find(xml.substringAfter("<usb-accessory"))?.groupValues?.get(1)
        assertEquals(Aoa.MANUFACTURER, attr("manufacturer"))
        assertEquals(Aoa.MODEL, attr("model"))
        assertEquals(Aoa.VERSION, attr("version"))
    }
}
