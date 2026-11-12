package com.roadstudy.mobile.mydata

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.roadstudy.mobile.R
import com.roadstudy.mobile.databinding.FragmentMydataStorageBinding
import com.roadstudy.mobile.main.MainActivity
import com.roadstudy.mobile.mydata.adapter.MydataAdapter // 본인 프로젝트의 어댑터 경로에 맞춰 확인
import com.roadstudy.mobile.mydata.data.MydataItem // 본인 프로젝트의 데이터 클래스 경로에 맞춰 확인

class MydataStorageFragment : Fragment() {

    private var _binding: FragmentMydataStorageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMydataStorageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 바텀바가 보이도록 설정
        (activity as? MainActivity)?.setBottomNavigationVisibility(true)

        // 2. 칩 필터 및 리사이클러뷰 세팅
        setupChipFilters()
        setupRecyclerView()

        // 3. '새 자료 올리기' 카드 클릭 리스너
        binding.cardAddNewData.setOnClickListener {
            // TODO: 새 자료 올리기 화면으로 이동하는 코드
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.setBottomNavigationVisibility(true)
    }

    private fun setupChipFilters() {
        val chips = listOf(
            binding.chipAll,
            binding.chipCert,
            binding.chipEnglish,
            binding.chipWork
        )

        for (chip in chips) {
            chip.setOnClickListener { clickedChip ->
                // 모든 칩 비선택 상태로 초기화
                for (c in chips) {
                    c.setBackgroundResource(R.drawable.bg_chip_unselected)
                    c.setTextColor(Color.parseColor("#7E858E"))
                    c.setTypeface(null, android.graphics.Typeface.NORMAL)
                }

                // 클릭된 칩만 선택 상태로 변경
                val selected = clickedChip as TextView
                selected.setBackgroundResource(R.drawable.bg_chip_selected)
                selected.setTextColor(Color.parseColor("#07121F"))
                selected.setTypeface(null, android.graphics.Typeface.BOLD)

                // TODO: 칩 카테고리별 필터링 로직 구현
            }
        }
    }

    private fun setupRecyclerView() {
        // 테스트용 가상 데이터 목록 (필요에 맞게 데이터 클래스 구조를 연동해 사용하세요)
        val dummyList = listOf(
            MydataItem("DB 정규화 정리", "DB_정규화_정리.pc · 3월 9일", "5/8 개념 · 24분", true),
            MydataItem("자바 객체지향 핵심 요약", "OOP_Summary.pdf · 3월 7일", "8/8 개념 · 40분", false),
            MydataItem("토익 파트 5 핵심 단어", "Toeic_Voca.pdf · 3월 5일", "12/20 개념 · 15분", false)
        )

        val recyclerView = binding.recyclerViewMyData
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = MydataAdapter(dummyList) // 어댑터 클래스 생성 후 주석 해제하여 사용
        recyclerView.isNestedScrollingEnabled = false // NestedScrollView 안에서 스크롤 꼬임 방지
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}