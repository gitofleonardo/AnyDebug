package com.hhvvg.anydebug.ui

import android.app.AndroidAppHelper
import android.app.Application
import android.os.Bundle
import android.text.SpannableString
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.view.ancestors
import androidx.core.view.children
import androidx.core.view.drawToBitmap
import androidx.core.view.isVisible
import com.hhvvg.anydebug.R
import com.hhvvg.anydebug.databinding.LayoutBaseAttributeDialogBinding
import com.hhvvg.anydebug.glide.GlideApp
import com.hhvvg.anydebug.handler.ViewClickWrapper
import com.hhvvg.anydebug.hook.AnyHookFramework.Companion.moduleRes
import com.hhvvg.anydebug.persistent.AppDatabase
import com.hhvvg.anydebug.persistent.RuleType
import com.hhvvg.anydebug.persistent.ViewRule
import com.hhvvg.anydebug.util.dp
import com.hhvvg.anydebug.util.getOnClickListener
import com.hhvvg.anydebug.util.inflater.MyLayoutInflater
import com.hhvvg.anydebug.util.isForceClickable
import com.hhvvg.anydebug.util.isPersistentEnabled
import com.hhvvg.anydebug.util.isShowBounds
import com.hhvvg.anydebug.util.specOrDp
import com.hhvvg.anydebug.util.specOrPx
import com.hhvvg.anydebug.util.updateDrawLayoutBounds
import com.hhvvg.anydebug.util.updateViewHookClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

/**
 * @author hhvvg
 *
 * Base dialog for editing basic view attributes.
 */
open class BaseAttributeDialog(protected val itemView: View) : BaseDialog(itemView.context) {
    private val viewModel = BaseViewModel()
    private val binding by lazy {
        LayoutBaseAttributeDialogBinding.bind(dialogContentView)
    }
    protected val application: Application by lazy {
        AndroidAppHelper.currentApplication()
    }

