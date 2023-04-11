package org.kimp.tfs.hw7.utils

import android.content.Context
import android.util.TypedValue
import org.kimp.tfs.hw7.R

fun Context.getPrimaryColor() = this.getAttrColor(R.attr.colorPrimary)
fun Context.getOnPrimaryColor() = this.getAttrColor(R.attr.colorOnPrimary)
fun Context.getTertiaryColor() = this.getAttrColor(R.attr.colorTertiary)
fun Context.getSecondaryColor() = this.getAttrColor(R.attr.colorOnPrimaryContainer)
fun Context.getSecondaryContainerColor() = this.getAttrColor(R.attr.colorSecondaryContainer)
fun Context.getTertiaryContainerColor() = this.getAttrColor(R.attr.colorTertiaryContainer)
fun Context.getNormalColor() = this.getAttrColor(R.attr.colorButtonNormal)

fun Context.getAttrColor(resId: Int) = TypedValue().let { tv ->
    this.theme.resolveAttribute(resId, tv, true)
    this.getColor(tv.resourceId)
}
