package com.hhvvg.anydebug.hook.hookimpl

import android.app.AndroidAppHelper
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.View
import com.hhvvg.anydebug.config.ConfigDbHelper
import com.hhvvg.anydebug.config.ConfigPreferences
import com.hhvvg.anydebug.hook.IHook
import com.hhvvg.anydebug.receiver.EditControlReceiver
import com.hhvvg.anydebug.receiver.EditControlReceiver.Companion.ACTION_ENABLE
import com.hhvvg.anydebug.receiver.EditControlReceiver.Companion.EXTRA_CONTROL_ACTION
import com.hhvvg.anydebug.ui.fragment.SettingsFragment.Companion.ACTION_PERSISTENT_ENABLE
import com.hhvvg.anydebug.util.ACTIVITY_FIELD_GLOBAL_ENABLE_RECEIVER
import com.hhvvg.anydebug.util.doAfter
import com.hhvvg.anydebug.util.doOnActivityDestroyed
import com.hhvvg.anydebug.util.doOnActivityPostCreated
import com.hhvvg.anydebug.util.getInjectedField
import com.hhvvg.anydebug.util.injectField
import com.hhvvg.anydebug.util.isForceClickable
import com.hhvvg.anydebug.util.isGlobalEditEnabled
import com.hhvvg.anydebug.util.isPersistentEnabled
import com.hhvvg.anydebug.util.updateViewHookClick
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * @author hhvvg
 *
 * Hook for listening config change broadcast.
 */
class GlobalControlReceiverHook : IHook {
    override fun onHook(param: XC_LoadPackage.LoadPackageParam) {
        Application::class.doAfter("onCreate") {
            val app = it.thisObject as Application
            // Read from module
            val sp = ConfigPreferences(app.applicationContext)
            val editEnabled = sp.getBoolean(ConfigDbHelper.CONFIG_EDIT_ENABLED_COLUMN, false)
            val persistentEnabled =
                sp.getBoolean(ConfigDbHelper.CONFIG_PERSISTENT_ENABLED_COLUMN, false)
            app.isGlobalEditEnabled = editEnabled
            app.isPersistentEnabled = persistentEnabled
            registerReceiverForApp(app)
            app.doOnActivityPostCreated { activity, _ ->
                // Register this receiver for every activity
                val receiver = EditControlReceiver { enabled, _ ->
                    setGlobalEnable(activity.window.decorView, enabled)
                }
                activity.injectField(ACTIVITY_FIELD_GLOBAL_ENABLE_RECEIVER, receiver)
                EditControlReceiver.register(receiver, activity)
            }
            app.doOnActivityDestroyed { activity ->
                // Don't forget to release it.
                activity.getInjectedField<BroadcastReceiver>(ACTIVITY_FIELD_GLOBAL_ENABLE_RECEIVER)
                    ?.let { receiver ->
                        activity.unregisterReceiver(receiver)
                    }
            }
        }
    }

    private fun registerReceiverForApp(app: Application) {
        val persistentEnableReceiver = PersistentEnableReceiver()
        val filter = IntentFilter(ACTION_PERSISTENT_ENABLE)
        app.registerReceiver(persistentEnableReceiver, filter)
    }

    private fun setGlobalEnable(decorView: View, enabled: Boolean) {
        val app = AndroidAppHelper.currentApplication()
        app.isGlobalEditEnabled = enabled
        if (!enabled) {
            app.isForceClickable = false
        }
        decorView.updateViewHookClick()
    }

    private class PersistentEnableReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val enabled = when (intent.getIntExtra(EXTRA_CONTROL_ACTION, ACTION_ENABLE)) {
                ACTION_ENABLE -> {
                    true
                }
                else -> false
            }
            val app = AndroidAppHelper.currentApplication()
            app.isPersistentEnabled = enabled
        }
    }
}
