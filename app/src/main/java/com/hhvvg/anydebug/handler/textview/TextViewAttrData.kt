package com.hhvvg.anydebug.handler.textview

import com.hhvvg.anydebug.data.BaseViewAttrData

class TextViewAttrData(
    baseData: BaseViewAttrData,
    val text: String,
    val maxLine: Int,
    val textSizeInSp: Float
) : BaseViewAttrData(baseData)