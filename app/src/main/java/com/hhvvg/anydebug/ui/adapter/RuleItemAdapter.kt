package com.hhvvg.anydebug.ui.adapter

import android.text.SpannableString
import android.view.View
import android.view.ViewGroup
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
class RuleItemAdapter(private val items: MutableList<ViewRule>) : RecyclerView.Adapter<Holder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val layout = moduleRes.getLayout(R.layout.layout_view_rule_item)
        val inflater = MyLayoutInflater.from(parent.context)
        val view = inflater.inflate(layout, null, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = items[position]
        holder.bindItem(item)
    }

    override fun getItemCount(): Int = items.size
}

class Holder(view: View) : RecyclerView.ViewHolder(view) {
    fun bindItem(item: ViewRule) {
        val binding = LayoutViewRuleItemBinding.bind(itemView)
        binding.apply {
            viewName.text = SpannableString(item.className)
            viewIdText.text =
                SpannableString(moduleRes.getString(R.string.view_id_text, item.viewId.toString()))
            parentIdText.text = SpannableString(
                moduleRes.getString(
                    R.string.view_parent_id_text,
                    item.viewParentId.toString()
                )
            )
            viewRuleTypeText.text = SpannableString(
                moduleRes.getString(
                    R.string.view_rule_type,
                    item.ruleType.toString()
                )
            )
            ruleContentText.text =
                SpannableString(moduleRes.getString(R.string.view_rule_content, item.viewRule))
        }
    }
}