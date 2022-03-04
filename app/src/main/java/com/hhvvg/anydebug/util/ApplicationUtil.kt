package com.hhvvg.anydebug.util

import android.app.Application
import de.robv.android.xposed.XposedHelpers

fun Application.injectField(name: String, value: Any?) {
    XposedHelpers.setAdditionalInstanceField(this, name, value)
}

fun <T> Application.getInjectedField(name: String, defaultValue: T? = null): T? {
    val value = XposedHelpers.getAdditionalInstanceField(this, name) ?: defaultValue
    return value as T?
}