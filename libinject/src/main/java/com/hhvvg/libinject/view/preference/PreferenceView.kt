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
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.hhvvg.libinject.R

class PreferenceView(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
    ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {

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

    var summary: CharSequence?
        get() = summaryView.text
        set(value) {
            summaryView.text = value
        }
    var title: CharSequence?
        get() = titleView.text
        set(value) {
            titleView.text = value
        }

    init {
        inflate(context, R.layout.layout_preference_view, this)
        val ta = context.obtainStyledAttributes(attrs, R.styleable.PreferenceView)
        titleView.text = ta.getText(R.styleable.PreferenceView_preference_title)
        summaryView.text = ta.getText(R.styleable.PreferenceView_preference_summary)
        ta.recycle()
    }
}