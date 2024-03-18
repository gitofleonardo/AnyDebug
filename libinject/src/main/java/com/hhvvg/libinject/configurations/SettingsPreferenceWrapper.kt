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

package com.hhvvg.libinject.configurations

import android.content.Context
import android.content.SharedPreferences

const val SETTINGS_SP_NAME = "settings_shared_preferences.xml"

private const val KEY_EDIT_ENABLED = "key_edit_enabled"

class SettingsPreferenceWrapper(context: Context) {

    private val preferences: SharedPreferences

    var editEnabled: Boolean
        get() = preferences.getBoolean(KEY_EDIT_ENABLED, true)
        set(value) {
            preferences.edit().putBoolean(KEY_EDIT_ENABLED, value).apply()
        }

    init {
        preferences = context.getSharedPreferences(SETTINGS_SP_NAME, Context.MODE_PRIVATE)
    }
}