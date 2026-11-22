package com.roadstudy.mobile.ui.onboarding

import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.TimePicker
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.roadstudy.mobile.R
import com.roadstudy.mobile.data.remote.onboarding.repository.OnBoardingRepository
import com.roadstudy.mobile.databinding.FragmentOnBoardingRoutineBinding
import com.roadstudy.mobile.ui.main.MainActivity
import com.roadstudy.mobile.ui.onboarding.adapter.PlaceAdapter
import com.roadstudy.mobile.ui.onboarding.data.SavedPlace

class OnBoardingRoutineFragment : Fragment() {

    private var _binding: FragmentOnBoardingRoutineBinding? = null
    private val binding get() = _binding!!

    private val prefs: SharedPreferences by lazy {
        requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val repository by lazy { OnBoardingRepository(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnBoardingRoutineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadSavedData()
        setupAddressEdit()
        setupTimePickers()
        setupSwitch()
        setupButtons()
    }

    // ---------- 저장된 값 불러오기 ----------
    private fun loadSavedData() {
        repository.loadPlace(OnBoardingRepository.KEY_HOME)?.let { binding.tvHomeAddress.text = it.name }
        repository.loadPlace(OnBoardingRepository.KEY_WORK)?.let { binding.tvWorkAddress.text = it.name }

        showTime(KEY_GO_HOUR, KEY_GO_MINUTE, binding.tvGoHour, binding.tvGoMinute)
        showTime(KEY_BACK_HOUR, KEY_BACK_MINUTE, binding.tvBackHour, binding.tvBackMinute)

        binding.switchOtherTrip.isChecked = prefs.getBoolean(KEY_OTHER_TRIP, true)
    }

    private fun showTime(hourKey: String, minuteKey: String, hourView: TextView, minuteView: TextView) {
        val hour = prefs.getInt(hourKey, -1)
        val minute = prefs.getInt(minuteKey, -1)
        if (hour >= 0 && minute >= 0) {
            hourView.text = "%02d".format(hour)
            minuteView.text = "%02d".format(minute)
        }
    }

    // ---------- 집 / 회사 주소 수정 ----------
    private fun setupAddressEdit() {
        binding.btnEditHome.setOnClickListener {
            showAddressSearchSheet("집 주소 검색") { place ->
                repository.savePlace(OnBoardingRepository.KEY_HOME, place)
                binding.tvHomeAddress.text = place.name
            }
        }

        binding.btnEditWork.setOnClickListener {
            showAddressSearchSheet("회사 주소 검색") { place ->
                repository.savePlace(OnBoardingRepository.KEY_WORK, place)
                binding.tvWorkAddress.text = place.name
            }
        }
    }

    private fun showAddressSearchSheet(title: String, onSelected: (SavedPlace) -> Unit) {
        val dialog = BottomSheetDialog(requireContext(), R.style.DarkBottomSheetDialog)
        val sheet = layoutInflater.inflate(R.layout.bottom_sheet_address_search, null)

        sheet.findViewById<TextView>(R.id.tvSearchTitle).text = title

        val etSearch = sheet.findViewById<EditText>(R.id.etSearch)
        val rvResults = sheet.findViewById<RecyclerView>(R.id.rvResults)
        val tvEmpty = sheet.findViewById<TextView>(R.id.tvEmpty)

        val adapter = PlaceAdapter { place ->
            onSelected(place)
            dialog.dismiss()
        }
        rvResults.layoutManager = LinearLayoutManager(requireContext())
        rvResults.adapter = adapter

        fun search(query: String) {
            val results = repository.searchPlaces(query)
            adapter.submit(results)
            val isEmpty = results.isEmpty()
            rvResults.visibility = if (isEmpty) View.GONE else View.VISIBLE
            tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
            tvEmpty.text = if (query.isBlank()) "주소를 검색해 주세요" else "검색 결과가 없어요"
        }

        // 입력할 때마다 검색
        etSearch.addTextChangedListener { search(it?.toString().orEmpty()) }

        rvResults.visibility = View.GONE
        dialog.showWithSameHeight(sheet)
    }

    // ---------- 출근 / 퇴근 시간 (스피너 BottomSheet) ----------
    private fun setupTimePickers() {
        val goClick = View.OnClickListener {
            showTimePicker(
                title = "출근 시간",
                hourKey = KEY_GO_HOUR,
                minuteKey = KEY_GO_MINUTE,
                defaultHour = 8,
                hourView = binding.tvGoHour,
                minuteView = binding.tvGoMinute
            )
        }
        binding.tvGoHour.setOnClickListener(goClick)
        binding.tvGoMinute.setOnClickListener(goClick)

        val backClick = View.OnClickListener {
            showTimePicker(
                title = "퇴근 시간",
                hourKey = KEY_BACK_HOUR,
                minuteKey = KEY_BACK_MINUTE,
                defaultHour = 18,
                hourView = binding.tvBackHour,
                minuteView = binding.tvBackMinute
            )
        }
        binding.tvBackHour.setOnClickListener(backClick)
        binding.tvBackMinute.setOnClickListener(backClick)
    }

    private fun showTimePicker(
        title: String,
        hourKey: String,
        minuteKey: String,
        defaultHour: Int,
        hourView: TextView,
        minuteView: TextView
    ) {
        val savedHour = prefs.getInt(hourKey, -1).takeIf { it >= 0 } ?: defaultHour
        val savedMinute = prefs.getInt(minuteKey, -1).takeIf { it >= 0 } ?: 0

        val dialog = BottomSheetDialog(requireContext(), R.style.DarkBottomSheetDialog)
        val sheet = layoutInflater.inflate(R.layout.bottom_sheet_time_picker, null)

        sheet.findViewById<TextView>(R.id.tvSheetTitle).text = title

        val picker = sheet.findViewById<TimePicker>(R.id.timePicker).apply {
            setIs24HourView(true)
            hour = savedHour
            minute = savedMinute
        }

        sheet.findViewById<TextView>(R.id.btnTimeConfirm).setOnClickListener {
            val hour = picker.hour
            val minute = picker.minute
            prefs.edit().putInt(hourKey, hour).putInt(minuteKey, minute).apply()
            hourView.text = "%02d".format(hour)
            minuteView.text = "%02d".format(minute)
            dialog.dismiss()
        }

        dialog.showWithSameHeight(sheet)
    }

    // ---------- 바텀시트 공통: 화면 높이 비율로 통일 ----------
    private fun BottomSheetDialog.showWithSameHeight(sheet: View) {
        setContentView(sheet)
        (sheet.parent as? View)?.setBackgroundColor(Color.TRANSPARENT)

        // 화면 높이의 55%로 통일 (비율만 바꾸면 모든 시트가 같이 바뀜)
        sheet.layoutParams = sheet.layoutParams.apply {
            height = (resources.displayMetrics.heightPixels * 0.55f).toInt()
        }

        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
        show()
    }

    // ---------- 다른 곳 이동 학습 제안 ON/OFF ----------
    private fun setupSwitch() {
        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked)
        )

        // 동그라미: ON 흰색 / OFF 회색
        binding.switchOtherTrip.thumbTintList = ColorStateList(
            states,
            intArrayOf(Color.parseColor("#FFFFFF"), Color.parseColor("#8B91A0"))
        )

        // 트랙: ON 파란색 / OFF 어두운 회색
        binding.switchOtherTrip.trackTintList = ColorStateList(
            states,
            intArrayOf(Color.parseColor("#5B9CF6"), Color.parseColor("#2A2E38"))
        )

        binding.switchOtherTrip.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_OTHER_TRIP, isChecked).apply()
        }
    }

    // ---------- 버튼 ----------
    private fun setupButtons() {
        binding.btnLoadFavorite.setOnClickListener {
            // TODO: 차량 내비게이션 즐겨찾기 불러오기 (Pleos SDK 연동)
        }

        binding.btnComplete.setOnClickListener {
            (requireActivity() as MainActivity).finishOnboarding()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val PREFS_NAME = "routine_prefs"
        private const val KEY_GO_HOUR = "go_hour"
        private const val KEY_GO_MINUTE = "go_minute"
        private const val KEY_BACK_HOUR = "back_hour"
        private const val KEY_BACK_MINUTE = "back_minute"
        private const val KEY_OTHER_TRIP = "other_trip_enabled"
    }
}