package com.hhvvg.anydebug.handler

import android.view.View
import com.hhvvg.anydebug.handler.imageview.ImageViewDispatchHandler
import com.hhvvg.anydebug.handler.textview.TextViewDispatchHandler
import kotlin.reflect.KClass

/**
 * @author hhvvg
 *
 * Dispatches views to certain handler.
 */
class ViewDispatcher private constructor() {
    private val handlers = ArrayList<ViewDispatchHandler>()

    init {
        val registries = sRegistryHandler
        for (reg in registries) {
            val instance = reg.java.newInstance() as ViewDispatchHandler
            handlers.add(instance)
        }
    }

    private fun dispatchInner(view: View): Boolean {
        var handled = false
        for (handler in handlers) {
            if (handler.support(view)) {
                handler.handle(view)
                handled = true
                break
            }
        }
        return handled
    }

    companion object {
        @JvmStatic
        private val sRegistryHandler = arrayOf<KClass<*>>(
            TextViewDispatchHandler::class,
            ImageViewDispatchHandler::class,
            DefaultViewDispatchHandler::class
        )

        @JvmStatic
        private val sInstance: ViewDispatcher by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { ViewDispatcher() }

        @JvmStatic
        fun getInstance(): ViewDispatcher = sInstance

        @JvmStatic
        fun dispatch(view: View): Boolean {
            val instance = getInstance()
            return instance.dispatchInner(view)
        }
    }
}