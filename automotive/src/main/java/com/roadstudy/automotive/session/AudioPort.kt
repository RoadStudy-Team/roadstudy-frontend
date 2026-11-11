package com.roadstudy.automotive.session

/**
 * 세션 진행기가 오디오를 다루는 창구.
 * audio 패키지에서 Media3(ExoPlayer)로 구현하고, 재생이 끝나면 SessionController.onAudioFinished 를 호출한다.
 * 나중에 Pleos TextToSpeech 가 열리면 이 인터페이스 구현만 바꾸면 된다.
 */
interface AudioPort {
    fun play(url: String)
    fun pause()
    fun resume()
    fun stop()
}
