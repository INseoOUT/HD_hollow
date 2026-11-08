package com.hdhollow.studio.shared

import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class Message(val type: Int, val payload: ByteArray) {
    override fun toString(): String = "${Protocol.typeName(type)}(${payload.size}B)"
}

/** 연결 직후 양쪽이 한 번 보내는 인사. 버전이 다르면 끊는다. */
data class Hello(val role: String, val screenW: Int, val screenH: Int, val v: Int = Protocol.VERSION) {
    fun toJson(): String = JSONObject()
        .put("v", v).put("role", role).put("screen", org.json.JSONArray().put(screenW).put(screenH))
        .toString()

    companion object {
        fun fromJson(text: String): Hello? = try {
            val o = JSONObject(text)
            val s = o.optJSONArray("screen")
            Hello(
                role = o.optString("role", "?"),
                screenW = s?.optInt(0) ?: 0,
                screenH = s?.optInt(1) ?: 0,
                v = o.optInt("v", -1),
            )
        } catch (_: JSONException) {
            null
        }
    }
}

/**
 * 프레임 = type(1B) | length(4B big-endian) | payload. 길이는 type 별 상한을 넘을 수 없다.
 * DataInput/OutputStream 이 big-endian 이라 그대로 쓴다.
 */
object MessageCodec {
    const val HEADER_BYTES = 5

    @Throws(IOException::class)
    fun write(out: OutputStream, type: Int, payload: ByteArray) {
        require(type in 1..0xFF) { "type 범위 밖: $type" }
        val max = Protocol.maxPayload(type)
        if (payload.size > max) throw IOException("${Protocol.typeName(type)} payload ${payload.size}B > 상한 ${max}B")
        val d = DataOutputStream(out)
        d.writeByte(type)
        d.writeInt(payload.size)
        d.write(payload)
        d.flush()
    }

    /** 메시지 하나. 스트림이 깨끗하게 끝났으면 null, 중간에 끊겼거나 길이가 상한을 넘으면 IOException. */
    @Throws(IOException::class)
    fun read(input: InputStream): Message? {
        val type = input.read()
        if (type < 0) return null
        val d = DataInputStream(input)
        val len = try {
            d.readInt()
        } catch (e: EOFException) {
            throw IOException("헤더 도중 끊김", e)
        }
        val max = Protocol.maxPayload(type)
        if (len < 0 || len > max) throw IOException("${Protocol.typeName(type)} length $len 은 상한 ${max}B 밖")
        val buf = ByteArray(len)
        try {
            d.readFully(buf)
        } catch (e: EOFException) {
            throw IOException("payload 도중 끊김 (${Protocol.typeName(type)} $len B)", e)
        }
        return Message(type, buf)
    }

    /** 테스트·도구용. 메시지 하나를 바이트로. */
    fun encode(type: Int, payload: ByteArray): ByteArray =
        ByteArrayOutputStream(HEADER_BYTES + payload.size).also { write(it, type, payload) }.toByteArray()
}
