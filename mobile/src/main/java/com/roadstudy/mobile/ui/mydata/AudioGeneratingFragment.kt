package com.roadstudy.mobile.ui.mydata

import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.roadstudy.mobile.R
import com.roadstudy.mobile.databinding.FragmentAudioGeneratingBinding

class AudioGeneratingFragment : Fragment() {

    private var _binding: FragmentAudioGeneratingBinding? = null
    private val binding get() = _binding!!

    private enum class StepState { PENDING, ACTIVE, DONE }

    private lateinit var stepIcons: List<ImageView>
    private lateinit var stepTexts: List<TextView>

    private var progressAnimator: ObjectAnimator? = null

    // 현재 단계 상태 (중복 갱신 방지용)
    private var lastCompletedCount = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAudioGeneratingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        stepIcons = listOf(binding.icStep1, binding.icStep2, binding.icStep3, binding.icStep4)
        stepTexts = listOf(binding.tvStep1, binding.tvStep2, binding.tvStep3, binding.tvStep4)

        binding.tvFileName.text = arguments?.getString("fileName") ?: ""

        binding.btnClose.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.btnConfirm.setOnClickListener {
            val fileName = arguments?.getString("fileName")

            val nextFragment = UploadStep3Fragment().apply {
                arguments = Bundle().apply {
                    putString("fileName", fileName)
                }
            }

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_frm, nextFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.progressBar.progress = 0
        updateStepsByProgress(0)
        startSmoothProgress()
    }

    // 막대바가 0 -> 100까지 끊김 없이 천천히 차오름 (총 6초)
    private fun startSmoothProgress() {
        progressAnimator = ObjectAnimator.ofInt(binding.progressBar, "progress", 0, 100).apply {
            duration = 6000L
            startDelay = 200L
            interpolator = LinearInterpolator()
            addUpdateListener { animator ->
                if (_binding == null) return@addUpdateListener
                updateStepsByProgress(animator.animatedValue as Int)
            }
            start()
        }
    }

    // 진행률에 맞춰 단계 상태 갱신: 25%마다 한 단계씩 완료
    private fun updateStepsByProgress(progress: Int) {
        val total = stepIcons.size
        val completed = (progress * total / 100).coerceAtMost(total)

        // 완료된 개수가 바뀔 때만 갱신
        if (completed == lastCompletedCount) return
        lastCompletedCount = completed

        stepIcons.indices.forEach { index ->
            val state = when {
                index < completed -> StepState.DONE
                index == completed -> StepState.ACTIVE
                else -> StepState.PENDING
            }
            setStepState(index, state)
        }
    }

    private fun setStepState(index: Int, state: StepState) {
        val icon = stepIcons[index]
        val text = stepTexts[index]

        when (state) {
            StepState.PENDING -> {
                icon.setBackgroundResource(R.drawable.bg_step_pending)
                icon.setImageDrawable(null)
                text.setTextColor(Color.parseColor("#747B85"))
            }
            StepState.ACTIVE -> {
                icon.setBackgroundResource(R.drawable.bg_check_active)
                icon.setImageDrawable(null)
                text.setTextColor(Color.parseColor("#D7DAE0"))
            }
            StepState.DONE -> {
                icon.setBackgroundResource(R.drawable.bg_step_done)
                icon.setImageResource(R.drawable.ic_check)
                text.setTextColor(Color.parseColor("#D7DAE0"))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        progressAnimator?.cancel()
        progressAnimator = null
        _binding = null
    }
}