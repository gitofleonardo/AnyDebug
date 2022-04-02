package com.hhvvg.anydebug.hook.hookers

import android.app.AndroidAppHelper
import android.app.Application
import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.hhvvg.anydebug.handler.ViewClickWrapper
import com.hhvvg.anydebug.handler.ViewClickWrapper.Companion.IGNORE_HOOK
import com.hhvvg.anydebug.hook.IHooker
import com.hhvvg.anydebug.util.doAfter
import com.hhvvg.anydebug.util.doAfterConstructor
import com.hhvvg.anydebug.util.doOnActivityPostCreated
import com.hhvvg.anydebug.util.doOnActivityResumed
import com.hhvvg.anydebug.util.isForceClickable
import com.hhvvg.anydebug.util.isGlobalEditEnabled
import com.hhvvg.anydebug.util.replaceOnClickListener
import com.hhvvg.anydebug.util.updateViewHookClick
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
        Application::class.doAfter("onCreate") {
            val app = it.thisObject as Application
            app.doOnActivityResumed { activity ->
                val decor = activity.window.decorView as ViewGroup
                decor.updateViewHookClick()
            }
            app.doOnActivityPostCreated { activity, _ ->
                val decorView = activity.window.decorView
                decorView.viewTreeObserver.addOnGlobalLayoutListener {
                    decorView.updateViewHookClick()
                }
            }
        }

        View::class.doAfter("setOnClickListener", View.OnClickListener::class.java) {
            val view = it.thisObject as View
            if (view.tag == IGNORE_HOOK) {
                return@doAfter
            }
            val app = AndroidAppHelper.currentApplication()
            val enabled = app.isGlobalEditEnabled
            val forceClickable = app.isForceClickable
            if (enabled) {
                val listener = it.args[0] as View.OnClickListener?
                view.replaceOnClickListener { origin ->
                    if (origin is ViewClickWrapper) {
                        ViewClickWrapper(listener, origin.originClickable, view)
                    } else {
                        ViewClickWrapper(listener, view.isClickable, view)
                    }
                }
                if (forceClickable) {
                    view.isClickable = true
                }
            }
        }

        // When setting tag to ignore, remove listener
        View::class.doAfter("setTag", Int::class.java) {
            val view = it.thisObject as View
            val tag = it.args[0]
            if (tag == IGNORE_HOOK) {
                view.updateViewHookClick(enabled = false, traversalChildren = false, forceClickable = false)
            }
        }
    }
}
