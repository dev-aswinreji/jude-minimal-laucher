package com.jude.minimallauncher.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jude.minimallauncher.R
import java.util.Locale

class AllAppsActivity : AppCompatActivity() {

    private lateinit var list: RecyclerView
    private lateinit var adapter: LauncherListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_apps)

        list = findViewById(R.id.all_apps_list)
        list.layoutManager = LinearLayoutManager(this)
        adapter = LauncherListAdapter { appInfo ->
            val intent = packageManager.getLaunchIntentForPackage(appInfo.packageName)
            if (intent != null) startActivity(intent)
        }
        list.adapter = adapter

        loadAllApps()
    }

    private fun loadAllApps() {
        val pm = packageManager
        val apps = pm.getInstalledApplications(0)
            .mapNotNull { info ->
                val intent = pm.getLaunchIntentForPackage(info.packageName) ?: return@mapNotNull null
                val label = pm.getApplicationLabel(info).toString()
                val icon = pm.getApplicationIcon(info)
                AppInfo(label, info.packageName, icon)
            }
            .sortedBy { it.label.lowercase(Locale.getDefault()) }

        adapter.submit(apps)
    }
}
