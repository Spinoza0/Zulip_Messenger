package org.kimp.tfs.hw7.presentation.base

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.content.withStyledAttributes
import androidx.core.view.children
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import org.kimp.tfs.hw7.R
import org.kimp.tfs.hw7.databinding.MergeSearchToolbarBinding
import org.kimp.tfs.hw7.utils.getEnum
import org.kimp.tfs.hw7.utils.getOnPrimaryColor
import org.kimp.tfs.hw7.utils.getPrimaryColor
import org.kimp.tfs.hw7.utils.getSecondaryColor
import timber.log.Timber

@AndroidEntryPoint
class SearchToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    private val searchButton: MaterialButton
    private val backButton: MaterialButton
    private val titleTextView: MaterialTextView
    private val queryInputLayout: TextInputLayout
    private val queryEditText: TextInputEditText

    @ColorInt private var accentColor: Int = 0
    @ColorInt private var backgroundColor: Int = 0
    @ColorInt private var normalColor: Int = 0

    var title: String
        get() = titleTextView.text.toString()
        set(value) { titleTextView.text = value }

    var state: SearchState = SearchState.IDLE
        set(value) {
            (value == SearchState.IDLE).also { st ->
                searchButton.isVisible = st
                titleTextView.isVisible = st
                backButton.isVisible = !st
                queryInputLayout.isVisible = !st
                queryEditText.isVisible = !st

                if (st) queryEditText.setText("")
            }
            field = value
        }

    init {
        MergeSearchToolbarBinding.inflate(
            LayoutInflater.from(context), this
        ).also { binding ->
            searchButton = binding.searchButton
            backButton = binding.backButton
            titleTextView = binding.titleTextView
            queryInputLayout = binding.searchQueryInputLayout
            queryEditText = binding.searchQueryEditText
        }

        context.withStyledAttributes(attrs, R.styleable.SearchToolbar) {
            accentColor = getColor(R.styleable.SearchToolbar_accentColor, context.getOnPrimaryColor())
            backgroundColor = getColor(R.styleable.SearchToolbar_backgroundColor, context.getPrimaryColor())
            normalColor = getColor(R.styleable.SearchToolbar_normalColor, context.getSecondaryColor())

            title = getString(R.styleable.SearchToolbar_title) ?: ""
            state = getEnum(R.styleable.SearchToolbar_searchState, state)
        }

        titleTextView.setTextColor(accentColor)
        searchButton.iconTint = ColorStateList.valueOf(accentColor)
        backButton.iconTint = ColorStateList.valueOf(accentColor)

        queryInputLayout.hintTextColor = ColorStateList.valueOf(accentColor)
        queryInputLayout.defaultHintTextColor = ColorStateList.valueOf(accentColor)
        queryEditText.setTextColor(accentColor)

        queryInputLayout.setBoxBackgroundColorStateList(ColorStateList.valueOf(normalColor))

        connectHandlers()
        setWillNotDraw(false)
    }

    private fun connectHandlers() {
        val toggleListener: (View) -> Unit = {
            state = when(state) {
                SearchState.IDLE -> SearchState.SEARCH
                else -> SearchState.IDLE
            }
        }

        searchButton.setOnClickListener(toggleListener)
        backButton.setOnClickListener(toggleListener)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Timber.tag(TAG).i("SearchingToolbar has $childCount children")
        measureChildren(widthMeasureSpec, heightMeasureSpec)

        setMeasuredDimension(
            resolveSize(MeasureSpec.getSize(widthMeasureSpec), widthMeasureSpec),
            resolveSize(
                paddingTop + children.map{el -> el.measuredHeight}.max() + paddingBottom,
                heightMeasureSpec
            )
        )
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(backgroundColor)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val leftPos = paddingLeft
        val rightPos = r - l - paddingRight
        val topPos = paddingTop
        val bottomPos = b - t - paddingBottom

        titleTextView.layout(
            leftPos + SPACING * 2,
            topPos,
            rightPos - 2 * SPACING - searchButton.measuredWidth,
            bottomPos
        )
        searchButton.layout(
            rightPos - SPACING - searchButton.measuredWidth,
            topPos,
            rightPos - SPACING,
            bottomPos
        )

        backButton.layout(
            leftPos + SPACING,
            topPos,
            leftPos + SPACING + backButton.measuredWidth,
            bottomPos
        )
        queryInputLayout.layout(
            leftPos + 2 * SPACING + backButton.measuredWidth,
            topPos + SPACING * 2 / 3,
            rightPos - SPACING,
            bottomPos - SPACING * 2 / 3
        )
    }

    enum class SearchState{
        IDLE, SEARCH
    }

    companion object {
        private const val TAG = "SearchToolbar"
        private const val SPACING = 16
    }
}
