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

package com.hhvvg.anydebug.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.hhvvg.anydebug.R
import java.util.function.Consumer

/**
 * Parent container for settings
 */
class ViewSettingsContainer(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private var factory: SettingsFactory? = null
    private var onCommitListener: Runnable? = null
    private var onViewRemoveListener: Consumer<View>? = null
    private var targetView: View? = null

    private val okButton by lazy {
        findViewById<View>(R.id.ok_button)
    }

    private val settingsContainer by lazy {
        findViewById<LinearLayout>(R.id.settings_container)
    }

    constructor(context: Context)
            : this(context, null)

    constructor(context: Context, attrs: AttributeSet?)

            : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : this(context, attrs, defStyleAttr, 0)

    init {
        inflate(context, R.layout.layout_view_settings_container, this)
        okButton.setOnClickListener {
            factory?.commit()
            onCommitListener?.run()
        }
    }

    fun removeTargetView() {
        targetView?.let { onViewRemoveListener?.accept(it) }
        targetView?.parent?.let {
            if (it is ViewGroup) {
                it.removeView(targetView)
            }
        }
    }

    fun setOnCommitListener(listener: Runnable) {
        onCommitListener = listener
    }

    fun setOnViewRemoveListener(listener: Consumer<View>) {
        onViewRemoveListener = listener
    }

    fun setTargetView(target: View) {
        targetView = target
        recreateSettings()
    }

    private fun clearSettings() {
        settingsContainer.removeAllViews()
    }

    private fun createTitle(titleText: CharSequence): TextView {
        val title =
            LayoutInflater.from(context).inflate(R.layout.layout_title, this, false) as TextView
        title.text = titleText
        return title
    }

    private fun recreateSettings() {
        clearSettings()
        val target = targetView ?: return
        val views = mutableListOf<SettingContent>()
        factory = SettingsFactoryManager.createFactory(target).apply {
            onCreate(target, settingsContainer, views)
        }
        views.forEach {
            settingsContainer.addView(createTitle(it.title))
            settingsContainer.addView(it.view)
        }
    }

}