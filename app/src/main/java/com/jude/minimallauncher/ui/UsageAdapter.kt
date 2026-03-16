package com.jude.minimallauncher.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jude.minimallauncher.R

class UsageAdapter(private val items: List<Pair<String, Int>>) : RecyclerView.Adapter<UsageAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_usage, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val (name, mins) = items[position]
        holder.name.text = name
        holder.mins.text = "${mins}m"
    }

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
        val mins: TextView = view.findViewById(R.id.mins)
    }
}
