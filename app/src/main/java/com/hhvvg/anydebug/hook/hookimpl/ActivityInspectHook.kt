package com.hhvvg.anydebug.hook.hookimpl

import android.app.Application
import com.hhvvg.anydebug.hook.IHook
import com.hhvvg.anydebug.util.currentActivity
import com.hhvvg.anydebug.util.doAfter
import com.hhvvg.anydebug.util.doOnActivityResumed
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * @author hhvvg
 */
class ActivityInspectHook : IHook{
    override fun onHook(param: XC_LoadPackage.LoadPackageParam) {
        Application::class.doAfter("onCreate") {
            val app = it.thisObject as Application
            app.doOnActivityResumed { activity ->
                app.currentActivity = activity
            }
        }
    }
}