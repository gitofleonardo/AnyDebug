package com.hhvvg.anydebug.util

import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import com.hhvvg.anydebug.IGNORE_HOOK
import com.hhvvg.anydebug.ViewClickWrapper
import de.robv.android.xposed.XposedHelpers

fun View.replaceOnClickListener(
    listenerGeneratorCallback: (origin: View.OnClickListener?) -> View.OnClickListener?
) {
    val info = XposedHelpers.callMethod(this, "getListenerInfo")
    val originListener =
        XposedHelpers.getObjectField(info, "mOnClickListener") as View.OnClickListener?
    val newListener = listenerGeneratorCallback.invoke(originListener)
    XposedHelpers.setObjectField(info, "mOnClickListener", newListener)
}

fun View.getOnClickListener(): View.OnClickListener? {
    val info = XposedHelpers.callMethod(this, "getListenerInfo")
    return XposedHelpers.getObjectField(info, "mOnClickListener") as View.OnClickListener?
}

fun View.drawLayoutBounds(drawEnabled: Boolean, traversalChildren: Boolean, invalidate: Boolean = true) {
    val attachInfo = XposedHelpers.getObjectField(this, "mAttachInfo") ?: return
    XposedHelpers.setBooleanField(attachInfo, "mDebugLayout", drawEnabled)
    if (traversalChildren && this is ViewGroup) {
        val children = this.children
        for (child in children) {
            child.drawLayoutBounds(drawEnabled, true, invalidate)
        }
    }
    if (invalidate) {
        this.invalidate()
    }
}

fun View.setAllViewsHookClick(
    enabled: Boolean,
    traversalChildren: Boolean = true,
    forceClickable: Boolean = false
) {
    if (tag == IGNORE_HOOK) {
        return
    }
    replaceOnClickListener { origin ->
        if (enabled) {
            // Replace with my custom one
            if (origin is ViewClickWrapper) {
                origin
            } else {
                ViewClickWrapper(origin, isClickable, this)
            }
        } else {
            // Restore to original listener
            if (origin != null && origin is ViewClickWrapper) {
                origin.originListener
            } else {
                origin
            }
        }
    }
    if (forceClickable) {
        isClickable = true
    } else {
        val listener = getOnClickListener()
        if (listener != null && listener is ViewClickWrapper) {
            isClickable = listener.originClickable
        }
    }
    if (this !is ViewGroup || !traversalChildren) {
        return
    }
    val children = this.children
    for (child in children) {
        child.setAllViewsHookClick(enabled, traversalChildren, forceClickable)
    }
}
