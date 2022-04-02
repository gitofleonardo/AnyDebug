package com.hhvvg.anydebug.ui

import android.os.Bundle
import android.text.InputType
import android.text.SpannableString
import android.view.View
import android.widget.ImageView
import com.hhvvg.anydebug.R
import com.hhvvg.anydebug.databinding.LayoutImageViewAttrBinding
import com.hhvvg.anydebug.glide.GlideApp
import com.hhvvg.anydebug.handler.imageview.ImageViewAttribute
import com.hhvvg.anydebug.hook.AnyHookFramework.Companion.moduleRes
import com.hhvvg.anydebug.util.inflater.MyLayoutInflater

/**
 * @author hhvvg
 *
 * Editing image attributes.
 */
class ImageViewAttrDialog(private val view: ImageView) : BaseAttributeDialog(view) {
    private var currentUrl: CharSequence = ""
    private var currentScaleType = view.scaleType

    private val attrData: ImageViewAttribute
        get() = ImageViewAttribute(currentUrl.toString(), currentScaleType)

    private val binding by lazy {
        val layout = moduleRes.getLayout(R.layout.layout_image_view_attr)
        val inflater = MyLayoutInflater.from(context)
        val view = inflater.inflate(layout, null)
        LayoutImageViewAttrBinding.bind(view)
    }

    override fun onLoadViewAttributes(view: View) {
        super.onLoadViewAttributes(view)
        binding.scaleTypeButton.subtitle = currentScaleType.toNameString()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appendAttributePanelView(binding.root)
        binding.imageButton.setOnClickListener {
            val dialog = InputDialog(context, InputType.TYPE_CLASS_TEXT, currentUrl) {
                currentUrl = it
            }
            dialog.show()
            dialog.setTitle(moduleRes.getString(R.string.image_url))
        }
        binding.scaleTypeButton.setOnClickListener {
            val dialog = ScaleTypeDialog(context, view.scaleType) {
                currentScaleType = it
                binding.scaleTypeButton.subtitle = it.toNameString()
            }
            dialog.show()
        }
    }

    private fun ImageView.ScaleType.toNameString(): CharSequence {
        return when (this) {
            ImageView.ScaleType.MATRIX -> "Matrix"
            ImageView.ScaleType.FIT_XY -> "FitXY"
            ImageView.ScaleType.FIT_START -> "FitStart"
            ImageView.ScaleType.FIT_CENTER -> "FitCenter"
            ImageView.ScaleType.FIT_END -> "FitEnd"
            ImageView.ScaleType.CENTER -> "Center"
            ImageView.ScaleType.CENTER_CROP -> "CenterCrop"
            ImageView.ScaleType.CENTER_INSIDE -> "CenterInside"
        }
    }

    override fun onSetupDialogText() {
        super.onSetupDialogText()
        binding.imageButton.title = SpannableString(moduleRes.getString(R.string.image_url))
        binding.scaleTypeButton.title = SpannableString(moduleRes.getString(R.string.scale_type))
    }

    override fun onApply() {
        super.onApply()
        val data = attrData
        if (data.imageUrl.isNotEmpty()) {
            GlideApp.with(itemView).load(data.imageUrl).into(view)
        }
        view.scaleType = data.scaleType
    }
}
