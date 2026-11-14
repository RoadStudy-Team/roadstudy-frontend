package com.roadstudy.mobile.ui.mydata.data

data class MydataItem(
    val title: String,
    val subInfo: String,
    val progressText: String,
    val isToday: Boolean = false,
    val id: Long = System.nanoTime()   // 카드 구분용 고유 id
)