package com.hhvvg.anydebug.hook.hookers

import android.view.View
import com.hhvvg.anydebug.IGNORE_HOOK
import com.hhvvg.anydebug.ViewClickWrapper
import com.hhvvg.anydebug.hook.IHooker
import com.hhvvg.anydebug.util.replaceOnClickListener
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * @author hhvvg
 *
 * Hooks TextView.
 */
class TextViewHooker : IHooker {
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