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
import android.widget.LinearLayout
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.view.ancestors
import androidx.core.view.children
import androidx.core.view.drawToBitmap
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.hhvvg.anydebug.IGNORE_HOOK
import com.hhvvg.anydebug.R
import com.hhvvg.anydebug.ViewClickWrapper
import com.hhvvg.anydebug.ViewDispatcher
import com.hhvvg.anydebug.databinding.LayoutBaseAttributeDialogBinding
import com.hhvvg.anydebug.databinding.LayoutImageBinding
import com.hhvvg.anydebug.glide.GlideApp
import com.hhvvg.anydebug.hook.AnyHookZygote.Companion.moduleRes
import com.hhvvg.anydebug.ui.adapter.ViewItemListAdapter
import com.hhvvg.anydebug.util.*

/**
 * @author hhvvg
 *
 * Base dialog for editing basic view attributes.
 */
open class BaseAttributeDialog(protected val itemView: View) : AlertDialog(itemView.context) {
    private val viewModel = BaseViewModel()
    private val binding by lazy {
        val layout = moduleRes.getLayout(R.layout.layout_base_attribute_dialog)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(layout, null)
        view.tag = IGNORE_HOOK
        LayoutBaseAttributeDialogBinding.bind(view)
    }
    private val application by lazy {
        AndroidAppHelper.currentApplication()
    }
    private val viewSpecSelectionMap by lazy {
        val m = HashMap<Int, Int>()
        m[ViewGroup.LayoutParams.MATCH_PARENT] = 0
        m[ViewGroup.LayoutParams.WRAP_CONTENT] = 1
        m
    }
    private val viewVisibilitySelectionMap by lazy {
        val m = HashMap<Int, Int>()
        m[View.VISIBLE] = 0
        m[View.INVISIBLE] = 1
        m[View.GONE] = 2
        m
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        onSetObserves()
        setSpinners()
        onLoadViewAttributes(itemView)
        setTitle(itemView.javaClass.name)
        onSetupDialogText()
        renderPreview()
        setListeners()
    }

