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
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.core.view.ancestors
import androidx.core.view.children
import androidx.core.view.drawToBitmap
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.hhvvg.anydebug.IGNORE_HOOK
import com.hhvvg.anydebug.R
import com.hhvvg.anydebug.ViewClickWrapper
import com.hhvvg.anydebug.ViewDispatcher
import com.hhvvg.anydebug.data.BaseViewAttrData
import com.hhvvg.anydebug.databinding.LayoutBaseAttrDialogBinding
import com.hhvvg.anydebug.glide.GlideApp
import com.hhvvg.anydebug.hook.AnyHookZygote.Companion.moduleRes
import com.hhvvg.anydebug.ui.adapter.ViewItemListAdapter
import com.hhvvg.anydebug.util.*

/**
 * @author hhvvg
 *
 * Base dialog for editing basic view attributes.
 */
abstract class BaseAttrDialog<T : BaseViewAttrData>(protected val itemView: View) :
    AlertDialog(itemView.context) {
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
            val width = actualViewWidth
            val height = actualViewHeight
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

    private val widthSpinnerAdapter = object : AdapterView.OnItemSelectedListener {
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
            // Do nothing
        }
    }

    private val heightSpinnerAdapter = object : AdapterView.OnItemSelectedListener {
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
            // Do nothing
        }

    }

    /**
     * Temporary view width
     */
    protected var viewWidth: Int = itemView.layoutParams.width

    /**
     * Temporary view height
     */
    protected var viewHeight: Int = itemView.layoutParams.height

    /**
     * Final view width in pixel
     */
    protected val actualViewWidth: Int
        get() = when (viewWidth) {
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT -> {
                viewWidth
            }
            else -> {
                viewWidth.px()
            }
        }

    /**
     * Final view height in pixel
     */
    protected val actualViewHeight: Int
        get() = when (viewHeight) {
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT -> {
                viewHeight
            }
            else -> {
                viewHeight.px()
            }
        }

    /**
     * View attributes data holder
     */
    protected abstract val attrData: T

    /**
     * Invoked when clicking the confirm button.
     * Child view should override this to apply their own data
     */
    @CallSuper
    protected open fun onApply(data: T) {
        val baseData = baseAttrData
        val param = itemView.layoutParams
        param.width = baseData.width
        param.height = baseData.height
        if (param is ViewGroup.MarginLayoutParams) {
            param.setMargins(data.marginLeft, data.marginTop, data.marginRight, data.marginBottom)
        }
        itemView.layoutParams = param
        itemView.setPadding(
            data.paddingLeft,
            data.paddingTop,
            data.paddingRight,
            data.paddingBottom
        )
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
        setupChildrenParentButton()
        renderPreview()
    }

    /**
     * Render bitmap and then load into the dialog to show preview of current {@link #itemView}.
     */
    protected fun renderPreview() {
        if (!itemView.isLaidOut) {
            return
        }
        binding.previewImage.setImageBitmap(itemView.drawToBitmap())
    }

    override fun setTitle(title: CharSequence?) {
        binding.title.text = SpannableString(title)
    }

    private fun setupChildrenParentButton() {
        val ancestors = findAncestors()
        val children = findChildren()
        binding.childrenButton.setOnClickListener {
            showViewItemDialog(moduleRes.getString(R.string.select_children), children)
        }
        binding.parentButton.setOnClickListener {
            showViewItemDialog(moduleRes.getString(R.string.select_parent), ancestors)
        }
    }

    private fun showViewItemDialog(title: String, views: List<View>) {
        GlideApp.get(context).clearMemory()
        Builder(context)
            .setTitle(title)
            .setAdapter(ViewItemListAdapter(views)) { d, which ->
                d.dismiss()
                val selected = views[which]
                ViewDispatcher.dispatch(selected)
                dismiss()
            }
            .create()
            .show()
    }

    /**
     * Set custom text here, since you can just simply put string id in xml layout file.
     */
    private fun setupText() {
        setTitle(itemView::class.java.name)

        binding.widthTitle.text =
            SpannableString(moduleRes.getString(R.string.width))
        binding.heightTitle.text =
            SpannableString(moduleRes.getString(R.string.height))
        binding.cancelButton.text = SpannableString(moduleRes.getText(R.string.cancel))
        binding.applyButton.text = SpannableString(moduleRes.getText(R.string.apply))
        binding.originClickButton.text =
            SpannableString(moduleRes.getText(R.string.perform_origin_click))
        binding.parentSpinnerTitle.text = SpannableString(moduleRes.getString(R.string.parent))
        binding.childrenSpinnerTitle.text = SpannableString(moduleRes.getString(R.string.children))
        binding.showLayoutBoundsSwitch.text =
            SpannableString(moduleRes.getString(R.string.show_global_layout_bounds))
        binding.ignoreEmptyVgSwitch.text =
            SpannableString(moduleRes.getString(R.string.force_clickable))
    }

    private fun setupButtons() {
        binding.cancelButton.setOnClickListener {
            dismiss()
        }
        binding.applyButton.setOnClickListener {
            onApply(attrData)
            dismiss()
        }
        val listener = itemView.getOnClickListener()
        if (listener == null || (listener is ViewClickWrapper && listener.originListener == null)) {
            binding.originClickButton.isVisible = false
        } else {
            binding.originClickButton.setOnClickListener {
                if (listener is ViewClickWrapper) {
                    listener.performOriginClick()
                } else {
                    listener.onClick(itemView)
                }
                dismiss()
            }
        }
        val app = AndroidAppHelper.currentApplication()
        val showBoundsNow = app.getInjectedField(APP_FIELD_SHOW_BOUNDS, false) ?: false
        binding.showLayoutBoundsSwitch.isChecked = showBoundsNow
        binding.showLayoutBoundsSwitch.setOnCheckedChangeListener { _, isChecked ->
            app.injectField(APP_FIELD_SHOW_BOUNDS, isChecked)
            itemView.rootView.drawLayoutBounds(isChecked, true)
            renderPreview()
            GlideApp.get(context).clearMemory()
        }
        val ignoreEmptyVg = app.getInjectedField(APP_FIELD_FORCE_CLICKABLE, false) ?: false
        binding.ignoreEmptyVgSwitch.isChecked = ignoreEmptyVg
        binding.ignoreEmptyVgSwitch.setOnCheckedChangeListener { _, isChecked ->
            app.injectField(APP_FIELD_FORCE_CLICKABLE, isChecked)
            itemView.rootView.setGlobalHookClick(
                enabled = true,
                traversalChildren = true,
                forceClickable = isChecked
            )
        }
    }

    /**
     * Find child views of the {@link #itemView}
     */
    protected fun findChildren(): List<View> {
        if (itemView !is ViewGroup || itemView.childCount <= 0) {
            return emptyList()
        }
        return itemView.children.toList()
    }

    /**
     * Find parent views of the {@link #itemView}
     */
    protected fun findAncestors(): List<ViewGroup> {
        val ancestor = itemView.ancestors
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
            onItemSelectedListener = heightSpinnerAdapter
        }
        binding.widthSpinner.apply {
            adapter =
                ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, specArray)
            onItemSelectedListener = widthSpinnerAdapter
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
        val margin = itemView.layoutParams
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
        binding.paddingLeft.setText(SpannableString(itemView.paddingLeft.dp().toString()))
        binding.paddingTop.setText(SpannableString(itemView.paddingTop.dp().toString()))
        binding.paddingBottom.setText(SpannableString(itemView.paddingBottom.dp().toString()))
        binding.paddingRight.setText(SpannableString(itemView.paddingRight.dp().toString()))
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

    /**
     * Add additional attributes settings panel.
     * If you implement this dialog, you should add your customized settings view through this method.
     *
     * @param view Additional settings view
     */
    protected fun appendAttrPanelView(view: View) {
        val param = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        binding.attrParentContainer.addView(view, param)
    }

    /**
     * Add additional attributes settings panel.
     * If you implement this dialog, you should add your customized settings view through this method.
     *
     * @param resId Layout resource id
     */
    protected fun appendAttrPanelView(@LayoutRes resId: Int): View {
        val layout = moduleRes.getLayout(resId)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(layout, null, false)
        appendAttrPanelView(view)
        return view
    }

    /**
     * Show this dialog, additionally setup soft input flags.
     */
    override fun show() {
        super.show()
        window?.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        }
    }
}

