package com.hhvvg.anydebug.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import com.hhvvg.anydebug.R
import com.hhvvg.anydebug.databinding.LayoutSpecDialogBinding
import com.hhvvg.anydebug.hook.AnyHookFramework.Companion.moduleRes
import com.hhvvg.anydebug.util.dp
import com.hhvvg.anydebug.util.specOrDp

/**
 * @author hhvvg
 */
class ViewSpecDialog(
    context: Context,
    private var spec: Int,
    private val onSpecChangedListener: ((Int, CharSequence) -> Unit)? = null
) : BaseDialog(context) {
    private var newSpec = spec
    private lateinit var binding: LayoutSpecDialogBinding

    override fun onInflateLayout(): Int = R.layout.layout_spec_dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutSpecDialogBinding.bind(dialogContentView)
        binding.apply {
            rpText.text = moduleRes.getString(R.string.wrap_content)
            mpText.text = moduleRes.getString(R.string.match_parent)
            valueInput.hint = moduleRes.getString(R.string.dp_input_hint)
            valueInput.background = ResourcesCompat.getDrawable(moduleRes, R.drawable.input_background, null)
        }
        when (spec) {
            ViewGroup.LayoutParams.MATCH_PARENT -> binding.mpRadio.isChecked = true
            ViewGroup.LayoutParams.WRAP_CONTENT -> binding.rpRadio.isChecked = true
            else -> {
                binding.valueRadio.isChecked = true
                binding.valueInput.setText(spec.toString())
            }
        }
        binding.specGroup.setOnCheckedChangeListener { _, checkedId ->
            newSpec = when(checkedId) {
                R.id.rp_radio -> ViewGroup.LayoutParams.WRAP_CONTENT
                R.id.mp_radio -> ViewGroup.LayoutParams.MATCH_PARENT
                R.id.value_radio -> binding.valueInput.text.toString().toIntOrNull() ?: 0
                else -> spec
            }
        }
        binding.valueInput.addTextChangedListener {
            newSpec = it.toString().toIntOrNull() ?: 0
        }
        setCancelButton(moduleRes.getString(R.string.cancel)) {
            dismiss()
        }
        setApplyButton(moduleRes.getString(R.string.apply)) {
            if (newSpec != spec) {
                onSpecChangedListener?.invoke(newSpec, fromSpecToString(newSpec))
            }
            dismiss()
        }
    }

    private fun fromSpecToString(spec: Int): CharSequence {
        return when (spec) {
            ViewGroup.LayoutParams.MATCH_PARENT -> moduleRes.getString(R.string.match_parent)
            ViewGroup.LayoutParams.WRAP_CONTENT -> moduleRes.getString(R.string.wrap_content)
            else -> "${spec}dp"
        }
    }
}
