package com.roadstudy.mobile.mydata.data

data class MydataItem(
    val title: String,          // 타이틀 (예: "DB 정규화 정리")
    val subInfo: String,        // 서브 정보 (예: "DB_정규화_정리.pc · 3월 9일")
    val progressText: String,   // 진행도 텍스트 (예: "5/8 개념 · 24분")
    val isTodaySession: Boolean // '오늘 세션' 뱃지 표시 여부 (true/false)
)