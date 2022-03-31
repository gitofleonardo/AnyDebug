package com.hhvvg.anydebug.hook.hookers

import android.content.Context
import android.view.View
import com.hhvvg.anydebug.handler.ViewClickWrapper.Companion.IGNORE_HOOK
import com.hhvvg.anydebug.hook.IHooker
import com.hhvvg.anydebug.util.doAfter
import com.hhvvg.anydebug.util.doAfterConstructor
import com.hhvvg.anydebug.util.updateViewHookClick
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * @author hhvvg
 *
 * When adding new views dynamically, check their visibility.
 */
class ViewAddingHooker : IHooker {
    override fun onHook(param: XC_LoadPackage.LoadPackageParam) {
        // Sets hook click.
        View::class.doAfterConstructor(Context::class.java) { methodHookParam ->
            val view = methodHookParam.thisObject as View
            view.updateViewHookClick(traversalChildren = false)
        }
        View::class.doAfter("setTag", Any::class.java) {
            val view = it.thisObject as View
            val tag = it.args[0]
            if (tag == IGNORE_HOOK) {
                view.updateViewHookClick(enabled = false, traversalChildren = false)
            }
        }
    }
}
