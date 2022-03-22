package com.hhvvg.anydebug.hook

import android.content.res.Resources
import android.content.res.XModuleResources
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge

/**
 * @author hhvvg
 *
 * Hook zygote and get resources.
 */
class AnyHookZygote : IXposedHookZygoteInit {
    override fun initZygote(p0: IXposedHookZygoteInit.StartupParam) {
        modulePath = p0.modulePath
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