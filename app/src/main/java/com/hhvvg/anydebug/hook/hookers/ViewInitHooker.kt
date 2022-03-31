package com.hhvvg.anydebug.hook.hookers

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.ViewGroup
import com.hhvvg.anydebug.hook.IHooker
import com.hhvvg.anydebug.util.doAfter
import com.hhvvg.anydebug.util.registerMyActivityLifecycleCallbacks
import com.hhvvg.anydebug.util.updateDrawLayoutBounds
import com.hhvvg.anydebug.util.updateViewHookClick
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * @author hhvvg
 *
 * Hooks application. Loads initial settings of current application.
 */
class ViewInitHooker : IHooker {
    override fun onHook(param: XC_LoadPackage.LoadPackageParam) {
        Application::class.doAfter("onCreate") {
            val app = it.thisObject as Application
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