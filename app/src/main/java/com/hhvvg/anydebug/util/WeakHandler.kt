package com.hhvvg.anydebug.util

import android.os.Handler
import android.os.Looper
import android.os.Message
import java.lang.ref.WeakReference

/**
 * @author hhvvg
 *
 * Handler to avoid memory leak
 */
class WeakHandler<T : HandlerCallback>(t: T) : Handler(Looper.getMainLooper()) {
    private val reference = WeakReference(t)

    override fun handleMessage(msg: Message) {
        reference.get()?.onHandleMessage(msg)
    }
}

/**
 * Callback to be invoked in main thread.
 */
interface HandlerCallback {

    /**
     * This method runs on main thread
     */
    fun onHandleMessage(msg: Message)
}