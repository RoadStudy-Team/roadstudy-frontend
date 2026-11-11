package com.roadstudy.automotive.ui.quiz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.roadstudy.automotive.R
import com.roadstudy.automotive.databinding.FragmentQuizBinding
import com.roadstudy.shared.R as SharedR
import com.roadstudy.shared.model.SampleQuizzes

/**
 * 확인 퀴즈 질문 화면. 한 화면에 한 문제씩 풀고, 마지막 문제를 지나면 결과 화면으로 넘어간다.
 * 풀 문제는 주차 후 복습 화면에서 넘겨받은 개념([ARG_CONCEPT_IDS])의 퀴즈다.
 */
class QuizFragment : Fragment() {

    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: QuizViewModel

    private val choiceAdapter = QuizChoiceAdapter { index -> viewModel.select(index) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val conceptIds = requireArguments().getStringArray(ARG_CONCEPT_IDS).orEmpty().toList()
        val sectionTitle = requireArguments().getString(ARG_SECTION_TITLE).orEmpty()
        viewModel = ViewModelProvider(this, quizViewModelFactory(conceptIds, sectionTitle))[QuizViewModel::class.java]

        // 풀 문제가 없으면 복습 화면으로 돌아간다 (복습 화면이 막아 두지만 방어용)
        if (!viewModel.hasQuestions) {
            parentFragmentManager.popBackStack()
            return
        }

        binding.recyclerChoices.layoutManager = GridLayoutManager(requireContext(), CHOICE_COLUMNS)
        binding.recyclerChoices.adapter = choiceAdapter

        binding.btnSkip.setOnClickListener { openResultIfDone(viewModel.skip()) }
        binding.btnNext.setOnClickListener { openResultIfDone(viewModel.next()) }

        viewModel.uiState.observe(viewLifecycleOwner) { state -> if (state != null) render(state) }
    }

    private fun render(state: QuizUiState) {
        binding.tvQuizTitle.text = getString(R.string.quiz_title, state.pageNumber, state.total)
        binding.tvQuizSection.text = state.sectionTitle
        renderProgressDots(state.pageNumber, state.total)

        binding.tvQuestion.text = state.question
        renderFeedbackLine(state)
        choiceAdapter.submitList(state.choices)
        // 고른 뒤에는 건너뛸 수 없다 (자리는 그대로 두어 화면이 흔들리지 않게 한다)
        binding.btnSkip.visibility = if (state.canSkip) View.VISIBLE else View.INVISIBLE

        binding.tvNext.setText(if (state.isLast) R.string.quiz_show_result else R.string.quiz_next)
        // 선택지를 골라 정오답을 확인하기 전에는 다음으로 갈 수 없다
        binding.btnNext.isEnabled = state.canAdvance
        binding.btnNext.alpha = if (state.canAdvance) 1f else DISABLED_ALPHA
    }

    /** 문제 아래 한 줄: 고르기 전에는 단서, 고른 뒤에는 정오답 안내 */
    private fun renderFeedbackLine(state: QuizUiState) {
        val context = requireContext()
        val (text, color) = when (state.feedback) {
            QuizFeedback.NONE -> state.hint to SharedR.color.rs_text_sub
            QuizFeedback.CORRECT -> getString(R.string.quiz_feedback_correct) to SharedR.color.rs_safe
            QuizFeedback.WRONG -> getString(R.string.quiz_feedback_wrong, state.answerText) to SharedR.color.rs_wrong
        }
        binding.tvHint.text = text
        binding.tvHint.setTextColor(ContextCompat.getColor(context, color))
    }

    /** 지금 페이지는 길쭉한 알약, 나머지는 작은 점. 문제 수보다 많은 점은 숨긴다 */
    private fun renderProgressDots(pageNumber: Int, total: Int) {
        val density = resources.displayMetrics.density
        val dots = listOf(binding.dot1, binding.dot2, binding.dot3)
        dots.forEachIndexed { index, dot ->
            dot.isVisible = index < total
            val active = index + 1 == pageNumber
            dot.setBackgroundResource(if (active) R.drawable.pill_progress_active else R.drawable.dot_progress)
            dot.layoutParams = dot.layoutParams.apply {
                width = ((if (active) ACTIVE_DOT_WIDTH_DP else DOT_SIZE_DP) * density).toInt()
            }
        }
    }

    private fun openResultIfDone(result: QuizResult?) {
        if (result == null) return
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, QuizResultFragment.newInstance(result.correct, result.total))
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun quizViewModelFactory(conceptIds: List<String>, sectionTitle: String) =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                // TODO: 퀴즈 데이터 형식이 정해지면 샘플 대신 실제 퀴즈를 쓴다
                val session = QuizSession(SampleQuizzes.forConcepts(conceptIds))
                return QuizViewModel(session, sectionTitle) as T
            }
        }

    companion object {
        private const val ARG_CONCEPT_IDS = "concept_ids"
        private const val ARG_SECTION_TITLE = "section_title"
        private const val CHOICE_COLUMNS = 4
        private const val DISABLED_ALPHA = 0.4f
        private const val DOT_SIZE_DP = 16f
        private const val ACTIVE_DOT_WIDTH_DP = 64f

        fun newInstance(conceptIds: List<String>, sectionTitle: String) = QuizFragment().apply {
            arguments = Bundle().apply {
                putStringArray(ARG_CONCEPT_IDS, conceptIds.toTypedArray())
                putString(ARG_SECTION_TITLE, sectionTitle)
            }
        }
    }
}
