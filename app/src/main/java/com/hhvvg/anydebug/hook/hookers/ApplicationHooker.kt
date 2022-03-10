package com.hhvvg.anydebug.hook.hookers

import android.app.Activity
import android.app.AndroidAppHelper
import android.app.Application
import android.os.Bundle
import android.view.ViewGroup
import com.hhvvg.anydebug.hook.IHooker
import com.hhvvg.anydebug.util.*
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * @author hhvvg
 *
 * Hooks application.
 */
class ApplicationHooker : IHooker {
    override fun onHook(param: XC_LoadPackage.LoadPackageParam) {
        val appClazz = Application::class.java
        val onCreateHook = ApplicationOnCreateMethodHook()
        val method = XposedHelpers.findMethodBestMatch(appClazz, "onCreate", arrayOf(), arrayOf())
        XposedBridge.hookMethod(method, onCreateHook)
    }

    private class ApplicationOnCreateMethodHook : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam?) {
            if (param == null) {
                return
            }
            val app = AndroidAppHelper.currentApplication()
            val appClazz = app::class.java
            val callback = XposedHelpers.findField(appClazz, "mActivityLifecycleCallbacks")
            val callbackArray =
                callback.get(app) as ArrayList<Application.ActivityLifecycleCallbacks>
            callbackArray.add(ActivityCallback())
        }
    }

    private class ActivityCallback : Application.ActivityLifecycleCallbacks {
        override fun onActivityPostCreated(activity: Activity, savedInstanceState: Bundle?) {
            val contentView = activity.window.decorView as ViewGroup
            contentView.viewTreeObserver.addOnGlobalLayoutListener {
                val app = AndroidAppHelper.currentApplication()
                val showBounds = app.getInjectedField(APP_FIELD_SHOW_BOUNDS, false) ?: false
                val forceClickable = app.getInjectedField(APP_FIELD_FORCE_CLICKABLE, false) ?: false
                contentView.drawLayoutBounds(showBounds, true)
                contentView.setGlobalHookClick(
                    enabled = true,
                    traversalChildren = true,
                    forceClickable
                )
            }
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            // Do nothing
        }

        override fun onActivityStarted(activity: Activity) {
            // Do nothing
        }

        override fun onActivityResumed(activity: Activity) {
            val app = AndroidAppHelper.currentApplication()
            val showBounds = app.getInjectedField(APP_FIELD_SHOW_BOUNDS, false) ?: false
            val forceClickable = app.getInjectedField(APP_FIELD_FORCE_CLICKABLE, false) ?: false
            val decor = activity.window.decorView as ViewGroup
            decor.drawLayoutBounds(showBounds, true)
            decor.setGlobalHookClick(enabled = true, traversalChildren = true, forceClickable)
        }

        override fun onActivityPaused(activity: Activity) {
            // Do nothing
        }

        override fun onActivityStopped(activity: Activity) {
            // Do nothing
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            // Do nothing
        }

        override fun onActivityDestroyed(activity: Activity) {
            // Do nothing
        }

    }
}