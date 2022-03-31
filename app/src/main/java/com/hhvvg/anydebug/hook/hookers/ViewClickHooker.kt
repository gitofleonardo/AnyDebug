package com.hhvvg.anydebug.hook.hookers

import android.app.AndroidAppHelper
import android.view.View
import com.hhvvg.anydebug.handler.ViewClickWrapper
import com.hhvvg.anydebug.handler.ViewClickWrapper.Companion.IGNORE_HOOK
import com.hhvvg.anydebug.hook.IHooker
import com.hhvvg.anydebug.util.doAfter
import com.hhvvg.anydebug.util.isGlobalEditEnabled
import com.hhvvg.anydebug.util.replaceOnClickListener
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * @author hhvvg
 *
 * Hooks {@link View#seOnClickListener}, replace listener with our own custom one if necessary.
 */
class ViewClickHooker : IHooker {
    override fun onHook(param: XC_LoadPackage.LoadPackageParam) {
        View::class.doAfter("setOnClickListener", View.OnClickListener::class.java) {
            val view = it.thisObject as View
            if (view.tag == IGNORE_HOOK) {
                return@doAfter
            }
            val app = AndroidAppHelper.currentApplication()
            val enabled = app.isGlobalEditEnabled
            if (enabled) {
                val listener = it.args[0] as View.OnClickListener?
                view.replaceOnClickListener { origin ->
                    if (origin is ViewClickWrapper) {
                        ViewClickWrapper(listener, origin.originClickable, view)
                    } else {
                        ViewClickWrapper(listener, view.isClickable, view)
                    }
                }
            }
        }
    }
}
