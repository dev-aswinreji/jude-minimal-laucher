package com.jude.minimallauncher.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jude.minimallauncher.R
import com.jude.minimallauncher.data.AppPrefs

class SettingsListAdapter(
    private val onToggle: (String, Boolean) -> Unit,
    private val onLimit: (String) -> Unit,
    private val onFavorite: (String) -> Unit,
    private val onEmergency: (String) -> Unit,
    private val onNet: (String) -> Unit
) : RecyclerView.Adapter<SettingsListAdapter.Holder>() {

    private val items = mutableListOf<SettingsAppInfo>()
    private val whitelist = mutableSetOf<String>()

    fun submit(newItems: List<SettingsAppInfo>, currentWhitelist: Set<String>) {
        items.clear()
        items.addAll(newItems)
        whitelist.clear()
        whitelist.addAll(currentWhitelist)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app_toggle, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = items[position]
        holder.name.text = item.label
        holder.icon.setImageDrawable(item.icon)
        holder.toggle.isChecked = whitelist.contains(item.packageName)
        holder.toggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) whitelist.add(item.packageName) else whitelist.remove(item.packageName)
            onToggle(item.packageName, isChecked)
        }
        holder.limit.setOnClickListener { onLimit(item.packageName) }

        val favorites = AppPrefs.getFavorites(holder.itemView.context)
        val emergency = AppPrefs.getEmergencyApps(holder.itemView.context)
        val netBlocked = AppPrefs.getNetBlocked(holder.itemView.context)
        holder.fav.text = if (favorites.contains(item.packageName)) "★" else "☆"
        holder.emergency.text = if (emergency.contains(item.packageName)) "⚠" else "!"
        holder.net.text = if (netBlocked.contains(item.packageName)) "NET✕" else "NET"
        holder.fav.setOnClickListener { onFavorite(item.packageName) }
        holder.emergency.setOnClickListener { onEmergency(item.packageName) }
        holder.net.setOnClickListener { onNet(item.packageName) }

        val limit = AppPrefs.getLimitMinutes(holder.itemView.context, item.packageName)
        holder.limit.text = if (limit == null) "Limit" else {
            val soft = limit.softMinutes?.let { "S:$it" } ?: ""
            val hard = limit.hardMinutes?.let { "H:$it" } ?: ""
            listOf(soft, hard).filter { it.isNotBlank() }.joinToString(" ")
        }
    }

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.icon)
        val name: TextView = view.findViewById(R.id.name)
        val toggle: Switch = view.findViewById(R.id.toggle)
        val limit: TextView = view.findViewById(R.id.limit)
        val fav: TextView = view.findViewById(R.id.fav)
        val emergency: TextView = view.findViewById(R.id.emergency)
        val net: TextView = view.findViewById(R.id.net)
    }
}
