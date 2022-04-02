package com.hhvvg.anydebug.util

import android.app.AndroidAppHelper
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import androidx.core.view.children
import com.hhvvg.anydebug.handler.ViewClickWrapper
import com.hhvvg.anydebug.handler.ViewClickWrapper.Companion.IGNORE_HOOK
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import kotlin.reflect.KClass

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

fun View.updateDrawLayoutBounds(
    drawEnabled: Boolean? = null,
    invalidate: Boolean = true
) {
    val app = AndroidAppHelper.currentApplication()
    val isDrawEnabled = drawEnabled ?: app.isShowBounds
    setShowLayoutBounds(isDrawEnabled)
    if (this is ViewGroup) {
        children.forEach {
            it.updateDrawLayoutBounds(isDrawEnabled, invalidate)
        }
    }
    if (invalidate) {
        invalidate()
    }
}

fun View.updateViewHookClick(
    enabled: Boolean? = null,
    forceClickable: Boolean? = null,
    traversalChildren: Boolean = true
) {
    if (tag == IGNORE_HOOK) {
        return
    }
    val app = AndroidAppHelper.currentApplication()
    val editEnabled = enabled ?: app.isGlobalEditEnabled
    val isForceClickable = if (editEnabled) forceClickable ?: app.isForceClickable else false
    replaceOnClickListener { origin ->
        if (editEnabled) {
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
    if (isForceClickable) {
        isClickable = true
    } else {
        val listener = getOnClickListener()
        if (listener is ViewClickWrapper) {
            isClickable = listener.originClickable
        }
    }
    if (this !is ViewGroup || !traversalChildren) {
        return
    }
    val children = this.children
    for (child in children) {
        child.updateViewHookClick(
            enabled = editEnabled,
            forceClickable = isForceClickable,
            traversalChildren = traversalChildren
        )
    }
}

fun Any.injectField(name: String, value: Any?) {
    XposedHelpers.setAdditionalInstanceField(this, name, value)
}

fun <T> Any.getInjectedField(name: String, defaultValue: T? = null): T? {
    val value = XposedHelpers.getAdditionalInstanceField(this, name) ?: defaultValue
    return value as T?
}

fun KClass<*>.doBefore(methodName: String, vararg methodParams: Class<*>, callback: (XC_MethodHook.MethodHookParam) -> Unit) {
    val method = XposedHelpers.findMethodBestMatch(this.java, methodName, *methodParams)
    val methodHook = object : XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam) {
            callback.invoke(param)
        }
    }
    XposedBridge.hookMethod(method, methodHook)
}

fun KClass<*>.doAfter(methodName: String, vararg methodParams: Class<*>, callback: (XC_MethodHook.MethodHookParam) -> Unit) {
    val method = XposedHelpers.findMethodBestMatch(this.java, methodName, *methodParams)
    val methodHook = object : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam) {
            callback.invoke(param)
        }
    }
    XposedBridge.hookMethod(method, methodHook)
}

fun KClass<*>.doAfterConstructor(vararg params: Class<*>, callback: (XC_MethodHook.MethodHookParam) -> Unit) {
    XposedHelpers.findAndHookConstructor(this.java, *params, object : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam) {
            callback.invoke(param)
        }
    })
}

fun View.setShowLayoutBounds(show: Boolean) {
    val attachInfo = XposedHelpers.getObjectField(this, "mAttachInfo") ?: return
    XposedHelpers.setBooleanField(attachInfo, "mDebugLayout", show)
}

fun ViewParent.viewId(): Int {
    return if (this is View) id else View.NO_ID
}