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

package com.hhvvg.anydebug

import android.content.Context
import android.content.res.Resources
import android.content.res.Resources.Theme
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import com.hhvvg.anydebug.utils.moduleResources
import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.hook.factory.classOf

/**
 * Context for loading module's resources
 */
class ModuleContext(baseContext: Context, theme: Theme) : ContextThemeWrapper(baseContext, theme) {

    private val inflater by lazy {
        (super.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater).cloneInContext(this)
    }

    override fun getResources(): Resources {
        return moduleResources
    }

    override fun getClassLoader(): ClassLoader {
        return classOf<YukiHookAPI>().classLoader ?: error("Error loading lsp class loader.")
    }

    override fun getSystemService(name: String): Any {
        if (LAYOUT_INFLATER_SERVICE == name) {
            return inflater
        }
        return super.getSystemService(name)
    }

}