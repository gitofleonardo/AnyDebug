package com.hhvvg.anydebug.observable

/**
 * @author hhvvg
 */
interface Observable<T> {
    var data: T
    fun observe(observer: Observer<T>)
    fun notifyObservers(data: T)
}