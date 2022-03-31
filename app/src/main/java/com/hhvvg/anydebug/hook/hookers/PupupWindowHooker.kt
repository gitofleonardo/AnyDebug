package com.hhvvg.anydebug.hook.hookers

import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupWindow
import com.hhvvg.anydebug.hook.IHooker
import com.hhvvg.anydebug.util.doBefore
import com.hhvvg.anydebug.util.setIgnoreTagRecursively
import com.hhvvg.anydebug.util.updateViewHookClick
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * @author hhvvg
 *
 * Hooks PupupWindow, disable all click hooks on all popups.
 */
class PupupWindowHooker : IHooker {
    override fun onHook(param: XC_LoadPackage.LoadPackageParam) {
        PopupWindow::class.doBefore("invokePopup", WindowManager.LayoutParams::class.java) {
            val decorView =
                XposedHelpers.getObjectField(it.thisObject, "mDecorView") as ViewGroup
            decorView.updateViewHookClick(
                enabled = false,
                forceClickable = false,
                traversalChildren = true
            )
            decorView.setIgnoreTagRecursively()
        }
    }
}
