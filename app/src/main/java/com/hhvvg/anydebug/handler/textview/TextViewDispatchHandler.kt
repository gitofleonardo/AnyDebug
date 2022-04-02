package com.hhvvg.anydebug.handler.textview

import android.view.View
import android.widget.TextView
import com.hhvvg.anydebug.handler.ViewDispatchHandler
import com.hhvvg.anydebug.ui.TextEditingDialog

/**
 * @author hhvvg
 *
 * Handles TextView
 */
class TextViewDispatchHandler : ViewDispatchHandler {
    override fun support(view: View): Boolean {
        return view is TextView
    }

    override fun handle(view: View) {
        val textView = view as TextView
        val dialog = TextEditingDialog(textView)
        dialog.show()
    }
}