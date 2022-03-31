package com.hhvvg.anydebug.ui

import android.app.AlertDialog
import android.app.AndroidAppHelper
import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hhvvg.anydebug.R
import com.hhvvg.anydebug.hook.AnyHookFramework.Companion.moduleRes
import com.hhvvg.anydebug.persistent.AppDatabase
import com.hhvvg.anydebug.persistent.ViewRule
import com.hhvvg.anydebug.ui.adapter.RuleItemAdapter
import com.hhvvg.anydebug.util.rules
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

/**
 * @author hhvvg
 *
 * Show local persistent rules.
 */
class RulePreviewDialog(context: Context) : AlertDialog(context) {
    private val items = mutableListOf<ViewRule>()
    private val ruleAdapter = RuleItemAdapter(items)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val rv = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ruleAdapter
        }
        val callback = ItemTouchHelperCallback(ruleAdapter)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(rv)
        setContentView(rv)
        setTitle(moduleRes.getString(R.string.rules))
        loadItems()
    }

    private fun loadItems() {
        val app = AndroidAppHelper.currentApplication()
        val rules = app.rules
        items.addAll(rules)
        ruleAdapter.notifyItemRangeInserted(0, rules.size)
        ruleAdapter.notifyItemRangeChanged(0, rules.size)
    }

    private inner class ItemTouchHelperCallback(private val adapter: RuleItemAdapter) :
        ItemTouchHelper.Callback() {
        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            return makeMovementFlags(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            val item = items.removeAt(position)
            adapter.notifyItemRemoved(position)
            runBlocking(context = Dispatchers.IO) {
                AppDatabase.viewRuleDao.deleteAll(item)
            }
        }

        override fun isLongPressDragEnabled(): Boolean {
            return false
        }

        override fun isItemViewSwipeEnabled(): Boolean {
            return true
        }
    }
}