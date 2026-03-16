package com.jude.minimallauncher.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jude.minimallauncher.R
import com.jude.minimallauncher.data.AppPrefs
import java.util.Locale

class AllAppsActivity : AppCompatActivity() {

    private lateinit var list: RecyclerView
    private lateinit var adapter: LauncherListAdapter
    private var allApps: List<AppInfo> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_apps)

        findViewById<android.view.View>(R.id.root).setBackgroundColor(android.graphics.Color.parseColor(AppPrefs.getWallpaper(this)))
        list = findViewById(R.id.all_apps_list)
        list.layoutManager = LinearLayoutManager(this)
        adapter = LauncherListAdapter { appInfo ->
            val intent = packageManager.getLaunchIntentForPackage(appInfo.packageName)
            if (intent != null) startActivity(intent)
        }
        list.adapter = adapter

        findViewById<android.widget.EditText>(R.id.search).addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterApps(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

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

        adapter.submit(allApps)
    }

    private fun filterApps(query: String) {
        val q = query.trim().lowercase(Locale.getDefault())
        if (q.isEmpty()) {
            adapter.submit(allApps)
        } else {
            adapter.submit(allApps.filter { it.label.lowercase(Locale.getDefault()).contains(q) })
        }
    }
}
