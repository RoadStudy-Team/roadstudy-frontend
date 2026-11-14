package com.roadstudy.automotive.ui.driving

import com.roadstudy.automotive.session.AudioClip
import com.roadstudy.automotive.session.AudioPort

/** 소리를 내지 않는 오디오. 개발용 가짜 주행 상황(데모)에서 화면만 확인할 때 쓴다 */
internal object SilentAudioPort : AudioPort {
    override fun play(clip: AudioClip) = Unit
    override fun pause() = Unit
    override fun resume() = Unit
    override fun stop() = Unit
}
