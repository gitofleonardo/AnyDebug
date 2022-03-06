package com.hhvvg.anydebug.ui

import android.app.AlertDialog
import android.app.AndroidAppHelper
import android.os.Bundle
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.view.ancestors
import androidx.core.view.children
import androidx.core.view.drawToBitmap
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.bumptech.glide.Glide
import com.hhvvg.anydebug.IGNORE_HOOK
import com.hhvvg.anydebug.R
import com.hhvvg.anydebug.ViewClickWrapper
import com.hhvvg.anydebug.ViewDispatcher
import com.hhvvg.anydebug.data.BaseViewAttrData
import com.hhvvg.anydebug.databinding.LayoutBaseAttrDialogBinding
import com.hhvvg.anydebug.hook.AnyHookZygote.Companion.moduleRes
import com.hhvvg.anydebug.ui.adapter.ViewItemListAdapter
import com.hhvvg.anydebug.util.APP_FIELD_FORCE_CLICKABLE
import com.hhvvg.anydebug.util.APP_FIELD_SHOW_BOUNDS
import com.hhvvg.anydebug.util.dp
import com.hhvvg.anydebug.util.drawLayoutBounds
import com.hhvvg.anydebug.util.getInjectedField
import com.hhvvg.anydebug.util.getOnClickListener
import com.hhvvg.anydebug.util.glide.GlideApp
import com.hhvvg.anydebug.util.injectField
import com.hhvvg.anydebug.util.px
import com.hhvvg.anydebug.util.setGlobalHookClick

/**
 * @author hhvvg
 *
 * Base dialog for editing basic view attributes.
 */
