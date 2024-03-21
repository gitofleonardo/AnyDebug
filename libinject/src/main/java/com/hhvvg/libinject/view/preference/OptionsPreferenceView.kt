/*
 *     Copyright (C) <2024>  <gitofleonardo>
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.hhvvg.libinject.view.preference

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RadioGroup.OnCheckedChangeListener
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.hhvvg.libinject.R
import java.lang.IllegalArgumentException

class OptionsPreferenceView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) :
    ConstraintLayout(context, attrs, defStyleAttr, defStyleRes), OnClickListener {

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?)

            : this(context, attrs, 0)

    constructor(context: Context)
            : this(context, null)

    private val titleView: TextView
        get() = findViewById(R.id.title_view)
    private val summaryView: TextView
        get() = findViewById(R.id.summary_view)
    private val radioGroup: RadioGroup by lazy { findViewById(R.id.radio_group) }
    private val options: Array<CharSequence>
    private var onRadioChangedListener: OnCheckedChangeListener? = null

    init {
        inflate(context, R.layout.layout_options_preference_view, this)
        val ta = context.obtainStyledAttributes(attrs, R.styleable.OptionsPreferenceView)
        titleView.text = ta.getText(R.styleable.OptionsPreferenceView_preference_title)
        summaryView.text = ta.getText(R.styleable.OptionsPreferenceView_preference_summary)
        options = ta.getTextArray(R.styleable.OptionsPreferenceView_preference_options)
        ta.recycle()
        setOnClickListener(this)
    }

    override fun onClick(v: View) {
        if (radioGroup.childCount == 0) {
            fillRadio()
        } else {
            radioGroup.removeAllViews()
        }
    }

    private fun fillRadio() {
        options.forEachIndexed { index, charSequence ->
            val radio = LayoutInflater.from(context)
                .inflate(R.layout.layout_radio_button, this, false) as RadioButton
            radio.text = charSequence
            radio.id = index
            radioGroup.addView(radio)
        }
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            onRadioChangedListener?.onCheckedChanged(group, checkedId)
        }
    }

    fun setOnCheckChangedListener(listener: OnCheckedChangeListener) {
        onRadioChangedListener = listener
    }
}