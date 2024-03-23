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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.hhvvg.libinject.R
import com.hhvvg.libinject.view.SettingContent
import com.hhvvg.libinject.view.factory.command.ImageUrlCommand
import com.hhvvg.libinject.view.preference.InputPreferenceView

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
}