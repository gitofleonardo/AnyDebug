package com.hhvvg.anydebug.observable

/**
 * @author hhvvg
 */
interface Observer<T> {
    fun onUpdate(data: T)
}