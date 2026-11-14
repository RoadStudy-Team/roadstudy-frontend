package com.roadstudy.mobile.mydata

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.roadstudy.mobile.R
import com.roadstudy.mobile.databinding.FragmentAudioGeneratingBinding
import com.roadstudy.mobile.main.MainActivity

class AudioGeneratingFragment : Fragment() {

    private var _binding: FragmentAudioGeneratingBinding? = null
    private val binding get() = _binding!!

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

        // 1. 프로그레스바 초기값을 0으로 설정 후 로딩 애니메이션 시작
        binding.progressBar.progress = 0
        simulateLoadingProgress()

        // 상단 닫기(X) 버튼 누를 때
        binding.btnClose.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // 확인하기 버튼 누를 때
        binding.btnConfirm.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_frm, UploadStep3Fragment())
                .addToBackStack(null)
                .commit()
        }
    }

    // ⭐ 막대 바가 0부터 100까지 부드럽게 차오르는 로딩 함수 추가
    private fun simulateLoadingProgress() {
        val handler = Handler(Looper.getMainLooper())
        var currentProgress = 0

        val runnable = object : Runnable {
            override fun run() {
                if (currentProgress < 100) {
                    currentProgress += 1
                    binding.progressBar.progress = currentProgress

                    // 0.05초마다 1씩 증가 (총 약 5초 동안 0 -> 100 채워짐)
                    handler.postDelayed(this, 50)
                } else {
                    // 100%가 완료되었을 때 처리할 작업이 있다면 여기에 작성
                }
            }
        }
        // 화면 진입 후 0.2초 뒤에 로딩 시작
        handler.postDelayed(runnable, 200)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}