package com.hhvvg.anydebug.handler.textview

import android.os.Bundle
import android.text.SpannableString
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import com.hhvvg.anydebug.R
import com.hhvvg.anydebug.ui.BaseAttrDialog
import com.hhvvg.anydebug.hook.AnyHookZygote.Companion.moduleRes

class TextEditingDialog(private val view: TextView) : BaseAttrDialog<TextViewAttrData>(view) {
    private val rootView by lazy {
        val layout = moduleRes.getLayout(R.layout.layout_text_view_attr)
        val inflater = LayoutInflater.from(context)
        inflater.inflate(layout, null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addAttrPanelView(rootView)
        val input = rootView.findViewById<EditText>(R.id.edit_text)
        input.hint = SpannableString(moduleRes.getString(R.string.enter_text))
        input.setText(SpannableString(view.text))
    }

    override val attrData: TextViewAttrData
        get() {
            return TextViewAttrData(viewWidth, viewHeight, rootView.findViewById<EditText>(R.id.edit_text).text.toString())
        }

    override fun onApply(data: TextViewAttrData) {
        super.onApply(data)
        view.text = SpannableString(data.text)
    }
}