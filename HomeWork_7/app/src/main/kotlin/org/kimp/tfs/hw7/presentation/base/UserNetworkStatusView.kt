package org.kimp.tfs.hw7.presentation.base

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.withStyledAttributes
import dagger.hilt.android.AndroidEntryPoint
import org.kimp.tfs.hw7.R
import org.kimp.tfs.hw7.utils.getEnum
import org.kimp.tfs.hw7.utils.getPrimaryColor
import org.kimp.tfs.hw7.utils.getSecondaryContainerColor
import org.kimp.tfs.hw7.utils.getTertiaryColor
import org.kimp.tfs.hw7.utils.getTextBounds
import org.kimp.tfs.hw7.utils.sp
import javax.inject.Inject

@AndroidEntryPoint
class UserNetworkStatusView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {
    @Inject lateinit var statusTypeface: Typeface

    private val basePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    @ColorInt private var onlineColor: Int = 0
    @ColorInt private var offlineColor: Int = 0
    @ColorInt private var idleColor: Int = 0

    private var statusTextSize: Float = 26f.sp(context)

    var status: UserNetworkStatus = UserNetworkStatus.UNKNOWN
        set(value) {
            field = value
            invalidate()
        }

    init {
        context.withStyledAttributes(attrs, R.styleable.UserNetworkStatusView) {
            onlineColor = getColor(R.styleable.UserNetworkStatusView_onlineColor, context.getPrimaryColor())
            offlineColor = getColor(R.styleable.UserNetworkStatusView_offlineColor, context.getSecondaryContainerColor())
            idleColor = getColor(R.styleable.UserNetworkStatusView_idleColor, context.getTertiaryColor())

            statusTextSize = getDimension(R.styleable.UserNetworkStatusView_statusTextSize, statusTextSize)
            status = getEnum(R.styleable.UserNetworkStatusView_networkStatus, status)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // The longest status
        val statusTextBounds = getStatusPaint().getTextBounds(
            UserNetworkStatus.values().map { v -> v.name }.maxBy { v -> v.length }
        )
        setMeasuredDimension(
            resolveSize(statusTextBounds.width() + paddingLeft + paddingRight, widthMeasureSpec),
            resolveSize(statusTextBounds.height() + paddingTop + paddingBottom, heightMeasureSpec)
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (status == UserNetworkStatus.UNKNOWN) return

        getStatusPaint().also { paint ->
            val statusBounds = paint.getTextBounds(status.name.lowercase())
            canvas.drawText(
                status.name.lowercase(),
                (width - statusBounds.width()) / 2f,
                height / 2 - statusBounds.exactCenterY(),
                paint
            )
        }
    }

    private fun getStatusPaint() = basePaint.apply {
        textSize = statusTextSize
        color = when(status) {
            UserNetworkStatus.ONLINE -> onlineColor
            UserNetworkStatus.OFFLINE -> offlineColor
            else -> idleColor
        }
        typeface = statusTypeface
    }

    enum class UserNetworkStatus {
        ONLINE, OFFLINE, IDLE, UNKNOWN
    }
}
