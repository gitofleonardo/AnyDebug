package com.hhvvg.anydebug.hook.hookers

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.text.SpannableString
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import com.hhvvg.anydebug.hook.IHooker
import com.hhvvg.anydebug.persistent.AppDatabase
import com.hhvvg.anydebug.persistent.RuleType
import com.hhvvg.anydebug.persistent.ViewRule
import com.hhvvg.anydebug.util.registerMyActivityLifecycleCallbacks
import com.hhvvg.anydebug.util.rules
import com.hhvvg.anydebug.util.rulesMap
import com.hhvvg.anydebug.util.sp
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * @author hhvvg
 *
 * Loads persistent rules and apply them to specific views.
 */
class ViewRulesLoader : IHooker {
    override fun onHook(param: XC_LoadPackage.LoadPackageParam) {
        val methodHook = AppOnCreateMethodHook()
        val method = XposedHelpers.findMethodBestMatch(
            Application::class.java,
            "onCreate",
            arrayOf(),
            arrayOf()
        )
        XposedBridge.hookMethod(method, methodHook)
    }

    private class AppOnCreateMethodHook : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam) {
            val app = param.thisObject as Application
            app.registerMyActivityLifecycleCallbacks(ActivityCallbacks())
            GlobalScope.launch(context = Dispatchers.IO) {
                val rulesFlow = AppDatabase.viewRuleDao.queryAllRules()
                rulesFlow.collect {
                    app.rules = it
                    app.rulesMap = it.groupBy { rule -> rule.viewId }
                }
            }
        }
    }

    private class ActivityCallbacks: Application.ActivityLifecycleCallbacks {
        override fun onActivityPostResumed(activity: Activity) {
            val app = activity.application
            val rules = app.rules
            val viewIdRulesMap = rules.groupBy {
                it.viewId
            }
            val decorView = activity.window.decorView
            applyRules(decorView, viewIdRulesMap)
        }

        private fun applyRules(view: View, rulesMap: Map<Int, List<ViewRule>>) {
            val rules = rulesMap[view.id] ?: emptyList()
            for (rule in rules) {
                // In the same context, having same class name and view id and parent id
                // We consider them as the same views.
                // Notice that this is not accurate, a better way needed.
                val parent = view.parent
                val parentId = if (parent is View) parent.id else View.NO_ID
                if (rule.viewParentId != parentId ||
                    rule.className != view::class.java.name) {
                    continue
                }
                when (rule.ruleType) {
                    RuleType.Visibility -> {
                        view.visibility = rule.viewRule.toIntOrNull() ?: View.VISIBLE
                    }
                    RuleType.Text -> {
                        if (view is TextView) {
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
            if (view !is ViewGroup) {
                return
            }
            val children = view.children
            for (child in children) {
                applyRules(child, rulesMap)
            }
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        }

        override fun onActivityStarted(activity: Activity) {
        }

        override fun onActivityResumed(activity: Activity) {
        }

        override fun onActivityPaused(activity: Activity) {
        }

        override fun onActivityStopped(activity: Activity) {
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        }

        override fun onActivityDestroyed(activity: Activity) {
        }
    }
}