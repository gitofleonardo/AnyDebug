package com.hhvvg.anydebug.handler.imageview

import android.os.Bundle
import android.text.SpannableString
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import com.hhvvg.anydebug.R
import com.hhvvg.anydebug.databinding.LayoutImageViewAttrBinding
import com.hhvvg.anydebug.glide.GlideApp
import com.hhvvg.anydebug.hook.AnyHookZygote.Companion.moduleRes
import com.hhvvg.anydebug.ui.BaseAttrDialog

/**
 * @author hhvvg
 *
 * Editing image attributes.
 */
class ImageViewAttrDialog(private val view: ImageView) : BaseAttrDialog<ImageViewAttrData>(view) {
    override val attrData: ImageViewAttrData
        get() = ImageViewAttrData(baseAttrData, binding.imageUrlInput.text.toString())

    private lateinit var binding: LayoutImageViewAttrBinding

    private val scaleTypeIndexMap by lazy {
        val map = HashMap<ImageView.ScaleType, Int>()
        val types = ImageView.ScaleType.values()
        for (i in types.indices) {
            map[types[i]] = i
        }
        map
    }

    private val onScaleTypeItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            parent: AdapterView<*>?,
            v: View?,
            position: Int,
            id: Long
        ) {
            view.scaleType = ImageView.ScaleType.values()[position]
            view.postDelayed({
                renderPreview()
            }, 200)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            // Do nothing
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val attrView = appendAttrPanelView(R.layout.layout_image_view_attr)
        binding = LayoutImageViewAttrBinding.bind(attrView)
        binding.imageUrlTitle.text = SpannableString(moduleRes.getString(R.string.image_url))
        binding.imageUrlInput.hint = SpannableString(moduleRes.getString(R.string.image_url))
        binding.scaleTypeTitle.text = SpannableString(moduleRes.getString(R.string.scale_type))

        val scaleTypes = moduleRes.getStringArray(R.array.image_scale_type)
        binding.scaleTypeSpinner.adapter =
            ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, scaleTypes)
        binding.scaleTypeSpinner.onItemSelectedListener = onScaleTypeItemSelectedListener
        val scaleType = view.scaleType
        binding.scaleTypeSpinner.setSelection(scaleTypeIndexMap[scaleType] ?: 0)
    }

    override fun onApply(data: ImageViewAttrData) {
        super.onApply(data)
        if (data.imageUrl.isNotEmpty()) {
            GlideApp.with(itemView).load(data.imageUrl).into(view)
        }
    }
}
