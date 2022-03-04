package com.hhvvg.anydebug

import android.view.View

const val IGNORE_HOOK = "ANYDEBUG_IGNORE_HOOK"

/**
 * @author hhvvg
 */
class ViewClickWrapper(
    private val originListener: View.OnClickListener?,
    private val view: View
): View.OnClickListener {

    override fun onClick(v: View?) {
        if (v == null) {
            return
        }
        if (v.tag == IGNORE_HOOK) {
            originListener?.onClick(v)
            return
        }
        ViewDispatcher.dispatch(v)
    }
}