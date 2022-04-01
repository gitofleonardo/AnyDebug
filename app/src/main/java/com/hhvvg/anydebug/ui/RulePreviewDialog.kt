package com.hhvvg.anydebug.ui

import android.app.AndroidAppHelper
import android.content.Context
import android.os.Bundle
import com.hhvvg.anydebug.R
import com.hhvvg.anydebug.databinding.LayoutListviewDialogBinding
import com.hhvvg.anydebug.hook.AnyHookFramework.Companion.moduleRes
import com.hhvvg.anydebug.persistent.AppDatabase
import com.hhvvg.anydebug.persistent.ViewRule
import com.hhvvg.anydebug.ui.adapter.RuleItemAdapter
import com.hhvvg.anydebug.ui.adapter.ViewRuleWrapper
import com.hhvvg.anydebug.util.rules
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @author hhvvg
 *
 * Show local persistent rules.
 */
class RulePreviewDialog(context: Context) : BaseDialog(context) {
    override val fitScreen: Boolean
        get() = true

    private val items = mutableListOf<ViewRuleWrapper>()
    private val ruleAdapter = RuleItemAdapter(context, items)
    private lateinit var binding: LayoutListviewDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutListviewDialogBinding.bind(dialogContentView)
        binding.listview.adapter = ruleAdapter
        setTitle(moduleRes.getString(R.string.rules))
        setApplyButton(moduleRes.getString(R.string.ok)) {
            dismiss()
        }
        setDetailsButton(moduleRes.getString(R.string.delete)) {
            deleteSelectedItems()
        }
        loadItems()
    }

    private fun deleteSelectedItems() {
        val selected = mutableListOf<ViewRule>()
        items.removeAll {
            if (it.selected) {
                selected.add(it.rule)
            }
            it.selected
        }
        if (selected.isNotEmpty()) {
            ruleAdapter.notifyDataSetChanged()
        }
        CoroutineScope(context = Dispatchers.IO).launch {
            AppDatabase.viewRuleDao.deleteAll(selected)
        }
    }

    override fun onInflateLayout(): Int = R.layout.layout_listview_dialog

    private fun loadItems() {
        val app = AndroidAppHelper.currentApplication()
        val rules = app.rules
        items.addAll(rules.map { ViewRuleWrapper(it, false) })
        ruleAdapter.notifyDataSetChanged()
    }
}
