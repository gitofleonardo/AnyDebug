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

package com.hhvvg.anydebug.instances

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import com.hhvvg.anydebug.Instance
import com.hhvvg.anydebug.configurations.ConfigChangedReceiver
import com.hhvvg.anydebug.configurations.RequestConfigReceiver
import com.hhvvg.anydebug.utils.doAfter
import com.hhvvg.anydebug.view.ActivityPreviewWindow
import de.robv.android.xposed.XC_MethodHook.Unhook

class ActivityInstance(private val activity: Activity) : Instance, ActivityLifecycleCallbacks {

    private var previewWindow: ActivityPreviewWindow? = null

    private var windowFocusHook: Unhook? = null
    private val configReceiver by lazy {
        ConfigChangedReceiver {
            onDisplayWindowChanged(it)
        }
    }

    init {
        activity.registerActivityLifecycleCallbacks(this)
        ConfigChangedReceiver.registerConfigReceiver(activity, configReceiver)
        windowFocusHook = Activity::class.doAfter("onWindowFocusChanged", Boolean::class.java) {
            if (it.args[0] as Boolean) {
                RequestConfigReceiver.requestConfigBroadcast(activity)
            }
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityPreDestroyed(activity: Activity) {
        super.onActivityPreDestroyed(activity)
        activity.unregisterReceiver(configReceiver)
        windowFocusHook?.unhook()
        onDisplayWindowChanged(false)
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    private fun onDisplayWindowChanged(show: Boolean) {
        previewWindow = if (show) {
            val preview = previewWindow
            if (preview != null) {
                return
            }
            ActivityPreviewWindow(activity).apply {
                show()
            }
        } else {
            previewWindow?.dismiss()
            null
        }
    }

}