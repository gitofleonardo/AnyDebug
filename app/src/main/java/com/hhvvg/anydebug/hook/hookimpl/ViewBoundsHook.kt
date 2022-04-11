package com.hhvvg.anydebug.hook.hookimpl

import android.app.AndroidAppHelper
import android.app.Application
import android.view.View
import android.view.ViewGroup
import com.hhvvg.anydebug.handler.ViewClickWrapper.Companion.IGNORE_HOOK
import com.hhvvg.anydebug.hook.IHook
import com.hhvvg.anydebug.util.doAfter
import com.hhvvg.anydebug.util.doOnActivityResumed
import com.hhvvg.anydebug.util.isShowBounds
import com.hhvvg.anydebug.util.setShowLayoutBounds
import com.hhvvg.anydebug.util.updateDrawLayoutBounds
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * @author hhvvg
 */
class ViewBoundsHook : IHook {
    override fun onHook(param: XC_LoadPackage.LoadPackageParam) {
        Application::class.doAfter("onCreate") {
            val app = it.thisObject as Application
            app.doOnActivityResumed { activity ->
                val decor = activity.window.decorView as ViewGroup
                decor.updateDrawLayoutBounds()
            }
        }
        View::class.doAfter("onAttachedToWindow") {
            val showBounds = AndroidAppHelper.currentApplication().isShowBounds
            val view = it.thisObject as View
            if (view.tag == IGNORE_HOOK) {
                view.setShowLayoutBounds(false)
                return@doAfter
            }
            view.setShowLayoutBounds(showBounds)
        }
    }
}