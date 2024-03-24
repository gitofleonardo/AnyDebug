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

package com.hhvvg.anydebug.configurations

import android.content.Context
import com.highcapable.yukihookapi.hook.factory.prefs
import com.highcapable.yukihookapi.hook.xposed.prefs.YukiHookPrefsBridge

private const val MODULE_SETTINGS_SP_NAME = "module_settings_shared_preferences.xml"
private const val KEY_EDIT_ENABLED = "key_edit_enabled"

class ModuleSettings private constructor(private val context: Context) {
    private val prefs: YukiHookPrefsBridge
        get() = context.prefs(MODULE_SETTINGS_SP_NAME)

    var editEnabled: Boolean
        get() = prefs.getBoolean(KEY_EDIT_ENABLED, true)
        set(value) {
            prefs.edit().putBoolean(KEY_EDIT_ENABLED, value).apply()
        }

    companion object {
        val Context.moduleSettings
            get() = ModuleSettings(this)
    }
}