package com.hhvvg.anydebug.ui

import com.hhvvg.anydebug.observable.ObservableField

class BaseViewModel {
    var visibility: Int? = null

    var width: Int = 0
    var height: Int = 0

    val widthInputVisible = ObservableField(false)
    val heightInputVisible = ObservableField(false)

    var forceClickable = false

    var showBounds = false
}