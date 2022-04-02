package com.hhvvg.anydebug.ui

import android.os.Bundle
import android.text.InputType
import android.text.SpannableString
import android.view.View
import android.widget.TextView
import com.hhvvg.anydebug.R
import com.hhvvg.anydebug.databinding.LayoutTextViewAttrBinding
import com.hhvvg.anydebug.handler.textview.TextViewAttribute
import com.hhvvg.anydebug.hook.AnyHookFramework.Companion.moduleRes
import com.hhvvg.anydebug.persistent.AppDatabase
import com.hhvvg.anydebug.persistent.RuleType
import com.hhvvg.anydebug.persistent.ViewRule
import com.hhvvg.anydebug.persistent.ViewRuleDao
import com.hhvvg.anydebug.util.inflater.MyLayoutInflater
import com.hhvvg.anydebug.util.isPersistentEnabled
import com.hhvvg.anydebug.util.sp
import com.hhvvg.anydebug.util.viewId
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

    private var currentText: CharSequence = view.text.toString()
    private var currentMaxLine = view.maxLines
    private var currentTextSize = view.textSize.sp()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appendAttributePanelView(binding.root)
        setListeners()
    }

    private fun setListeners() {
        binding.textButton.setOnClickListener {
            val dialog = InputDialog(context, InputType.TYPE_CLASS_TEXT, currentText) {
                currentText = it
            }
            dialog.show()
            dialog.setTitle(moduleRes.getString(R.string.enter_text))
        }
        binding.maxLineButton.setOnClickListener {
            val dialog = InputDialog(context, InputType.TYPE_CLASS_NUMBER, currentMaxLine.toString()) {
                currentMaxLine = it.toString().toIntOrNull() ?: view.maxLines
                binding.maxLineButton.subtitle = currentMaxLine.toString()
            }
            dialog.show()
            dialog.setTitle(moduleRes.getString(R.string.max_line))
        }
        binding.textSizeButton.setOnClickListener {
            val dialog = InputDialog(context, InputType.TYPE_CLASS_NUMBER, currentTextSize.toString()) {
                currentTextSize = it.toString().toFloatOrNull() ?: view.textSize
                binding.textSizeButton.subtitle = currentTextSize.toString()
            }
            dialog.show()
            dialog.setTitle(moduleRes.getString(R.string.text_size))
        }
    }

    override fun onLoadViewAttributes(view: View) {
        super.onLoadViewAttributes(view)
        if (view !is TextView) {
            return
        }
        binding.maxLineButton.subtitle = currentMaxLine.toString()
        binding.textSizeButton.subtitle = currentTextSize.toString()
    }

    override fun onSetupDialogText() {
        super.onSetupDialogText()
        binding.textButton.title = getString(R.string.text_content)
        binding.maxLineButton.title = getString(R.string.max_line)
        binding.textSizeButton.title = getString(R.string.text_size)
    }

    private val attrData: TextViewAttribute
        get() {
            val text = if (currentText == view.text.toString()) {
                null
            } else {
                currentText.toString()
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
            runBlocking(context = Dispatchers.IO) {
                val dao = AppDatabase.viewRuleDao
                val rules = makeRules(dao, data)
                dao.insertAll(rules)
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

    private fun makeRules(dao: ViewRuleDao,data: TextViewAttribute): List<ViewRule> {
        val originTextRule = dao.findByIdAndParentIdAndType(view.id, view.parent.viewId(), RuleType.Text)
        val rules = mutableListOf<ViewRule>()
        val parent = itemView.parent
        val parentId = if (parent is View) {
            parent.id
        } else {
            View.NO_ID
        }
        data.text?.let {
            val originText = originTextRule?.originViewContent ?: view.text.toString()
            val textRule = ViewRule(
                className = itemView::class.java.name,
                viewParentId = parentId,
                viewId = itemView.id,
                ruleType = RuleType.Text,
                viewRule = it,
                originViewContent = originText
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
                originViewContent = view.textSize.toString()
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
                originViewContent = view.maxLines.toString()
            )
            rules.add(textMaxLineRule)
        }
        return rules
    }
}