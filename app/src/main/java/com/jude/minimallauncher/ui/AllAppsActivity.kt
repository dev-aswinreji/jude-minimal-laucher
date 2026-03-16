package com.jude.minimallauncher.ui

import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jude.minimallauncher.R
import com.jude.minimallauncher.data.AppPrefs
import java.util.Locale

class AllAppsActivity : AppCompatActivity() {

    private lateinit var list: RecyclerView
    private lateinit var adapter: LauncherListAdapter
    private var allApps: List<AppInfo> = emptyList()
    private var filteredApps: List<AppInfo> = emptyList()
    private var downY: Float = 0f
    private var downTime: Long = 0L

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downY = ev.y
                downTime = System.currentTimeMillis()
            }
            MotionEvent.ACTION_UP -> {
                val deltaY = ev.y - downY
                val dt = (System.currentTimeMillis() - downTime).coerceAtLeast(1)
                val velocity = deltaY / dt
                if (deltaY > 100 && velocity > 0.5f) {
                    finish()
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_apps)

        val root = findViewById<android.view.View>(R.id.root)
        val wp = AppPrefs.getWallpaper(this)
        if (wp == "SYSTEM") {
            val tv = android.util.TypedValue()
            theme.resolveAttribute(android.R.attr.colorBackground, tv, true)
            root.setBackgroundColor(tv.data)
        } else {
            root.setBackgroundColor(android.graphics.Color.parseColor(wp))
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
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        list = findViewById(R.id.all_apps_list)
        list.layoutManager = LinearLayoutManager(this)
        adapter = LauncherListAdapter { appInfo ->
            val intent = packageManager.getLaunchIntentForPackage(appInfo.packageName)
            if (intent != null) startActivity(intent)
        }
        list.adapter = adapter

        val search = findViewById<android.widget.EditText>(R.id.search)
        search.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterApps(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        search.setOnEditorActionListener { _, actionId, _ ->
            if (AppPrefs.isAutoLaunchSingle(this) && filteredApps.size == 1) {
                val intent = packageManager.getLaunchIntentForPackage(filteredApps[0].packageName)
                if (intent != null) startActivity(intent)
                true
            } else {
                false
            }
        }

        loadAllApps()
    }

    private fun loadAllApps() {
        val pm = packageManager
        allApps = pm.getInstalledApplications(0)
            .mapNotNull { info ->
                val intent = pm.getLaunchIntentForPackage(info.packageName) ?: return@mapNotNull null
                val label = pm.getApplicationLabel(info).toString()
                val icon = pm.getApplicationIcon(info)
                AppInfo(label, info.packageName, icon)
            }
            .sortedBy { it.label.lowercase(Locale.getDefault()) }

        filteredApps = allApps
        adapter.submit(allApps)
    }

    private fun filterApps(query: String) {
        val q = query.trim().lowercase(Locale.getDefault())
        filteredApps = if (q.isEmpty()) {
            allApps
        } else {
            allApps.filter { it.label.lowercase(Locale.getDefault()).contains(q) }
        }
        adapter.submit(filteredApps)
    }
}
