package com.roadstudy.mobile.data.remote.onboarding.repository

import android.content.Context
import com.roadstudy.mobile.ui.onboarding.data.SavedPlace

class OnBoardingRepository(context: Context) {

    private val prefs = context.getSharedPreferences("routine_prefs", Context.MODE_PRIVATE)

    // ---------- 저장 / 불러오기 ----------
    fun savePlace(key: String, place: SavedPlace) {
        prefs.edit()
            .putString("${key}_name", place.name)
            .putFloat("${key}_lat", place.latitude.toFloat())
            .putFloat("${key}_lng", place.longitude.toFloat())
            .apply()
    }

    fun loadPlace(key: String): SavedPlace? {
        val name = prefs.getString("${key}_name", null) ?: return null
        return SavedPlace(
            name = name,
            latitude = prefs.getFloat("${key}_lat", 0f).toDouble(),
            longitude = prefs.getFloat("${key}_lng", 0f).toDouble()
        )
    }

    // ---------- 주소 검색 (지금은 임시 데이터) ----------
    // TODO: API 연동 시 이 함수 내용만 카카오 로컬 API 호출로 교체
    fun searchPlaces(query: String): List<SavedPlace> {
        if (query.isBlank()) return emptyList()
        return DUMMY_PLACES.filter { it.name.contains(query.trim()) }
    }

    companion object {
        const val KEY_HOME = "home"
        const val KEY_WORK = "work"

        private val DUMMY_PLACES = listOf(
            SavedPlace("서울 마포구 월드컵북로 396", 37.5796, 126.8998),
            SavedPlace("서울 마포구 월드컵로 240", 37.5683, 126.8972),
            SavedPlace("서울 강남구 테헤란로 152", 37.5000, 127.0364),
            SavedPlace("서울 송파구 올림픽로 300", 37.5133, 127.1028),
            SavedPlace("경기 성남시 분당구 판교역로 235", 37.4020, 127.1086),
            SavedPlace("경기 성남시 분당구 판교역로 166", 37.3948, 127.1112),
            SavedPlace("경기 성남시 분당구 대왕판교로 660", 37.4038, 127.1056)
        )
    }
}