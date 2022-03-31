package com.hhvvg.anydebug.handler.textview

import android.os.Bundle
import android.text.SpannableString
import android.view.View
import android.widget.TextView
import com.hhvvg.anydebug.R
import com.hhvvg.anydebug.databinding.LayoutTextViewAttrBinding
import com.hhvvg.anydebug.hook.AnyHookFramework.Companion.moduleRes
import com.hhvvg.anydebug.persistent.AppDatabase
import com.hhvvg.anydebug.persistent.RuleType
import com.hhvvg.anydebug.persistent.ViewRule
import com.hhvvg.anydebug.ui.BaseAttributeDialog
import com.hhvvg.anydebug.util.inflater.MyLayoutInflater
import com.hhvvg.anydebug.util.isPersistentEnabled
import com.hhvvg.anydebug.util.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

/**
 * @author hhvvg
 *
 * Editing attributes in TextView.
 */
class TextEditingDialog(private val view: TextView) : BaseAttributeDialog(view) {

    private val rootView by lazy {
        val layout = moduleRes.getLayout(R.layout.layout_text_view_attr)
        val inflater = MyLayoutInflater.from(context)
        inflater.inflate(layout, null)
    }

    private val binding by lazy {
        LayoutTextViewAttrBinding.bind(rootView)
    }

    private val currentText
        get() = binding.editText.text.toString()
    private val currentMaxLine
        get() = binding.textMaxLine.text.toString().toIntOrNull()
    private val currentTextSize
        get() = binding.textSizeInput.text.toString().toFloatOrNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appendAttributePanelView(binding.root)
    }

    override fun onLoadViewAttributes(view: View) {
        super.onLoadViewAttributes(view)
        if (view !is TextView) {
            return
        }
        binding.editText.setText(SpannableString(view.text))
        binding.textMaxLine.setText(SpannableString(view.maxLines.toString()))
        binding.textSizeInput.setText(SpannableString(view.textSize.sp().toString()))
    }

    override fun onSetupDialogText() {
        super.onSetupDialogText()
        binding.editText.hint = getString(R.string.enter_text)
        binding.textMaxLine.hint = getString(R.string.max_line)
        binding.textContentTitle.text = getString(R.string.text_content)
        binding.textMaxLineTitle.text = getString(R.string.max_line)
        binding.textSizeTitle.text = getString(R.string.text_size)
        binding.textSizeInput.hint = getString(R.string.sp)
    }

    private val attrData: TextViewAttribute
        get() {
            val text = if (currentText == view.text.toString()) {
                null
            } else {
                currentText
            }
            val maxLines = if (currentMaxLine == view.maxLines) {
                null
            } else {
                currentMaxLine
            }
            val textSize = if (currentTextSize == view.textSize.sp()) {
                null
            } else {
                currentTextSize
            }
            return TextViewAttribute(
                text,
                maxLines,
                textSize
            )
        }

    override fun onApply() {
        super.onApply()
        val data = attrData
        val persistent = application.isPersistentEnabled
        if (persistent) {
            val rules = makeRules(data)
            runBlocking(context = Dispatchers.IO) {
                AppDatabase.viewRuleDao.insertAll(rules)
            }
        }

        data.text?.let {
            view.text = SpannableString(it)
        }
        data.maxLine?.let {
            view.maxLines = it
        }
        data.textSizeInSp?.let {
            view.textSize = it
        }
    }

    private fun makeRules(data: TextViewAttribute): List<ViewRule> {
        val rules = mutableListOf<ViewRule>()
        val parent = itemView.parent
        val parentId = if (parent is View) {
            parent.id
        } else {
            View.NO_ID
        }
        data.text?.let {
            val textRule = ViewRule(
                className = itemView::class.java.name,
                viewParentId = parentId,
                viewId = itemView.id,
                ruleType = RuleType.Text,
                viewRule = it,
            )
            rules.add(textRule)
        }
        data.textSizeInSp?.let {
            val textSizeRule = ViewRule(
                className = itemView::class.java.name,
                viewParentId = parentId,
                viewId = itemView.id,
                ruleType = RuleType.TextSize,
                viewRule = it.toString(),
            )
            rules.add(textSizeRule)
        }
        data.maxLine?.let {
            val textMaxLineRule = ViewRule(
                className = itemView::class.java.name,
                viewParentId = parentId,
                viewId = itemView.id,
                ruleType = RuleType.TextMaxLine,
                viewRule = it.toString(),
            )
            rules.add(textMaxLineRule)
        }
        return rules
    }
}