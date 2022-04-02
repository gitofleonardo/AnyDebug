package com.hhvvg.anydebug.ui

import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageView
import com.hhvvg.anydebug.R
import com.hhvvg.anydebug.databinding.LayoutListviewDialogBinding
import com.hhvvg.anydebug.hook.AnyHookFramework.Companion.moduleRes

/**
 * @author hhvvg
 */
class ScaleTypeDialog(context: Context, private val scaleType: ImageView.ScaleType, private val onScaleTypeClickListener: ((ImageView.ScaleType) -> Unit)? = null) : BaseDialog(context) {
    private lateinit var binding: LayoutListviewDialogBinding
    private val scaleTypes = ImageView.ScaleType.values()

    override fun onInflateLayout(): Int = R.layout.layout_listview_dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutListviewDialogBinding.bind(dialogContentView)
        binding.listview.apply {
            val array = moduleRes.getStringArray(R.array.image_scale_type)
            adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, array)
            setOnItemClickListener { _, _, position, _ ->
                if (scaleType != scaleTypes[position]) {
                    onScaleTypeClickListener?.invoke(scaleTypes[position])
                }
                dismiss()
            }
        }
        setTitle(moduleRes.getString(R.string.scale_type))
    }
}