abstract class BaseAttrDialog<T : BaseViewAttrData>(private val view: View) :
    AlertDialog(view.context) {
    private val binding by lazy {
        val layout = moduleRes.getLayout(R.layout.layout_base_attr_dialog)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(layout, null)
        view.tag = IGNORE_HOOK
        LayoutBaseAttrDialogBinding.bind(view)
    }

    /**
     * This is the basic view attributes holder.
     */
    protected val baseAttrData: BaseViewAttrData
        get() {
            val width = when (viewWidth) {
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
            val marginLeft = binding.marginLeft.text.toString().toIntOrNull() ?: 0
            val marginTop = binding.marginTop.text.toString().toIntOrNull() ?: 0
            val marginBottom = binding.marginBottom.text.toString().toIntOrNull() ?: 0
            val marginRight = binding.marginRight.text.toString().toIntOrNull() ?: 0
            val paddingTop = binding.paddingTop.text.toString().toIntOrNull() ?: 0
            val paddingLeft = binding.paddingLeft.text.toString().toIntOrNull() ?: 0
            val paddingBottom = binding.paddingBottom.text.toString().toIntOrNull() ?: 0
            val paddingRight = binding.paddingRight.text.toString().toIntOrNull() ?: 0
            return BaseViewAttrData(
                width,
                height,
                paddingLeft.px(),
                paddingTop.px(),
                paddingBottom.px(),
                paddingRight.px(),
                marginLeft.px(),
                marginTop.px(),
                marginBottom.px(),
                marginRight.px()
            )
        }

    protected var viewWidth: Int = view.layoutParams.width
    protected var viewHeight: Int = view.layoutParams.height

    protected abstract val attrData: T

    protected open fun onApply(data: T) {
        val baseData = baseAttrData
        val param = view.layoutParams
        param.width = baseData.width
        param.height = baseData.height
        if (param is ViewGroup.MarginLayoutParams) {
            param.setMargins(data.marginLeft, data.marginTop, data.marginRight, data.marginBottom)
        }
        view.layoutParams = param
        view.setPadding(data.paddingLeft, data.paddingTop, data.paddingRight, data.paddingBottom)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupButtons()
        setSpecSpinner()
        setupInput()
        setupText()
        setupMargin()
        setupPadding()
        setupChildrenParentSpinner()
        renderPreview()
        setTitle(view::class.java.name)
    }

    private fun renderPreview() {
        if (!view.isLaidOut) {
            return
        }
        binding.previewImage.setImageBitmap(view.drawToBitmap())
    }

    override fun setTitle(title: CharSequence?) {
        binding.title.text = SpannableString(title)
    }

    private fun setupChildrenParentSpinner() {
        val ancestors = findAncestors()
        val children = findChildren()
        binding.parentSpinnerTitle.text = SpannableString(moduleRes.getString(R.string.parent))
        binding.childrenSpinnerTitle.text = SpannableString(moduleRes.getString(R.string.children))
        binding.childrenButton.setOnClickListener {
            val dialog = Builder(context)
                .setTitle(moduleRes.getString(R.string.select_children))
                .setAdapter(ViewItemListAdapter(children)) { d, which ->
                    d.dismiss()
                    val selected = children[which]
                    ViewDispatcher.dispatch(selected)
                    dismiss()
                }
                .create()
            dialog.show()
        }
        binding.parentButton.setOnClickListener {
            val dialog = Builder(context)
                .setTitle(moduleRes.getString(R.string.select_parent))
                .setAdapter(ViewItemListAdapter(ancestors)) { d, which ->
                    d.dismiss()
                    val selected = ancestors[which]
                    ViewDispatcher.dispatch(selected)
                    dismiss()
                }
                .create()
            dialog.show()
        }
    }

    private fun setupText() {
        binding.widthTitle.text =
            SpannableString(moduleRes.getString(R.string.width))
        binding.heightTitle.text =
            SpannableString(moduleRes.getString(R.string.height))
        binding.cancelButton.text = SpannableString(moduleRes.getText(R.string.cancel))
        binding.applyButton.text = SpannableString(moduleRes.getText(R.string.apply))
        binding.originClickButton.text =
            SpannableString(moduleRes.getText(R.string.perform_origin_click))
    }

    private fun setupButtons() {
        binding.cancelButton.setOnClickListener {
            dismiss()
        }
        binding.applyButton.setOnClickListener {
            onApply(attrData)
            dismiss()
        }
        val listener = view.getOnClickListener()
        if (listener == null || (listener is ViewClickWrapper && listener.originListener == null)) {
            binding.originClickButton.isVisible = false
        } else {
            binding.originClickButton.setOnClickListener {
                if (listener is ViewClickWrapper) {
                    listener.performOriginClick()
                } else {
                    listener.onClick(view)
                }
                dismiss()
            }
        }
        val app = AndroidAppHelper.currentApplication()
        val showBoundsNow = app.getInjectedField(APP_FIELD_SHOW_BOUNDS, false) ?: false
        binding.showLayoutBoundsSwitch.isChecked = showBoundsNow
        binding.showLayoutBoundsSwitch.text =
            SpannableString(moduleRes.getString(R.string.show_global_layout_bounds))
        binding.showLayoutBoundsSwitch.setOnCheckedChangeListener { _, isChecked ->
            app.injectField(APP_FIELD_SHOW_BOUNDS, isChecked)
            view.rootView.drawLayoutBounds(isChecked, true)
            renderPreview()
        }

        val ignoreEmptyVg = app.getInjectedField(APP_FIELD_FORCE_CLICKABLE, false) ?: false
        binding.ignoreEmptyVgSwitch.isChecked = ignoreEmptyVg
        binding.ignoreEmptyVgSwitch.text =
            SpannableString(moduleRes.getString(R.string.force_clickable))
        binding.ignoreEmptyVgSwitch.setOnCheckedChangeListener { _, isChecked ->
            app.injectField(APP_FIELD_FORCE_CLICKABLE, isChecked)
            view.rootView.setGlobalHookClick(
                enabled = true,
                traversalChildren = true,
                forceClickable = isChecked
            )
        }
    }

    private fun findChildren(): List<View> {
        if (view !is ViewGroup || view.childCount <= 0) {
            return emptyList()
        }
        return view.children.toList()
    }

    private fun findAncestors(): List<ViewGroup> {
        val ancestor = view.ancestors
        val result = ArrayList<ViewGroup>()
        for (a in ancestor) {
            if (a is ViewGroup) {
                result.add(a)
            }
        }
        return result
    }

    private fun setupInput() {
        binding.heightValue.setText(SpannableString(viewHeight.toString()))
        binding.widthValue.setText(SpannableString(viewWidth.toString()))
        binding.widthValue.addTextChangedListener {
            val width = it?.toString()?.toIntOrNull() ?: return@addTextChangedListener
            viewWidth = width
        }
        binding.heightValue.addTextChangedListener {
            val height = it?.toString()?.toIntOrNull() ?: return@addTextChangedListener
            viewHeight = height
        }
    }

    private fun setSpecSpinner() {
        val specArray = moduleRes.getStringArray(R.array.spec_spinner_values)
        binding.heightSpinner.apply {
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
                            binding.heightValue.isVisible = false
                        }
                        1 -> {
                            viewHeight = ViewGroup.LayoutParams.WRAP_CONTENT
                            binding.heightValue.isVisible = false
                        }
                        2 -> {
                            binding.heightValue.isVisible = true
                            viewHeight = binding.heightValue.text.toString().toIntOrNull() ?: return
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

            }
        }
        binding.widthSpinner.apply {
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
                            binding.widthValue.isVisible = false
                        }
                        1 -> {
                            viewWidth = ViewGroup.LayoutParams.WRAP_CONTENT
                            binding.widthValue.isVisible = false
                        }
                        2 -> {
                            binding.widthValue.isVisible = true
                            viewWidth = binding.widthValue.text.toString().toIntOrNull() ?: return
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }
        when (viewWidth) {
            ViewGroup.LayoutParams.MATCH_PARENT -> {
                binding.widthSpinner.setSelection(0)
            }
            ViewGroup.LayoutParams.WRAP_CONTENT -> {
                binding.widthSpinner.setSelection(1)
            }
            else -> {
                viewWidth = viewWidth.dp()
                binding.widthSpinner.setSelection(2)
            }
        }
        when (viewHeight) {
            ViewGroup.LayoutParams.MATCH_PARENT -> {
                binding.heightSpinner.setSelection(0)
            }
            ViewGroup.LayoutParams.WRAP_CONTENT -> {
                binding.heightSpinner.setSelection(1)
            }
            else -> {
                viewHeight = viewHeight.dp()
                binding.heightSpinner.setSelection(2)
            }
        }
    }

    private fun setupMargin() {
        val margin = view.layoutParams
        if (margin !is ViewGroup.MarginLayoutParams) {
            binding.marginValues.isVisible = false
            return
        }
        binding.marginLeft.setText(SpannableString(margin.leftMargin.dp().toString()))
        binding.marginTop.setText(SpannableString(margin.topMargin.dp().toString()))
        binding.marginBottom.setText(SpannableString(margin.bottomMargin.dp().toString()))
        binding.marginRight.setText(SpannableString(margin.rightMargin.dp().toString()))
        binding.marginIdenticalCheckbox.text =
            SpannableString(moduleRes.getString(R.string.identical))

        val children = binding.marginInputs.children
        for (child in children) {
            if (child !is EditText) {
                continue
            }
            child.addTextChangedListener {
                if (!child.isFocused || !binding.marginIdenticalCheckbox.isChecked) {
                    return@addTextChangedListener
                }
                setMarginValues(it.toString())
            }
        }
    }

    private fun setMarginValues(value: String) {
        val children = binding.marginInputs.children
        for (child in children) {
            if (child !is EditText) {
                continue
            }
            if (child.text.toString() == value) {
                continue
            }
            child.setText(SpannableString(value))
        }
    }

    private fun setupPadding() {
        binding.paddingLeft.setText(SpannableString(view.paddingLeft.dp().toString()))
        binding.paddingTop.setText(SpannableString(view.paddingTop.dp().toString()))
        binding.paddingBottom.setText(SpannableString(view.paddingBottom.dp().toString()))
        binding.paddingRight.setText(SpannableString(view.paddingRight.dp().toString()))
        binding.paddingIdenticalCheckbox.text =
            SpannableString(moduleRes.getString(R.string.identical))

        val children = binding.paddingInputs.children
        for (child in children) {
            if (child !is EditText) {
                continue
            }
            child.addTextChangedListener {
                if (!child.isFocused || !binding.paddingIdenticalCheckbox.isChecked) {
                    return@addTextChangedListener
                }
                setPaddingValues(it.toString())
            }
        }
    }

    private fun setPaddingValues(value: String) {
        val children = binding.paddingInputs.children
        for (child in children) {
            if (child !is EditText) {
                continue
            }
            if (child.text.toString() == value) {
                continue
            }
            child.setText(SpannableString(value))
        }
    }

    protected fun addAttrPanelView(view: View) {
        val param = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        binding.attrParentContainer.addView(view, param)
    }

    override fun show() {
        super.show()
        window?.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        }
    }
}

