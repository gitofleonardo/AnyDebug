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

import android.view.View
import android.view.ViewGroup

/**
 * Basic factory for creating settings for a View
 */
interface SettingsFactory {

    /**
     * Creating settings
     *
     * @param targetView target view for mod
     * @param parent parent view of settings
     * @param outViews all settings contents
     */
    fun onCreate(targetView: View, parent: ViewGroup, outViews: MutableList<SettingContent>)

    /**
     * Commit all pending settings
     */
    fun commit()

}

/**
 * Settings identity
 */
data class SettingContent(val view: View, val title: CharSequence)