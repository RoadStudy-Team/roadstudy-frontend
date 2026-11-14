package com.roadstudy.mobile.ui.mydata

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.roadstudy.mobile.R
import com.roadstudy.mobile.databinding.FragmentMydataStorageBinding
import com.roadstudy.mobile.mydata.MydataRepository
import com.roadstudy.mobile.ui.main.MainActivity
import com.roadstudy.mobile.ui.mydata.adapter.MydataAdapter
import com.roadstudy.mobile.ui.mydata.data.MydataItem

class MydataStorageFragment : Fragment() {

    private var _binding: FragmentMydataStorageBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: MydataAdapter

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

        // 3. '새 자료 올리기' 카드 클릭 -> 업로드 1단계로 이동
        binding.cardAddNewData.setOnClickListener {
            // 업로드 플로우 동안 바텀 네비게이션 숨김 (Step1의 X 버튼에서 다시 보이게 함)
            (activity as? MainActivity)?.setBottomNavigationVisibility(false)

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_frm, UploadStep1Fragment())
                .addToBackStack(null)
                .commit()
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
                    c.setTypeface(null, Typeface.NORMAL)
                }

                // 클릭된 칩만 선택 상태로 변경
                val selected = clickedChip as TextView
                selected.setBackgroundResource(R.drawable.bg_chip_selected)
                selected.setTextColor(Color.parseColor("#07121F"))
                selected.setTypeface(null, Typeface.BOLD)

                // TODO: 칩 카테고리별 필터링 로직 구현
            }
        }
    }

    private fun setupRecyclerView() {
        // ⋮ 버튼(btnCardMore)을 누르면 (버튼 View, 해당 카드 데이터)가 전달돼요
        adapter = MydataAdapter { anchor, item ->
            showCardMenu(anchor, item)
        }

        val recyclerView = binding.recyclerViewMyData
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        recyclerView.isNestedScrollingEnabled = false // NestedScrollView 안에서 스크롤 꼬임 방지

        refreshList()
    }

    // 저장소의 최신 목록을 화면에 반영
    private fun refreshList() {
        adapter.submitList(MydataRepository.getAll())
    }

    private fun showCardMenu(anchor: View, item: MydataItem) {
        CardMenuPopup(
            context = requireContext(),
            onSetToday = {
                // 오늘 세션으로 설정 -> 해당 카드에만 배지 표시
                MydataRepository.setToday(item.id)
                refreshList()
            },
            onRename = {
                showRenameDialog(item)
            },
            onAddMaterial = {
                // TODO: 자료 추가로 올리기 화면으로 이동
            },
            onDelete = {
                // 카드 삭제
                MydataRepository.remove(item.id)
                refreshList()
            }
        ).show(anchor)
    }

    // 이름 변경 다이얼로그
    // 이름 변경 다이얼로그
    private fun showRenameDialog(item: MydataItem) {
        val editText = EditText(requireContext()).apply {
            setText(item.title)
            setSelection(text.length)
            maxLines = 1
            inputType = android.text.InputType.TYPE_CLASS_TEXT

            // 어두운 배경에 맞는 글자색 / 밑줄색
            setTextColor(Color.parseColor("#F3F4F6"))
            setHintTextColor(Color.parseColor("#8A9099"))
            backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#5B9CF6"))
        }

        val padding = (20 * resources.displayMetrics.density).toInt()
        val container = FrameLayout(requireContext()).apply {
            setPadding(padding, padding / 2, padding, 0)
            addView(editText)
        }

        MaterialAlertDialogBuilder(requireContext(), R.style.RoadstudyDialog)
            .setTitle("이름 변경")
            .setView(container)
            .setPositiveButton("확인") { _, _ ->
                val newTitle = editText.text.toString().trim()
                // 빈 값이면 기존 이름 유지
                if (newTitle.isNotEmpty()) {
                    MydataRepository.rename(item.id, newTitle)
                    refreshList()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}