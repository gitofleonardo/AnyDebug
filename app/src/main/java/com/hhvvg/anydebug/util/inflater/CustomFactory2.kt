package com.hhvvg.anydebug.util.inflater

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import com.hhvvg.anydebug.handler.ViewClickWrapper.Companion.IGNORE_HOOK
import com.hhvvg.anydebug.ui.view.LtrbView
import kotlin.reflect.KClass

/**
 * @author hhvvg
 *
 * Inflates custom views.
 */
class CustomFactory2 : LayoutInflater.Factory2 {
    private val customViewRegistry = arrayListOf<KClass<*>>(
        LtrbView::class,
    )

    override fun onCreateView(
        parent: View?,
        name: String,
        context: Context,
        attrs: AttributeSet
    ): View? {
        var view: View? = null
        customViewRegistry.forEach {
            if (it.java.name == name) {
                val constructor = it.java.getDeclaredConstructor(Context::class.java, AttributeSet::class.java)
                view = constructor.newInstance(context, attrs) as View
                view?.apply {
                    fun dfsSetTag(view: View, tag: String) {
                        view.tag = tag
                        if (view is ViewGroup) {
                            view.children.forEach { child ->
                                dfsSetTag(child, tag)
                            }
                        }
                    }
                    dfsSetTag(this, IGNORE_HOOK)
                }
                return@forEach
            }
        }
        return view
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        return null
    }
}