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

package com.hhvvg.anydebug.modules

import android.app.Activity
import android.os.Bundle
import com.hhvvg.anydebug.Module
import com.hhvvg.anydebug.instances.ActivityInstance
import com.hhvvg.anydebug.utils.doAfter
import de.robv.android.xposed.callbacks.XC_LoadPackage

class ActivityModule : Module {

    override fun onHook(param: XC_LoadPackage.LoadPackageParam) {
        Activity::class.doAfter("onCreate", Bundle::class.java) {
            ActivityInstance(it.thisObject as Activity)
        }
    }
}