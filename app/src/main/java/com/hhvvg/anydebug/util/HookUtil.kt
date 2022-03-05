package com.hhvvg.anydebug.util

import android.view.View
import de.robv.android.xposed.XposedHelpers

fun hookViewOnClickListener(view: View, listenerGeneratorCallback: (origin: View.OnClickListener?) -> View.OnClickListener?) {
    // Make it clickable first
    if (!view.isClickable) {
        view.isClickable = true
    }
    if (!view.isFocusable) {
        view.isFocusable = true
    }
    val info = XposedHelpers.callMethod(view, "getListenerInfo")
    val originListener = XposedHelpers.getObjectField(info, "mOnClickListener") as View.OnClickListener?
    val newListener = listenerGeneratorCallback.invoke(originListener)
    XposedHelpers.setObjectField(info, "mOnClickListener", newListener)
}

fun View.getOnClickListener(): View.OnClickListener? {
    val info = XposedHelpers.callMethod(this, "getListenerInfo")
    return XposedHelpers.getObjectField(info, "mOnClickListener") as View.OnClickListener?
}