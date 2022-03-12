package com.hhvvg.anydebug.observable

/**
 * @author hhvvg
 *
 * Common observable field.
 */
class ObservableField<T>(initValue: T) : Observable<T>{
    private val observers = ArrayList<Observer<T>>()

    override var data: T = initValue
        set(value) {
            field = value
            notifyObservers(value)
        }

    override fun observe(observer: Observer<T>) {
        observers.add(observer)
    }

    fun observe(observer: (data: T) -> Unit) {
        observers.add(ObserverImp(observer))
    }

    override fun notifyObservers(data: T) {
        for (observer in observers) {
            observer.onUpdate(data)
        }
    }

    private class ObserverImp<T>(private val observer: (data: T) -> Unit) : Observer<T> {
        override fun onUpdate(data: T) {
            observer.invoke(data)
        }
    }
}