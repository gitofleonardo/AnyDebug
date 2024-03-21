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
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.isVisible
import com.hhvvg.libinject.R
import com.hhvvg.libinject.utils.Logger
import com.hhvvg.libinject.view.PreviewList
import com.hhvvg.libinject.view.PreviewView
import com.hhvvg.libinject.view.SettingContent
import com.hhvvg.libinject.view.SettingsFactory
import com.hhvvg.libinject.view.preference.PreferenceView

class BasicViewFactory : SettingsFactory {

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
}