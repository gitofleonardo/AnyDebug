package com.hhvvg.anydebug.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import com.hhvvg.anydebug.R
import com.hhvvg.anydebug.databinding.LayoutImageItemBinding
import com.hhvvg.anydebug.glide.GlideApp
import com.hhvvg.anydebug.hook.AnyHookFramework.Companion.moduleRes

/**
 * @author hhvvg
 */
class ViewRenderPreviewDialog(context: Context, private val view: View) : BaseDialog(context){
    override val fitScreen: Boolean
        get() = false

    private lateinit var binding: LayoutImageItemBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutImageItemBinding.bind(dialogContentView)
        GlideApp.with(context).load(view).into(binding.previewImage)
        setTitle(moduleRes.getString(R.string.preview))
        setApplyButton(moduleRes.getString(R.string.ok)) {
            dismiss()
        }
    }

    override fun onInflateLayout(): Int = R.layout.layout_image_item
}