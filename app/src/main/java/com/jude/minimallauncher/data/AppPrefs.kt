package com.jude.minimallauncher.data

import android.content.Context
import android.content.SharedPreferences

object AppPrefs {
    private const val PREFS = "minimal_launcher"
    private const val KEY_WHITELIST = "whitelist"
    private const val KEY_PIN_ENABLED = "pin_enabled"
    private const val KEY_PIN_VALUE = "pin_value"
    private const val KEY_LIMIT_PREFIX = "limit_"
    private const val KEY_FAVORITES = "favorites"
    private const val KEY_EMERGENCY = "emergency"
    private const val KEY_FOCUS_MODE = "focus_mode"
    private const val KEY_WALLPAPER = "wallpaper"
    private const val KEY_HIDE_STATUS = "hide_status"
    private const val KEY_MONO = "mono"
    private const val KEY_FOCUS_NOW = "focus_now"
    private const val KEY_FOCUS_START = "focus_start"
    private const val KEY_FOCUS_END = "focus_end"
    private const val KEY_NET_BLOCK = "net_block"
    private const val KEY_NO_TELEMETRY = "no_telemetry"
    private const val KEY_AUTO_LAUNCH = "auto_launch_single"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getWhitelistedPackages(context: Context): Set<String> =
        prefs(context).getStringSet(KEY_WHITELIST, emptySet()) ?: emptySet()

    fun setWhitelisted(context: Context, pkg: String, enabled: Boolean) {
        val set = getWhitelistedPackages(context).toMutableSet()
        if (enabled) set.add(pkg) else set.remove(pkg)
        prefs(context).edit().putStringSet(KEY_WHITELIST, set).apply()
    }

    fun getLimitMinutes(context: Context, pkg: String): LimitConfig? {
        val value = prefs(context).getString(KEY_LIMIT_PREFIX + pkg, null) ?: return null
        val parts = value.split(":")
        val soft = parts.getOrNull(0)?.toIntOrNull()
        val hard = parts.getOrNull(1)?.toIntOrNull()
        return LimitConfig(soft, hard)
    }

    fun setLimitMinutes(context: Context, pkg: String, config: LimitConfig) {
        val value = "${config.softMinutes ?: ""}:${config.hardMinutes ?: ""}"
        prefs(context).edit().putString(KEY_LIMIT_PREFIX + pkg, value).apply()
    }

    fun isPinEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_PIN_ENABLED, true)

    fun setPinEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_PIN_ENABLED, enabled).apply()
    }

    fun setPin(context: Context, pin: String) {
        prefs(context).edit().putString(KEY_PIN_VALUE, pin).apply()
    }

    fun getPin(context: Context): String? = prefs(context).getString(KEY_PIN_VALUE, "0000")

    fun getFavorites(context: Context): Set<String> =
        prefs(context).getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()

    fun toggleFavorite(context: Context, pkg: String) {
        val set = getFavorites(context).toMutableSet()
        if (set.contains(pkg)) set.remove(pkg) else set.add(pkg)
        prefs(context).edit().putStringSet(KEY_FAVORITES, set).apply()
    }

    fun getEmergencyApps(context: Context): Set<String> =
        prefs(context).getStringSet(KEY_EMERGENCY, emptySet()) ?: emptySet()

    fun toggleEmergency(context: Context, pkg: String) {
        val set = getEmergencyApps(context).toMutableSet()
        if (set.contains(pkg)) set.remove(pkg) else set.add(pkg)
        prefs(context).edit().putStringSet(KEY_EMERGENCY, set).apply()
    }

    fun isFocusMode(context: Context): Boolean =
        prefs(context).getBoolean(KEY_FOCUS_MODE, false)

    fun isFocusActive(context: Context): Boolean {
        if (isFocusNow(context)) return true
        val (start, end) = getFocusSchedule(context)
        if (start < 0 || end < 0) return false
        val cal = java.util.Calendar.getInstance()
        val minutes = cal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + cal.get(java.util.Calendar.MINUTE)
        return if (start <= end) {
            minutes in start..end
        } else {
            // overnight range
            minutes >= start || minutes <= end
        }
    }

    fun getNetBlocked(context: Context): Set<String> =
        prefs(context).getStringSet(KEY_NET_BLOCK, emptySet()) ?: emptySet()

    fun toggleNetBlocked(context: Context, pkg: String) {
        val set = getNetBlocked(context).toMutableSet()
        if (set.contains(pkg)) set.remove(pkg) else set.add(pkg)
        prefs(context).edit().putStringSet(KEY_NET_BLOCK, set).apply()
    }

    fun setNoTelemetry(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_NO_TELEMETRY, enabled).apply()
    }

    fun isNoTelemetry(context: Context): Boolean =
        prefs(context).getBoolean(KEY_NO_TELEMETRY, false)

    fun setAutoLaunchSingle(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_AUTO_LAUNCH, enabled).apply()
    }

    fun isAutoLaunchSingle(context: Context): Boolean =
        prefs(context).getBoolean(KEY_AUTO_LAUNCH, false)

    fun setFocusMode(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_FOCUS_MODE, enabled).apply()
    }

    fun setWallpaper(context: Context, color: String) {
        prefs(context).edit().putString(KEY_WALLPAPER, color).apply()
    }

    fun getWallpaper(context: Context): String =
        prefs(context).getString(KEY_WALLPAPER, "SYSTEM") ?: "SYSTEM"

    fun setHideStatusBar(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_HIDE_STATUS, enabled).apply()
    }

    fun isHideStatusBar(context: Context): Boolean =
        prefs(context).getBoolean(KEY_HIDE_STATUS, false)

    fun setMonochrome(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_MONO, enabled).apply()
    }

    fun isMonochrome(context: Context): Boolean =
        prefs(context).getBoolean(KEY_MONO, false)

    fun setFocusNow(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_FOCUS_NOW, enabled).apply()
    }

    fun isFocusNow(context: Context): Boolean =
        prefs(context).getBoolean(KEY_FOCUS_NOW, false)

    fun setFocusSchedule(context: Context, startMinutes: Int, endMinutes: Int) {
        prefs(context).edit()
            .putInt(KEY_FOCUS_START, startMinutes)
            .putInt(KEY_FOCUS_END, endMinutes)
            .apply()
    }

    fun getFocusSchedule(context: Context): Pair<Int, Int> {
        val start = prefs(context).getInt(KEY_FOCUS_START, -1)
        val end = prefs(context).getInt(KEY_FOCUS_END, -1)
        return start to end
    }
}
