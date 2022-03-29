package com.hhvvg.anydebug.handler

import android.view.View

/**
 * @author hhvvg
 *
 * Views will be dispatched here, implement this interface and add handling logic.
 */
interface ViewDispatchHandler {

    /**
     * To test if this handler can handle this view
     *
     * @param view View dispatched
     * @return true if this handler supports handling this type of view
     */
    fun support(view: View): Boolean

    /**
     * Handle the view dispatched
     *
     * @param view View dispatched
     */
    fun handle(view: View)
}