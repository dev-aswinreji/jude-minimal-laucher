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
        holder.dot.visibility = View.GONE
        val textColor = if (AppPrefs.isDarkWallpaper(holder.itemView.context)) android.graphics.Color.WHITE else android.graphics.Color.BLACK
        holder.name.setTextColor(textColor)

        holder.itemView.setOnClickListener { onClick(item) }
    }

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
        val dot: View = view.findViewById(R.id.dot)
    }
}
