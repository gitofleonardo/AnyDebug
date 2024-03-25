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

import android.content.res.XModuleResources
import com.hhvvg.anydebug.modules.ActivityModule
import com.hhvvg.anydebug.utils.Logger
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.xposed.bridge.event.YukiXposedEvent
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import kotlin.reflect.KClass

@InjectYukiHookWithXposed
object InjectHookEntry : IYukiHookXposedInit {
    lateinit var moduleRes: XModuleResources
    lateinit var modulePath: String
    private val modules = arrayOf<KClass<*>>(
        ActivityModule::class,
    )

    override fun onHook() {
    }

    override fun onXposedEvent() {
        super.onXposedEvent()
        YukiXposedEvent.onHandleLoadPackage { params ->
            if (!params.isFirstApplication) {
                Logger.log("Process already loaded, skip init modules.")
                return@onHandleLoadPackage
            }
            modules.forEach {
                (it.java.constructors[0].newInstance() as Module).onHook(params)
            }
        }
        YukiXposedEvent.onInitZygote {
            modulePath = it.modulePath
            moduleRes = XModuleResources.createInstance(modulePath, null)
        }
    }
}