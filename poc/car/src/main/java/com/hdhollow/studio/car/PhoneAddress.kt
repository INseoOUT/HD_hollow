package com.hdhollow.studio.car

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import android.util.Log
import com.hdhollow.studio.shared.Protocol
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.net.Inet4Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume

/**
 * 폰 주소 결정. plan/work-plan.md C14. 순서대로 시도한다.
 *
 * 1. USB 테더링: 차가 폰의 USB 네트워크에 붙어 있으면(TRANSPORT_USB) 그 기본 경로의 게이트웨이가 폰이다.
 * 2. 무선(NSD): 같은 Wi-Fi 또는 폰 핫스팟에서 폰 앱이 광고하는 `_carstudio._tcp` 를 찾는다(2초).
 * 3. 데모 고정값: 폰 USB → Mac(`adb forward tcp:8080 tcp:8080`) → 에뮬레이터에서 본 Mac(10.0.2.2).
 *
 * ponytail: 실차에서 1·2 중 무엇이 되는지는 Pleos 확인 전까지 모른다. 3 은 에뮬레이터 전용 폴백.
 */
object PhoneAddress {
    private const val TAG = "PhoneAddress"
    const val DEFAULT_HOST = "10.0.2.2"
    private const val NSD_TIMEOUT_MS = 2_000L

    suspend fun resolve(context: Context): InetSocketAddress? {
        usbGateway(context)?.let {
            Log.d(TAG, "USB 테더링 게이트웨이 $it")
            return InetSocketAddress(it, Protocol.PORT)
        }
        discover(context)?.let {
            Log.d(TAG, "NSD 로 찾음 $it")
            return it
        }
        return InetSocketAddress(DEFAULT_HOST, Protocol.PORT)
    }

    /** TRANSPORT_USB 는 API 31 부터. 그 전 버전은 건너뛴다. */
    private fun usbGateway(context: Context): InetAddress? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
        val cm = context.getSystemService(ConnectivityManager::class.java) ?: return null
        return runCatching {
            @Suppress("DEPRECATION")
            cm.allNetworks.asSequence().mapNotNull { network ->
                val caps = cm.getNetworkCapabilities(network) ?: return@mapNotNull null
                if (!caps.hasTransport(NetworkCapabilities.TRANSPORT_USB)) return@mapNotNull null
                cm.getLinkProperties(network)?.routes
                    ?.firstOrNull { it.isDefaultRoute && it.gateway is Inet4Address }
                    ?.gateway
            }.firstOrNull()
        }.onFailure { Log.w(TAG, "USB 네트워크 조회 실패", it) }.getOrNull()
    }

    private suspend fun discover(context: Context): InetSocketAddress? = withTimeoutOrNull(NSD_TIMEOUT_MS) {
        suspendCancellableCoroutine { cont ->
            val nsd = context.getSystemService(NsdManager::class.java)
            if (nsd == null) {
                cont.resume(null)
                return@suspendCancellableCoroutine
            }
            val done = AtomicBoolean(false)
            fun finish(result: InetSocketAddress?) {
                if (done.compareAndSet(false, true) && cont.isActive) cont.resume(result)
            }
            var listener: NsdManager.DiscoveryListener? = null
            fun stop() = runCatching { listener?.let(nsd::stopServiceDiscovery) }
            listener = object : NsdManager.DiscoveryListener {
                override fun onServiceFound(info: NsdServiceInfo) {
                    if (!info.serviceType.contains(Protocol.NSD_SERVICE_TYPE.trimEnd('.'))) return
                    @Suppress("DEPRECATION")
                    nsd.resolveService(info, object : NsdManager.ResolveListener {
                        override fun onServiceResolved(resolved: NsdServiceInfo) {
                            @Suppress("DEPRECATION")
                            val host = resolved.host
                            if (host != null) finish(InetSocketAddress(host, resolved.port))
                            stop()
                        }

                        override fun onResolveFailed(info: NsdServiceInfo, errorCode: Int) {
                            Log.d(TAG, "NSD resolve 실패 $errorCode")
                        }
                    })
                }

                override fun onServiceLost(info: NsdServiceInfo) {}
                override fun onDiscoveryStarted(serviceType: String) {}
                override fun onDiscoveryStopped(serviceType: String) {}
                override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                    Log.d(TAG, "NSD 탐색 시작 실패 $errorCode")
                    finish(null)
                }

                override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {}
            }
            runCatching { nsd.discoverServices(Protocol.NSD_SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, listener) }
                .onFailure { Log.w(TAG, "NSD 탐색 실패", it); finish(null) }
            cont.invokeOnCancellation { stop() }
        }
    }
}
