package com.roadstudy.automotive.ui.quiz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.roadstudy.automotive.R
import com.roadstudy.automotive.databinding.FragmentQuizResultBinding

/**
 * 확인 퀴즈 결과 화면. `N문제 중 M문제 맞혔어요`와 이번 주 정답률, 연속 학습일을 보여준다.
 * 닫기는 홈으로 돌아간다.
 */
class QuizResultFragment : Fragment() {

    private var _binding: FragmentQuizResultBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val result = QuizResult(
            correct = requireArguments().getInt(ARG_CORRECT),
            total = requireArguments().getInt(ARG_TOTAL)
        )
        render(QuizUiMapper().mapResult(result))

        // 닫기: 홈으로 돌아간다 (홈 위에 쌓인 화면을 모두 걷어낸다)
        binding.btnClose.setOnClickListener {
            parentFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
        binding.btnReport.setOnClickListener {
            // TODO: 휴대폰 리포트 연결(딥링크 등)이 정해지면 연다
            Toast.makeText(requireContext(), R.string.toast_report_todo, Toast.LENGTH_SHORT).show()
        }
    }

    private fun render(state: QuizResultUiState) {
        binding.tvHeadline.text = getString(R.string.quiz_result_headline, state.total, state.correct)
        binding.tvWeekly.text = getString(R.string.quiz_result_percent, state.weeklyAccuracyPercent)
        binding.tvStreak.text = getString(R.string.parked_stat_days, state.streakDays)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_CORRECT = "correct"
        private const val ARG_TOTAL = "total"

        fun newInstance(correct: Int, total: Int) = QuizResultFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_CORRECT, correct)
                putInt(ARG_TOTAL, total)
            }
        }
    }
}
