package com.jude.minimallauncher.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.net.VpnService
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.jude.minimallauncher.R
import com.jude.minimallauncher.data.AppPrefs

class BlockVpnService : VpnService() {

    private var tun: ParcelFileDescriptor? = null

    override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
        startForeground(1, buildNotification())
        startVpn()
        return START_STICKY
    }

    override fun onDestroy() {
        tun?.close()
        tun = null
        super.onDestroy()
    }

    private fun buildNotification(): Notification {
        val channelId = "telemetry"
        val nm = getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(channelId) == null) {
            nm.createNotificationChannel(NotificationChannel(channelId, "No‑telemetry", NotificationManager.IMPORTANCE_LOW))
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("No‑telemetry mode")
            .setContentText("Blocking network for selected apps")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }

    private fun startVpn() {
        if (tun != null) return
        val builder = Builder()
            .setSession("MinimalLauncher")
            .addAddress("10.0.0.2", 32)
            .addRoute("0.0.0.0", 0)

        val blocked = AppPrefs.getNetBlocked(this)
        if (blocked.isNotEmpty()) {
            blocked.forEach { pkg ->
                try { builder.addAllowedApplication(pkg) } catch (_: Exception) {}
            }
        }

        // By not forwarding packets, apps routed through VPN have no network access.
        tun = builder.establish()
    }
}
