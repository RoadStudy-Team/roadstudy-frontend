package com.roadstudy.automotive.ui.quiz

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.roadstudy.automotive.R
import com.roadstudy.automotive.databinding.ItemQuizChoiceBinding
import com.roadstudy.shared.R as SharedR

/**
 * 퀴즈 선택지 4칸. 고르면 바로 정오답이 드러난다.
 * 정답은 초록, 내가 고른 오답은 빨강, 나머지는 흐리게 보여주고, 고른 뒤에는 다시 누를 수 없다.
 */
class QuizChoiceAdapter(
    private val onChoose: (index: Int) -> Unit
) : ListAdapter<QuizChoiceUi, QuizChoiceAdapter.ViewHolder>(DIFF) {

    class ViewHolder(val binding: ItemQuizChoiceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemQuizChoiceBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val choice = getItem(position)
        val binding = holder.binding
        val context = binding.root.context

        binding.tvChoice.text = choice.text

        val background = when (choice.mark) {
            ChoiceMark.CORRECT -> R.drawable.bg_choice_correct
            ChoiceMark.WRONG -> R.drawable.bg_choice_wrong
            ChoiceMark.NONE, ChoiceMark.DIMMED -> R.drawable.bg_choice
        }
        binding.root.setBackgroundResource(background)

        // 정답과 내가 고른 오답에만 원(체크 아이콘 자리)을 보여주고, 정오답에 맞는 색을 입힌다
        val checkColor = when (choice.mark) {
            ChoiceMark.CORRECT -> SharedR.color.rs_safe
            ChoiceMark.WRONG -> SharedR.color.rs_wrong
            ChoiceMark.NONE, ChoiceMark.DIMMED -> null
        }
        binding.checkMark.isVisible = checkColor != null
        if (checkColor != null) {
            binding.checkMark.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, checkColor))
        }

        binding.root.alpha = if (choice.mark == ChoiceMark.DIMMED) DIMMED_ALPHA else 1f
        // 고른 뒤에는 칸을 다시 누를 수 없다
        val answered = choice.mark != ChoiceMark.NONE
        binding.root.isClickable = !answered
        binding.root.setOnClickListener { if (!answered) onChoose(holder.layoutPosition) }
    }

    companion object {
        private const val DIMMED_ALPHA = 0.45f

        private val DIFF = object : DiffUtil.ItemCallback<QuizChoiceUi>() {
            // 선택지 글자가 같으면 같은 칸으로 본다 (한 문제 안에서 선택지는 서로 다르다)
            override fun areItemsTheSame(old: QuizChoiceUi, new: QuizChoiceUi) = old.text == new.text

            override fun areContentsTheSame(old: QuizChoiceUi, new: QuizChoiceUi) = old == new
        }
    }
}
