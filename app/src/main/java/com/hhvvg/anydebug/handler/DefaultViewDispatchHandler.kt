package com.hhvvg.anydebug.handler

import android.view.View
import com.hhvvg.anydebug.ui.DefaultAttrDialog

class DefaultViewDispatchHandler : ViewDispatchHandler {
    override fun support(view: View): Boolean {
        return true
    }

    override fun handle(view: View) {
        val dialog = DefaultAttrDialog(view)
        dialog.show()
    }
}