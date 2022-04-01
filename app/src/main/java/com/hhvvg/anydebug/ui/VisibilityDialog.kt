package com.hhvvg.anydebug.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import com.hhvvg.anydebug.R
import com.hhvvg.anydebug.databinding.LayoutVisibilityDialogBinding
import com.hhvvg.anydebug.hook.AnyHookFramework.Companion.moduleRes

/**
 * @author hhvvg
 */
class VisibilityDialog(
    context: Context,
    private val visibility: Int,
    private val onVisibilityChanged: ((Int, CharSequence) -> Unit)? = null
) : BaseDialog(context) {
    private var newVisibility = visibility
    private lateinit var binding: LayoutVisibilityDialogBinding

    override fun onInflateLayout(): Int = R.layout.layout_visibility_dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutVisibilityDialogBinding.bind(dialogContentView)
        when (visibility) {
            View.VISIBLE -> binding.visibleRadio.isChecked = true
            View.INVISIBLE -> binding.invisibleRadio.isChecked = true
            View.GONE -> binding.goneRadio.isChecked = true
        }
        binding.visibilityGroup.setOnCheckedChangeListener { _, checkedId ->
            newVisibility = when (checkedId) {
                R.id.visible_radio -> View.VISIBLE
                R.id.invisible_radio -> View.INVISIBLE
                R.id.gone_radio -> View.GONE
                else -> visibility
            }
        }
        binding.apply {
            visibleRadio.text = moduleRes.getString(R.string.visible)
            invisibleRadio.text = moduleRes.getString(R.string.invisible)
            goneRadio.text = moduleRes.getString(R.string.gone)
        }
        setApplyButton(moduleRes.getString(R.string.apply)) {
            if (newVisibility != visibility) {
                onVisibilityChanged?.invoke(newVisibility, fromVisibilityToString(newVisibility))
            }
            dismiss()
        }
        setCancelButton(moduleRes.getString(R.string.cancel)) {
            dismiss()
        }
        setTitle(moduleRes.getString(R.string.visibility))
    }

    private fun fromVisibilityToString(visibility: Int): CharSequence {
        return when (visibility) {
            View.VISIBLE -> moduleRes.getString(R.string.visible)
            View.INVISIBLE -> moduleRes.getString(R.string.invisible)
            View.GONE -> moduleRes.getString(R.string.gone)
            else -> ""
        }
    }
}
