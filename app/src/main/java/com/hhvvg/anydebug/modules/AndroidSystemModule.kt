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

import com.hhvvg.anydebug.BuildConfig
import com.hhvvg.anydebug.Module
import com.hhvvg.anydebug.utils.Logger
import com.highcapable.yukihookapi.hook.factory.toJavaPrimitiveType
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class AndroidSystemModule : Module {

    override fun onHook(param: XC_LoadPackage.LoadPackageParam) {
        Logger.log("Hook system server")
        try {
            val clz = XposedHelpers.findClass(
                "com.android.server.pm.AppsFilterBase",
                param.classLoader
            )
            val pdsClz = XposedHelpers.findClass(
                "com.android.server.pm.snapshot.PackageDataSnapshot",
                param.classLoader
            )
            val psiClz = XposedHelpers.findClass(
                "com.android.server.pm.pkg.PackageStateInternal",
                param.classLoader
            )
            val objClz = XposedHelpers.findClass("java.lang.Object", param.classLoader)
            XposedHelpers.findAndHookMethod(
                clz,
                "shouldFilterApplication",
                pdsClz,
                Int::class.java.toJavaPrimitiveType(),
                objClz,
                psiClz,
                Int::class.java.toJavaPrimitiveType(),
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        try {
                            val psi = param.args[3]
                            val androidPkg = XposedHelpers.callMethod(psi, "getPkg")
                            val targetPkg =
                                XposedHelpers.callMethod(androidPkg, "getManifestPackageName")
                            if (BuildConfig.APPLICATION_ID == targetPkg) {
                                param.result = false
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}