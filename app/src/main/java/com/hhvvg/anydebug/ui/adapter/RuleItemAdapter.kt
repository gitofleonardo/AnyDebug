package com.hhvvg.anydebug.ui.adapter

import android.content.Context
import android.text.SpannableString
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hhvvg.anydebug.R
import com.hhvvg.anydebug.databinding.LayoutViewRuleItemBinding
import com.hhvvg.anydebug.hook.AnyHookFramework.Companion.moduleRes
import com.hhvvg.anydebug.persistent.ViewRule
import com.hhvvg.anydebug.util.inflater.MyLayoutInflater

/**
 * @author hhvvg
 *
 * Adapter for visiting persistent rules.
 */
class RuleItemAdapter(private val context: Context, private val items: MutableList<ViewRuleWrapper>) : BaseAdapter() {

    override fun getCount(): Int = items.size

    override fun getItem(position: Int): Any = items[position]

    override fun getItemId(position: Int): Long = items[position].rule.ruleId.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rule = items[position]
        val itemView: View = if (convertView == null) {
            val layout = moduleRes.getLayout(R.layout.layout_view_rule_item)
            val inflater = MyLayoutInflater.from(context)
            inflater.inflate(layout, null, false)
        } else {
            convertView
        }
        bindItem(itemView, rule)
        return itemView
    }

    private fun bindItem(itemView: View, item: ViewRuleWrapper) {
        val binding = LayoutViewRuleItemBinding.bind(itemView)
        binding.apply {
            viewName.text = SpannableString(item.rule.className)
            viewIdText.text =
                SpannableString(moduleRes.getString(R.string.view_id_text, item.rule.viewId.toString()))
            parentIdText.text = SpannableString(
                moduleRes.getString(
                    R.string.view_parent_id_text,
                    item.rule.viewParentId.toString()
                )
            )
            viewRuleTypeText.text = SpannableString(
                moduleRes.getString(
                    R.string.view_rule_type,
                    item.rule.ruleType.toString()
                )
            )
            ruleContentText.text =
                SpannableString(moduleRes.getString(R.string.view_rule_content, item.rule.viewRule))
            ruleCheckbox.isChecked = item.selected
            ruleCheckbox.setOnCheckedChangeListener { _, isChecked ->
                item.selected = isChecked
            }
        }
    }
}

data class ViewRuleWrapper(val rule: ViewRule, var selected: Boolean)