    private fun setSpinners() {
        binding.visibilitySpinner.adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            moduleRes.getStringArray(R.array.visibility)
        )
        binding.visibilitySpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                    viewModel.visibility = visibilityArray[pos]
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    // Do nothing
                }

            }
        binding.widthSpinner.adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            moduleRes.getStringArray(R.array.spec_spinner_values)
        )
        binding.widthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                when (pos) {
                    0 -> {
                        viewModel.width = ViewGroup.LayoutParams.MATCH_PARENT
                        viewModel.widthInputVisible.data = false
                    }
                    1 -> {
                        viewModel.width = ViewGroup.LayoutParams.WRAP_CONTENT
                        viewModel.widthInputVisible.data = false
                    }
                    2 -> {
                        viewModel.widthInputVisible.data = true
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                // Do nothing
            }

        }
        binding.heightSpinner.adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            moduleRes.getStringArray(R.array.spec_spinner_values)
        )
        binding.heightSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                when (pos) {
                    0 -> {
                        viewModel.height = ViewGroup.LayoutParams.MATCH_PARENT
                        viewModel.heightInputVisible.data = false
                    }
                    1 -> {
                        viewModel.height = ViewGroup.LayoutParams.WRAP_CONTENT
                        viewModel.heightInputVisible.data = false
                    }
                    2 -> {
                        viewModel.heightInputVisible.data = true
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    /**
     * Invoked to setup text, since string in layout cannot be directly set as an id.
     */
    @CallSuper
    protected open fun onSetupDialogText() {
        binding.showLayoutBoundsSwitch.text = getString(R.string.show_global_layout_bounds)
        binding.forceWidgetsClickable.text = getString(R.string.force_clickable)
        binding.ancestorButton.text = getString(R.string.ancestors)
        binding.childrenButton.text = getString(R.string.children)
        binding.visibilitySpinnerTitle.text = getString(R.string.visibility)
        binding.widthTitle.text = getString(R.string.width)
        binding.heightTitle.text = getString(R.string.height)
        binding.marginTitle.text = getString(R.string.margin_title)
        binding.paddingTitle.text = getString(R.string.padding_title)
        binding.marginLeft.hint = getString(R.string.left)
        binding.marginTop.hint = getString(R.string.top)
        binding.marginRight.hint = getString(R.string.right)
        binding.marginBottom.hint = getString(R.string.bottom)
        binding.paddingLeft.hint = getString(R.string.left)
        binding.paddingTop.hint = getString(R.string.top)
        binding.paddingRight.hint = getString(R.string.right)
        binding.paddingBottom.hint = getString(R.string.bottom)
        binding.heightInput.hint = getString(R.string.height)
        binding.widthInput.hint = getString(R.string.width)
        binding.paddingIdenticalCheckbox.text = getString(R.string.identical)
        binding.marginIdenticalCheckbox.text = getString(R.string.identical)
        binding.originClickButton.text = getString(R.string.perform_origin_click)
        binding.cancelButton.text = getString(R.string.cancel)
        binding.applyButton.text = getString(R.string.apply)
    }

    @CallSuper
    protected open fun onSetObserves() {
        viewModel.heightInputVisible.observe {
            binding.heightInput.isVisible = it
        }
        viewModel.widthInputVisible.observe {
            binding.widthInput.isVisible = it
        }
        viewModel.marginAvailable.observe {
            binding.marginTitle.isVisible = it
            binding.marginInputs.isVisible = it
        }
        viewModel.margin.observe {
            viewModel.marginTop.data = it
            viewModel.marginLeft.data = it
            viewModel.marginRight.data = it
            viewModel.marginBottom.data = it
        }
        viewModel.padding.observe {
            viewModel.paddingTop.data = it
            viewModel.paddingLeft.data = it
            viewModel.paddingRight.data = it
            viewModel.paddingBottom.data = it
        }
        viewModel.marginTop.observe {
            binding.marginTop.apply {
                if (!hasFocus()) {
                    setText(it.toString())
                }
            }
        }
        viewModel.marginLeft.observe {
            binding.marginLeft.apply {
                if (!hasFocus()) {
                    setText(it.toString())
                }
            }
        }
        viewModel.marginRight.observe {
            binding.marginRight.apply {
                if (!hasFocus()) {
                    setText(it.toString())
                }
            }
        }
        viewModel.marginBottom.observe {
            binding.marginBottom.apply {
                if (!hasFocus()) {
                    setText(it.toString())
                }
            }
        }
        viewModel.paddingLeft.observe {
            binding.paddingLeft.apply {
                if (!hasFocus()) {
                    setText(it.toString())
                }
            }
        }
        viewModel.paddingTop.observe {
            binding.paddingTop.apply {
                if (!hasFocus()) {
                    setText(it.toString())
                }
            }
        }
        viewModel.paddingRight.observe {
            binding.paddingRight.apply {
                if (!hasFocus()) {
                    setText(it.toString())
                }
            }
        }
        viewModel.paddingBottom.observe {
            binding.paddingBottom.apply {
                if (!hasFocus()) {
                    setText(it.toString())
                }
            }
        }
    }

    private fun setListeners() {
        binding.showLayoutBoundsSwitch.setOnCheckedChangeListener { _, checked ->
            application.injectField(APP_FIELD_SHOW_BOUNDS, checked)
            itemView.rootView.drawLayoutBounds(checked, true)
            renderPreview()
            GlideApp.get(context).clearMemory()
        }
        binding.forceWidgetsClickable.setOnCheckedChangeListener { _, checked ->
            viewModel.forceClickable = checked
        }
        binding.ancestorButton.setOnClickListener {
            showViewsDialog(getString(R.string.select_parent), findAncestors())
        }
        binding.childrenButton.setOnClickListener {
            showViewsDialog(getString(R.string.select_children), findChildren())
        }
        binding.marginIdenticalCheckbox.setOnCheckedChangeListener { _, checked ->
            viewModel.marginIdentical = checked
        }
        binding.paddingIdenticalCheckbox.setOnCheckedChangeListener { _, checked ->
            viewModel.paddingIdentical = checked
        }
        binding.originClickButton.setOnClickListener {
            val listener = itemView.getOnClickListener()
            if (listener is ViewClickWrapper) {
                listener.performOriginClick()
            } else {
                listener?.onClick(itemView)
            }
            dismiss()
        }
        binding.cancelButton.setOnClickListener {
            dismiss()
        }
        binding.applyButton.setOnClickListener {
            onApply()
            dismiss()
        }
        binding.previewImage.setOnClickListener {
            showViewPreviewDialog(itemView)
        }
        binding.marginLeft.addTextChangedListener {
            if (!binding.marginLeft.hasFocus()) {
                return@addTextChangedListener
            }
            val value = it.toString().toIntOrNull() ?: 0
            if (viewModel.marginIdentical) {
                viewModel.margin.data = value
            } else {
                viewModel.marginLeft.data = value
            }
        }
        binding.marginTop.addTextChangedListener {
            if (!binding.marginTop.hasFocus()) {
                return@addTextChangedListener
            }
            val value = it.toString().toIntOrNull() ?: 0
            if (viewModel.marginIdentical) {
                viewModel.margin.data = value
            } else {
                viewModel.marginTop.data = value
            }
        }
        binding.marginRight.addTextChangedListener {
            if (!binding.marginRight.hasFocus()) {
                return@addTextChangedListener
            }
            val value = it.toString().toIntOrNull() ?: 0
            if (viewModel.marginIdentical) {
                viewModel.margin.data = value
            } else {
                viewModel.marginRight.data = value
            }
        }
        binding.marginBottom.addTextChangedListener {
            if (!binding.marginBottom.hasFocus()) {
                return@addTextChangedListener
            }
            val value = it.toString().toIntOrNull() ?: 0
            if (viewModel.marginIdentical) {
                viewModel.margin.data = value
            } else {
                viewModel.marginBottom.data = value
            }
        }
        binding.paddingLeft.addTextChangedListener {
            if (!binding.paddingLeft.hasFocus()) {
                return@addTextChangedListener
            }
            val value = it.toString().toIntOrNull() ?: 0
            if (viewModel.paddingIdentical) {
                viewModel.padding.data = value
            } else {
                viewModel.paddingLeft.data = value
            }
        }
        binding.paddingTop.addTextChangedListener {
            if (!binding.paddingTop.hasFocus()) {
                return@addTextChangedListener
            }
            val value = it.toString().toIntOrNull() ?: 0
            if (viewModel.paddingIdentical) {
                viewModel.padding.data = value
            } else {
                viewModel.paddingTop.data = value
            }
        }
        binding.paddingRight.addTextChangedListener {
            if (!binding.paddingRight.hasFocus()) {
                return@addTextChangedListener
            }
            val value = it.toString().toIntOrNull() ?: 0
            if (viewModel.paddingIdentical) {
                viewModel.padding.data = value
            } else {
                viewModel.paddingRight.data = value
            }
        }
        binding.paddingBottom.addTextChangedListener {
            if (!binding.paddingBottom.hasFocus()) {
                return@addTextChangedListener
            }
            val value = it.toString().toIntOrNull() ?: 0
            if (viewModel.paddingIdentical) {
                viewModel.padding.data = value
            } else {
                viewModel.paddingBottom.data = value
            }
        }
        binding.widthInput.addTextChangedListener {
            viewModel.width = it.toString().toIntOrNull() ?: viewModel.width
        }
        binding.heightInput.addTextChangedListener {
            viewModel.height = it.toString().toIntOrNull() ?: viewModel.height
        }
    }

    private fun showViewsDialog(title: CharSequence, views: List<View>) {
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

    private fun showViewPreviewDialog(view: View) {
        val layout = moduleRes.getLayout(R.layout.layout_image)
        val inflater = LayoutInflater.from(view.context)
        val itemView = inflater.inflate(layout, null, false)
        val binding = LayoutImageBinding.bind(itemView)
        Builder(view.context)
            .setTitle(view::class.java.name)
            .setView(itemView)
            .create()
            .show()
        GlideApp.with(view).load(view).into(binding.previewImage)
    }


    protected fun getString(@StringRes id: Int): CharSequence {
        return SpannableString(moduleRes.getString(id))
    }

    /**
     * Invoked when user clicks confirm button.
     */
    @CallSuper
    protected open fun onApply() {
        val data = viewModel.getData()
        val params = itemView.layoutParams
        params.width = data.width
        params.height = data.height
        if (params is ViewGroup.MarginLayoutParams) {
            params.setMargins(data.marginLeft, data.marginTop, data.marginRight, data.marginBottom)
        }
        itemView.layoutParams = params
        itemView.setPadding(
            data.paddingLeft,
            data.paddingTop,
            data.paddingRight,
            data.paddingBottom
        )
        itemView.visibility = data.visibility
        application.injectField(APP_FIELD_FORCE_CLICKABLE, data.forceClickable)
        val enabled = application.getInjectedField(APP_FIELD_GLOBAL_CONTROL_ENABLED, true) ?: true
        itemView.rootView.setAllViewsHookClick(enabled = enabled, traversalChildren = true, data.forceClickable)
    }

    /**
     * Render current item view into preview image view.
     */
    protected fun renderPreview() {
        if (!itemView.isLaidOut) {
            return
        }
        binding.previewImage.setImageBitmap(itemView.drawToBitmap())
    }

    override fun setTitle(title: CharSequence?) {
        binding.title.text = itemView.javaClass.name
    }

    /**
     * Load current attributes of view into dialog interface.
     */
    @CallSuper
    protected open fun onLoadViewAttributes(view: View) {
        val params = view.layoutParams
        val showBounds = application.getInjectedField(APP_FIELD_SHOW_BOUNDS, false) ?: false
        val forceClickable = application.getInjectedField(APP_FIELD_FORCE_CLICKABLE, false) ?: false
        val width = params.width.specOrDp()
        val height = params.height.specOrDp()
        val visibility = view.visibility
        val paddingLeft = view.paddingLeft.dp()
        val paddingRight = view.paddingRight.dp()
        val paddingTop = view.paddingTop.dp()
        val paddingBottom = view.paddingBottom.dp()

        binding.showLayoutBoundsSwitch.isChecked = showBounds
        binding.forceWidgetsClickable.isChecked = forceClickable

        when (width) {
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT -> {
                viewModel.widthInputVisible.data = false
            }
            else -> {
                viewModel.widthInputVisible.data = true
            }
        }
        when (height) {
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT -> {
                viewModel.heightInputVisible.data = false
            }
            else -> {
                viewModel.heightInputVisible.data = true
            }
        }
        binding.widthSpinner.setSelection(viewSpecSelectionMap[width] ?: 2)
        binding.heightSpinner.setSelection(viewSpecSelectionMap[height] ?: 2)
        binding.visibilitySpinner.setSelection(viewVisibilitySelectionMap[visibility] ?: 0)
        binding.widthInput.setText(width.toString())
        binding.heightInput.setText(height.toString())

        viewModel.width = width
        viewModel.height = height
        viewModel.visibility = visibility
        viewModel.paddingLeft.data = paddingLeft
        viewModel.paddingRight.data = paddingRight
        viewModel.paddingTop.data = paddingTop
        viewModel.paddingBottom.data = paddingBottom

        if (params is ViewGroup.MarginLayoutParams) {
            viewModel.marginLeft.data = params.leftMargin.dp()
            viewModel.marginRight.data = params.rightMargin.dp()
            viewModel.marginTop.data = params.topMargin.dp()
            viewModel.marginBottom.data = params.bottomMargin.dp()
        }

        val viewListener = itemView.getOnClickListener()
        binding.originClickButton.isVisible =
            !(viewListener == null || (viewListener is ViewClickWrapper && viewListener.originListener == null))
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

    /**
     * Add additional attributes settings panel.
     * If you implement this dialog, you should add your customized settings view through this method.
     *
     * @param view Additional settings view
     */
    protected fun appendAttributePanelView(view: View) {
        val param = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        binding.additionalPanelContainer.addView(view, param)
    }

    /**
     * Add additional attributes settings panel.
     * If you implement this dialog, you should add your customized settings view through this method.
     *
     * @param resId Layout resource id
     */
    protected fun appendAttributePanelView(@LayoutRes resId: Int): View {
        val layout = moduleRes.getLayout(resId)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(layout, null, false)
        appendAttributePanelView(view)
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