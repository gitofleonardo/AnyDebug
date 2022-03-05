package com.hhvvg.anydebug.ui

import android.app.Dialog
import android.os.Bundle
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.hhvvg.anydebug.IGNORE_HOOK
import com.hhvvg.anydebug.R
import com.hhvvg.anydebug.ViewClickWrapper
import com.hhvvg.anydebug.ViewDispatcher
import com.hhvvg.anydebug.data.BaseViewAttrData
import com.hhvvg.anydebug.hook.AnyHookZygote.Companion.moduleRes
import com.hhvvg.anydebug.util.dp
import com.hhvvg.anydebug.util.getOnClickListener
import com.hhvvg.anydebug.util.px

/**
 * @author hhvvg
 *
 * Base dialog for editing basic view attributes.
 */
abstract class BaseAttrDialog<T : BaseViewAttrData>(private val view: View) : Dialog(view.context) {
    private val rootView by lazy {
        val layout = moduleRes.getLayout(R.layout.layout_base_attr_dialog)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(layout, null)
        view.tag = IGNORE_HOOK
        view
    }
    private val cancelButton by lazy {
        rootView.findViewById<Button>(R.id.cancel_button)
    }
    private val applyButton by lazy {
        rootView.findViewById<Button>(R.id.apply_button)
    }
    private val originClickButton by lazy {
        rootView.findViewById<Button>(R.id.origin_click_button)
    }
    private val heightSpinner by lazy {
        rootView.findViewById<Spinner>(R.id.height_spinner)
    }
    private val widthSpinner by lazy {
        rootView.findViewById<Spinner>(R.id.width_spinner)
    }
    private val container by lazy {
        rootView.findViewById<LinearLayout>(R.id.attr_parent_container)
    }
    private val title by lazy {
        rootView.findViewById<TextView>(R.id.title)
    }
    private val parentViewButton by lazy {
        rootView.findViewById<ImageView>(R.id.parent_view_attr_button)
    }

    private val heightValue by lazy {
        rootView.findViewById<EditText>(R.id.height_value)
    }
    private val widthValue by lazy {
        rootView.findViewById<EditText>(R.id.width_value)
    }

    protected var viewWidth: Int = view.layoutParams.width
    protected var viewHeight: Int = view.layoutParams.height

    protected val baseAttrData: BaseViewAttrData
        get() {
            val width = when(viewWidth) {
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT -> {
                    viewWidth
                }
                else -> {
                    viewWidth.px()
                }
            }
            val height = when (viewHeight) {
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT -> {
                    viewHeight
                }
                else -> {
                    viewHeight.px()
                }
            }
            return BaseViewAttrData(width, height)
        }

    protected abstract val attrData: T

    protected open fun onApply(data: T) {
        val baseData = baseAttrData
        val param = view.layoutParams
        param.width = baseData.width
        param.height = baseData.height
        view.layoutParams = param
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(rootView)
        setupButtons()
        setSpecSpinner()
        setupInput()
        setupText()
        title.text = SpannableString(view::class.java.name)
    }

    private fun setupText() {
        rootView.findViewById<TextView>(R.id.width_title).text =
            SpannableString(moduleRes.getString(R.string.width))
        rootView.findViewById<TextView>(R.id.height_title).text =
            SpannableString(moduleRes.getString(R.string.height))
        cancelButton.text = SpannableString(moduleRes.getText(R.string.cancel))
        applyButton.text = SpannableString(moduleRes.getText(R.string.apply))
        originClickButton.text = SpannableString(moduleRes.getText(R.string.perform_origin_click))
    }

    private fun setupButtons() {
        cancelButton.setOnClickListener {
            dismiss()
        }
        applyButton.setOnClickListener {
            onApply(attrData)
            dismiss()
        }
        originClickButton.setOnClickListener {
            val listener = view.getOnClickListener()
            if (listener is ViewClickWrapper) {
                listener.performOriginClick()
            } else {
                listener?.onClick(view)
            }
            dismiss()
        }
        val parentView = view.parent
        if (parentView is View) {
            parentViewButton.isVisible = true
            parentViewButton.setOnClickListener {
                ViewDispatcher.dispatch(parentView)
                dismiss()
            }
            parentViewButton.setImageDrawable(ResourcesCompat.getDrawable(moduleRes, R.drawable.ic_baseline_arrow_drop_up_24, null))
        } else {
            parentViewButton.isVisible = false
        }
    }

    private fun setupInput() {
        heightValue.setText(SpannableString(viewHeight.toString()))
        widthValue.setText(SpannableString(viewWidth.toString()))
        widthValue.addTextChangedListener {
            val width = it?.toString()?.toIntOrNull() ?: return@addTextChangedListener
            viewWidth = width
        }
        heightValue.addTextChangedListener {
            val height = it?.toString()?.toIntOrNull() ?: return@addTextChangedListener
            viewHeight = height
        }
    }

    private fun setSpecSpinner() {
        val specArray = moduleRes.getStringArray(R.array.spec_spinner_values)
        heightSpinner.apply {
            adapter =
                ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, specArray)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    when (position) {
                        0 -> {
                            viewHeight = ViewGroup.LayoutParams.MATCH_PARENT
                            heightValue.isVisible = false
                        }
                        1 -> {
                            viewHeight = ViewGroup.LayoutParams.WRAP_CONTENT
                            heightValue.isVisible = false
                        }
                        2 -> {
                            heightValue.isVisible = true
                            viewHeight = heightValue.text.toString().toIntOrNull() ?: return
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

            }
        }
        widthSpinner.apply {
            adapter =
                ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, specArray)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    when (position) {
                        0 -> {
                            viewWidth = ViewGroup.LayoutParams.MATCH_PARENT
                            widthValue.isVisible = false
                        }
                        1 -> {
                            viewWidth = ViewGroup.LayoutParams.WRAP_CONTENT
                            widthValue.isVisible = false
                        }
                        2 -> {
                            widthValue.isVisible = true
                            viewWidth = widthValue.text.toString().toIntOrNull() ?: return
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }
        when (viewWidth) {
            ViewGroup.LayoutParams.MATCH_PARENT -> {
                widthSpinner.setSelection(0)
            }
            ViewGroup.LayoutParams.WRAP_CONTENT -> {
                widthSpinner.setSelection(1)
            }
            else -> {
                viewWidth = viewWidth.dp()
                widthSpinner.setSelection(2)
            }
        }
        when (viewHeight) {
            ViewGroup.LayoutParams.MATCH_PARENT -> {
                heightSpinner.setSelection(0)
            }
            ViewGroup.LayoutParams.WRAP_CONTENT -> {
                heightSpinner.setSelection(1)
            }
            else -> {
                viewHeight = viewHeight.dp()
                heightSpinner.setSelection(2)
            }
        }
    }

    protected fun addAttrPanelView(view: View) {
        val param = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        container.addView(view, param)
    }
}
