package com.hhvvg.anydebug.hook.hookers

import android.app.AndroidAppHelper
import android.view.View
import com.hhvvg.anydebug.handler.ViewClickWrapper.Companion.IGNORE_HOOK
import com.hhvvg.anydebug.hook.IHooker
import com.hhvvg.anydebug.persistent.RuleType
import com.hhvvg.anydebug.util.rulesMap
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * @author hhvvg
 *
 * Hooks {@link View#setVisibility} to apply our own rules.
 */
class ViewVisibilityHooker : IHooker {
    override fun onHook(param: XC_LoadPackage.LoadPackageParam) {
        val clazz = View::class.java
        val method = XposedHelpers.findMethodBestMatch(clazz, "setFlags", Int::class.java, Int::class.java)
        val methodHook = SetVisibilityMethodHook()
        XposedBridge.hookMethod(method, methodHook)
    }

    private class SetVisibilityMethodHook : XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam) {
            val view = param.thisObject as View
            if (view.tag == IGNORE_HOOK) {
                return
            }
            val mask = XposedHelpers.getStaticIntField(View::class.java, "VISIBILITY_MASK")
            if (param.args[1] != mask) {
                return
            }
            val viewId = view.id
            val parent = view.parent
            val parentId = if (parent is View) parent.id else View.NO_ID
            val app = AndroidAppHelper.currentApplication()
            val rules = app.rulesMap[viewId] ?: emptyList()
            for (rule in rules) {
                if (rule.ruleType == RuleType.Visibility && parentId == rule.viewParentId && view::class.java.name == rule.className) {
                    param.args[0] = rule.viewRule.toIntOrNull() ?: param.args[0]
                    break
                }
            }
        }
    }
}