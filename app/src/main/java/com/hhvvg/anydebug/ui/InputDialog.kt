package com.hhvvg.anydebug.ui

import android.content.Context
import android.os.Bundle
import androidx.core.content.res.ResourcesCompat
import com.hhvvg.anydebug.R
import com.hhvvg.anydebug.databinding.LayoutInputDialogBinding
import com.hhvvg.anydebug.hook.AnyHookFramework.Companion.moduleRes

/**
 * @author hhvvg
 */
class InputDialog(
    context: Context,
    private val inputType: Int,
    private val originText: CharSequence? = null,
    private val onTextChangedListener: ((CharSequence) -> Unit)? = null
) : BaseDialog(context) {
    private lateinit var binding: LayoutInputDialogBinding

    override fun onInflateLayout(): Int = R.layout.layout_input_dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutInputDialogBinding.bind(dialogContentView)
        binding.textInput.inputType = inputType
        binding.textInput.setText(originText ?: "")
        binding.textInput.background = ResourcesCompat.getDrawable(moduleRes, R.drawable.input_background, null)

        setApplyButton(moduleRes.getString(R.string.apply)) {
            val result = binding.textInput.text.toString()
            if (originText != result) {
                onTextChangedListener?.invoke(result)
            }
            dismiss()
        }
    }
}
