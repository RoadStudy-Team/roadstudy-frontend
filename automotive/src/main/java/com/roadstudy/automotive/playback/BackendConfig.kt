package com.roadstudy.automotive.playback

/**
 * 학습 음성(mp3)을 받아 오는 백엔드 설정.
 *
 * - 에뮬레이터에서 10.0.2.2 는 에뮬레이터를 실행한 컴퓨터(로컬 백엔드)를 가리킨다. 실제 서버를 쓰면 https 주소로 바꾼다.
 * - MVP: 자료 목록 API 가 없어서, 백엔드에 저장된 음성 하나를 샘플 섹션에 고정으로 연결한다.
 *   음성은 개념별 조각이 아니라 자료 전체 설명 한 파일(약 9분)이다.
 */
object BackendConfig {

    const val BASE_URL = "http://10.0.2.2:8080"

    /** 섹션 id → 백엔드 음성 id. 백엔드 자료 1번 "프로세스와 스레드, 멀티태스킹의 핵심 이해" */
    private val AUDIO_IDS = mapOf("process-thread" to "aa878ce8-abef-4833-a586-bc237b98ceb4")

    /** 섹션에 연결된 백엔드 음성 주소. 없으면 null (그 섹션은 TTS 로 개념별로 읽는다) */
    fun audioUrlFor(sectionId: String?): String? = AUDIO_IDS[sectionId]?.let { "$BASE_URL/api/studies/audio/$it" }
}
