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

        findViewById<android.view.View>(R.id.root).setBackgroundColor(android.graphics.Color.parseColor(AppPrefs.getWallpaper(this)))

        findViewById<android.widget.Switch>(R.id.focus_mode).apply {
            isChecked = AppPrefs.isFocusMode(this@SettingsActivity)
            setOnCheckedChangeListener { _, isChecked ->
                AppPrefs.setFocusMode(this@SettingsActivity, isChecked)
            }
        }

        findViewById<android.widget.Button>(R.id.check_usage).setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

        findViewById<android.widget.Button>(R.id.wallpaper).setOnClickListener {
            val options = arrayOf("White", "Black", "Gray", "Blue", "Green")
            val values = arrayOf("#FFFFFF", "#000000", "#E0E0E0", "#1E88E5", "#2E7D32")
            AlertDialog.Builder(this)
                .setTitle("Wallpaper color")
                .setItems(options) { _, which ->
                    AppPrefs.setWallpaper(this, values[which])
                    findViewById<android.view.View>(R.id.root)
                        .setBackgroundColor(android.graphics.Color.parseColor(values[which]))
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
            }
        )
        list.adapter = adapter

        loadApps()
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
