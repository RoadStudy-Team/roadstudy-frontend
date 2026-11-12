package com.roadstudy.automotive.ui.driving

import kotlin.math.PI
import kotlin.math.sin

/**
 * 파형 막대가 움직이는 방식. 소리와 상관없는 가짜 움직임이다.
 * 막대마다 위상을 조금씩 달리해서 파도가 옆으로 흐르는 것처럼 보이게 한다.
 * TODO: 오디오 재생을 붙이면 재생 중인 소리 크기를 amplitude 로 넣는다.
 */
object WaveformMotion {

    /** 한 바퀴를 도는 위상 (라디안) */
    const val FULL_CYCLE = (2 * PI).toFloat()

    /** 옆 막대와의 위상 차이. 클수록 파도가 촘촘하다 */
    private const val PHASE_STEP = 0.55f

    /**
     * 막대 하나의 높이에 곱할 값. [depth] 가 0 이면 항상 1(움직임 없음),
     * 1 이면 0~1 사이를 오간다. 예) depth 0.3 이면 0.7~1.0
     */
    fun barFactor(phase: Float, index: Int, depth: Float): Float {
        val wave = 0.5f + 0.5f * sin(phase + index * PHASE_STEP)
        return (1f - depth) + depth * wave
    }
}
