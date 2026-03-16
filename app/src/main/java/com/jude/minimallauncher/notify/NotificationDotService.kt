package com.jude.minimallauncher.notify

import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

object NotificationStore {
    val active = mutableSetOf<String>()
}

class NotificationDotService : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        NotificationStore.active.add(sbn.packageName)
        sendBroadcast(Intent("com.jude.minimallauncher.NOTIF_CHANGED"))
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        NotificationStore.active.remove(sbn.packageName)
        sendBroadcast(Intent("com.jude.minimallauncher.NOTIF_CHANGED"))
    }
}
