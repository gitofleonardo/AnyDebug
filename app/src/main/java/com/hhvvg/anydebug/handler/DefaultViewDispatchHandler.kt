package com.hhvvg.anydebug.handler

import android.view.View
import com.hhvvg.anydebug.ui.BaseAttributeDialog

class DefaultViewDispatchHandler : ViewDispatchHandler {
    override fun support(view: View): Boolean {
        return true
    }

    override fun handle(view: View) {
        val dialog = BaseAttributeDialog(view)
        dialog.show()
    }
}