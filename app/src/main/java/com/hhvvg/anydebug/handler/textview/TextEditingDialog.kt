package com.hhvvg.anydebug.handler.textview

import android.os.Bundle
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.hhvvg.anydebug.R
import com.hhvvg.anydebug.databinding.LayoutTextViewAttrBinding
import com.hhvvg.anydebug.hook.AnyHookFramework.Companion.moduleRes
import com.hhvvg.anydebug.ui.BaseAttributeDialog
import com.hhvvg.anydebug.util.sp

/**
 * @author hhvvg
 *
 * Editing attributes in TextView.
 */
class TextEditingDialog(private val view: TextView) : BaseAttributeDialog(view) {

    private val rootView by lazy {
        val layout = moduleRes.getLayout(R.layout.layout_text_view_attr)
        val inflater = LayoutInflater.from(context)
        inflater.inflate(layout, null)
    }

    private val binding by lazy {
        LayoutTextViewAttrBinding.bind(rootView)
    }

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
        get() = TextViewAttribute(
            binding.editText.text.toString(),
            binding.textMaxLine.text.toString().toIntOrNull() ?: view.maxLines,
            binding.textSizeInput.text.toString().toFloatOrNull() ?: view.textSize.sp()
        )

    override fun onApply() {
        super.onApply()
        val data = attrData
        view.text = SpannableString(data.text)
        view.maxLines = data.maxLine
        view.textSize = data.textSizeInSp
    }
}