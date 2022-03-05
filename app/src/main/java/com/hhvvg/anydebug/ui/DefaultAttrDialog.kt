package com.hhvvg.anydebug.ui

import android.view.View
import com.hhvvg.anydebug.data.BaseViewAttrData

class DefaultAttrDialog(view: View) : BaseAttrDialog<BaseViewAttrData>(view) {
    override val attrData: BaseViewAttrData
        get() = baseAttrData
}