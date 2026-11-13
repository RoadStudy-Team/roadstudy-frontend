package com.roadstudy.automotive.session

/**
 * 재생할 한 조각(설명·질문·정답 중 하나).
 * 지금은 [text] 를 Pleos TextToSpeech 로 읽는다. 백엔드가 녹음 파일을 만들면 [url] 로 재생하도록 바꿀 수 있다.
 */
data class AudioClip(val text: String, val url: String)

/**
 * 세션 진행기가 오디오를 다루는 창구.
 * 구현은 playback 패키지에 있고, 재생이 끝나면 SessionController.onAudioFinished 를 호출한다.
 */
interface AudioPort {
    fun play(clip: AudioClip)
    fun pause()
    fun resume()
    fun stop()
}
