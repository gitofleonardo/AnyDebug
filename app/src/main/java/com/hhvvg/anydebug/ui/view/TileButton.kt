package com.hhvvg.anydebug.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.text.SpannableString
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.core.view.isVisible
import com.hhvvg.anydebug.R
import com.hhvvg.anydebug.databinding.LayoutTileButtonBinding
import com.hhvvg.anydebug.hook.AnyHookFramework.Companion.moduleRes
import com.hhvvg.anydebug.util.inflater.MyLayoutInflater
import com.hhvvg.anydebug.util.setIgnoreTagRecursively

/**
 * @author hhvvg
 */
@SuppressLint("UseCompatLoadingForDrawables")
class TileButton(context: Context, attributeSet: AttributeSet?) :
    RelativeLayout(context, attributeSet) {
    constructor(context: Context) : this(context, null)

    private var binding: LayoutTileButtonBinding

    init {
        binding = getBindingInternal()
        binding.arrowImage.setImageDrawable(moduleRes.getDrawable(R.drawable.ic_baseline_arrow_right_24, null))
    }

    private fun getBindingInternal(): LayoutTileButtonBinding {
        val layout = moduleRes.getLayout(R.layout.layout_tile_button)
        val inflater = MyLayoutInflater.from(context)
        val view = inflater.inflate(layout, this, true).apply {
            setIgnoreTagRecursively()
        }
        return LayoutTileButtonBinding.bind(view)
    }

    var title: CharSequence
        get() = binding.tileTitle.text
        set(value) {
            binding.tileTitle.text = SpannableString(value)
        }

    var subtitle: CharSequence
        get() = binding.tileSubtitle.text
        set(value) {
            if (!binding.tileSubtitle.isVisible) {
                binding.tileSubtitle.isVisible = true
            }
            binding.tileSubtitle.text = SpannableString(value)
        }
}