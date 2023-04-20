package org.kimp.tfs.hw7.presentation.base

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.withStyledAttributes
import dagger.hilt.android.AndroidEntryPoint
import org.kimp.tfs.hw7.R
import org.kimp.tfs.hw7.utils.asBitmap
import org.kimp.tfs.hw7.utils.circlize
import org.kimp.tfs.hw7.utils.dp
import org.kimp.tfs.hw7.utils.getPrimaryColor
import org.kimp.tfs.hw7.utils.getSecondaryContainerColor
import org.kimp.tfs.hw7.utils.getTextBounds
import org.kimp.tfs.hw7.utils.sp
import javax.inject.Inject
import kotlin.math.sin

@AndroidEntryPoint
class AvatarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private val basePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    @Inject
    lateinit var initialsTypeface: Typeface

    @ColorInt
    private var initialsColor: Int = 0
    @ColorInt
    private var strokeColor: Int = 0
    @ColorInt
    private var badgeColor: Int = 0
    @ColorInt
    private var fillColor: Int = 0

    private var initialsSize: Float = 0f
    private var strokeWidth: Float = 0f
    private var badgeRadius: Float = 0f

    private var avatarSource: Bitmap? = null
    fun setAvatarSource(src: Drawable?) {
        avatarSource = src?.asBitmap()?.circlize()
        invalidate()
    }

    var hasBadge: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    var initials: String = ""
        set(value) {
            require(value.length <= 2) {
                "Initials length cannot be more than 2: $value"
            }
            field = value
            invalidate()
        }

    init {
        context.withStyledAttributes(attrs, R.styleable.AvatarView) {
            context.getSecondaryContainerColor().also { secondaryContainerColor ->
                fillColor = getColor(R.styleable.AvatarView_fillColor, secondaryContainerColor)
                badgeColor = getColor(R.styleable.AvatarView_badgeColor, secondaryContainerColor)
            }

            context.getPrimaryColor().also { primaryColor ->
                initialsColor = getColor(R.styleable.AvatarView_initialsColor, primaryColor)
                strokeColor = getColor(R.styleable.AvatarView_strokeColor, primaryColor)
            }

            initialsSize = getDimension(R.styleable.AvatarView_initialsSize, 14f.sp(context))
            strokeWidth = getDimension(R.styleable.AvatarView_strokeWidth, 3f.dp(context))
            badgeRadius = getDimension(R.styleable.AvatarView_badgeRadius, 4f.dp(context))

            hasBadge = getBoolean(R.styleable.AvatarView_hasBadge, false)
            initials = getString(R.styleable.AvatarView_initials) ?: ""

            setAvatarSource(getDrawable(R.styleable.AvatarView_src))
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val purportedSize = minOf(
            MeasureSpec.getSize(widthMeasureSpec),
            MeasureSpec.getSize(heightMeasureSpec)
        )

        setMeasuredDimension(
            resolveSize(purportedSize, widthMeasureSpec),
            resolveSize(purportedSize, heightMeasureSpec)
        )
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        val avatarRadius = minOf(
            width - paddingLeft - paddingRight,
            height - paddingTop - paddingBottom
        ) / 2f
        val contentRadius = avatarRadius - strokeWidth

        val cx = width / 2f
        val cy = height / 2f

        canvas.drawCircle(cx, cy, avatarRadius, getStrokePaint())
        canvas.drawCircle(cx, cy, contentRadius, getBackgroundPaint())

        if (avatarSource == null && initials.isNotEmpty()) {
            getInitialsPaint().also { paint ->
                val initialsBounds = paint.getTextBounds(initials)
                canvas.drawText(
                    initials,
                    cx - initialsBounds.width() / 1.8f,
                    cy - initialsBounds.exactCenterY(),
                    paint
                )
            }
        }

        avatarSource?.let { avatar ->
            canvas.drawBitmap(
                avatar,
                Rect(0, 0, avatar.width, avatar.height),
                Rect(
                    (cx - contentRadius).toInt(),
                    (cy - contentRadius).toInt(),
                    (cx + contentRadius).toInt(),
                    (cy + contentRadius).toInt()
                ),
                basePaint
            )
        }

        if (hasBadge) {
            val multiplier = sin(Math.PI / 4)

            canvas.drawCircle(
                (cx + avatarRadius * multiplier).toFloat(),
                (cy + avatarRadius * multiplier).toFloat(),
                badgeRadius + strokeWidth,
                getStrokePaint()
            )

            canvas.drawCircle(
                (cx + avatarRadius * multiplier).toFloat(),
                (cy + avatarRadius * multiplier).toFloat(),
                badgeRadius,
                getBadgePaint()
            )
        }
    }

    private fun getBadgePaint() = basePaint.apply {
        color = badgeColor
    }

    private fun getStrokePaint() = basePaint.apply {
        color = strokeColor
    }

    private fun getBackgroundPaint() = basePaint.apply {
        color = fillColor
    }

    private fun getInitialsPaint() = basePaint.apply {
        typeface = initialsTypeface
        textSize = initialsSize
        color = initialsColor
    }
}
