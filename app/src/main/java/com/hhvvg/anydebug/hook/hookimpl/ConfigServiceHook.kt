package com.hhvvg.anydebug.hook.hookimpl

import com.hhvvg.anydebug.BuildConfig
import com.hhvvg.anydebug.config.ConfigurationManagerService
import com.hhvvg.anydebug.hook.IHook
import com.hhvvg.anydebug.util.CONFIGURATION_SERVICE
import com.kaisar.xservicemanager.XServiceManager
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * @author hhvvg
 */
class ConfigServiceHook : IHook {
    override fun onHook(param: XC_LoadPackage.LoadPackageParam) {
        if (param.packageName != "android") {
            return
        }
        XServiceManager.initForSystemServer()
        val service = ConfigurationManagerService()
        XServiceManager.addService(CONFIGURATION_SERVICE, service)

        XServiceManager.debug(BuildConfig.DEBUG)
    }
}
