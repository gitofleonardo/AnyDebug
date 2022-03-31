package com.hhvvg.anydebug.handler

import android.view.View

/**
 * @author hhvvg
 */
class ViewClickWrapper(
    val originListener: View.OnClickListener?,
    val originClickable: Boolean,
    private val view: View
) : View.OnClickListener {

    override fun onClick(v: View?) {
        if (v == null) {
            return
        }
        if (v.tag == IGNORE_HOOK) {
            performOriginClick()
            return
        }
        val dispatched = ViewDispatcher.dispatch(v)
        if (!dispatched) {
            performOriginClick()
        }
    }

    fun performOriginClick() {
        originListener?.onClick(view)
    }

    companion object {
        const val IGNORE_HOOK = "ANYDEBUG_IGNORE_HOOK"
    }
}