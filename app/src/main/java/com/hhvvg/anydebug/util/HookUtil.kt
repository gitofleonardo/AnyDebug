package com.hhvvg.anydebug.util

import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import de.robv.android.xposed.XposedHelpers

fun hookViewOnClickListener(
    view: View,
    listenerGeneratorCallback: (origin: View.OnClickListener?) -> View.OnClickListener?
) {
    // Make it clickable first
    if (!view.isClickable) {
        view.isClickable = true
    }
    if (!view.isFocusable) {
        view.isFocusable = true
    }
    val info = XposedHelpers.callMethod(view, "getListenerInfo")
    val originListener =
        XposedHelpers.getObjectField(info, "mOnClickListener") as View.OnClickListener?
    val newListener = listenerGeneratorCallback.invoke(originListener)
    XposedHelpers.setObjectField(info, "mOnClickListener", newListener)
}

fun View.getOnClickListener(): View.OnClickListener? {
    val info = XposedHelpers.callMethod(this, "getListenerInfo")
    return XposedHelpers.getObjectField(info, "mOnClickListener") as View.OnClickListener?
}

fun View.drawLayoutBounds(drawEnabled: Boolean, cascadeChildren: Boolean) {
    val attachInfo = XposedHelpers.getObjectField(this, "mAttachInfo") ?: return
    XposedHelpers.setBooleanField(attachInfo, "mDebugLayout", drawEnabled)
    if (cascadeChildren && this is ViewGroup) {
        val children = this.children
        for (child in children) {
            child.drawLayoutBounds(drawEnabled, true)
        }
    }
    this.invalidate()
}