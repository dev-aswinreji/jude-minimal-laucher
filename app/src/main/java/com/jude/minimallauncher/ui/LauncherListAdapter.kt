package com.jude.minimallauncher.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jude.minimallauncher.R
import com.jude.minimallauncher.data.AppPrefs
import com.jude.minimallauncher.data.UsageLimiter
import com.jude.minimallauncher.notify.NotificationStore

class LauncherListAdapter(
    private val onClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<LauncherListAdapter.Holder>() {

    private val items = mutableListOf<AppInfo>()

    fun submit(newItems: List<AppInfo>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app_text, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = items[position]
        holder.name.text = item.label
        holder.dot.visibility = if (NotificationStore.active.contains(item.packageName)) View.VISIBLE else View.GONE
        val textColor = if (AppPrefs.isDarkWallpaper(holder.itemView.context)) android.graphics.Color.WHITE else android.graphics.Color.BLACK
        holder.name.setTextColor(textColor)

        val limit = AppPrefs.getLimitMinutes(holder.itemView.context, item.packageName)
        val used = UsageLimiter.getTodayUsageMinutes(holder.itemView.context, item.packageName)
        val percent = if (limit?.hardMinutes != null && limit.hardMinutes > 0) {
            (used * 100 / limit.hardMinutes).coerceIn(0, 100)
        } else 0
        holder.usage.progress = percent

        holder.itemView.setOnClickListener { onClick(item) }
    }

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
        val dot: View = view.findViewById(R.id.dot)
        val usage: android.widget.ProgressBar = view.findViewById(R.id.usage_bar)
    }
}
