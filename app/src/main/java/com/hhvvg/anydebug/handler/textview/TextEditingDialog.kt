package com.hhvvg.anydebug.handler.textview

import android.os.Bundle
import android.text.SpannableString
import android.view.LayoutInflater
import android.widget.TextView
import com.hhvvg.anydebug.R
import com.hhvvg.anydebug.databinding.LayoutTextViewAttrBinding
import com.hhvvg.anydebug.ui.BaseAttrDialog
import com.hhvvg.anydebug.hook.AnyHookZygote.Companion.moduleRes
import com.hhvvg.anydebug.util.sp

/**
 * @author hhvvg
 *
 * Editing attributes in TextView.
 */
class TextEditingDialog(private val view: TextView) : BaseAttrDialog<TextViewAttrData>(view) {
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
        appendAttrPanelView(binding.root)

        binding.editText.hint = SpannableString(moduleRes.getString(R.string.enter_text))
        binding.textMaxLine.hint = SpannableString(moduleRes.getString(R.string.max_line))
        binding.textContentTitle.text = SpannableString(moduleRes.getText(R.string.text_content))
        binding.textMaxLineTitle.text = SpannableString(moduleRes.getText(R.string.max_line))
        binding.textSizeTitle.text = SpannableString(moduleRes.getString(R.string.text_size))
        binding.textSizeInput.hint = SpannableString(moduleRes.getString(R.string.sp))

        binding.editText.setText(SpannableString(view.text))
        binding.textMaxLine.setText(SpannableString(view.maxLines.toString()))
        binding.textSizeInput.setText(SpannableString(view.textSize.sp().toString()))
    }

    override val attrData: TextViewAttrData
        get() {
            return TextViewAttrData(
                baseAttrData,
                binding.editText.text.toString(),
                binding.textMaxLine.text.toString().toIntOrNull() ?: view.maxLines,
                binding.textSizeInput.text.toString().toFloatOrNull() ?: view.textSize.sp()
            )
        }

    override fun onApply(data: TextViewAttrData) {
        super.onApply(data)
        view.text = SpannableString(data.text)
        view.maxLines = data.maxLine
        view.textSize = data.textSizeInSp
    }
}