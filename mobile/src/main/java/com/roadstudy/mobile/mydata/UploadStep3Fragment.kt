package com.roadstudy.mobile.mydata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.roadstudy.mobile.R
import com.roadstudy.mobile.databinding.FragmentUploadStep3Binding
import com.roadstudy.mobile.main.MainActivity
import com.roadstudy.mobile.mydata.adapter.ChapterAdapter
import com.roadstudy.mobile.mydata.data.ChapterItem

class UploadStep3Fragment : Fragment() {

    private var _binding: FragmentUploadStep3Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadStep3Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 상단 닫기(X) 버튼 누를 때
        binding.btnClose.setOnClickListener {
            // 1. MainActivity의 바텀 네비게이션을 다시 보이게 켜기
            (requireActivity() as? MainActivity)?.setBottomNavigationVisibility(true)

            // 2. 백스택에 기대지 않고 '내 자료' 화면(MydataStorageFragment)으로 안전하게 교체
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_frm, MydataStorageFragment()) // MainActivity의 프레임레이아웃 ID인 main_frm 사용
                .commit()
        }

        // 가상 데이터 및 리사이클러뷰 세팅 실행
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val dummyList = listOf(
            ChapterItem(
                "01",
                "정규화란",
                "데이터 중복을 줄이고 이상 현상을 막기 위해 릴레이션을 나누는 과정이에요..",
                "▶ 2분 40초 · 확인 질문 1개",
                expandedDescription = "정규화는 데이터의 중복을 최소화하고 무손실 분해를 통해 이상 현상을 방지하는 데이터베이스 설계 기법입니다.",
                question = "정규화의 주된 목적은?",
                answer = "데이터 중복 최소화 및 이상 현상 방지"
            ),
            ChapterItem(
                "02",
                "제1정규형",
                "모든 속성의 값이 더 이상 나눌 수 없는 원자값이 되도록 정리해요..",
                "▶ 2분 20초 · 확인 질문 1개",
                expandedDescription = "제1정규형은 릴레이션에 속한 모든 속성의 도메인이 원자값으로만 구성되어야 하는 조건을 만족해야 합니다.",
                question = "제1정규형의 핵심 조건은?",
                answer = "모든 속성 값이 원자값이어야 함"
            ),
            ChapterItem(
                "03",
                "제2정규형",
                "부분 함수 종속을 제거해 기본키 전체에 의존하도록 릴레이션을 나눠요..",
                "▶ 3분 10초 · 확인 질문 1개",
                expandedDescription = "제2정규형은 제1정규형을 만족하고, 기본키가 아닌 모든 속성이 기본키에 완전 함수 종속되는 상태입니다.",
                question = "제2정규형에서 제거하는 종속은?",
                answer = "부분 함수 종속"
            )
        )

        val recyclerView = binding.recyclerViewChapters
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = ChapterAdapter(dummyList)
        recyclerView.isNestedScrollingEnabled = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}