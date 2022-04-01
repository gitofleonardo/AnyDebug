package com.hhvvg.anydebug.ui.adapter

import android.text.SpannableString
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.hhvvg.anydebug.R
import com.hhvvg.anydebug.databinding.LayoutViewPreviewItemBinding
import com.hhvvg.anydebug.glide.GlideApp
import com.hhvvg.anydebug.handler.ViewClickWrapper.Companion.IGNORE_HOOK
import com.hhvvg.anydebug.hook.AnyHookFramework.Companion.moduleRes
import com.hhvvg.anydebug.ui.ViewRenderPreviewDialog
import com.hhvvg.anydebug.util.inflater.MyLayoutInflater

class ViewItemListAdapter(
    private val views: List<View>,
    private val onClickListener: ((Int) -> Unit)? = null
) : BaseAdapter() {

    override fun getCount(): Int = views.size

    override fun getItem(position: Int): Any = views[position]

    override fun getItemId(position: Int): Long {
        return views[position].id.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = views[position]
        val itemView: View = if (convertView == null) {
            val layout = moduleRes.getLayout(R.layout.layout_view_preview_item)
            val inflater = MyLayoutInflater.from(view.context)
            inflater.inflate(layout, null, false)
        } else {
            convertView
        }
        itemView.tag = IGNORE_HOOK
        val binding: LayoutViewPreviewItemBinding = LayoutViewPreviewItemBinding.bind(itemView)
        GlideApp
            .with(view)
            .load(view)
            .thumbnail()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(binding.viewImage)
        binding.viewImage.setOnClickListener {
            showViewImageDialog(view)
        }
        binding.viewName.text = SpannableString(view::class.java.name)
        binding.root.setOnClickListener {
            onClickListener?.invoke(position)
        }
        return itemView
    }

    private fun showViewImageDialog(view: View) {
        ViewRenderPreviewDialog(view.context, view).show()
    }
}
