package com.hhvvg.anydebug.util

import de.robv.android.xposed.XposedHelpers

fun Any.injectField(name: String, value: Any?) {
    XposedHelpers.setAdditionalInstanceField(this, name, value)
}

fun <T> Any.getInjectedField(name: String, defaultValue: T? = null): T? {
    val value = XposedHelpers.getAdditionalInstanceField(this, name) ?: defaultValue
    return value as T?
}