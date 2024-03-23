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

package com.hhvvg.libinject.view.remote

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.hhvvg.libinject.utils.APPLICATION_ID
import dalvik.system.DexClassLoader

private const val FACTORY_IMPL_CLASS = "com.hhvvg.libinject.view.remote.RemoteViewFactoryImpl"

class RemoteViewFactoryLoader(private val context: Context) {

    fun getRemoteFactory(): RemoteViewFactory {
        val intent = Intent().apply {
            setPackage(APPLICATION_ID)
            action = "com.hhvvg.anydebug.action.MAIN"
        }
        val pm = context.packageManager
        val plugins = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        if (plugins.isEmpty()) {
            throw RuntimeException()
        }
        val resolveInfo = plugins[0]
        val ai = resolveInfo.activityInfo
        val dexPath = ai.applicationInfo.sourceDir
        val dexOutputPath = context.applicationInfo.dataDir
        val libPath = ai.applicationInfo.nativeLibraryDir
        val dcLoader = DexClassLoader(dexPath, dexOutputPath, libPath, this.javaClass.classLoader)
        try {
            val clz = dcLoader.loadClass(FACTORY_IMPL_CLASS)
            val instance = clz.constructors[0].newInstance()
            return instance as RemoteViewFactory
        } catch (e: Exception) {
            throw RuntimeException()
        }
    }
}