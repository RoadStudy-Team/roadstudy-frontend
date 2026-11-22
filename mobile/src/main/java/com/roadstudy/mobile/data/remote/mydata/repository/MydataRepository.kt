package com.roadstudy.mobile.mydata

import com.roadstudy.mobile.ui.mydata.data.MydataItem

// API 연동 전까지 앱 실행 중에만 목록을 보관하는 저장소
object MydataRepository {

    private val items = mutableListOf(
        MydataItem("DB 정규화 정리", "DB_정규화_정리.pdf · 3월 9일", "5/8 개념 · 24분"),
        MydataItem("자바 객체지향 핵심 요약", "OOP_Summary.pdf · 3월 7일", "8/8 개념 · 40분"),
        MydataItem("토익 파트 5 핵심 단어", "Toeic_Voca.pdf · 3월 5일", "12/20 개념 · 15분")
    )

    fun getAll(): List<MydataItem> = items.toList()

    // 새로 저장한 자료는 맨 위에 추가
    fun add(item: MydataItem) {
        items.add(0, item)
    }

    fun remove(id: Long) {
        items.removeAll { it.id == id }
    }

    fun rename(id: Long, newTitle: String) {
        val index = items.indexOfFirst { it.id == id }
        if (index >= 0) items[index] = items[index].copy(title = newTitle)
    }

    // 오늘 세션은 하나만: 선택한 카드만 true, 나머지는 false
    fun setToday(id: Long) {
        for (i in items.indices) {
            items[i] = items[i].copy(isToday = items[i].id == id)
        }
    }
}