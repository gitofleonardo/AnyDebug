package com.hhvvg.anydebug.util

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.hhvvg.anydebug.persistent.ViewRule
import de.robv.android.xposed.XposedHelpers
import java.lang.ref.WeakReference

var Application.isGlobalEditEnabled
    get() = getInjectedField(APP_FIELD_GLOBAL_CONTROL_ENABLED, defaultValue = false)!!
    set(value) = injectField(APP_FIELD_GLOBAL_CONTROL_ENABLED, value)

var Application.isPersistentEnabled
    get() = getInjectedField(APP_FIELD_PERSISTENT_ENABLE, defaultValue = false)!!
    set(value) = injectField(APP_FIELD_PERSISTENT_ENABLE, value)

var Application.isShowBounds
    get() = getInjectedField(APP_FIELD_SHOW_BOUNDS, defaultValue = false)!!
    set(value) {
        injectField(APP_FIELD_SHOW_BOUNDS, value)
    }

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

val Application.myLifecycleCallbacks: MyLifecycleCallbacks
    get() {
        var field = getInjectedField<MyLifecycleCallbacks>(APP_FIELD_LIFECYCLE_CALLBACK)
        if (field == null) {
            field = MyLifecycleCallbacks()
            injectField(APP_FIELD_LIFECYCLE_CALLBACK, field)
            registerMyActivityLifecycleCallbacks(field)
        }
        return field
    }

var Application.currentActivity: Activity?
    get() {
        val weakRef = getInjectedField<WeakReference<Activity>>(APP_FIELD_CURRENT_ACTIVITY_REF, null)
        return weakRef?.get()
    }
    set(value) {
        val weakRef = WeakReference<Activity>(value)
        injectField(APP_FIELD_CURRENT_ACTIVITY_REF, weakRef)
    }

fun Application.doOnActivityPostCreated(action: (Activity, Bundle?) -> Unit) {
    val callbacks = myLifecycleCallbacks
    callbacks.addPostCreatedAction(action)
}

fun Application.doOnActivityResumed(action: (Activity) -> Unit) {
    val callbacks = myLifecycleCallbacks
    callbacks.addResumedAction(action)
}

fun Application.doOnActivityDestroyed(action: (Activity) -> Unit) {
    val callbacks = myLifecycleCallbacks
    callbacks.addDestroyedAction(action)
}