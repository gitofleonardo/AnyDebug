package com.hhvvg.anydebug.data

/**
 * @author hhvvg
 *
 * Holding information for view.
 */
open class BaseViewAttribute(
    val width: Int,
    val height: Int,
    val paddingLeft: Int,
    val paddingTop: Int,
    val paddingRight: Int,
    val paddingBottom: Int,
    val marginLeft: Int,
    val marginTop: Int,
    val marginRight: Int,
    val marginBottom: Int,
    val visibility: Int,
) {
    constructor(data: BaseViewAttribute) : this(
        data.width,
        data.height,
        data.paddingLeft,
        data.paddingTop,
        data.paddingRight,
        data.paddingBottom,
        data.marginLeft,
        data.marginTop,
        data.marginRight,
        data.marginBottom,
        data.visibility
    )
}