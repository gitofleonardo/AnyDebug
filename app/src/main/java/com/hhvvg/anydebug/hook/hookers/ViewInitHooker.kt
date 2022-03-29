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
 * Hooks application. Loads initial settings of current application.
 */
class ViewInitHooker : IHooker {
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
            app.registerMyActivityLifecycleCallbacks(ActivityCallback())
        }
    }

    private class ActivityCallback : Application.ActivityLifecycleCallbacks {
        override fun onActivityPostCreated(activity: Activity, savedInstanceState: Bundle?) {
            val contentView = activity.window.decorView as ViewGroup
            contentView.viewTreeObserver.addOnGlobalLayoutListener {
                contentView.updateDrawLayoutBounds()
                contentView.updateViewHookClick()
            }
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            // Do nothing
        }

        override fun onActivityStarted(activity: Activity) {
            // Do nothing
        }

        override fun onActivityResumed(activity: Activity) {
            val decor = activity.window.decorView as ViewGroup
            decor.updateDrawLayoutBounds()
            decor.updateViewHookClick()
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