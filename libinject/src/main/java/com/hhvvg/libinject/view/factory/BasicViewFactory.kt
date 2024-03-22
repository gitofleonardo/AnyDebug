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

package com.hhvvg.libinject.view.factory

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.view.children
import androidx.core.view.isVisible
import com.hhvvg.libinject.R
import com.hhvvg.libinject.view.PreviewList
import com.hhvvg.libinject.view.PreviewView
import com.hhvvg.libinject.view.SettingContent
import com.hhvvg.libinject.view.SettingsFactory
import com.hhvvg.libinject.view.preference.InputPreferenceView
import com.hhvvg.libinject.view.preference.OptionsPreferenceView
import com.hhvvg.libinject.view.preference.PreferenceView

class BasicViewFactory : SettingsFactory {

    private val View.paddingLtrb: String
        get() = "[${paddingLeft},${paddingTop},${paddingRight},${paddingBottom}]"
    private val MarginLayoutParams.ltrb: String
        get() = "[${leftMargin},${topMargin},${rightMargin},${bottomMargin}]"

    override fun onCreate(
        targetView: View,
        parent: ViewGroup,
        outViews: MutableList<SettingContent>
    ) {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_basic_view_settings, parent, false)
        val preview = view.findViewById<PreviewView>(R.id.preview_item)
        val clzName = view.findViewById<PreferenceView>(R.id.view_class_name)
        val contextInfo = view.findViewById<PreferenceView>(R.id.context_info)
        val previewList = view.findViewById<PreviewList>(R.id.preview_children)
        val previewContainer = view.findViewById<ViewGroup>(R.id.preview_children_container)
        val visibilityPreference =
            view.findViewById<OptionsPreferenceView>(R.id.visibility_preference)
        val widthPreference = view.findViewById<InputPreferenceView>(R.id.width_input_preference)
        val heightPreference = view.findViewById<InputPreferenceView>(R.id.height_input_preference)
        val paddingPreference =
            view.findViewById<InputPreferenceView>(R.id.padding_ltrb_input_preference)
        val marginPreference =
            view.findViewById<InputPreferenceView>(R.id.margin_ltrb_input_preference)
        val dividerMargin = view.findViewById<View>(R.id.divider_margin)

        paddingPreference.text = targetView.paddingLtrb
        val viewParams = targetView.layoutParams
        if (viewParams is MarginLayoutParams) {
            marginPreference.isVisible = true
            dividerMargin.isVisible = true
            marginPreference.text = viewParams.ltrb
        } else {
            marginPreference.isVisible = false
            dividerMargin.isVisible = false
        }
        widthPreference.text = viewParams.width.toString()
        heightPreference.text = viewParams.height.toString()
        val visibilityMapper = createVisibilityMapper(parent.context)
        visibilityPreference.summary = visibilityMapper[targetView.visibility]
        preview.setRenderer(targetView)
        clzName.summary = targetView.javaClass.name
        contextInfo.summary = targetView.context.javaClass.name
        if (targetView is ViewGroup) {
            previewContainer.isVisible = true
            previewList.updatePreviewItems(targetView.children.toList())
        } else {
            previewContainer.isVisible = false
        }
        outViews.add(
            SettingContent(
                view,
                parent.context.resources.getString(R.string.title_basic_view)
            )
        )
    }

    private fun createVisibilityMapper(context: Context): Map<Int, CharSequence> {
        val arr = context.resources.getTextArray(R.array.visibility_array)
        val map = mutableMapOf<Int, CharSequence>()
        map[View.VISIBLE] = arr[0]
        map[View.INVISIBLE] = arr[1]
        map[View.GONE] = arr[2]
        return map
    }
}