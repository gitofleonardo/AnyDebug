package com.hhvvg.anydebug.util.inflater

import android.content.Context
import android.view.LayoutInflater
import androidx.core.view.LayoutInflaterCompat

/**
 * @author hhvvg
 *
 * Inflater that can inflate custom views.
 */
class MyLayoutInflater {
    companion object {
        @JvmStatic
        @Volatile
        private var inflater: LayoutInflater? = null

        @JvmStatic
        fun from(context: Context): LayoutInflater {
            if (inflater == null) {
                synchronized(MyLayoutInflater::class.java) {
                    if (inflater == null) {
                        inflater = LayoutInflater.from(context).apply {
                            factory2 = CustomFactory2()
                        }
                    }
                }
            }
            return inflater!!
        }
    }
}
