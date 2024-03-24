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

package com.hhvvg.libinject

import android.content.res.Resources
import android.content.res.XModuleResources
import com.hhvvg.libinject.modules.ApplicationModule
import com.hhvvg.libinject.modules.ActivityModule
import com.hhvvg.libinject.utils.Logger
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlin.reflect.KClass

class MainInjectHook : IXposedHookLoadPackage, IXposedHookZygoteInit {

    private var processLoaded = false

    private val modules = arrayOf<KClass<*>>(
        ApplicationModule::class,
        ActivityModule::class,
    )

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (processLoaded) {
            Logger.log("Process already loaded, skip init modules.")
            return
        }
        modules.forEach {
            (it.java.constructors[0].newInstance() as Module).onHook(lpparam)
        }
        processLoaded = true
    }

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        modulePath = startupParam.modulePath
        moduleRes = getModuleRes(modulePath)
    }

    companion object {
        @JvmStatic
        lateinit var moduleRes: Resources

        @JvmStatic
        lateinit var modulePath: String

        @JvmStatic
        fun getModuleRes(path: String): Resources {
            return XModuleResources.createInstance(path, null)
        }
    }

}