package com.roadstudy.mobile.mydata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.roadstudy.mobile.R
import com.roadstudy.mobile.databinding.FragmentMydataBinding
import com.roadstudy.mobile.main.MainActivity

class MydataFragment : Fragment() {

    private var _binding: FragmentMydataBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMydataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // "첫 자료 올리기" 버튼 클릭 시
        binding.btnFirstUpload.setOnClickListener {
            // 1. 바텀 네비게이션 숨기기
            (activity as? MainActivity)?.setBottomNavigationVisibility(false)

            // 2. 업로드 화면으로 프래그먼트 전환 (백스택에 추가하여 뒤로가기 가능하게 함)
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_frm, UploadStep1Fragment())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}