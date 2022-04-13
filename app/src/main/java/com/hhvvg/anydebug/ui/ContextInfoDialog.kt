package com.hhvvg.anydebug.ui

import android.app.Activity
import android.app.AndroidAppHelper
import android.content.Context
import android.os.Bundle
import android.text.SpannableString
import android.util.DisplayMetrics
import android.view.View
import com.hhvvg.anydebug.R
import com.hhvvg.anydebug.databinding.LayoutContextInfoDialogBinding
import com.hhvvg.anydebug.hook.AnyHookFramework.Companion.moduleRes
import com.hhvvg.anydebug.util.currentActivity

/**
 * @author hhvvg
 */
class ContextInfoDialog(context: Context, private val view: View) : BaseDialog(context){
    private lateinit var binding: LayoutContextInfoDialogBinding
    private val currentActivity: Activity?
        get() {
            val app = AndroidAppHelper.currentApplication()
            return app.currentActivity
        }
    private val windowMetrics: DisplayMetrics
        get() {
            return context.resources.displayMetrics
        }

    override fun onInflateLayout(): Int  = R.layout.layout_context_info_dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutContextInfoDialogBinding.bind(dialogContentView)
        binding.viewNameText.text = SpannableString(moduleRes.getString(R.string.view_name_text, view.javaClass.name))
        binding.activityNameText.text = SpannableString(moduleRes.getString(R.string.activity_name_text, "${currentActivity?.javaClass?.name}"))
        binding.contextClassNameText.text = SpannableString(moduleRes.getString(R.string.context_class_text, view.context.javaClass.name))
        binding.screenWidth.text = SpannableString(moduleRes.getString(R.string.screen_width, windowMetrics.widthPixels.toString()))
        binding.screenHeight.text = SpannableString(moduleRes.getString(R.string.screen_height, windowMetrics.heightPixels.toString()))

        setTitle(moduleRes.getString(R.string.context_info))
        setApplyButton(moduleRes.getString(R.string.ok)) {
            dismiss()
        }
    }
}