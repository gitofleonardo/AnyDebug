package com.hhvvg.anydebug.hook.hookimpl

import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupWindow
import com.hhvvg.anydebug.hook.IHook
import com.hhvvg.anydebug.util.doBefore
import com.hhvvg.anydebug.util.updateViewHookClick
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * @author hhvvg
 *
 * Hooks PupupWindow, disable all click hooks on all popups.
 */
class PupupWindowHook : IHook {
    override fun onHook(param: XC_LoadPackage.LoadPackageParam) {
        PopupWindow::class.doBefore("invokePopup", WindowManager.LayoutParams::class.java) {
            val decorView =
                XposedHelpers.getObjectField(it.thisObject, "mDecorView") as ViewGroup
            decorView.updateViewHookClick(
                enabled = false,
                forceClickable = false,
                traversalChildren = true
            )
        }
    }
}
