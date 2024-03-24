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

/**
 * Do action before method called.
 */
fun KClass<*>.doBefore(methodName: String, vararg methodParams: Class<*>, callback: (XC_MethodHook.MethodHookParam) -> Unit): Unhook {
    val method = XposedHelpers.findMethodBestMatch(this.java, methodName, *methodParams)
    val methodHook = object : XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam) {
            callback.invoke(param)
        }
    }
    return XposedBridge.hookMethod(method, methodHook)
}

/**
 * Do action after method called.
 */
fun KClass<*>.doAfter(methodName: String, vararg methodParams: Class<*>, callback: (XC_MethodHook.MethodHookParam) -> Unit): Unhook {
    val method = XposedHelpers.findMethodBestMatch(this.java, methodName, *methodParams)
    val methodHook = object : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam) {
            callback.invoke(param)
        }
    }
    return XposedBridge.hookMethod(method, methodHook)
}

/**
 * Overrides method call.
 */
fun KClass<*>.override(methodName: String, vararg methodParams: Class<*>, callback: (XC_MethodHook.MethodHookParam) -> Any?): Unhook {
    val method = XposedHelpers.findMethodBestMatch(this.java, methodName, *methodParams)
    val methodHook = object : XC_MethodReplacement() {
        override fun replaceHookedMethod(param: MethodHookParam): Any? {
            return callback.invoke(param)
        }
    }
    return XposedBridge.hookMethod(method, methodHook)
}

/**
 * Calls method with specific name
 */
fun Any.call(method: String, vararg args: Any) {
    XposedHelpers.callMethod(this, method, *args)
}