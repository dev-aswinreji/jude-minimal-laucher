package com.jude.minimallauncher.data

import android.app.usage.UsageStatsManager
import android.content.Context
import java.util.Calendar

object UsageLimiter {
    fun getTodayUsageMinutes(context: Context, packageName: String): Int {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        val end = System.currentTimeMillis()
        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end)
        val stat = stats.firstOrNull { it.packageName == packageName }
        val ms = stat?.totalTimeInForeground ?: 0L
        return (ms / 60000L).toInt()
    }
}
