package com.roadstudy.automotive.ui.parked

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.roadstudy.automotive.R
import com.roadstudy.automotive.playback.LearningHub
import com.roadstudy.automotive.databinding.FragmentParkedBinding
import com.roadstudy.automotive.session.TripType
import com.roadstudy.automotive.ui.home.FakeHomeDataSource
import com.roadstudy.automotive.ui.quiz.QuizFragment

/**
 * 주차 후 복습 화면. 도착(P단) 후 오늘 학습한 중요한 개념을 정리한다.
 * 개념마다 "다시 들을래요 / 이제 알아요"를 고르면 진도와 다시 듣기 목록에 반영된다.
 */
class ParkedFragment : Fragment() {

    private var _binding: FragmentParkedBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ParkedViewModel

    private val conceptAdapter = ParkedConceptAdapter { conceptId, check -> viewModel.choose(conceptId, check) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentParkedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sectionId = requireArguments().getString(ARG_SECTION_ID)
        viewModel = ViewModelProvider(this, parkedViewModelFactory(sectionId))[ParkedViewModel::class.java]

        binding.recyclerConcepts.layoutManager = GridLayoutManager(requireContext(), CONCEPT_COLUMNS)
        binding.recyclerConcepts.adapter = conceptAdapter

        binding.btnQuiz.setOnClickListener { openQuiz() }
        // 닫기: 홈으로 돌아간다 (홈 위에 쌓인 화면을 모두 걷어낸다)
        binding.btnClose.setOnClickListener {
            parentFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state -> render(state) }
    }

    /** 화면에 보이는 개념으로 확인 퀴즈를 연다 */
    private fun openQuiz() {
        val state = viewModel.uiState.value ?: return
        if (state.quizCount == 0) return
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, QuizFragment.newInstance(state.concepts.map { it.conceptId }, state.sectionTitle))
            .addToBackStack(null)
            .commit()
    }

    private fun render(state: ParkedUiState) {
        binding.tvTitle.text = when {
            state.tripMinutes == null -> getString(R.string.parked_title_no_trip, state.conceptTotal)
            state.tripType == TripType.TO_WORK ->
                getString(R.string.parked_title_to_work, state.tripMinutes, state.conceptTotal)
            state.tripType == TripType.TO_HOME ->
                getString(R.string.parked_title_to_home, state.tripMinutes, state.conceptTotal)
            else -> getString(R.string.parked_title_other, state.tripMinutes, state.conceptTotal)
        }
        binding.tvSubtitle.text = state.fileName
            ?.let { getString(R.string.parked_subtitle, state.sectionTitle, it) }
            ?: state.sectionTitle

        binding.tvKnown.text = state.knownCount.toString()
        binding.tvRelisten.text = state.relistenCount.toString()
        binding.tvStreak.text = getString(R.string.parked_stat_days, state.streakDays)

        binding.tvSectionCount.text = getString(R.string.parked_section_count, state.concepts.size)

        val againLabel = when (state.tripType) {
            TripType.TO_WORK -> R.string.parked_choice_again_to_work
            TripType.TO_HOME -> R.string.parked_choice_again_to_home
            else -> R.string.parked_choice_again_other
        }
        conceptAdapter.relistenLabel = againLabel
        conceptAdapter.submitList(state.concepts)

        binding.tvAgainNotice.text = getString(R.string.parked_again_notice, getString(againLabel), state.relistenCount)
        binding.tvQuiz.text = getString(R.string.parked_quiz, state.quizCount)
        // 풀 개념이 없으면 퀴즈 버튼을 막는다
        binding.btnQuiz.isEnabled = state.quizCount > 0
        binding.btnQuiz.alpha = if (state.quizCount > 0) 1f else DISABLED_ALPHA
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun parkedViewModelFactory(sectionId: String?) = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            // 방금 도착한 세션의 실제 결과가 있으면 그걸 쓰고, 없으면(가짜 상황 등) 데모 세션을 돌린다
            // TODO: 저장소(Room) 후에는 저장된 진도와 연속 학습일을 쓴다
            val input = FakeHomeDataSource.fromIntent(requireActivity().intent).load()
            val result = LearningHub.lastResult?.takeIf { it.sectionId == sectionId }
            val start = result?.let { ParkedDemo.fromSession(input, it) } ?: ParkedDemo(input, sectionId).start()
            return ParkedViewModel(start) as T
        }
    }

    companion object {
        private const val ARG_SECTION_ID = "section_id"
        private const val CONCEPT_COLUMNS = 3
        private const val DISABLED_ALPHA = 0.4f

        fun newInstance(sectionId: String?) = ParkedFragment().apply {
            arguments = Bundle().apply { putString(ARG_SECTION_ID, sectionId) }
        }
    }
}
