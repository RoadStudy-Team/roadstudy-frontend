package com.roadstudy.automotive.ui.driving

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.roadstudy.automotive.R
import com.roadstudy.automotive.databinding.FragmentDrivingBinding
import com.roadstudy.automotive.navi.CommuteHub
import com.roadstudy.automotive.navi.NaviSignalHub
import com.roadstudy.automotive.session.CommuteInfo
import com.roadstudy.automotive.session.CommuteTarget
import com.roadstudy.automotive.session.NaviSignals
import com.roadstudy.automotive.session.VehicleSignals
import com.roadstudy.automotive.ui.home.FakeHomeDataSource
import com.roadstudy.automotive.ui.home.withRoute
import com.roadstudy.automotive.ui.parked.ParkedFragment
import com.roadstudy.automotive.vehicle.VehicleSignalHub
import com.roadstudy.shared.R as SharedR

/**
 * 주행 중 화면. [DrivingViewModel] 의 [DrivingUiState] 를 그린다.
 * 홈에서 선택한 섹션([ARG_SECTION_ID])으로 세션을 진행한다.
 */
class DrivingFragment : Fragment() {

    private var _binding: FragmentDrivingBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DrivingViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDrivingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 기본은 실제 차량 신호를 따른다. 개발용으로 `am start ... --es driving_scenario night` 를 주면
        // 그 가짜 상황으로 시작한다 (safe, thinking, night, turn_ahead, arrived)
        val scenario = requireActivity().intent.getStringExtra(EXTRA_SCENARIO)?.let { DrivingScenario.fromKey(it) }
        val sectionId = requireArguments().getString(ARG_SECTION_ID)
        val commuteTarget = requireArguments().getString(ARG_COMMUTE_TARGET)?.let { CommuteTarget.valueOf(it) }
        viewModel = ViewModelProvider(
            this,
            driveViewModelFactory(sectionId, commuteTarget, scenario)
        )[DrivingViewModel::class.java]

        // 개발용: 화면을 누르면 가짜 상황이 차례로 바뀐다(실제 신호는 무시). 실제 운행에서는 터치 입력을 받지 않는다.
        // TODO: 신호 연동이 안정되면 제거
        binding.rootDriving.setOnClickListener { viewModel.nextScenario() }

        VehicleSignalHub.signals.observe(viewLifecycleOwner) { signals -> viewModel.onVehicleSignals(signals) }
        NaviSignalHub.signals.observe(viewLifecycleOwner) { signals -> viewModel.onNaviSignals(signals) }
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            render(state)
            // 도착(P단)하면 주차 후 복습 화면으로 넘어간다. 뒤로 돌아왔을 때 다시 넘어가지 않도록 한 번만 처리한다
            if (state.arrived && viewModel.consumeArrival()) openParkedReview(sectionId)
        }
    }

    private fun openParkedReview(sectionId: String?) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, ParkedFragment.newInstance(sectionId))
            .addToBackStack(null)
            .commit()
    }

    private fun render(state: DrivingUiState) {
        val context = requireContext()

        val dotColor = when (state.tone) {
            DrivingTone.SAFE -> SharedR.color.rs_safe
            DrivingTone.CAUTION -> SharedR.color.rs_caution
            DrivingTone.FOCUS -> SharedR.color.rs_text_sub
        }
        binding.dotStatus.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, dotColor))
        // 주의 상태의 문구는 회색으로 낮춘다 (도안 06)
        val statusColor = if (state.tone == DrivingTone.CAUTION) SharedR.color.rs_text_sub else SharedR.color.rs_text_main
        binding.tvStatus.setTextColor(ContextCompat.getColor(context, statusColor))
        binding.tvStatus.text = state.statusText

        binding.waveform.isVisible = state.visual != DrivingVisual.PAUSED
        binding.pauseCircle.isVisible = state.visual == DrivingVisual.PAUSED
        binding.ivCaution.isVisible = state.visual == DrivingVisual.WAVE_WITH_ICON
        // 상황별 파형: 크기, 진하기, 물결 속도(주기 ms)와 물결 깊이. 운전 중이므로 움직임은 은은하게 둔다
        when (state.visual) {
            DrivingVisual.WAVE -> bindWaveform(amplitude = 1f, intensity = 0.55f, periodMs = 1_800L, depth = 0.35f)
            DrivingVisual.WAVE_QUIET -> bindWaveform(amplitude = 0.55f, intensity = 0.45f, periodMs = 3_200L, depth = 0.2f)
            DrivingVisual.WAVE_WITH_ICON -> bindWaveform(amplitude = 0.75f, intensity = 0.5f, periodMs = 2_600L, depth = 0.25f)
            DrivingVisual.PAUSED -> Unit
        }

        binding.tvInfo.text = if (state.distanceKm != null && state.etaMinutes != null) {
            getString(
                R.string.driving_info,
                state.destinationName, state.distanceKm, state.etaMinutes, state.conceptIndex, state.conceptTotal
            )
        } else {
            getString(R.string.driving_info_no_route, state.conceptIndex, state.conceptTotal)
        }
        binding.tvSection.text = state.sectionTitle
        binding.tvHint.isVisible = state.hint != DrivingHint.NONE
        binding.tvHint.setText(
            if (state.hint == DrivingHint.AUTO_RESUME) R.string.driving_hint_auto_resume
            else R.string.driving_hint_steering
        )
    }

    private fun bindWaveform(amplitude: Float, intensity: Float, periodMs: Long, depth: Float) {
        binding.waveform.amplitude = amplitude
        binding.waveform.intensity = intensity
        binding.waveform.setMotion(periodMs, depth)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun driveViewModelFactory(sectionId: String?, commuteTarget: CommuteTarget?, scenario: DrivingScenario?) =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                // TODO: 저장된 진도 연동 시 가짜 입력 대신 실제 진도를 쓴다 (내비 경로는 NaviSignalHub 값이 있으면 그것을 쓴다)
                // 홈과 같은 경로로 학습 계획(개념 수)을 세워서, 홈에서 본 분량과 주행 화면의 "개념 n/N"이 맞게 한다
                val navi = NaviSignalHub.signals.value ?: NaviSignals()
                val input = FakeHomeDataSource.fromIntent(requireActivity().intent).load()
                    .withRoute(navi, CommuteHub.info.value ?: CommuteInfo(), commuteTarget)
                return DrivingViewModel(
                    DrivingDemo(input, sectionId),
                    scenario,
                    VehicleSignalHub.signals.value ?: VehicleSignals(),
                    navi
                ) as T
            }
        }

    companion object {
        const val EXTRA_SCENARIO = "driving_scenario"
        private const val ARG_SECTION_ID = "section_id"
        private const val ARG_COMMUTE_TARGET = "commute_target"

        /** [commuteTarget]: 홈 경로 카드에서 고른 회사/집. 경로 안내 전이면 그쪽 미리보기로 학습 계획을 세운다 */
        fun newInstance(sectionId: String?, commuteTarget: CommuteTarget? = null) = DrivingFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_SECTION_ID, sectionId)
                putString(ARG_COMMUTE_TARGET, commuteTarget?.name)
            }
        }
    }
}
