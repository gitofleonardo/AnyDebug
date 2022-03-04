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
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.core.view.isVisible
import com.hhvvg.anydebug.IGNORE_HOOK
import com.hhvvg.anydebug.R
import com.hhvvg.anydebug.data.BaseViewAttrData
import com.hhvvg.anydebug.hook.AnyHookZygote.Companion.moduleRes
import com.hhvvg.anydebug.util.dp
import com.hhvvg.anydebug.util.px

private enum class SpecMode {
    MatchParent,
    WrapContent,
    Value
}

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
    protected var viewWidth: Int = view.layoutParams.width
    protected var viewHeight: Int = view.layoutParams.height

    private val heightValue
        get() = rootView.findViewById<EditText>(R.id.height_value)
    private val widthValue
        get() = rootView.findViewById<EditText>(R.id.width_value)

    private val baseAttrData: BaseViewAttrData
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
        rootView.findViewById<Button>(R.id.cancel_button).apply {
            setOnClickListener {
                dismiss()
            }
            text = SpannableString(moduleRes.getText(R.string.cancel))
        }
        rootView.findViewById<Button>(R.id.apply_button).apply {
            setOnClickListener {
                onApply(attrData)
                dismiss()
            }
            text = SpannableString(moduleRes.getString(R.string.apply))
        }
        rootView.findViewById<TextView>(R.id.width_title).text =
            SpannableString(moduleRes.getString(R.string.width))
        rootView.findViewById<TextView>(R.id.height_title).text =
            SpannableString(moduleRes.getString(R.string.height))

        setSpecSpinner()
    }

    private fun setSpecSpinner() {
        val heightSpinner = rootView.findViewById<Spinner>(R.id.height_spinner)
        val widthSpinner = rootView.findViewById<Spinner>(R.id.width_spinner)

        val specArray = moduleRes.getStringArray(R.array.spec_spinner_values)
        heightValue.setText(SpannableString(viewHeight.toString()))
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
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

            }
        }
        widthValue.setText(SpannableString(viewWidth.toString()))
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

    fun addAttrPanelView(view: View) {
        val param = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        rootView.findViewById<LinearLayout>(R.id.attr_parent_container).addView(view, param)
    }
}
