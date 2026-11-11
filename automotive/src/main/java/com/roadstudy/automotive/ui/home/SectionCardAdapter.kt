package com.roadstudy.automotive.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.roadstudy.automotive.R
import com.roadstudy.automotive.databinding.ItemSectionCardBinding
import com.roadstudy.shared.model.MaterialType

/** 홈 화면의 스터디 섹션 카드 목록. 카드 모양은 item_section_card.xml. */
class SectionCardAdapter(
    private val onSectionClick: (sectionId: String) -> Unit
) : ListAdapter<SectionCardUi, SectionCardAdapter.ViewHolder>(DIFF) {

    class ViewHolder(val binding: ItemSectionCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSectionCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val card = getItem(position)
        val binding = holder.binding
        val context = binding.root.context

        binding.tvType.setText(typeLabelOf(card.type))
        binding.tvTitle.text = card.title
        binding.tvFileName.text = card.fileName.orEmpty()
        binding.tvConceptCount.text = context.getString(R.string.concept_count, card.conceptCount)
        binding.tvProgressPercent.text = context.getString(R.string.progress_percent, card.progressPercent)
        binding.progressBar.progress = card.progressPercent
        binding.tvReviewCount.text = context.getString(R.string.review_count, card.reviewCount)

        // 다시 들을 개념 제목을 위에서부터 채우고, 빈 칸은 숨긴다. 하나도 없으면 소제목도 숨긴다
        val reviewViews = listOf(binding.tvReview1, binding.tvReview2, binding.tvReview3)
        reviewViews.forEachIndexed { index, view ->
            val title = card.reviewTitles.getOrNull(index)
            view.isVisible = title != null
            view.text = title?.let { context.getString(R.string.review_item, it) }.orEmpty()
        }
        binding.tvReviewListLabel.isVisible = card.reviewTitles.isNotEmpty()

        // 선택된 카드: 파란 테두리 + "선택됨" 라벨
        binding.tvSelected.isVisible = card.selected
        binding.cardRoot.setBackgroundResource(
            if (card.selected) R.drawable.bg_card_selected else R.drawable.bg_card
        )

        binding.cardRoot.setOnClickListener { onSectionClick(card.sectionId) }
    }

    private fun typeLabelOf(type: MaterialType): Int = when (type) {
        MaterialType.CERTIFICATE -> R.string.type_certificate
        MaterialType.ENGLISH -> R.string.type_english
        MaterialType.WORK -> R.string.type_work
        MaterialType.ETC -> R.string.type_etc
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<SectionCardUi>() {
            override fun areItemsTheSame(old: SectionCardUi, new: SectionCardUi) =
                old.sectionId == new.sectionId

            override fun areContentsTheSame(old: SectionCardUi, new: SectionCardUi) = old == new
        }
    }
}
