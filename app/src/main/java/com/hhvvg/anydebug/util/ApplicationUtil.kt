package com.hhvvg.anydebug.util

import android.app.Application
import com.hhvvg.anydebug.persistent.ViewRule
import de.robv.android.xposed.XposedHelpers

var Application.isGlobalEditEnabled
    get() = getInjectedField(APP_FIELD_GLOBAL_CONTROL_ENABLED, defaultValue = false)!!
    set(value) = injectField(APP_FIELD_GLOBAL_CONTROL_ENABLED, value)

var Application.isPersistentEnabled
    get() = getInjectedField(APP_FIELD_PERSISTENT_ENABLE, defaultValue = false)!!
    set(value) = injectField(APP_FIELD_PERSISTENT_ENABLE, value)

var Application.isShowBounds
    get() = getInjectedField(APP_FIELD_SHOW_BOUNDS, defaultValue = false)!!
    set(value) = injectField(APP_FIELD_SHOW_BOUNDS, value)

var Application.isForceClickable
    get() = getInjectedField(APP_FIELD_FORCE_CLICKABLE, defaultValue = false)!!
    set(value) = injectField(APP_FIELD_FORCE_CLICKABLE, value)

var Application.rules: List<ViewRule>
    get() {
        return if (isPersistentEnabled) {
            getInjectedField(APP_FIELD_PERSISTENT_RULES, defaultValue = emptyList())!!
        } else {
            emptyList()
        }
    }
    set(value) = injectField(APP_FIELD_PERSISTENT_RULES, value)

var Application.rulesMap: Map<Int, List<ViewRule>>
    get() {
        return if (isPersistentEnabled) {
            getInjectedField(APP_FIELD_PERSISTENT_RULES_MAP, defaultValue = emptyMap())!!
        } else {
            emptyMap()
        }
    }
    set(value) = injectField(APP_FIELD_PERSISTENT_RULES_MAP, value)

fun Application.registerMyActivityLifecycleCallbacks(callback: Application.ActivityLifecycleCallbacks) {
    val callbackField =
        XposedHelpers.findField(Application::class.java, "mActivityLifecycleCallbacks")
    val callbackArray =
        callbackField.get(this) as ArrayList<Application.ActivityLifecycleCallbacks>
    callbackArray.add(callback)
}
