package com.roadstudy.mobile.mydata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.roadstudy.mobile.R
import com.roadstudy.mobile.databinding.FragmentUploadStep1Binding
import com.roadstudy.mobile.main.MainActivity

class UploadStep1Fragment : Fragment() {

    private var _binding: FragmentUploadStep1Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadStep1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 상단 닫기(X) 버튼 누를 때
        binding.btnClose.setOnClickListener {
            // 1. 바텀 네비게이션 다시 보이기
            (activity as? MainActivity)?.setBottomNavigationVisibility(true)

            // 2. 이전 화면으로 복귀
            requireActivity().supportFragmentManager.popBackStack()
        }

        //  "올리기" 버튼 누를 때 다음 단계(생성/로딩 화면 등)로 이동
        binding.btnUploadNext.setOnClickListener {
            // 바텀 네비게이션은 계속 숨긴 채로 다음 단계 프래그먼트로 교체
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_frm, AudioGeneratingFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}