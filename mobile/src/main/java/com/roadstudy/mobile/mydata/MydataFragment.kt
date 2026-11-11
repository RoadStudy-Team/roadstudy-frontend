package com.roadstudy.mobile.mydata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.roadstudy.mobile.databinding.FragmentMydataBinding

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

        // 여기에 버튼 클릭 리스너나 데이터 세팅 등 UI 로직을 작성
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}