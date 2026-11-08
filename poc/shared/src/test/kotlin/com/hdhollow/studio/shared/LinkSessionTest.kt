package com.hdhollow.studio.shared

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class LinkSessionTest {

    /** 파이프 두 개로 폰(a)과 차(b)를 잇는다. */
    private class Pair {
        val aIn = PipedInputStream(1 shl 17)
        val bOut = PipedOutputStream(aIn)
        val bIn = PipedInputStream(1 shl 17)
        val aOut = PipedOutputStream(bIn)
        val a = StreamTransport(aIn, aOut, "a")
        val b = StreamTransport(bIn, bOut, "b")
    }

    private class Recorder : LinkSession.Listener {
        val hello = CountDownLatch(1)
        val light = CountDownLatch(1)
        val frame = CountDownLatch(1)
        val closed = CountDownLatch(1)
        val peer = AtomicReference<Hello>()
        val lastLight = AtomicReference<LightingParams>()
        val lastFrame = AtomicReference<ByteArray>()
        val reason = AtomicReference<String>()
        override fun onHello(peer: Hello) { this.peer.set(peer); hello.countDown() }
        override fun onLight(params: LightingParams) { lastLight.set(params); light.countDown() }
        override fun onFrame(jpeg: ByteArray) { lastFrame.set(jpeg); frame.countDown() }
        override fun onClosed(reason: String) { this.reason.set(reason); closed.countDown() }
    }

    @Test(timeout = 5_000)
    fun `HELLO 를 주고받고 LIGHT 와 FRAME 이 전달된다`() {
        val p = Pair()
        val phone = Recorder()
        val car = Recorder()
        val phoneSession = LinkSession(p.a, Hello(Protocol.ROLE_PHONE, 1080, 2400), phone)
        val carSession = LinkSession(p.b, Hello(Protocol.ROLE_CAR, 1680, 1210), car)
        phoneSession.start()
        carSession.start()

        assertTrue(phone.hello.await(2, TimeUnit.SECONDS))
        assertTrue(car.hello.await(2, TimeUnit.SECONDS))
        assertEquals(Protocol.ROLE_CAR, phone.peer.get().role)
        assertEquals(1080, car.peer.get().screenW)

        assertTrue(carSession.sendLight(LightingParams(kelvin = 3500, level = 0.8f, src = Protocol.ROLE_CAR)))
        assertTrue(phone.light.await(2, TimeUnit.SECONDS))
        assertEquals(3500, phone.lastLight.get().kelvin)

        val jpeg = ByteArray(70_000) { it.toByte() }
        assertTrue(phoneSession.offerFrame(jpeg))
        assertTrue(car.frame.await(2, TimeUnit.SECONDS))
        assertEquals(70_000, car.lastFrame.get().size)

        phoneSession.close("test")
        assertTrue(phone.closed.await(2, TimeUnit.SECONDS))
        assertTrue("상대 종료를 읽기 스레드가 감지한다", car.closed.await(2, TimeUnit.SECONDS))
        assertFalse(carSession.isOpen)
        assertFalse("닫힌 뒤 보내기는 false", carSession.sendLight(LightingParams()))
    }

    @Test(timeout = 5_000)
    fun `버전이 다른 HELLO 면 끊는다`() {
        val p = Pair()
        val car = Recorder()
        val carSession = LinkSession(p.b, Hello(Protocol.ROLE_CAR, 1, 1), car)
        carSession.start()
        // 상대(폰 역할)가 손으로 v=2 HELLO 를 쓴다
        MessageCodec.write(p.aOut, Protocol.TYPE_HELLO, Hello("phone", 1, 1, v = 2).toJson().toByteArray())
        assertTrue(car.closed.await(2, TimeUnit.SECONDS))
        assertTrue(car.reason.get().contains("버전"))
    }

    @Test(timeout = 5_000)
    fun `무수신 타임아웃이면 끊는다`() {
        val p = Pair()
        val car = Recorder()
        var clock = 0L
        val carSession = LinkSession(p.b, Hello(Protocol.ROLE_CAR, 1, 1), car, now = { clock })
        carSession.start()
        clock = Protocol.IDLE_TIMEOUT_MS + 1 // 다음 PING 주기에 검사된다
        assertTrue(car.closed.await(Protocol.PING_INTERVAL_MS + 2_000, TimeUnit.MILLISECONDS))
        assertTrue(car.reason.get().contains("무수신"))
    }
}
