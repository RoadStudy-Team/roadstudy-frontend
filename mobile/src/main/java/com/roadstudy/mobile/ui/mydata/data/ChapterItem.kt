package com.roadstudy.mobile.ui.mydata.data

data class ChapterItem(
    val number: String,
    var title: String,
    var description: String,
    val info: String,
    val expandedDescription: String = "제3정규형은 기본키가 아닌 속성이 다른 속성을 거쳐 결정되는 이행 함수 종속을 없앤 상태예요...",
    val question: String = "제3정규형에서 없애는 종속은?",
    val answer: String = "이행 함수 종속",
    var isExpanded: Boolean = false,
    var isEditing: Boolean = false
)