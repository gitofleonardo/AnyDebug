package com.hhvvg.anydebug.util

import android.app.AndroidAppHelper
import android.content.Context
import android.view.ViewGroup

private val context: Context
    get() = AndroidAppHelper.currentApplication().applicationContext

private val density: Float
    get() = context.resources.displayMetrics.density

private val scaledDensity: Float
    get() = context.resources.displayMetrics.scaledDensity

fun Float.px(): Float {
    val scale = density
    return this * scale
}

fun Float.dp(): Float {
    val scale = density
    return this / scale
}

fun Int.px(): Int {
    val scale = density
    return (this * scale + 0.5F).toInt()
}

fun Int.dp(): Int {
    val scale = density
    return (this / scale + 0.5F).toInt()
}

fun Float.sp(): Float {
    val scaleDensity = scaledDensity
    return this / scaleDensity
}

fun Float.spToPx(): Float {
    val scaleDensity = scaledDensity
    return this * scaleDensity
}

fun Int.spToPx(): Int {
    return (this.toFloat().spToPx() + 0.5F).toInt()
}

fun Int.specOrPx(): Int {
    return when (this) {
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT -> {
            this
        }
        else -> {
            this.px()
        }
    }
}

fun Int.specOrDp(): Int {
    return when(this) {
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT -> {
            this
        }
        else -> {
            this.dp()
        }
    }
}