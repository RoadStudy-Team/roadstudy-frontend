package com.roadstudy.automotive.ui.othertrip

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.roadstudy.automotive.R
import com.roadstudy.automotive.databinding.FragmentOtherTripBinding
import com.roadstudy.automotive.ui.driving.DrivingFragment
import com.roadstudy.automotive.ui.home.FakeHomeDataSource

/**
 * 다른 이동 안내 화면. 출퇴근이 아닌 이동에서 출발 전에 "이번 이동도 학습할까요?"를 묻는다.
 * - 학습하기: 주행 중 화면으로 간다
 * - 이번엔 쉬기: 홈으로 돌아간다
 */
class OtherTripFragment : Fragment() {

    private var _binding: FragmentOtherTripBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: OtherTripViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOtherTripBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sectionId = requireArguments().getString(ARG_SECTION_ID)
        viewModel = ViewModelProvider(this, otherTripViewModelFactory(sectionId))[OtherTripViewModel::class.java]

        binding.btnStudy.setOnClickListener { startStudy(sectionId) }
        // 이번엔 쉬기: 홈으로 돌아간다 (홈 위에 쌓인 화면을 모두 걷어낸다)
        // TODO: "다음부터 묻지 않기"가 체크되어 있으면 이 장소를 저장소(Room)에 기록한다
        binding.btnRest.setOnClickListener {
            parentFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
        binding.rowDontAsk.setOnClickListener { viewModel.toggleDontAskAgain() }

        viewModel.uiState.observe(viewLifecycleOwner) { state -> render(state) }
    }

    private fun startStudy(sectionId: String?) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, DrivingFragment.newInstance(sectionId))
            .addToBackStack(null)
            .commit()
    }

    private fun render(state: OtherTripUiState) {
        binding.tvDestination.text = if (state.distanceKm != null && state.etaMinutes != null) {
            getString(R.string.other_destination, state.destinationName, state.distanceKm, state.etaMinutes)
        } else {
            getString(R.string.other_destination_no_route, state.destinationName)
        }
        binding.tvMaxMinutes.text = getString(R.string.other_max_minutes, state.maxLearningMinutes)
        binding.tvConceptCount.text = getString(R.string.concept_count, state.conceptCount)
        binding.tvSection.text = state.sectionTitle

        // 체크하면 상자 테두리가 파랗게 바뀌고 안쪽 표시가 나타난다
        binding.checkBox.setBackgroundResource(
            if (state.dontAskAgain) R.drawable.bg_choice_selected else R.drawable.bg_choice
        )
        binding.checkInner.isVisible = state.dontAskAgain
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun otherTripViewModelFactory(sectionId: String?) = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            // TODO: Pleos SDK 연동 시 가짜 입력 대신 실제 내비 경로와 선택한 섹션을 쓴다
            val input = FakeHomeDataSource.fromIntent(requireActivity().intent).load()
            return OtherTripViewModel(input, sectionId) as T
        }
    }

    companion object {
        private const val ARG_SECTION_ID = "section_id"

        fun newInstance(sectionId: String?) = OtherTripFragment().apply {
            arguments = Bundle().apply { putString(ARG_SECTION_ID, sectionId) }
        }
    }
}
