package org.kimp.tfs.hw7.presentation.base

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.withStyledAttributes
import dagger.hilt.android.AndroidEntryPoint
import org.kimp.tfs.hw7.R
import org.kimp.tfs.hw7.utils.getNormalColor

@AndroidEntryPoint
class HorizontalLineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {
    private val basePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    @ColorInt private var lineColor: Int = 0

    init {
        context.withStyledAttributes(attrs, R.styleable.HorizontalLineView) {
            lineColor = getColor(R.styleable.HorizontalLineView_lineColor, context.getNormalColor())
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            resolveSize(MeasureSpec.getSize(widthMeasureSpec), widthMeasureSpec),
            resolveSize(paddingTop + LINE_WIDTH + paddingBottom, heightMeasureSpec)
        )
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawLine(
            paddingLeft.toFloat(),
            height / 2f,
            width.toFloat() - paddingRight,
            height / 2f,
            getLinePaint()
        )
    }

    private fun getLinePaint() = basePaint.apply {
        strokeWidth = LINE_WIDTH.toFloat()
        color = lineColor
    }

    companion object {
        private const val LINE_WIDTH = 2
    }
}
