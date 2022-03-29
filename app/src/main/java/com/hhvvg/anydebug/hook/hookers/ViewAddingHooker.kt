package com.hhvvg.anydebug.hook.hookers

import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import com.hhvvg.anydebug.handler.ViewClickWrapper.Companion.IGNORE_HOOK
import com.hhvvg.anydebug.hook.IHooker
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * @author hhvvg
 *
 * When adding new views dynamically, check their visibility.
 */
class ViewAddingHooker : IHooker {
    override fun onHook(param: XC_LoadPackage.LoadPackageParam) {
        val clazz = ViewGroup::class.java
        val method = XposedHelpers.findMethodBestMatch(clazz, "addView", View::class.java, Int::class.java, ViewGroup.LayoutParams::class.java)
        val methodHook = AddViewMethodHook()
        XposedBridge.hookMethod(method, methodHook)
    }

    private class AddViewMethodHook : XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam) {
            val view = param.args[0] as View
            dfsSetVisibility(view)
        }

        private fun dfsSetVisibility(view: View) {
            if (view.tag == IGNORE_HOOK) {
                return
            }
            // Manually set visibility again to update visibility
            view.visibility = view.visibility
            if (view !is ViewGroup) {
                return
            }
            val children = view.children
            for (child in children) {
                dfsSetVisibility(child)
            }
        }
    }
}