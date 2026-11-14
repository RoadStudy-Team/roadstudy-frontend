package com.roadstudy.mobile.ui.growth

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.roadstudy.mobile.R
import com.roadstudy.mobile.databinding.FragmentGrowthBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class GrowthFragment : Fragment() {

    private var _binding: FragmentGrowthBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGrowthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 기본값: '전체' 탭 선택 상태
        updateTabSelection(TabType.ALL)

        // 탭 클릭 이벤트 연결 (ViewBinding을 통해 안전하게 접근)
        binding.chipAll.setOnClickListener { updateTabSelection(TabType.ALL) }
        binding.chipWeek.setOnClickListener { updateTabSelection(TabType.WEEK) }
        binding.chipMonth.setOnClickListener { updateTabSelection(TabType.MONTH) }
    }

    private enum class TabType { ALL, WEEK, MONTH }

    // 오늘 기준 N일 전 날짜를 "11월 14일" 형식으로 반환
    private fun dateDaysAgo(daysAgo: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return SimpleDateFormat("M월 d일", Locale.KOREA).format(calendar.time)
    }

    private fun updateTabSelection(tabType: TabType) {
        // 모든 탭의 스타일을 초기화 (선택 안 된 상태)
        resetTabStyles()

        when (tabType) {
            TabType.ALL -> {
                // 전체 탭 선택된 스타일 적용
                binding.chipAll.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_segment_selected)
                binding.chipAll.setTextColor(Color.parseColor("#0F1116"))

                // 전체 데이터로 갱신: 날짜는 항상 오늘 기준
                val today = dateDaysAgo(0)   // 오늘 날짜 (예: 11월 14일)

                binding.tvPeriod.text = "$today — 오늘"
                binding.tvBigNumber.text = "36"
                binding.tvBigDesc.text = "${today}부터 꾸준히 함께했어요"
            }

            TabType.WEEK -> {
                // 주간 탭 선택된 스타일 적용
                binding.chipWeek.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_segment_selected)
                binding.chipWeek.setTextColor(Color.parseColor("#0F1116"))

                // 주간 데이터로 갱신 (예시)
                binding.tvPeriod.text = "최근 7일간"
                binding.tvBigNumber.text = "5"
                binding.tvBigDesc.text = "이번 주도 목표 페이스를 잘 달성했어요"
            }
            TabType.MONTH -> {
                // 월간 탭 선택된 스타일 적용
                binding.chipMonth.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_segment_selected)
                binding.chipMonth.setTextColor(Color.parseColor("#0F1116"))

                // 월간 데이터로 갱신 (예시)
                binding.tvPeriod.text = "최근 30일간"
                binding.tvBigNumber.text = "22"
                binding.tvBigDesc.text = "한 달 동안 꾸준한 성장을 이뤘어요"
            }
        }
    }

    private fun resetTabStyles() {
        val unselectedColor = Color.parseColor("#8E9199")

        binding.chipAll.background = null
        binding.chipAll.setTextColor(unselectedColor)

        binding.chipWeek.background = null
        binding.chipWeek.setTextColor(unselectedColor)

        binding.chipMonth.background = null
        binding.chipMonth.setTextColor(unselectedColor)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}