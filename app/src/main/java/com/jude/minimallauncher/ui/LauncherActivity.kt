package com.jude.minimallauncher.ui

import android.app.AlertDialog
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.format.DateFormat
import android.view.MotionEvent
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jude.minimallauncher.R
import com.jude.minimallauncher.data.AppPrefs
import com.jude.minimallauncher.data.UsageLimiter
import java.util.Calendar
import java.util.Locale

class LauncherActivity : AppCompatActivity() {

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downY = ev.y
                downTime = System.currentTimeMillis()
            }
            MotionEvent.ACTION_UP -> {
                val deltaY = downY - ev.y
                val dt = (System.currentTimeMillis() - downTime).coerceAtLeast(1)
                val velocity = deltaY / dt
                if (deltaY > 100 && velocity > 0.5f) {
                    startActivity(Intent(this@LauncherActivity, AllAppsActivity::class.java))
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private lateinit var clock: TextView
    private lateinit var date: TextView
    private lateinit var list: RecyclerView
    private lateinit var adapter: LauncherListAdapter
    private lateinit var usage: TextView
    private var downY: Float = 0f
    private var downTime: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        clock = findViewById(R.id.clock)
        date = findViewById(R.id.date)
        usage = findViewById(R.id.usage_summary)
        list = findViewById(R.id.app_list)

        list.layoutManager = LinearLayoutManager(this)
        adapter = LauncherListAdapter { appInfo ->
            handleLaunch(appInfo.packageName)
        }
        list.adapter = adapter

        findViewById<TextView>(R.id.settings_btn).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        findViewById<TextView>(R.id.all_apps_btn).setOnClickListener {
            startActivity(Intent(this, AllAppsActivity::class.java))
        }


        ensureUsageAccess()
    }

    override fun onResume() {
        super.onResume()
        updateClock()
        updateUsageSummary()
        loadApps()
    }

    private fun updateClock() {
        val now = Calendar.getInstance()
        val time = DateFormat.format("HH:mm", now).toString()
        val dateText = DateFormat.format("EEE, MMM d", now).toString()
        clock.text = time
        date.text = dateText
    }

    private fun updateUsageSummary() {
        val whitelist = AppPrefs.getWhitelistedPackages(this)
        val total = whitelist.sumOf { pkg ->
            UsageLimiter.getTodayUsageMinutes(this, pkg)
        }
        usage.text = "Today: ${total} min"
    }

    private fun ensureUsageAccess() {
        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now - 1000L * 60 * 60, now)
        if (stats.isNullOrEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Usage access needed")
                .setMessage("Allow usage access so the launcher can enforce time limits.")
                .setPositiveButton("Open Settings") { _, _ ->
                    startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                }
                .setNegativeButton("Later", null)
                .show()
        }
    }

    private fun loadApps() {
        val whitelist = AppPrefs.getWhitelistedPackages(this)
        if (whitelist.isEmpty()) {
            startActivity(Intent(this, SettingsActivity::class.java))
            return
        }

        val focusMode = AppPrefs.isFocusMode(this)
        val favorites = AppPrefs.getFavorites(this)
        val emergency = AppPrefs.getEmergencyApps(this)
        val base = if (focusMode && favorites.isNotEmpty()) favorites else whitelist
        val merged = (base + emergency).toSet()

        val pm = packageManager
        val apps = merged.mapNotNull { pkg ->
            try {
                val info = pm.getApplicationInfo(pkg, 0)
                val label = pm.getApplicationLabel(info).toString()
                val icon = pm.getApplicationIcon(info)
                AppInfo(label, pkg, icon)
            } catch (e: Exception) {
                null
            }
        }.sortedBy { it.label.lowercase(Locale.getDefault()) }

        adapter.submit(apps)
    }

    private fun handleLaunch(packageName: String) {
        val limit = AppPrefs.getLimitMinutes(this, packageName)
        if (limit == null) {
            launchApp(packageName)
            return
        }

        val usageMinutes = UsageLimiter.getTodayUsageMinutes(this, packageName)
        val softLimit = limit.softMinutes
        val hardLimit = limit.hardMinutes

        if (hardLimit != null && usageMinutes >= hardLimit) {
            showBlockedDialog(packageName, hardLimit)
            return
        }

        if (softLimit != null && usageMinutes >= softLimit) {
            AlertDialog.Builder(this)
                .setTitle("Time limit reached")
                .setMessage("You used $usageMinutes min today. Soft limit is $softLimit min. Open anyway?")
                .setPositiveButton("Open") { _, _ -> launchApp(packageName) }
                .setNegativeButton("Cancel", null)
                .show()
            return
        }

        launchApp(packageName)
    }

    private fun showBlockedDialog(packageName: String, hardLimit: Int) {
        val pinEnabled = AppPrefs.isPinEnabled(this)
        val message = "Hard limit reached ($hardLimit min)."
        if (!pinEnabled) {
            AlertDialog.Builder(this)
                .setTitle("Blocked")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show()
            return
        }

        PinDialog.show(this, message) { ok ->
            if (ok) launchApp(packageName)
        }
    }

    private fun launchApp(packageName: String) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            startActivity(launchIntent)
        }
    }
}
