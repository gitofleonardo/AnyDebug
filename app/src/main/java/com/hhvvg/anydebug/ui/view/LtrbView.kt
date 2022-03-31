package com.hhvvg.anydebug.ui.view

import android.content.Context
import android.text.SpannableString
import android.util.AttributeSet
import android.widget.EditText
import android.widget.RelativeLayout
import androidx.core.view.children
import androidx.core.widget.addTextChangedListener
import com.hhvvg.anydebug.R
import com.hhvvg.anydebug.databinding.LayoutLtrbViewBinding
import com.hhvvg.anydebug.hook.AnyHookFramework.Companion.moduleRes
import com.hhvvg.anydebug.observable.ObservableField
import com.hhvvg.anydebug.util.inflater.MyLayoutInflater
import com.hhvvg.anydebug.util.px

/**
 * @author hhvvg
 */
class LtrbView(context: Context, attributeSet: AttributeSet?) : RelativeLayout(context, attributeSet) {
    constructor(context: Context) : this(context, null)

    private val binding by lazy {
        val layout = moduleRes.getLayout(R.layout.layout_ltrb_view)
        val inflater = MyLayoutInflater.from(context)
        val view = inflater.inflate(layout, this, true)
        LayoutLtrbViewBinding.bind(view)
    }

    private var identical: Boolean = false
        set(value) {
            field = value
            binding.ltrbIdenticalCheckbox.isChecked = value
        }
        get() = binding.ltrbIdenticalCheckbox.isChecked

    private val identicalValue = ObservableField(0)

    var leftValue: Int
        get() = (binding.left.text.toString().toIntOrNull() ?: 0).px()
        set(value) = binding.left.setText(value.toString())
    var topValue: Int
        get() = (binding.top.text.toString().toIntOrNull() ?: 0).px()
        set(value) = binding.top.setText(value.toString())
    var rightValue: Int
        get() = (binding.right.text.toString().toIntOrNull() ?: 0).px()
        set(value) = binding.right.setText(value.toString())
    var bottomValue: Int
        get() = (binding.bottom.text.toString().toIntOrNull() ?: 0).px()
        set(value) = binding.bottom.setText(value.toString())

    init {
        setupText()
        setListeners()
    }

    private fun setListeners() {
        binding.ltrbContent.children.forEach { view ->
            if (view is EditText) {
                view.addTextChangedListener { text ->
                    if (!view.hasFocus()) {
                        return@addTextChangedListener
                    }
                    if (identical) {
                        val value = text.toString().toIntOrNull() ?: 0
                        if (value != identicalValue.data) {
                            identicalValue.data = value
                        }
                    }
                }
                identicalValue.observe {
                    view.setText(it.toString())
                    view.setSelection(view.text.length)
                }
            }
        }
    }

    private fun setupText() {
        binding.apply {
            left.hint = moduleRes.getString(R.string.left)
            top.hint = moduleRes.getString(R.string.top)
            right.hint = moduleRes.getString(R.string.right)
            bottom.hint = moduleRes.getString(R.string.bottom)
            ltrbIdenticalCheckbox.text = moduleRes.getString(R.string.identical)
        }
    }

    fun setTitle(title: CharSequence) {
        binding.ltrbTitle.text = SpannableString(title)
    }
}