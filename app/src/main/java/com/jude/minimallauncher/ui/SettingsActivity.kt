package com.jude.minimallauncher.ui

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jude.minimallauncher.R
import com.jude.minimallauncher.data.AppPrefs
import com.jude.minimallauncher.data.LimitConfig

class SettingsActivity : AppCompatActivity() {

    private lateinit var list: RecyclerView
    private lateinit var adapter: SettingsListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        findViewById<android.widget.Button>(R.id.set_pin).setOnClickListener {
            val input = android.widget.EditText(this)
            input.hint = "New PIN"
            input.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD

            AlertDialog.Builder(this)
                .setTitle("Set override PIN")
                .setView(input)
                .setPositiveButton("Save") { _, _ ->
                    val pin = input.text.toString().trim()
                    if (pin.isNotEmpty()) {
                        AppPrefs.setPin(this, pin)
                        AppPrefs.setPinEnabled(this, true)
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        val root = findViewById<android.view.View>(R.id.root)
        val wp = AppPrefs.getWallpaper(this)
        if (wp == "SYSTEM") {
            val tv = android.util.TypedValue()
            theme.resolveAttribute(android.R.attr.colorBackground, tv, true)
            root.setBackgroundColor(tv.data)
        } else {
            root.setBackgroundColor(android.graphics.Color.parseColor(wp))
        }

        findViewById<android.widget.Switch>(R.id.focus_mode).apply {
            isChecked = AppPrefs.isFocusMode(this@SettingsActivity)
            setOnCheckedChangeListener { _, isChecked ->
                AppPrefs.setFocusMode(this@SettingsActivity, isChecked)
            }
        }

        findViewById<android.widget.Switch>(R.id.hide_status).apply {
            isChecked = AppPrefs.isHideStatusBar(this@SettingsActivity)
            setOnCheckedChangeListener { _, isChecked ->
                AppPrefs.setHideStatusBar(this@SettingsActivity, isChecked)
            }
        }

        findViewById<android.widget.Switch>(R.id.mono).apply {
            isChecked = AppPrefs.isMonochrome(this@SettingsActivity)
            setOnCheckedChangeListener { _, isChecked ->
                AppPrefs.setMonochrome(this@SettingsActivity, isChecked)
            }
        }

        findViewById<android.widget.Switch>(R.id.no_telemetry).apply {
            isChecked = AppPrefs.isNoTelemetry(this@SettingsActivity)
            setOnCheckedChangeListener { _, isChecked ->
                AppPrefs.setNoTelemetry(this@SettingsActivity, isChecked)
                if (isChecked) {
                    val intent = android.net.VpnService.prepare(this@SettingsActivity)
                    if (intent != null) {
                        startActivity(intent)
                    } else {
                        startService(Intent(this@SettingsActivity, com.jude.minimallauncher.vpn.BlockVpnService::class.java))
                    }
                } else {
                    stopService(Intent(this@SettingsActivity, com.jude.minimallauncher.vpn.BlockVpnService::class.java))
                }
            }
        }

        findViewById<android.widget.Button>(R.id.check_usage).setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

        findViewById<android.widget.Button>(R.id.focus_schedule).setOnClickListener {
            val view = layoutInflater.inflate(R.layout.dialog_focus_schedule, null)
            val startH = view.findViewById<android.widget.EditText>(R.id.start_h)
            val startM = view.findViewById<android.widget.EditText>(R.id.start_m)
            val endH = view.findViewById<android.widget.EditText>(R.id.end_h)
            val endM = view.findViewById<android.widget.EditText>(R.id.end_m)
            AlertDialog.Builder(this)
                .setTitle("Focus schedule (24h)")
                .setView(view)
                .setPositiveButton("Save") { _, _ ->
                    val sh = startH.text.toString().toIntOrNull() ?: 0
                    val sm = startM.text.toString().toIntOrNull() ?: 0
                    val eh = endH.text.toString().toIntOrNull() ?: 0
                    val em = endM.text.toString().toIntOrNull() ?: 0
                    AppPrefs.setFocusSchedule(this, sh * 60 + sm, eh * 60 + em)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        findViewById<android.widget.Button>(R.id.usage_screen).setOnClickListener {
            startActivity(Intent(this, UsageActivity::class.java))
        }

        findViewById<android.widget.Button>(R.id.notification_access).setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        findViewById<android.widget.Button>(R.id.wallpaper).setOnClickListener {
            val options = arrayOf("System", "White", "Black", "Gray", "Blue", "Green")
            val values = arrayOf("SYSTEM", "#FFFFFF", "#000000", "#E0E0E0", "#1E88E5", "#2E7D32")
            AlertDialog.Builder(this)
                .setTitle("Wallpaper color")
                .setItems(options) { _, which ->
                    AppPrefs.setWallpaper(this, values[which])
                    val root = findViewById<android.view.View>(R.id.root)
                    if (values[which] == "SYSTEM") {
                        val tv = android.util.TypedValue()
                        theme.resolveAttribute(android.R.attr.colorBackground, tv, true)
                        root.setBackgroundColor(tv.data)
                    } else {
                        root.setBackgroundColor(android.graphics.Color.parseColor(values[which]))
                    }
                }
                .show()
        }

        list = findViewById(R.id.apps_list)
        list.layoutManager = LinearLayoutManager(this)
        adapter = SettingsListAdapter(
            onToggle = { pkg, enabled ->
                AppPrefs.setWhitelisted(this, pkg, enabled)
            },
            onLimit = { pkg ->
                showLimitDialog(pkg)
            },
            onFavorite = { pkg ->
                AppPrefs.toggleFavorite(this, pkg)
                adapter.notifyDataSetChanged()
            },
            onEmergency = { pkg ->
                AppPrefs.toggleEmergency(this, pkg)
                adapter.notifyDataSetChanged()
            },
            onNet = { pkg ->
                AppPrefs.toggleNetBlocked(this, pkg)
                adapter.notifyDataSetChanged()
            }
        )
        list.adapter = adapter

        loadApps()
    }

    override fun onResume() {
        super.onResume()
        if (AppPrefs.isNoTelemetry(this)) {
            val intent = android.net.VpnService.prepare(this)
            if (intent == null) {
                startService(Intent(this, com.jude.minimallauncher.vpn.BlockVpnService::class.java))
            }
        }
    }

    private fun loadApps() {
        val pm = packageManager
        val apps = pm.getInstalledApplications(0)
            .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
            .map { info ->
                val label = pm.getApplicationLabel(info).toString()
                val icon = pm.getApplicationIcon(info)
                SettingsAppInfo(label, info.packageName, icon)
            }
            .sortedBy { it.label.lowercase() }

        adapter.submit(apps, AppPrefs.getWhitelistedPackages(this))
    }

    private fun showLimitDialog(pkg: String) {
        val current = AppPrefs.getLimitMinutes(this, pkg)
        val view = layoutInflater.inflate(R.layout.dialog_limits, null)
        val soft = view.findViewById<android.widget.EditText>(R.id.soft_limit)
        val hard = view.findViewById<android.widget.EditText>(R.id.hard_limit)
        soft.setText(current?.softMinutes?.toString() ?: "")
        hard.setText(current?.hardMinutes?.toString() ?: "")

        AlertDialog.Builder(this)
            .setTitle("Set daily limits (minutes)")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                val softVal = soft.text.toString().toIntOrNull()
                val hardVal = hard.text.toString().toIntOrNull()
                AppPrefs.setLimitMinutes(this, pkg, LimitConfig(softVal, hardVal))
                adapter.notifyDataSetChanged()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
