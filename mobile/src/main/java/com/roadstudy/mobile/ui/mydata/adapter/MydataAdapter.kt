package com.roadstudy.mobile.ui.mydata.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.roadstudy.mobile.databinding.ItemMydataCardBinding
import com.roadstudy.mobile.ui.mydata.data.MydataItem

class MydataAdapter(
    private val onMoreClick: (View, MydataItem) -> Unit
) : ListAdapter<MydataItem, MydataAdapter.ViewHolder>(MydataDiffCallback()) {

    class ViewHolder(val binding: ItemMydataCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMydataCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        with(holder.binding) {
            tvCardTitle.text = item.title
            tvCardSubInfo.text = item.subInfo
            tvProgressText.text = item.progressText

            // '오늘 세션'으로 설정된 카드에만 배지 표시
            tvBadge.visibility = if (item.isToday) View.VISIBLE else View.GONE

            btnCardMore.setOnClickListener { onMoreClick(it, item) }
        }
    }
}

private class MydataDiffCallback : DiffUtil.ItemCallback<MydataItem>() {
    override fun areItemsTheSame(oldItem: MydataItem, newItem: MydataItem) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: MydataItem, newItem: MydataItem) = oldItem == newItem
}