package com.hhvvg.anydebug.hook

import android.content.res.Resources
import android.content.res.XModuleResources
import de.robv.android.xposed.IXposedHookZygoteInit

/**
 * @author hhvvg
 *
 * Hook zygote and get resources.
 */
class AnyHookZygote : IXposedHookZygoteInit {
    override fun initZygote(p0: IXposedHookZygoteInit.StartupParam?) {
        if (p0 == null) {
            return
        }
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