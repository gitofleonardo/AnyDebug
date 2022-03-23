package com.hhvvg.anydebug.hook.hookers

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.hhvvg.anydebug.IConfigManager
import com.hhvvg.anydebug.hook.IHooker
import com.hhvvg.anydebug.service.ConfigService
import com.hhvvg.anydebug.util.APP_FIELD_GLOBAL_CONTROL_ENABLED
import com.hhvvg.anydebug.util.APP_FIELD_PERSISTENT_ENABLE
import com.hhvvg.anydebug.util.injectField
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Load global settings from local shared preferences.
 */
class GlobalSettingsLoaderHooker : IHooker {
    override fun onHook(param: XC_LoadPackage.LoadPackageParam) {
        val methodHook = OnCreateMethodHook()
        val method = XposedHelpers.findMethodBestMatch(Application::class.java, "onCreate", arrayOf(), arrayOf())
        XposedBridge.hookMethod(method, methodHook)
    }

    private class OnCreateMethodHook : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam) {
            XposedBridge.log("Binding service")
            val app = param.thisObject as Application
            val intent = Intent()
            intent.component = ComponentName(app.applicationContext, ConfigService::class.java)
            app.bindService(intent, ServiceConnectionImpl(app), Context.BIND_AUTO_CREATE)
            XposedBridge.log("Bind complete")
        }
    }

    private class ServiceConnectionImpl(private val app: Application) : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            XposedBridge.log("OnBind: ${name}")
            if (service == null) {
                return
            }
            val configService = IConfigManager.Stub.asInterface(service)
            val hookEnabled = configService.isEditEnable
            val persistentEnabled = configService.isPersistentEnable
            app.injectField(APP_FIELD_GLOBAL_CONTROL_ENABLED, hookEnabled)
            app.injectField(APP_FIELD_PERSISTENT_ENABLE, persistentEnabled)

            XposedBridge.log("Config enabled: $hookEnabled, persistent enabled: $persistentEnabled")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            XposedBridge.log("OnUnBind: ${name}")
        }
    }
}