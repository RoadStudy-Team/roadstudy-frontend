package com.roadstudy.mobile.mydata.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.roadstudy.mobile.databinding.ItemMydataCardBinding
import com.roadstudy.mobile.mydata.data.MydataItem // ⭐ MydataItem으로 임포트 확인

class MydataAdapter(private val items: List<MydataItem>) :
    RecyclerView.Adapter<MydataAdapter.MyDataViewHolder>() {

    inner class MyDataViewHolder(val binding: ItemMydataCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MydataItem) {
            binding.tvCardTitle.text = item.title
            binding.tvCardSubInfo.text = item.subInfo
            binding.tvProgressText.text = item.progressText

            // 오늘 세션 뱃지 보이기/숨기기 처리
            if (item.isTodaySession) {
                binding.tvBadge.visibility = View.VISIBLE
            } else {
                binding.tvBadge.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyDataViewHolder {
        val binding = ItemMydataCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyDataViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyDataViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}