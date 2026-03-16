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

    fun setFocusMode(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_FOCUS_MODE, enabled).apply()
    }

    fun setWallpaper(context: Context, color: String) {
        prefs(context).edit().putString(KEY_WALLPAPER, color).apply()
    }

    fun getWallpaper(context: Context): String =
        prefs(context).getString(KEY_WALLPAPER, "#FFFFFF") ?: "#FFFFFF"
}
