package com.jude.minimallauncher.vpn

import android.net.VpnService
import android.os.ParcelFileDescriptor

class BlockVpnService : VpnService() {

    private var tun: ParcelFileDescriptor? = null

    override fun onCreate() {
        super.onCreate()
    }

    override fun onDestroy() {
        tun?.close()
        tun = null
        super.onDestroy()
    }

    fun startVpn() {
        if (tun != null) return
        val builder = Builder()
            .setSession("MinimalLauncher")
            .addAddress("10.0.0.2", 32)
            .addRoute("0.0.0.0", 0)

        // TODO: Implement packet parsing + blocklist (DNS or IP rules)
        tun = builder.establish()
    }
}
