package com.hdhollow.studio.car

import android.content.Context
import android.util.Log
import com.hdhollow.studio.shared.TcpTransport
import com.hdhollow.studio.shared.Transport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.Socket

/**
 * 폰까지의 전송 계층 하나를 연다. plan/work-plan.md 2.1절.
 * 1. USB 액세서리(AOA): 차가 호스트로 폰을 연다(2차, 실차 미확인).
 * 2. TCP: USB 테더링 게이트웨이 → NSD → 데모 고정값(10.0.2.2) 순서로 주소를 정해 접속한다.
 * 못 열면 null. 호출자(StudioLinkClient.run)가 2초 뒤 다시 부른다.
 */
object PhoneLink {
    private const val TAG = "PhoneLink"
    private const val CONNECT_TIMEOUT_MS = 3000

    suspend fun connect(context: Context): Transport? {
        runCatching { UsbHostLink(context).open() }
            .onFailure { Log.w(TAG, "USB 호스트 경로 실패", it) }
            .getOrNull()?.let { return it }
        val addr = PhoneAddress.resolve(context) ?: return null
        return withContext(Dispatchers.IO) {
            runCatching { TcpTransport(Socket().apply { connect(addr, CONNECT_TIMEOUT_MS) }) }
                .onFailure { Log.d(TAG, "접속 실패 $addr: ${it.message}") }
                .getOrNull()
        }
    }
}
