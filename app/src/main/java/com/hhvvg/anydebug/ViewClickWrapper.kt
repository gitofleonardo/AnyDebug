package com.hhvvg.anydebug

import android.view.View

const val IGNORE_HOOK = "ANYDEBUG_IGNORE_HOOK"

/**
 * @author hhvvg
 */
class ViewClickWrapper(
    val originListener: View.OnClickListener?,
    val originClickable: Boolean,
    private val view: View
): View.OnClickListener {

    override fun onClick(v: View?) {
        if (v == null) {
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
}