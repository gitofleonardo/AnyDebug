package com.hhvvg.anydebug.ui

import com.hhvvg.anydebug.data.BaseViewAttribute
import com.hhvvg.anydebug.observable.ObservableField
import com.hhvvg.anydebug.util.px
import com.hhvvg.anydebug.util.specOrPx

class BaseViewModel {
    var visibility: Int? = null

    var width: Int = 0
    var height: Int = 0

    val margin = ObservableField(0)
    var marginIdentical = false

    val padding = ObservableField(0)
    var paddingIdentical = false

    val marginLeft = ObservableField(0)
    val marginTop = ObservableField(0)
    val marginRight = ObservableField(0)
    val marginBottom = ObservableField(0)

    val paddingLeft = ObservableField(0)
    val paddingTop = ObservableField(0)
    val paddingRight = ObservableField(0)
    val paddingBottom = ObservableField(0)

    val widthInputVisible = ObservableField(false)
    val heightInputVisible = ObservableField(false)

    val marginAvailable = ObservableField(false)

    var forceClickable = false

    fun getData(): BaseViewAttribute {
        return BaseViewAttribute(
            width.specOrPx(),
            height.specOrPx(),
            paddingLeft.data.px(),
            paddingTop.data.px(),
            paddingRight.data.px(),
            paddingBottom.data.px(),
            marginLeft.data.px(),
            marginTop.data.px(),
            marginRight.data.px(),
            marginBottom.data.px(),
            visibility,
            forceClickable,
        )
    }
}