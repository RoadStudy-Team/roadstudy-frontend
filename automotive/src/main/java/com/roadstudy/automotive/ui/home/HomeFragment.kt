package com.roadstudy.automotive.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.roadstudy.automotive.R
import com.roadstudy.automotive.databinding.FragmentHomeBinding
import com.roadstudy.automotive.session.TripType
import com.roadstudy.automotive.ui.driving.DrivingFragment
import com.roadstudy.automotive.ui.othertrip.OtherTripFragment

/**
 * 홈 화면. [HomeViewModel] 의 [HomeUiState] 를 그리고, 사용자 입력(섹션 선택, 시작 버튼)을 ViewModel 에 넘긴다.
 * 무엇을 보여줄지의 계산은 [HomeUiMapper] 가 한다.
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeViewModel

    private val sectionAdapter = SectionCardAdapter { sectionId -> viewModel.selectSection(sectionId) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 개발용: `am start ... --ez home_empty true` 로 실행하면 자료가 없는 빈 상태로 시작한다
        val startEmpty = requireActivity().intent.getBooleanExtra(EXTRA_START_EMPTY, false)
        viewModel = ViewModelProvider(this, homeViewModelFactory(startEmpty))[HomeViewModel::class.java]

        binding.recyclerSections.layoutManager = GridLayoutManager(requireContext(), SECTION_COLUMNS)
        binding.recyclerSections.adapter = sectionAdapter

        binding.ctaButton.setOnClickListener { onCtaClick() }

        viewModel.uiState.observe(viewLifecycleOwner) { state -> render(state) }
    }

    private fun render(state: HomeUiState) {
        binding.groupSections.isVisible = !state.isEmpty
        binding.cardEmpty.isVisible = state.isEmpty

        // 오늘의 경로 카드. 값이 없으면 "-"
        val noValue = getString(R.string.no_value)
        binding.tvTo.text = getString(R.string.to_destination, state.destinationName)
        binding.tvDistance.text = state.distanceKm ?: noValue
        binding.tvEta.text = state.etaMinutes?.toString() ?: noValue
        binding.tvArrival.text = state.arrivalTime ?: noValue
        binding.tvCongestion.text = state.congestionCount
            ?.let { getString(R.string.congestion_value, it) } ?: noValue
        // 경로선 가운데의 노란 점은 정체 구간이 있을 때만 보인다
        binding.dotCongestion.isVisible = (state.congestionCount ?: 0) > 0
        // 자료가 없을 때는 "0분 · 개념 0개" 대신 "자료 없음"
        binding.tvLearning.text = if (state.isEmpty) {
            getString(R.string.learning_empty)
        } else {
            getString(R.string.learning_value, state.learningMinutes, state.learningConceptCount)
        }

        binding.tvCta.setText(
            when {
                state.isEmpty -> R.string.cta_sample
                state.tripType == TripType.TO_WORK -> R.string.cta_to_work
                state.tripType == TripType.TO_HOME -> R.string.cta_to_home
                else -> R.string.cta_other
            }
        )

        sectionAdapter.submitList(state.sections)
    }

    private fun onCtaClick() {
        val state = viewModel.uiState.value ?: return
        if (state.isEmpty) {
            viewModel.startSampleExperience()
        } else {
            val sectionId = state.sections.firstOrNull { it.selected }?.sectionId
            // 출근도 퇴근도 아닌 이동은 시작 전에 학습할지 한 번 묻는다
            val next = if (state.tripType == TripType.OTHER) {
                OtherTripFragment.newInstance(sectionId)
            } else {
                DrivingFragment.newInstance(sectionId)
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, next)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun homeViewModelFactory(startEmpty: Boolean) = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            // TODO: Pleos SDK 연동 시 FakeHomeDataSource 를 실제 구현으로 교체한다
            HomeViewModel(FakeHomeDataSource.fromIntent(requireActivity().intent, startEmpty)) as T
    }

    companion object {
        const val EXTRA_START_EMPTY = "home_empty"
        private const val SECTION_COLUMNS = 2
    }
}