    override val fitScreen = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onSetObserves()
        onLoadViewAttributes(itemView)
        setTitle(itemView.javaClass.name)
        onSetupDialogText()
        renderPreview()
        setListeners()
    }

    /**
     * Invoked to setup text, since string in layout cannot be directly set as an id.
     */
    @CallSuper
    protected open fun onSetupDialogText() {
        binding.showLayoutBoundsSwitch.text = getString(R.string.show_global_layout_bounds)
        binding.forceWidgetsClickable.text = getString(R.string.force_clickable)
        binding.ancestorButton.title = getString(R.string.ancestors)
        binding.childrenButton.title = getString(R.string.children)
        binding.visibilityButton.title = getString(R.string.visibility)
        binding.widthSpecButton.title = getString(R.string.width)
        binding.heightSpecButton.title = getString(R.string.height)
        binding.rulesButton.title = moduleRes.getString(R.string.rules)
        binding.ltrbMarginInputs.setTitle(getString(R.string.margin_title))
        binding.ltrbPaddingInput.setTitle(moduleRes.getString(R.string.padding_title))
    }

    @CallSuper
    protected open fun onSetObserves() {
    }

    private fun setListeners() {
        binding.visibilityButton.setOnClickListener {
            VisibilityDialog(
                context,
                viewModel.visibility ?: itemView.visibility
            ) { visibility, text ->
                viewModel.visibility = visibility
                binding.visibilityButton.subtitle = text
            }.show()
        }
        binding.widthSpecButton.setOnClickListener {
            val dialog = ViewSpecDialog(context, viewModel.width) { spec, text ->
                viewModel.width = spec
                binding.widthSpecButton.subtitle = text
            }
            dialog.show()
            dialog.setTitle(moduleRes.getString(R.string.width))
        }
        binding.heightSpecButton.setOnClickListener {
            val dialog = ViewSpecDialog(context, viewModel.height) { spec, text ->
                viewModel.height = spec
                binding.heightSpecButton.subtitle = text
            }
            dialog.show()
            dialog.setTitle(moduleRes.getString(R.string.height))
        }
        binding.rulesButton.setOnClickListener {
            val dialog = RulePreviewDialog(context)
            dialog.show()
        }
        binding.showLayoutBoundsSwitch.setOnCheckedChangeListener { _, checked ->
            viewModel.showBounds = checked
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
        binding.previewImage.setOnClickListener {
            showViewPreviewDialog(itemView)
        }
        setApplyButton(moduleRes.getString(R.string.apply)) {
            onApply()
            dismiss()
        }
        setCancelButton(moduleRes.getString(R.string.cancel)) {
            dismiss()
        }
        setDetailsButton(moduleRes.getString(R.string.perform_origin_click)) {
            val listener = itemView.getOnClickListener()
            if (listener is ViewClickWrapper) {
                listener.performOriginClick()
            } else {
                listener?.onClick(itemView)
            }
            dismiss()
        }
    }

    private fun showViewsDialog(title: CharSequence, views: List<View>) {
        GlideApp.get(context).clearMemory()
        val dialog = ViewsDialog(context, views)
        dialog.show()
        dialog.setTitle(title)
    }

    private fun showViewPreviewDialog(view: View) {
        ViewRenderPreviewDialog(context, view).show()
    }


    protected fun getString(@StringRes id: Int): CharSequence {
        return SpannableString(moduleRes.getString(id))
    }

    private fun getData(): BaseViewAttribute {
        return BaseViewAttribute(
            viewModel.width.specOrPx(),
            viewModel.height.specOrPx(),
            binding.ltrbPaddingInput.leftValue,
            binding.ltrbPaddingInput.topValue,
            binding.ltrbPaddingInput.rightValue,
            binding.ltrbPaddingInput.bottomValue,
            binding.ltrbMarginInputs.leftValue,
            binding.ltrbMarginInputs.topValue,
            binding.ltrbMarginInputs.rightValue,
            binding.ltrbMarginInputs.bottomValue,
            viewModel.visibility,
            viewModel.forceClickable,
            viewModel.showBounds,
        )
    }

    /**
     * Invoked when user clicks confirm button.
     */
    @CallSuper
    protected open fun onApply() {
        val data = getData()

        // Save settings if persistent is enabled
        val persistentEnabled = application.isPersistentEnabled
        if (persistentEnabled) {
            val rules = makeRules(data)
            runBlocking(context = Dispatchers.IO) {
                AppDatabase.viewRuleDao.insertAll(rules)
            }
        }

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
        data.visibility?.let {
            itemView.visibility = it
        }
        application.isForceClickable = data.forceClickable
        itemView.rootView.updateViewHookClick(forceClickable = data.forceClickable)

        if (data.showBounds != application.isShowBounds) {
            application.isShowBounds = data.showBounds
            itemView.rootView.updateDrawLayoutBounds(drawEnabled = data.showBounds)
            GlideApp.get(context).clearMemory()
        }
    }

    private fun makeRules(data: BaseViewAttribute): List<ViewRule> {
        val rules = mutableListOf<ViewRule>()
        data.visibility?.let {
            val parent = itemView.parent
            val parentId = if (parent is View) {
                parent.id
            } else {
                View.NO_ID
            }
            val visibilityRule = ViewRule(
                className = itemView::class.java.name,
                viewId = itemView.id,
                ruleType = RuleType.Visibility,
                viewRule = it.toString(),
                viewParentId = parentId
            )
            rules.add(visibilityRule)
        }
        return rules
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

    override fun onInflateLayout(): Int = R.layout.layout_base_attribute_dialog

    /**
     * Load current attributes of view into dialog interface.
     */
    @CallSuper
    protected open fun onLoadViewAttributes(view: View) {
        val params = view.layoutParams
        val showBounds = application.isShowBounds
        val forceClickable = application.isForceClickable
        val width = params.width.specOrDp()
        val height = params.height.specOrDp()
        val paddingLeft = view.paddingLeft.dp()
        val paddingRight = view.paddingRight.dp()
        val paddingTop = view.paddingTop.dp()
        val paddingBottom = view.paddingBottom.dp()

        binding.showLayoutBoundsSwitch.isChecked = showBounds
        binding.forceWidgetsClickable.isChecked = forceClickable

        binding.visibilityButton.subtitle = fromVisibilityToString(itemView.visibility)

        viewModel.width = width
        viewModel.height = height

        binding.ltrbPaddingInput.apply {
            leftValue = paddingLeft
            topValue = paddingTop
            rightValue = paddingRight
            bottomValue = paddingBottom
        }

        if (params is ViewGroup.MarginLayoutParams) {
            binding.ltrbMarginInputs.isVisible = true
            binding.ltrbMarginInputs.apply {
                leftValue = params.leftMargin
                topValue = params.topMargin
                rightValue = params.rightMargin
                bottomValue = params.bottomMargin
            }
        }

        binding.widthSpecButton.subtitle = fromSpecToString(params.width.specOrDp())
        binding.heightSpecButton.subtitle = fromSpecToString(params.height.specOrDp())

        viewModel.showBounds = application.isShowBounds
        viewModel.forceClickable = application.isForceClickable
    }

    private fun fromSpecToString(spec: Int): CharSequence {
        return when (spec) {
            ViewGroup.LayoutParams.MATCH_PARENT -> moduleRes.getString(R.string.match_parent)
            ViewGroup.LayoutParams.WRAP_CONTENT -> moduleRes.getString(R.string.wrap_content)
            else -> "${spec}dp"
        }
    }

    private fun fromVisibilityToString(visibility: Int): CharSequence {
        return when (visibility) {
            View.VISIBLE -> moduleRes.getString(R.string.visible)
            View.INVISIBLE -> moduleRes.getString(R.string.invisible)
            View.GONE -> moduleRes.getString(R.string.gone)
            else -> ""
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
        val inflater = MyLayoutInflater.from(context)
        val view = inflater.inflate(layout, null, false)
        appendAttributePanelView(view)
        return view
    }

    /**
     * Holding information for view.
     */
    open class BaseViewAttribute(
        val width: Int,
        val height: Int,
        val paddingLeft: Int,
        val paddingTop: Int,
        val paddingRight: Int,
        val paddingBottom: Int,
        val marginLeft: Int,
        val marginTop: Int,
        val marginRight: Int,
        val marginBottom: Int,
        val visibility: Int?,
        val forceClickable: Boolean,
        val showBounds: Boolean,
    ) {
        constructor(data: BaseViewAttribute) : this(
            data.width,
            data.height,
            data.paddingLeft,
            data.paddingTop,
            data.paddingRight,
            data.paddingBottom,
            data.marginLeft,
            data.marginTop,
            data.marginRight,
            data.marginBottom,
            data.visibility,
            data.forceClickable,
            data.showBounds,
        )
    }
}