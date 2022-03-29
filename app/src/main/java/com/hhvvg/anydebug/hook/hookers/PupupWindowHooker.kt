package com.hhvvg.anydebug.hook.hookers

import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupWindow
import com.hhvvg.anydebug.hook.IHooker
import com.hhvvg.anydebug.util.updateViewHookClick
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * @author hhvvg
 *
 * Hooks PupupWindow, disable all click hooks on all popups.
 */
class PupupWindowHooker : IHooker {
    override fun onHook(param: XC_LoadPackage.LoadPackageParam) {
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
            val decorView =
                XposedHelpers.getObjectField(param.thisObject, "mDecorView") as ViewGroup
            decorView.updateViewHookClick(
                enabled = false,
                forceClickable = false,
                traversalChildren = true
            )
        }
    }
}