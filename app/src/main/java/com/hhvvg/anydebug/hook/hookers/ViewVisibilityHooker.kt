package com.hhvvg.anydebug.hook.hookers

import android.app.AndroidAppHelper
import android.view.View
import com.hhvvg.anydebug.handler.ViewClickWrapper.Companion.IGNORE_HOOK
import com.hhvvg.anydebug.hook.IHooker
import com.hhvvg.anydebug.persistent.RuleType
import com.hhvvg.anydebug.util.doBefore
import com.hhvvg.anydebug.util.rulesMap
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * @author hhvvg
 *
 * Hooks {@link View#setVisibility} to apply our own rules.
 */
class ViewVisibilityHooker : IHooker {
    override fun onHook(param: XC_LoadPackage.LoadPackageParam) {
        View::class.doBefore("setFlags", Int::class.java, Int::class.java) {
            val view = it.thisObject as View
            if (view.tag == IGNORE_HOOK) {
                return@doBefore
            }
            val mask = XposedHelpers.getStaticIntField(View::class.java, "VISIBILITY_MASK")
            if (it.args[1] != mask) {
                return@doBefore
            }
            val viewId = view.id
            val parent = view.parent
            val parentId = if (parent is View) parent.id else View.NO_ID
            val app = AndroidAppHelper.currentApplication()
            val rules = app.rulesMap[viewId] ?: emptyList()
            for (rule in rules) {
                if (rule.ruleType == RuleType.Visibility && parentId == rule.viewParentId && view::class.java.name == rule.className) {
                    it.args[0] = rule.viewRule.toIntOrNull() ?: it.args[0]
                    break
                }
            }
        }
    }
}
