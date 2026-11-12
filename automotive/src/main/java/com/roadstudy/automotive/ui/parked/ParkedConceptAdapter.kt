package com.roadstudy.automotive.ui.parked

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.roadstudy.automotive.R
import com.roadstudy.automotive.databinding.ItemParkedConceptBinding
import com.roadstudy.automotive.session.SelfCheck

/**
 * 오늘 학습한 중요한 개념 카드 목록.
 * [relistenLabel] 은 "퇴근길에 다시"처럼 이동 유형에 따라 바뀌는 첫 번째 버튼 문구다.
 */
class ParkedConceptAdapter(
    private val onChoose: (conceptId: String, check: SelfCheck) -> Unit
) : ListAdapter<ParkedConceptUi, ParkedConceptAdapter.ViewHolder>(DIFF) {

    @StringRes
    var relistenLabel: Int = R.string.parked_choice_again_to_work
        set(value) {
            if (field != value) {
                field = value
                notifyDataSetChanged()
            }
        }

    class ViewHolder(val binding: ItemParkedConceptBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemParkedConceptBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val concept = getItem(position)
        val binding = holder.binding

        binding.tvConceptTitle.text = concept.title
        binding.tvConceptSummary.text = concept.summary
        binding.btnRelisten.setText(relistenLabel)

        binding.btnRelisten.setBackgroundResource(backgroundOf(selected = concept.choice == SelfCheck.RELISTEN))
        binding.btnKnow.setBackgroundResource(backgroundOf(selected = concept.choice == SelfCheck.KNOW))

        binding.btnRelisten.setOnClickListener { onChoose(concept.conceptId, SelfCheck.RELISTEN) }
        binding.btnKnow.setOnClickListener { onChoose(concept.conceptId, SelfCheck.KNOW) }
    }

    private fun backgroundOf(selected: Boolean) =
        if (selected) R.drawable.bg_choice_selected else R.drawable.bg_choice

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ParkedConceptUi>() {
            override fun areItemsTheSame(old: ParkedConceptUi, new: ParkedConceptUi) =
                old.conceptId == new.conceptId

            override fun areContentsTheSame(old: ParkedConceptUi, new: ParkedConceptUi) = old == new
        }
    }
}
