package com.hhvvg.anydebug.data

/**
 * @author hhvvg
 *
 * Holding information for view.
 */
open class BaseViewAttrData(
    val width: Int,
    val height: Int,
    val paddingLeft: Int,
    val paddingTop: Int,
    val paddingBottom: Int,
    val paddingRight: Int,
    val marginLeft: Int,
    val marginTop: Int,
    val marginBottom: Int,
    val marginRight: Int,
    val visibility: Int,
) {
    constructor(data: BaseViewAttrData) : this(
        data.width,
        data.height,
        data.paddingLeft,
        data.paddingTop,
        data.paddingBottom,
        data.paddingRight,
        data.marginLeft,
        data.marginTop,
        data.marginBottom,
        data.marginRight,
        data.visibility
    )
}