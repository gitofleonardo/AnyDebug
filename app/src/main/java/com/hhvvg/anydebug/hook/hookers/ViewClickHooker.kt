package com.hhvvg.anydebug.hook.hookers

import android.app.AndroidAppHelper
import android.view.View
import com.hhvvg.anydebug.BuildConfig
import com.hhvvg.anydebug.IGNORE_HOOK
import com.hhvvg.anydebug.ViewClickWrapper
import com.hhvvg.anydebug.hook.IHooker
import com.hhvvg.anydebug.util.APP_FIELD_GLOBAL_CONTROL_ENABLED
import com.hhvvg.anydebug.util.getInjectedField
import com.hhvvg.anydebug.util.replaceOnClickListener
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * @author hhvvg
 *
 * Hooks {@link View#seOnClickListener}, replace listener with our own custom one if necessary.
 */
class ViewClickHooker : IHooker {
    override fun onHook(param: XC_LoadPackage.LoadPackageParam) {
        val clickMethodHook = ViewSetOnClickListenerMethodHook()
        val clickMethod = XposedHelpers.findMethodBestMatch(
            View::class.java,
            "setOnClickListener",
            View.OnClickListener::class.java
        )
        XposedBridge.hookMethod(clickMethod, clickMethodHook)
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
            val app = AndroidAppHelper.currentApplication()
            val enabled = app.getInjectedField(APP_FIELD_GLOBAL_CONTROL_ENABLED, false) ?: false
            if (enabled) {
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
}