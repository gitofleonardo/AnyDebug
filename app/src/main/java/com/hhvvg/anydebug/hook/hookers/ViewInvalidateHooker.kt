package com.hhvvg.anydebug.hook.hookers

import android.app.Application
import android.view.ViewGroup
import com.hhvvg.anydebug.hook.IHooker
import com.hhvvg.anydebug.util.doAfter
import com.hhvvg.anydebug.util.doOnActivityResumed
import com.hhvvg.anydebug.util.updateDrawLayoutBounds
import com.hhvvg.anydebug.util.updateViewHookClick
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * @author hhvvg
 *
 * Hooks application, update view states on activity resumed.
 */
class ViewInvalidateHooker : IHooker {
    override fun onHook(param: XC_LoadPackage.LoadPackageParam) {
        Application::class.doAfter("onCreate") {
            val app = it.thisObject as Application
            app.doOnActivityResumed { activity ->
                val decor = activity.window.decorView as ViewGroup
                decor.updateDrawLayoutBounds()
                decor.updateViewHookClick()
            }
        }
    }
}
