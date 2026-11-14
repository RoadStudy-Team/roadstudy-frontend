package com.roadstudy.mobile.ui.mydata.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.roadstudy.mobile.databinding.ItemChapterBinding
import com.roadstudy.mobile.ui.mydata.data.ChapterItem
import kotlin.math.roundToInt

class ChapterAdapter(private val items: List<ChapterItem>) :
    RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder>() {

    class ChapterViewHolder(val binding: ItemChapterBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterViewHolder {
        val binding = ItemChapterBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ChapterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChapterViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context
        val cardView = holder.binding.root as? MaterialCardView

        // strokeWidth는 dp가 아니라 px 단위이므로 dp -> px 변환이 필요해요
        val density = context.resources.displayMetrics.density
        val strokeNormal = (1f * density).roundToInt()
        val strokeExpanded = (1.5f * density).roundToInt()

        holder.binding.apply {
            tvChapterNumber.text = item.number
            tvChapterInfo.text = item.info
            tvExpandedDescription.text = item.expandedDescription
            tvQuestion.text = item.question
            tvAnswer.text = item.answer

            // 1. 수정 모드(isEditing)에 따른 제목과 설명 전환
            if (item.isEditing) {
                tvChapterTitle.visibility = View.GONE
                etChapterTitle.visibility = View.VISIBLE
                etChapterTitle.setText(item.title)

                tvChapterDesc.visibility = View.GONE
                etChapterDesc.visibility = View.VISIBLE
                etChapterDesc.setText(item.description)

                etChapterTitle.requestFocus()
                etChapterTitle.setSelection(etChapterTitle.text.length)

                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(etChapterTitle, InputMethodManager.SHOW_IMPLICIT)
            } else {
                tvChapterTitle.visibility = View.VISIBLE
                etChapterTitle.visibility = View.GONE
                tvChapterTitle.text = item.title

                tvChapterDesc.visibility = View.VISIBLE
                etChapterDesc.visibility = View.GONE
                tvChapterDesc.text = item.description
            }

            // 2. 확장 상태에 따른 상세 영역 및 테두리 색상 처리
            if (item.isExpanded) {
                layoutExpandedContent.visibility = View.VISIBLE
                cardView?.strokeColor = Color.parseColor("#5B9CF6")
                cardView?.strokeWidth = strokeExpanded
            } else {
                layoutExpandedContent.visibility = View.GONE
                cardView?.strokeColor = Color.parseColor("#2A2E37")
                cardView?.strokeWidth = strokeNormal
            }

            // 3. 카드 전체 클릭 시 확장/축소 토글 (수정 중이 아닐 때만)
            root.setOnClickListener {
                val pos = holder.bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
                if (!item.isEditing) {
                    item.isExpanded = !item.isExpanded
                    notifyItemChanged(pos)
                }
            }

            // 4. 연필 아이콘 클릭 시 수정 모드 진입/해제 토글
            btnEditChapter.setOnClickListener {
                val pos = holder.bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
                if (item.isEditing) {
                    item.title = etChapterTitle.text.toString()
                    item.description = etChapterDesc.text.toString()
                    item.isEditing = false

                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(etChapterTitle.windowToken, 0)
                } else {
                    item.isEditing = true
                }
                notifyItemChanged(pos)
            }

            // 5. 하단 확장 영역 안의 '저장' 버튼 클릭 시: 수정 내용 반영 + 카드 접힘 + 수정 모드 종료
            btnSave.setOnClickListener {
                val pos = holder.bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener

                if (item.isEditing) {
                    item.title = etChapterTitle.text.toString()
                    item.description = etChapterDesc.text.toString()
                }

                item.isEditing = false
                item.isExpanded = false

                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(etChapterTitle.windowToken, 0)

                notifyItemChanged(pos)
            }
        }
    }

    override fun getItemCount(): Int = items.size
}