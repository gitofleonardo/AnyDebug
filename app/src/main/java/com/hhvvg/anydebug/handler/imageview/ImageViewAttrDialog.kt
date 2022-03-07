package com.hhvvg.anydebug.handler.imageview

import android.os.Bundle
import android.text.SpannableString
import android.widget.ImageView
import com.hhvvg.anydebug.R
import com.hhvvg.anydebug.databinding.LayoutImageViewAttrBinding
import com.hhvvg.anydebug.hook.AnyHookZygote
import com.hhvvg.anydebug.ui.BaseAttrDialog
import com.hhvvg.anydebug.util.glide.GlideApp

/**
 * @author hhvvg
 *
 * Editing image attributes.
 */
class ImageViewAttrDialog(private val view: ImageView) : BaseAttrDialog<ImageViewAttrData>(view) {
    override val attrData: ImageViewAttrData
        get() {
            val url = binding.imageUrlInput.text.toString()
            return ImageViewAttrData(baseAttrData, url)
        }

    private lateinit var binding: LayoutImageViewAttrBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val attrView = appendAttrPanelView(R.layout.layout_image_view_attr)
        binding = LayoutImageViewAttrBinding.bind(attrView)
        binding.imageUrlTitle.text = SpannableString(AnyHookZygote.moduleRes.getString(R.string.image_url))
        binding.imageUrlInput.hint = SpannableString(AnyHookZygote.moduleRes.getString(R.string.image_url))
    }

    override fun onApply(data: ImageViewAttrData) {
        super.onApply(data)
        if (data.imageUrl.isNotEmpty()) {
            GlideApp.with(itemView).load(data.imageUrl).into(view)
        }
    }
}
