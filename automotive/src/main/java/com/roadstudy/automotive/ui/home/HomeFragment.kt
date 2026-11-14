package com.roadstudy.automotive.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.roadstudy.automotive.R
import com.roadstudy.automotive.databinding.FragmentHomeBinding
import com.roadstudy.automotive.navi.CommuteHub
import com.roadstudy.automotive.navi.NaviSignalHub
import com.roadstudy.automotive.session.CommuteInfo
import com.roadstudy.automotive.session.CommuteTarget
import com.roadstudy.automotive.session.NaviSignals
import com.roadstudy.automotive.session.TripType
import com.roadstudy.automotive.ui.driving.DrivingFragment
import com.roadstudy.automotive.ui.othertrip.OtherTripFragment
import com.roadstudy.automotive.vehicle.VehicleSignalHub
import com.roadstudy.shared.R as SharedR

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
        binding.btnCommuteWork.setOnClickListener { viewModel.selectCommute(CommuteTarget.WORK) }
        binding.btnCommuteHome.setOnClickListener { viewModel.selectCommute(CommuteTarget.HOME) }

        viewModel.uiState.observe(viewLifecycleOwner) { state -> render(state) }
        NaviSignalHub.signals.observe(viewLifecycleOwner) { signals -> viewModel.onNaviSignals(signals) }
        CommuteHub.info.observe(viewLifecycleOwner) { info -> viewModel.onCommute(info) }
        startWhenDriveSelected()
    }

    /**
     * 기어가 D단으로 바뀌면(출발) 시작 버튼을 누른 것처럼 세션을 시작한다.
     * 이미 D단인 채로 이 화면이 만들어진 경우에는 시작하지 않고, 자료가 없을 때도 시작하지 않는다.
     */
    private fun startWhenDriveSelected() {
        var previousGear = VehicleSignalHub.signals.value?.gear
        VehicleSignalHub.signals.observe(viewLifecycleOwner) { signals ->
            val state = viewModel.uiState.value
            if (VehicleSignalHub.startedDriving(previousGear, signals.gear) && state != null && !state.isEmpty) {
                openSession(state)
            }
            previousGear = signals.gear
        }
    }

    private fun render(state: HomeUiState) {
        binding.groupSections.isVisible = !state.isEmpty
        binding.cardEmpty.isVisible = state.isEmpty

        renderCommuteToggle(state.commuteToggle)

        // 오늘의 경로 카드. 값이 없으면 "-"
        val noValue = getString(R.string.no_value)
        // 내비 즐겨찾기에 집·회사가 없고 안내 중인 경로도 없으면 목적지 이름이 비어 있다
        binding.tvTo.text = if (state.destinationName.isBlank()) {
            getString(R.string.to_destination_none)
        } else {
            getString(R.string.to_destination, state.destinationName)
        }
        binding.tvDistance.text = state.distanceKm ?: noValue
        binding.tvEta.text = state.etaMinutes?.toString() ?: noValue
        binding.tvArrival.text = state.arrivalTime ?: noValue
        binding.tvCongestion.text = state.congestionCount
            ?.let { getString(R.string.congestion_value, it) } ?: noValue
        // 경로선 가운데의 노란 점은 정체 구간이 있을 때만 보인다
        binding.dotCongestion.isVisible = (state.congestionCount ?: 0) > 0
        // 자료가 없을 때는 "0분 · 개념 0개" 대신 "자료 없음"
        // 경로를 몰라 이동 시간에 맞춘 학습 분량을 계산할 수 없으면 "-"
        binding.tvLearning.text = if (state.isEmpty) {
            getString(R.string.learning_empty)
        } else if (state.etaMinutes == null) {
            noValue
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

    private fun renderCommuteToggle(toggle: CommuteToggleUi?) {
        binding.groupCommuteToggle.isVisible = toggle != null
        if (toggle == null) return
        renderToggleButton(binding.btnCommuteWork, toggle.selected == CommuteTarget.WORK, toggle.workEnabled)
        renderToggleButton(binding.btnCommuteHome, toggle.selected == CommuteTarget.HOME, toggle.homeEnabled)
    }

    /** 고른 쪽은 강조하고, 내비에 등록하지 않은 쪽은 흐리게 해서 누를 수 없게 한다 */
    private fun renderToggleButton(button: TextView, selected: Boolean, enabled: Boolean) {
        button.isEnabled = enabled
        button.isSelected = selected
        button.alpha = if (enabled) 1f else DISABLED_ALPHA
        button.setBackgroundResource(if (selected) R.drawable.bg_toggle_selected else R.drawable.bg_pill)
        button.setTextColor(
            ContextCompat.getColor(requireContext(), if (selected) SharedR.color.rs_accent else SharedR.color.rs_text_sub)
        )
    }

    private fun onCtaClick() {
        val state = viewModel.uiState.value ?: return
        if (state.isEmpty) {
            viewModel.startSampleExperience()
        } else {
            openSession(state)
        }
    }

    /** 선택한 섹션으로 세션을 시작한다 */
    private fun openSession(state: HomeUiState) {
        val sectionId = state.sections.firstOrNull { it.selected }?.sectionId
        // 출근도 퇴근도 아닌 이동은 시작 전에 학습할지 한 번 묻는다
        val next = if (state.tripType == TripType.OTHER) {
            OtherTripFragment.newInstance(sectionId)
        } else {
            DrivingFragment.newInstance(sectionId, state.commuteToggle?.selected)
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, next)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun homeViewModelFactory(startEmpty: Boolean) = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            // TODO: 저장소 연동 시 FakeHomeDataSource 를 실제 구현으로 교체한다 (경로·출퇴근 판별은 내비 값이 있으면 그것을 쓴다)
            HomeViewModel(
                FakeHomeDataSource.fromIntent(requireActivity().intent, startEmpty),
                initialNavi = NaviSignalHub.signals.value ?: NaviSignals(),
                initialCommute = CommuteHub.info.value ?: CommuteInfo()
            ) as T
    }

    companion object {
        const val EXTRA_START_EMPTY = "home_empty"
        private const val SECTION_COLUMNS = 2
        private const val DISABLED_ALPHA = 0.35f
    }
}
