package com.roadstudy.mobile.ui.today

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.roadstudy.mobile.R
import com.roadstudy.mobile.data.remote.today.repository.TodayRepository
import com.roadstudy.mobile.databinding.FragmentTodayMainBinding
import com.roadstudy.mobile.ui.today.data.TodaySummary
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class TodayMainFragment : Fragment() {

    private var _binding: FragmentTodayMainBinding? = null
    private val binding get() = _binding!!

    private val repository by lazy { TodayRepository(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodayMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDate()
        setupGreeting()
        setupDaySelection()
        bindSummary(repository.getTodaySummary())
    }

    // ---------- 날짜 ----------
    private fun setupDate() {
        val today = LocalDate.now()
        val dayName = DAY_NAMES_FULL[today.dayOfWeek] ?: ""
        binding.tvDate.text = "${today.monthValue}월 ${today.dayOfMonth}일 $dayName"
    }

    // ---------- 인사말 ----------
    private fun setupGreeting() {
        val greeting = if (LocalTime.now().hour < 12) "좋은 아침이에요" else "좋은 오후예요"
        binding.tvGreeting.text = "$greeting, ${loadUserName()}님"
    }

    // TODO: 회원가입 연동 후 저장된 이름으로 교체
    private fun loadUserName(): String {
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return prefs.getString("user_name", "사용자") ?: "사용자"
    }

    // ---------- 요일: 오늘 요일만 배경 표시 ----------
    private fun setupDaySelection() {
        val days = mapOf(
            DayOfWeek.MONDAY to Pair(binding.dayMon, binding.tvMon),
            DayOfWeek.TUESDAY to Pair(binding.dayTue, binding.tvTue),
            DayOfWeek.WEDNESDAY to Pair(binding.dayWed, binding.tvWed),
            DayOfWeek.THURSDAY to Pair(binding.dayThu, binding.tvThu),
            DayOfWeek.FRIDAY to Pair(binding.dayFri, binding.tvFri)
        )
        val today = LocalDate.now().dayOfWeek

        days.forEach { (day, views) ->
            val (container, label) = views
            if (day == today) {
                container.setBackgroundResource(R.drawable.bg_day_selected)
                label.setTextColor(0xFFF3F4F6.toInt())
                label.paint.isFakeBoldText = true
            } else {
                container.background = null
                label.setTextColor(0xFF8A9099.toInt())
                label.paint.isFakeBoldText = false
            }
        }
    }

    // ---------- 요약 데이터 바인딩 ----------
    private fun bindSummary(summary: TodaySummary) {
        val b = binding

        // 출근길 / 퇴근길 전환
        if (summary.isGoToWork) {
            b.tvCommuteType.text = "출근길"
            b.tvToLabel.text = "회사까지"
            b.tvStartLabel.text = "집"
            b.tvEndLabel.text = "회사"
        } else {
            b.tvCommuteType.text = "퇴근길"
            b.tvToLabel.text = "집까지"
            b.tvStartLabel.text = "회사"
            b.tvEndLabel.text = "집"
        }

        // 거리 / 시간 / 정체
        b.tvDistance.text = "%.1f".format(summary.distanceKm)
        b.tvDuration.text = summary.durationMin.toString()
        b.tvTraffic.text = "정체 %.1f".format(summary.trafficKm)

        // 자료 / 학습 개수
        b.tvMaterialTitle.text = summary.materialTitle
        b.tvStudyCount.text = summary.studyCount.toString()

        // 오늘 카드
        b.tvTodayGo.text = "출근길 · 새 개념 ${summary.studyCount}개 예정"
        b.tvTodayBack.text = "퇴근길 · 결과에 따라 준비돼요"

        // 차량 연결 상태
        if (summary.isCarConnected) {
            b.tvConnection.text = "차량과 연결됨"
            b.tvConnection.setTextColor(0xFF8FBAA8.toInt())
            b.dotConnection.backgroundTintList = null
        } else {
            b.tvConnection.text = "차량 연결 안 됨"
            b.tvConnection.setTextColor(0xFF8A9099.toInt())
            b.dotConnection.backgroundTintList = ColorStateList.valueOf(0xFF8A9099.toInt())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private val DAY_NAMES_FULL = mapOf(
            DayOfWeek.MONDAY to "월요일",
            DayOfWeek.TUESDAY to "화요일",
            DayOfWeek.WEDNESDAY to "수요일",
            DayOfWeek.THURSDAY to "목요일",
            DayOfWeek.FRIDAY to "금요일",
            DayOfWeek.SATURDAY to "토요일",
            DayOfWeek.SUNDAY to "일요일"
        )
    }
}