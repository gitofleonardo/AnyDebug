/*
 *     Copyright (C) <2024>  <gitofleonardo>
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.hhvvg.libinject.utils

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodHook.Unhook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import kotlin.reflect.KClass

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

fun KClass<*>.override(methodName: String, vararg methodParams: Class<*>, callback: (XC_MethodHook.MethodHookParam) -> Any?): Unhook {
    val method = XposedHelpers.findMethodBestMatch(this.java, methodName, *methodParams)
    val methodHook = object : XC_MethodReplacement() {
        override fun replaceHookedMethod(param: MethodHookParam): Any? {
            return callback.invoke(param)
        }
    }
    return XposedBridge.hookMethod(method, methodHook)
}

fun Any.injectField(name: String, value: Any?) {
    XposedHelpers.setAdditionalInstanceField(this, name, value)
}

fun <T> Any.getInjectedField(name: String): T? {
    val value = XposedHelpers.getAdditionalInstanceField(this, name)
    return value as T?
}

fun <T> Any.getInjectedField(name: String, defaultValue: T): T {
    val value = XposedHelpers.getAdditionalInstanceField(this, name) ?: defaultValue
    return value as T
}

fun Any.call(method: String, vararg args: Any) {
    XposedHelpers.callMethod(this, method, *args)
}