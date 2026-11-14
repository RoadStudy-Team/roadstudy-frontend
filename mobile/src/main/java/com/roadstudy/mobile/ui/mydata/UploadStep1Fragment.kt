package com.roadstudy.mobile.ui.mydata

import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.roadstudy.mobile.R
import com.roadstudy.mobile.databinding.FragmentUploadStep1Binding
import com.roadstudy.mobile.ui.main.MainActivity

class UploadStep1Fragment : Fragment() {

    private var _binding: FragmentUploadStep1Binding? = null
    private val binding get() = _binding!!

    private var selectedCategory: String = "자격증"
    private var selectedFileName: String? = null

    // 선택한 파일의 Uri (나중에 업로드할 때 사용)
    private var selectedFileUri: Uri? = null

    // 파일 선택기 (PDF, 이미지, 텍스트 문서 허용)
    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let {
                selectedFileUri = it
                showFileInfo(it)
            }
        }

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

        binding.tvFileName.text = "PDF 파일을 선택해주세요"
        binding.tvFileInfo.text = "최대 50MB"

        binding.btnClose.setOnClickListener {
            (activity as? MainActivity)?.setBottomNavigationVisibility(true)
            requireActivity().supportFragmentManager.popBackStack()
        }

        // 파일 선택: 드롭존 전체 / "다른 파일 선택하기" 둘 다 파일 선택기 열기
        binding.cardFileDropZone.setOnClickListener { openFilePicker() }
        binding.tvChangeFile.setOnClickListener { openFilePicker() }

        val chips = listOf(
            binding.chipCert,
            binding.chipEnglish,
            binding.chipWork,
            binding.chipEtc
        )

        chips.forEach { chip ->
            chip.setOnClickListener {
                selectedCategory = chip.text.toString()
                updateChipSelection(chips, chip)
            }
        }

        updateChipSelection(chips, binding.chipCert)

        binding.btnUploadNext.setOnClickListener {
            val nextFragment = AudioGeneratingFragment().apply {
                arguments = Bundle().apply {
                    putString("fileName", selectedFileName ?: binding.tvFileName.text.toString())
                    putString("category", selectedCategory)
                    putString("fileUri", selectedFileUri?.toString())
                }
            }

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_frm, nextFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun openFilePicker() {
        filePickerLauncher.launch(
            arrayOf(
                "application/pdf",
                "image/*",
                "text/plain"
            )
        )
    }

    // 선택한 파일의 이름/용량을 화면에 표시
    private fun showFileInfo(uri: Uri) {
        requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)

                val name = if (nameIndex >= 0) cursor.getString(nameIndex) else "알 수 없는 파일"
                val size = if (sizeIndex >= 0) cursor.getLong(sizeIndex) else 0L

                selectedFileName = name   // 추가
                binding.tvFileName.text = name
                binding.tvFileInfo.text = formatFileSize(size)
            }
        }
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 -> String.format("%.1fMB", bytes / (1024.0 * 1024.0))
            bytes >= 1024 -> String.format("%.0fKB", bytes / 1024.0)
            else -> "${bytes}B"
        }
    }

    private fun updateChipSelection(chips: List<TextView>, selected: TextView) {
        chips.forEach { chip ->
            if (chip == selected) {
                chip.setBackgroundResource(R.drawable.bg_chip_selected)
                chip.setTextColor(Color.parseColor("#07121F"))
                chip.setTypeface(null, Typeface.BOLD)
            } else {
                chip.setBackgroundResource(R.drawable.bg_chip_unselected)
                chip.setTextColor(Color.parseColor("#818892"))
                chip.setTypeface(null, Typeface.NORMAL)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}