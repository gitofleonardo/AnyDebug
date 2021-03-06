package com.hhvvg.anydebug.hook.hookimpl

import android.app.AndroidAppHelper
import android.view.View
import android.widget.TextView
import com.hhvvg.anydebug.hook.IHook
import com.hhvvg.anydebug.persistent.RuleType
import com.hhvvg.anydebug.util.doBefore
import com.hhvvg.anydebug.util.rulesMap
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * @author hhvvg
 *
 * Prevent from setting text.
 */
class TextViewHook : IHook {
    override fun onHook(param: XC_LoadPackage.LoadPackageParam) {
        TextView::class.doBefore(
            "setText",
            CharSequence::class.java,
            TextView.BufferType::class.java
        ) {
            val app = AndroidAppHelper.currentApplication()
            val textView = it.thisObject as TextView
            val charseq: CharSequence? = it.args[0] as CharSequence?
            val viewId = textView.id
            val parent = textView.parent
            val parentId = if (parent is View) parent.id else View.NO_ID
            val rules = app.rulesMap[viewId] ?: emptyList()
            for (rule in rules) {
                if (rule.ruleType == RuleType.Text &&
                    textView::class.java.name == rule.className &&
                    parentId == rule.viewParentId &&
                    rule.originViewContent == charseq
                ) {
                    it.args[0] = rule.viewRule
                    break
                }
            }
        }
    }
}