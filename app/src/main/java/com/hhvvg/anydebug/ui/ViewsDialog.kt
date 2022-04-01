package com.hhvvg.anydebug.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import com.hhvvg.anydebug.R
import com.hhvvg.anydebug.databinding.LayoutListviewDialogBinding
import com.hhvvg.anydebug.handler.ViewDispatcher
import com.hhvvg.anydebug.hook.AnyHookFramework.Companion.moduleRes
import com.hhvvg.anydebug.ui.adapter.ViewItemListAdapter

/**
 * @author hhvvg
 */
class ViewsDialog(context: Context, private val views: List<View>) : BaseDialog(context) {
    override val fitScreen: Boolean
        get() = true

    private val listAdapter = ViewItemListAdapter(views) {
        val view = views[it]
        ViewDispatcher.dispatch(view)
        dismiss()
    }

    private lateinit var binding: LayoutListviewDialogBinding

    override fun onInflateLayout(): Int = R.layout.layout_listview_dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutListviewDialogBinding.bind(dialogContentView)
        binding.apply {
            listview.adapter = listAdapter
        }
        setApplyButton(moduleRes.getString(R.string.ok)) {
            dismiss()
        }
    }
}