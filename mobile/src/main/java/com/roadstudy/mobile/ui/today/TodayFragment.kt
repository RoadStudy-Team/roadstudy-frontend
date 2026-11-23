package com.roadstudy.mobile.ui.today

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.roadstudy.mobile.R
import com.roadstudy.mobile.databinding.FragmentTodayBinding

class TodayFragment : Fragment() {

    private var _binding: FragmentTodayBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnUpload.setOnClickListener {
            // TODO: 실제 파일 업로드 연동 전까지는 바로 메인 화면으로 이동
            moveToTodayMain()
        }

        binding.tvSampleGuide.setOnClickListener {
            // TODO: 샘플 자료 체험 (지금은 같은 메인 화면으로 이동)
            moveToTodayMain()
        }
    }

    private fun moveToTodayMain() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_frm, TodayMainFragment())
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}