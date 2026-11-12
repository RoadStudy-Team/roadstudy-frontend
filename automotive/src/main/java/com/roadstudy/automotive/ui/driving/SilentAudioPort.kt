package com.roadstudy.automotive.ui.driving

import com.roadstudy.automotive.session.AudioPort

/**
 * 소리를 내지 않는 오디오. 실제 재생이 없는 데모에서 화면만 확인할 때 쓴다.
 * TODO: 오디오 재생(Media3)을 만들면 데모 외에는 쓰지 않는다.
 */
internal object SilentAudioPort : AudioPort {
    override fun play(url: String) = Unit
    override fun pause() = Unit
    override fun resume() = Unit
    override fun stop() = Unit
}
