package com.hhvvg.anydebug.hook.hookers

import android.app.Application
import com.hhvvg.anydebug.R
import com.hhvvg.anydebug.hook.AnyHookFramework.Companion.moduleRes
import com.hhvvg.anydebug.hook.IHooker
import com.hhvvg.anydebug.util.APP_FIELD_GLOBAL_CONTROL_ENABLED
import com.hhvvg.anydebug.util.APP_FIELD_PERSISTENT_ENABLE
import com.hhvvg.anydebug.util.injectField
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Load global settings from local shared preferences.
 */
class GlobalSettingsLoaderHooker : IHooker {
    override fun onHook(param: XC_LoadPackage.LoadPackageParam) {
        val prefs = XSharedPreferences("com.hhvvg.anydebug")
        val initialHookEnabled = prefs.getBoolean(moduleRes.getString(R.string.global_edit_enable_key), false)
        val initialPersistentEnabled = prefs.getBoolean(moduleRes.getString(R.string.edit_persistent_key), false)
        val methodHook = OnCreateMethodHook(initialHookEnabled, initialPersistentEnabled)
        val method = XposedHelpers.findMethodBestMatch(Application::class.java, "onCreate", arrayOf(), arrayOf())
        XposedBridge.hookMethod(method, methodHook)
    }

    private class OnCreateMethodHook(private val hookEnabled: Boolean, private val persistentEnabled: Boolean) : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam) {
            val app = param.thisObject as Application
            app.injectField(APP_FIELD_GLOBAL_CONTROL_ENABLED, hookEnabled)
            app.injectField(APP_FIELD_PERSISTENT_ENABLE, persistentEnabled)
        }
    }
}