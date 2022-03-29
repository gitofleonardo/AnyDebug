package com.hhvvg.anydebug.hook.hookers

import android.app.AndroidAppHelper
import android.view.View
import android.widget.TextView
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
 * Prevent from setting text.
 */
class SetTextHooker : IHooker {
    override fun onHook(param: XC_LoadPackage.LoadPackageParam) {
        val clazz = TextView::class.java
        val method = XposedHelpers.findMethodBestMatch(clazz, "setText", CharSequence::class.java, TextView.BufferType::class.java)
        val methodHook = SetTextMethodHook()
        XposedBridge.hookMethod(method, methodHook)
    }

    private class SetTextMethodHook : XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam) {
            val app = AndroidAppHelper.currentApplication()
            val textView = param.thisObject as TextView
            val viewId = textView.id
            val parent = textView.parent
            val parentId = if (parent is View) parent.id else View.NO_ID
            val rules = app.rulesMap[viewId] ?: emptyList()
            for (rule in rules) {
                if (rule.ruleType == RuleType.Text && textView::class.java.name == rule.className && parentId == rule.viewParentId) {
                    param.args[0] = rule.viewRule
                    break
                }
            }
        }
    }
}