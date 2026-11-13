package com.roadstudy.mobile.mydata

import android.R.attr.elevation
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import com.roadstudy.mobile.databinding.PopupCardMenuBinding

class CardMenuPopup(
    context: Context,
    private val onSetToday: () -> Unit,
    private val onRename: () -> Unit,
    private val onAddMaterial: () -> Unit,
    private val onDelete: () -> Unit
) {

    private val binding = PopupCardMenuBinding.inflate(LayoutInflater.from(context))
    private val density = context.resources.displayMetrics.density

    private val widthPx = (180 * density).toInt()

    private val popupWindow = PopupWindow(
        binding.root,
        widthPx, // 가로 길이를 180dp로 고정
        ViewGroup.LayoutParams.WRAP_CONTENT,
        true // 바깥 터치 시 닫힘
    ).apply {
        // 둥근 모서리 바깥에 사각형이 보이지 않도록 윈도우 배경을 투명으로
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        isOutsideTouchable = true
        elevation = 8f * density
    }

    init {
        binding.menuSetToday.setOnClickListener { popupWindow.dismiss(); onSetToday() }
        binding.menuRename.setOnClickListener { popupWindow.dismiss(); onRename() }
        binding.menuAddMaterial.setOnClickListener { popupWindow.dismiss(); onAddMaterial() }
        binding.menuDelete.setOnClickListener { popupWindow.dismiss(); onDelete() }
    }

    fun show(anchor: View) {
        binding.root.measure(
            View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        // 팝업이 버튼(anchor)의 오른쪽에 붙도록 위치 조정 (필요에 따라 xOffset 조절 가능)
        val xOffset = anchor.width - widthPx
        val yOffset = (4 * density).toInt()

        popupWindow.showAsDropDown(anchor, xOffset, yOffset)
    }
}