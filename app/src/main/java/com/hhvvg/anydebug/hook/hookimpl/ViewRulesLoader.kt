package com.hhvvg.anydebug.hook.hookimpl

import android.app.AndroidAppHelper
import android.app.Application
import android.text.SpannableString
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import com.hhvvg.anydebug.handler.ViewClickWrapper.Companion.IGNORE_HOOK
import com.hhvvg.anydebug.hook.IHook
import com.hhvvg.anydebug.persistent.AppDatabase
import com.hhvvg.anydebug.persistent.RuleType
import com.hhvvg.anydebug.util.doAfter
import com.hhvvg.anydebug.util.rules
import com.hhvvg.anydebug.util.rulesMap
import com.hhvvg.anydebug.util.sp
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * @author hhvvg
 *
 * Loads persistent rules and apply them to specific views.
 */
class ViewRulesLoader : IHook {
    override fun onHook(param: XC_LoadPackage.LoadPackageParam) {
        Application::class.doAfter("onCreate") {
            val app = it.thisObject as Application
            CoroutineScope(context = Dispatchers.IO).launch {
                val rulesFlow = AppDatabase.viewRuleDao.queryAllRules()
                rulesFlow.collect { list ->
                    app.rules = list
                    app.rulesMap = list.groupBy { rule -> rule.viewId }
                }
            }
        }
        View::class.doAfter("onAttachedToWindow") {
            val view = it.thisObject as View
            if (view.tag == IGNORE_HOOK) {
                return@doAfter
            }
            applyRules(view, applyForChildren = false)
        }
    }

    private fun applyRules(view: View, applyForChildren: Boolean = true) {
        val rules = AndroidAppHelper.currentApplication().rulesMap[view.id] ?: emptyList()
        for (rule in rules) {
            // In the same context, having same class name and view id and parent id
            // We consider them as the same views.
            // Notice that this is not accurate, a better way needed.
            val parent = view.parent
            val parentId = if (parent is View) parent.id else View.NO_ID
            if (rule.viewParentId != parentId ||
                rule.className != view::class.java.name
            ) {
                continue
            }
            when (rule.ruleType) {
                RuleType.Visibility -> {
                    view.visibility = rule.viewRule.toIntOrNull() ?: View.VISIBLE
                }
                RuleType.Text -> {
                    if (view is TextView && view.text.toString() == rule.originViewContent) {
                        view.text = SpannableString(rule.viewRule)
                    }
                }
                RuleType.TextMaxLine -> {
                    if (view is TextView) {
                        view.maxLines = rule.viewRule.toIntOrNull() ?: view.maxLines
                    }
                }
                RuleType.TextSize -> {
                    if (view is TextView) {
                        view.textSize = rule.viewRule.toFloatOrNull() ?: view.textSize.sp()
                    }
                }
                else -> {}
            }
        }
        if (view !is ViewGroup || !applyForChildren) {
            return
        }
        val children = view.children
        for (child in children) {
            applyRules(child)
        }
    }
}
