package com.hdhollow.studio.car

import android.content.Context
import com.hdhollow.studio.shared.Protocol
import java.net.InetSocketAddress

/**
 * 폰 주소 결정. 1차는 데모 경로 고정값: 폰 USB → Mac(`adb forward tcp:8080 tcp:8080`) → 에뮬레이터에서 본 Mac(10.0.2.2).
 * 2차(C14)에서 USB 테더링 게이트웨이 탐지와 NSD 탐색을 앞에 두고 이 값은 폴백으로 남긴다.
 * ponytail: 실차에서 앱이 폰 USB 를 쓸 수 있는지 미확인(Pleos 문의 중)이라 데모 경로 고정값.
 */
object PhoneAddress {
    const val DEFAULT_HOST = "10.0.2.2"

    suspend fun resolve(@Suppress("UNUSED_PARAMETER") context: Context): InetSocketAddress? =
        InetSocketAddress(DEFAULT_HOST, Protocol.PORT)
}
