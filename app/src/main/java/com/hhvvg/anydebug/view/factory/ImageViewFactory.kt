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

package com.hhvvg.anydebug.view.factory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import com.hhvvg.anydebug.R
import com.hhvvg.anydebug.utils.reverse
import com.hhvvg.anydebug.view.SettingContent
import com.hhvvg.anydebug.view.factory.command.ImageScaleTypeCommand
import com.hhvvg.anydebug.view.factory.command.ImageUrlCommand
import com.hhvvg.anydebug.view.preference.InputPreferenceView
import com.hhvvg.anydebug.view.preference.OptionsPreferenceView

/**
 * Settings factory for a ImageView
 */
class ImageViewFactory : BasicViewFactory() {

    override fun onCreate(
        targetView: View,
        parent: ViewGroup,
        outViews: MutableList<SettingContent>
    ) {
        super.onCreate(targetView, parent, outViews)
        if (targetView !is ImageView) {
            return
        }
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_image_view_settings, parent, false)
        val urlPref = view.findViewById<InputPreferenceView>(R.id.image_url_input)
        val scaleTypePref = view.findViewById<OptionsPreferenceView>(R.id.scale_type_preference)

        val scaleTypeIndexMapper = createScaleTypeMapper()
        val indexScaleTypeMapper = scaleTypeIndexMapper.reverse()
        scaleTypePref.selectedIndex = scaleTypeIndexMapper.getOrDefault(targetView.scaleType, -1)
        scaleTypePref.setOnCheckChangedListener { _, id ->
            indexScaleTypeMapper[id]?.let {
                addCommand(ImageScaleTypeCommand(targetView, it))
            }
        }
        urlPref.setOnTextChangedListener {
            if (it.isNotEmpty()) {
                addCommand(ImageUrlCommand(targetView, it.toString()))
            } else {
                removeCommand(ImageUrlCommand::class)
            }
        }
        outViews.add(
            SettingContent(view, parent.context.resources.getString(R.string.title_image_view))
        )
    }

    private fun createScaleTypeMapper(): Map<ScaleType, Int> {
        return mapOf(
            ScaleType.FIT_XY to 0,
            ScaleType.FIT_START to 1,
            ScaleType.FIT_CENTER to 2,
            ScaleType.FIT_END to 3,
            ScaleType.CENTER to 4,
            ScaleType.CENTER_CROP to 5,
            ScaleType.CENTER_INSIDE to 6
        )
    }
}