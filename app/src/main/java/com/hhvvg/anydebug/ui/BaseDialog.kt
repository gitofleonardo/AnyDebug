package com.hhvvg.anydebug.ui

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import androidx.annotation.LayoutRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.hhvvg.anydebug.R
import com.hhvvg.anydebug.databinding.LayoutBaseDialogBinding
import com.hhvvg.anydebug.hook.AnyHookFramework.Companion.moduleRes
import com.hhvvg.anydebug.util.inflater.MyLayoutInflater

/**
 * @author hhvvg
 */
abstract class BaseDialog(context: Context) : AlertDialog(context) {
    private lateinit var baseBinding: LayoutBaseDialogBinding
    protected lateinit var dialogContentView: View

    protected open val fitScreen: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLayout()
        setBackground()
    }

    private fun setLayout() {
        val inflater = MyLayoutInflater.from(context)
        val layout = moduleRes.getLayout(onInflateLayout())
        val baseLayout = moduleRes.getLayout(R.layout.layout_base_dialog)
        baseBinding = LayoutBaseDialogBinding.bind(inflater.inflate(baseLayout, null, false))
        dialogContentView = inflater.inflate(layout, null, false)
        baseBinding.contentContainer.addView(dialogContentView)
        setContentView(baseBinding.root)
    }

    private fun setBackground() {
        baseBinding.root.background =
            ResourcesCompat.getDrawable(moduleRes, R.drawable.backgroun_dialog, null)
    }

    override fun setTitle(title: CharSequence?) {
        baseBinding.dialogTitle.isVisible = true
        baseBinding.dialogTitle.text = title
    }

    fun setDetailsButton(text: CharSequence?, action: (View) -> Unit) {
        val t = text ?: moduleRes.getString(R.string.details)
        baseBinding.detailButton.isVisible = true
        baseBinding.detailButton.text = t
        baseBinding.detailButton.setOnClickListener(action)
    }

    fun setCancelButton(text: CharSequence?, action: (View) -> Unit) {
        val t = text ?: moduleRes.getString(R.string.cancel)
        baseBinding.cancelButton.isVisible = true
        baseBinding.cancelButton.text = t
        baseBinding.cancelButton.setOnClickListener(action)
    }

    fun setApplyButton(text: CharSequence?, action: (View) -> Unit) {
        val t = text ?: moduleRes.getString(R.string.apply)
        baseBinding.applyButton.isVisible = true
        baseBinding.applyButton.text = t
        baseBinding.applyButton.setOnClickListener(action)
    }

    override fun show() {
        super.show()
        window?.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            setBackgroundDrawableResource(android.R.color.transparent)

            if (fitScreen) {
                val param = this.attributes
                val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val metrics = DisplayMetrics()
                wm.defaultDisplay.getMetrics(metrics)
                param.height = (metrics.heightPixels * .9F).toInt()
                this.attributes = param
            }
        }
    }

    @LayoutRes
    abstract fun onInflateLayout(): Int
}