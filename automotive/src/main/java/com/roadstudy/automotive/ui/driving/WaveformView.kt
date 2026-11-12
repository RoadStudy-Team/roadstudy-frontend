package com.roadstudy.automotive.ui.driving

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import com.roadstudy.shared.R as SharedR

/**
 * 가운데가 높은 오디오 파형 모양.
 * 막대 높이는 고정이고 [amplitude] 로 전체 크기를, [setMotion] 으로 물결치는 움직임을 정한다.
 * 움직임은 소리와 상관없는 가짜다 (WaveformMotion 참고).
 * TODO: 오디오 재생을 붙이면 재생 중인 소리 크기를 amplitude 로 넣는다.
 */
class WaveformView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val density = resources.displayMetrics.density
    private val barWidth = BAR_WIDTH_DP * density
    private val barGap = BAR_GAP_DP * density
    private val maxBarHeight = MAX_BAR_HEIGHT_DP * density

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, SharedR.color.rs_accent)
    }
    private val rect = RectF()

    private var animator: ValueAnimator? = null
    private var phase = 0f
    private var motionPeriodMs = 0L
    private var motionDepth = 0f

    /** 0.0~1.0. 막대 높이에 곱한다 */
    var amplitude: Float = 1f
        set(value) {
            field = value.coerceIn(0f, 1f)
            invalidate()
        }

    /** 0.0~1.0. 막대의 진하기 */
    var intensity: Float = 0.55f
        set(value) {
            field = value.coerceIn(0f, 1f)
            paint.alpha = (255 * field).toInt()
            invalidate()
        }

    init {
        paint.alpha = (255 * intensity).toInt()
    }

    /**
     * 물결 움직임을 정한다.
     * @param periodMs 물결이 한 바퀴 도는 시간. 길수록 느리다
     * @param depth 0.0~1.0. 막대가 줄어드는 정도. 0 이면 움직이지 않는다
     */
    fun setMotion(periodMs: Long, depth: Float) {
        motionPeriodMs = periodMs
        motionDepth = depth.coerceIn(0f, 1f)
        updateAnimator()
    }

    private fun updateAnimator() {
        animator?.cancel()
        animator = null
        if (motionDepth <= 0f || motionPeriodMs <= 0L || !isShown) {
            invalidate()
            return
        }
        animator = ValueAnimator.ofFloat(0f, WaveformMotion.FULL_CYCLE).apply {
            duration = motionPeriodMs
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                phase = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        updateAnimator()
    }

    override fun onDetachedFromWindow() {
        // 화면이 사라지면 애니메이션을 멈춘다 (계속 돌면 배터리와 메모리를 쓴다)
        animator?.cancel()
        animator = null
        super.onDetachedFromWindow()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        updateAnimator()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = (HEIGHT_FRACTIONS.size * barWidth + (HEIGHT_FRACTIONS.size - 1) * barGap).toInt()
        setMeasuredDimension(
            resolveSize(width, widthMeasureSpec),
            resolveSize(maxBarHeight.toInt(), heightMeasureSpec)
        )
    }

    override fun onDraw(canvas: Canvas) {
        val totalWidth = HEIGHT_FRACTIONS.size * barWidth + (HEIGHT_FRACTIONS.size - 1) * barGap
        val startX = (width - totalWidth) / 2f
        val centerY = height / 2f
        HEIGHT_FRACTIONS.forEachIndexed { index, fraction ->
            val motion = WaveformMotion.barFactor(phase, index, motionDepth)
            val half = maxBarHeight * fraction * amplitude * motion / 2f
            val left = startX + index * (barWidth + barGap)
            rect.set(left, centerY - half, left + barWidth, centerY + half)
            canvas.drawRoundRect(rect, barWidth / 2f, barWidth / 2f, paint)
        }
    }

    private companion object {
        const val BAR_WIDTH_DP = 5f
        const val BAR_GAP_DP = 17f
        const val MAX_BAR_HEIGHT_DP = 126f

        /** 막대 15개의 높이 비율 (도안 04 의 파형) */
        val HEIGHT_FRACTIONS = floatArrayOf(
            0.20f, 0.35f, 0.55f, 0.65f, 0.40f, 0.95f, 0.50f, 1.00f,
            0.78f, 0.38f, 0.82f, 0.62f, 0.36f, 0.26f, 0.20f
        )
    }
}
