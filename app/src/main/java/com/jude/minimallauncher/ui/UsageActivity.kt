package com.jude.minimallauncher.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jude.minimallauncher.R
import com.jude.minimallauncher.data.AppPrefs
import com.jude.minimallauncher.data.UsageLimiter

class UsageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usage)

        val root = findViewById<android.view.View>(R.id.root)
        val wp = AppPrefs.getWallpaper(this)
        if (wp == "SYSTEM") {
            val tv = android.util.TypedValue()
            theme.resolveAttribute(android.R.attr.colorBackground, tv, true)
            root.setBackgroundColor(tv.data)
        } else {
            try {
                root.setBackgroundColor(android.graphics.Color.parseColor(wp))
            } catch (_: Exception) {
                root.setBackgroundColor(android.graphics.Color.WHITE)
            }
        }
        if (AppPrefs.isMonochrome(this)) {
            root.alpha = 0.9f
        }
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        if (AppPrefs.isHideStatusBar(this)) {
            controller.hide(WindowInsetsCompat.Type.statusBars())
        } else {
            controller.show(WindowInsetsCompat.Type.statusBars())
        }

        val list = findViewById<RecyclerView>(R.id.usage_list)
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = UsageAdapter(loadUsage())
    }

    private fun loadUsage(): List<Pair<String, Int>> {
        val pm = packageManager
        return AppPrefs.getWhitelistedPackages(this)
            .mapNotNull { pkg ->
                try {
                    val info = pm.getApplicationInfo(pkg, 0)
                    val label = pm.getApplicationLabel(info).toString()
                    val mins = UsageLimiter.getTodayUsageMinutes(this, pkg)
                    label to mins
                } catch (e: Exception) {
                    null
                }
            }
            .sortedByDescending { it.second }
    }
}
