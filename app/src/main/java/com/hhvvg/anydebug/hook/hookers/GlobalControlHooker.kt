package com.hhvvg.anydebug.hook.hookers

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toBitmap
import com.hhvvg.anydebug.R
import com.hhvvg.anydebug.hook.AnyHookZygote.Companion.moduleRes
import com.hhvvg.anydebug.hook.IHooker
import com.hhvvg.anydebug.util.*
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

private const val ACTION_GLOBAL_ENABLE = "com.hhvvg.action.control.enable"

private const val EXTRA_CONTROL_ACTION = "EXTRA_CONTROL_ACTION"

private const val ACTION_ENABLE = 0
private const val ACTION_DISABLE = 1

private const val NOTIFY_ID = 10086

/**
 * @author hhvvg
 *
 * Hook to send notification for global enable control.
 */
class GlobalControlHooker : IHooker {
    override fun onHook(param: XC_LoadPackage.LoadPackageParam) {
        if (!param.isFirstApplication) {
            return
        }
        val methodHook = OnAppCreateMethodHook(param)
        val method = XposedHelpers.findMethodBestMatch(
            Application::class.java,
            "onCreate",
            arrayOf(),
            arrayOf()
        )
        XposedBridge.hookMethod(method, methodHook)
    }
}

private class OnAppCreateMethodHook(private val packageParam: XC_LoadPackage.LoadPackageParam) :
    XC_MethodHook() {
    override fun afterHookedMethod(param: MethodHookParam) {
        if (!packageParam.isFirstApplication) {
            return
        }
        val app = param.thisObject as Application
        // Enabled by default
        app.injectField(APP_FIELD_GLOBAL_CONTROL_ENABLED, true)

        // Create a channel for devices the api level of which is higher than O
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            createDefaultNotificationChannel(manager)
        }
        registerReceiverForApp(app)
        registerReceiverForNotification(app)
        showControlNotification(app)
    }

    private fun registerReceiverForNotification(app: Application) {
        val filter = IntentFilter(ACTION_GLOBAL_ENABLE)
        app.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.getIntExtra(EXTRA_CONTROL_ACTION, ACTION_ENABLE)) {
                    ACTION_ENABLE -> {
                        app.injectField(APP_FIELD_GLOBAL_CONTROL_ENABLED, true)
                    }
                    ACTION_DISABLE -> {
                        app.injectField(APP_FIELD_GLOBAL_CONTROL_ENABLED, false)
                    }
                }
                showControlNotification(app)
            }
        }, filter)
    }

    private fun registerReceiverForApp(app: Application) {
        val callback =
            XposedHelpers.findField(Application::class.java, "mActivityLifecycleCallbacks")
        val callbackArray =
            callback.get(app) as ArrayList<Application.ActivityLifecycleCallbacks>
        callbackArray.add(ActivityCallback())
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createDefaultNotificationChannel(manager: NotificationManager) {
        val name = moduleRes.getString(R.string.default_channel_name)
        val desc = moduleRes.getString(R.string.default_channel_desc)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(DEFAULT_NOTIFICATION_CHANNEL_ID, name, importance).apply {
            description = desc
            enableLights(false)
            enableVibration(false)
        }
        manager.createNotificationChannel(channel)
    }
}

private class ActivityCallback : Application.ActivityLifecycleCallbacks {
    override fun onActivityPostCreated(activity: Activity, savedInstanceState: Bundle?) {
        val receiver = GlobalEnableReceiver(activity)
        activity.injectField(ACTIVITY_FIELD_GLOBAL_ENABLE, receiver)
        val filter = IntentFilter(ACTION_GLOBAL_ENABLE)
        activity.registerReceiver(receiver, filter)
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
    }

    override fun onActivityStarted(p0: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(p0: Activity) {
    }

    override fun onActivityStopped(p0: Activity) {
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
        val receiver = activity.getInjectedField<BroadcastReceiver>(ACTIVITY_FIELD_GLOBAL_ENABLE)
        receiver?.let {
            activity.unregisterReceiver(receiver)
        }
    }

}

private class GlobalEnableReceiver(private val activity: Activity) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val decorView = activity.window.decorView
        when (intent.getIntExtra(EXTRA_CONTROL_ACTION, ACTION_ENABLE)) {
            ACTION_ENABLE -> {
                setGlobalEnable(decorView, true)
            }
            ACTION_DISABLE -> {
                setGlobalEnable(decorView, false)
            }
        }
    }

    private fun setGlobalEnable(decorView: View, enabled: Boolean) {
        val app = AndroidAppHelper.currentApplication()
        app.injectField(APP_FIELD_GLOBAL_CONTROL_ENABLED, enabled)
        val forceClick = app.getInjectedField(APP_FIELD_FORCE_CLICKABLE, false) ?: false
        decorView.setAllViewsHookClick(
            enabled = enabled,
            traversalChildren = true,
            forceClickable = forceClick
        )
    }
}

private fun showControlNotification(app: Application) {
    val manager = app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val enabled = app.getInjectedField(APP_FIELD_GLOBAL_CONTROL_ENABLED, true) ?: true
    val stateText = if (enabled) {
        moduleRes.getString(R.string.enabled)
    } else {
        moduleRes.getString(R.string.disabled)
    }
    val intent = Intent(ACTION_GLOBAL_ENABLE).apply {
        val enableAction = if (enabled) {
            ACTION_DISABLE
        } else {
            ACTION_ENABLE
        }
        putExtra(EXTRA_CONTROL_ACTION, enableAction)
    }
    val pendingIntent = PendingIntent.getBroadcast(
        app.applicationContext,
        NOTIFY_ID,
        intent,
        PendingIntent.FLAG_ONE_SHOT
    )
    val iconDrawable =
        ResourcesCompat.getDrawable(moduleRes, R.drawable.ic_launcher_foreground, null)?.toBitmap()
    val notification =
        NotificationCompat.Builder(app.applicationContext, DEFAULT_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(IconCompat.createWithBitmap(iconDrawable))
            .setContentTitle(moduleRes.getString(R.string.global_control))
            .setContentText(moduleRes.getString(R.string.current_enable_state, stateText))
            .setContentIntent(pendingIntent)
            .build()
    manager.notify(NOTIFY_ID, notification)
}
