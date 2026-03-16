package com.jude.minimallauncher.limits

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (com.jude.minimallauncher.data.AppPrefs.isNoTelemetry(context)) {
            context.startService(Intent(context, com.jude.minimallauncher.vpn.BlockVpnService::class.java))
        }
    }
}
