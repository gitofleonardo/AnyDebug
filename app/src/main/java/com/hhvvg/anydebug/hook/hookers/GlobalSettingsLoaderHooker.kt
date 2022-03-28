package com.hhvvg.anydebug.hook.hookers

import android.app.Application
import com.hhvvg.anydebug.config.ConfigDbHelper
import com.hhvvg.anydebug.config.ConfigPreferences
import com.hhvvg.anydebug.hook.IHooker
import com.hhvvg.anydebug.util.APP_FIELD_GLOBAL_CONTROL_ENABLED
import com.hhvvg.anydebug.util.APP_FIELD_PERSISTENT_ENABLE
import com.hhvvg.anydebug.util.injectField
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Load global settings from local databases.
 */
class GlobalSettingsLoaderHooker : IHooker {
    override fun onHook(param: XC_LoadPackage.LoadPackageParam) {
        val methodHook = OnCreateMethodHook()
        val method = XposedHelpers.findMethodBestMatch(
            Application::class.java,
            "onCreate",
            arrayOf(),
            arrayOf()
        )
        XposedBridge.hookMethod(method, methodHook)
    }

    private class OnCreateMethodHook : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam) {
            val app = param.thisObject as Application
            val sp = ConfigPreferences(app.applicationContext)
            val editEnabled = sp.getBoolean(ConfigDbHelper.CONFIG_EDIT_ENABLED_COLUMN, false)
            val persistentEnabled = sp.getBoolean(ConfigDbHelper.CONFIG_PERSISTENT_ENABLED_COLUMN, false)
            app.injectField(APP_FIELD_GLOBAL_CONTROL_ENABLED, editEnabled)
            app.injectField(APP_FIELD_PERSISTENT_ENABLE, persistentEnabled)
        }
    }
}