package com.hhvvg.anydebug.hook

import android.app.Activity
import android.app.AndroidAppHelper
import android.app.Application
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupWindow
import com.hhvvg.anydebug.BuildConfig
import com.hhvvg.anydebug.IGNORE_HOOK
import com.hhvvg.anydebug.ViewClickWrapper
import com.hhvvg.anydebug.util.APP_FIELD_FORCE_CLICKABLE
import com.hhvvg.anydebug.util.APP_FIELD_SHOW_BOUNDS
import com.hhvvg.anydebug.util.drawLayoutBounds
import com.hhvvg.anydebug.util.getInjectedField
import com.hhvvg.anydebug.util.replaceOnClickListener
import com.hhvvg.anydebug.util.setGlobalHookClick
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.util.ArrayList

/**
 * Class for package hook.
 *
 * @author hhvvg
 */
class AnyHookPackage : IXposedHookLoadPackage {
    override fun handleLoadPackage(p0: XC_LoadPackage.LoadPackageParam?) {
        if (p0 == null) {
            return
        }

        // Don't hook itself
        val packageName = p0.packageName
        if (packageName == BuildConfig.PACKAGE_NAME) {
            return
        }

        // Hook application#onCreate
        val appClazz = Application::class.java
        val onCreateHook = ApplicationOnCreateMethodHook()
        val method = XposedHelpers.findMethodBestMatch(appClazz, "onCreate", arrayOf(), arrayOf())
        XposedBridge.hookMethod(method, onCreateHook)

        // Hook setOnClickListener
        val clickMethodHook = ViewSetOnClickListenerMethodHook()
        val clickMethod = XposedHelpers.findMethodBestMatch(
            View::class.java,
            "setOnClickListener",
            View.OnClickListener::class.java
        )
        XposedBridge.hookMethod(clickMethod, clickMethodHook)

        // Hook PupupWindow invoke popup
        val popupMethodHook = PopupWindowInvokePopupMethodHook()
        val popupMethod = XposedHelpers.findMethodBestMatch(
            PopupWindow::class.java,
            "invokePopup",
            WindowManager.LayoutParams::class.java
        )
        XposedBridge.hookMethod(popupMethod, popupMethodHook)
    }

    private class PopupWindowInvokePopupMethodHook : XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam?) {
            if (param == null) {
                return
            }
            val decorView = XposedHelpers.getObjectField(param.thisObject, "mDecorView") as ViewGroup
            decorView.setGlobalHookClick(enabled = false)
        }
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
        }

        override fun onActivityStarted(activity: Activity) {
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
        }

        override fun onActivityStopped(activity: Activity) {
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        }

        override fun onActivityDestroyed(activity: Activity) {
        }

    }

    private class ViewSetOnClickListenerMethodHook : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam?) {
            if (param == null) {
                return
            }

            val view = param.thisObject as View
            if (view.tag == IGNORE_HOOK) {
                return
            }
            view.replaceOnClickListener { origin ->
                if (origin is ViewClickWrapper) {
                    origin
                } else {
                    ViewClickWrapper(origin, view.isClickable, view)
                }
            }
        }
    }
}
