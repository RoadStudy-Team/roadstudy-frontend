package com.roadstudy.mobile.ui.onboarding.adapter

import com.roadstudy.mobile.ui.onboarding.data.SavedPlace
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.roadstudy.mobile.R

class PlaceAdapter(
    private val onClick: (SavedPlace) -> Unit
) : RecyclerView.Adapter<PlaceAdapter.ViewHolder>() {

    private var items: List<SavedPlace> = emptyList()

    fun submit(list: List<SavedPlace>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_place_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val place = items[position]
        holder.name.text = place.name
        holder.itemView.setOnClickListener { onClick(place) }
    }

    override fun getItemCount() = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvPlaceName)
    }
}