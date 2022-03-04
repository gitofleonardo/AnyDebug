package com.hhvvg.anydebug.util

import android.app.AndroidAppHelper

fun Float.px(): Float {
    val context = AndroidAppHelper.currentApplication().applicationContext
    val scale = context.resources.displayMetrics.density
    return this * scale + 0.5F
}

fun Float.dp(): Float {
    val context = AndroidAppHelper.currentApplication().applicationContext
    val scale = context.resources.displayMetrics.density
    return this / scale + 0.5F
}