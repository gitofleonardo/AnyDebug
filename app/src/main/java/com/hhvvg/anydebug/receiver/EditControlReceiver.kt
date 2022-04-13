package com.hhvvg.anydebug.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

/**
 * @author hhvvg
 */
class EditControlReceiver(private val callback: (Boolean, String) -> Unit) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val isEditEnabled = when (intent.getIntExtra(EXTRA_CONTROL_ACTION, ACTION_ENABLE)) {
            ACTION_ENABLE -> {
                true
            }
            else -> {
                false
            }
        }
        val source = intent.getStringExtra(EXTRA_BROADCAST_SOURCE)
        callback.invoke(isEditEnabled, source ?: SOURCE_DEFAULT)
    }

    companion object {
        const val ACTION_GLOBAL_ENABLE = "com.hhvvg.action.control.enable"
        const val EXTRA_CONTROL_ACTION = "EXTRA_CONTROL_ACTION"
        const val EXTRA_BROADCAST_SOURCE = "EXTRA_BROADCAST_SOURCE"
        const val SOURCE_DEFAULT = "SOURCE_DEFAULT"

        const val ACTION_ENABLE = 0
        const val ACTION_DISABLE = 1

        fun register(receiver: EditControlReceiver, context: Context) {
            val filter = IntentFilter(ACTION_GLOBAL_ENABLE)
            context.registerReceiver(receiver, filter)
        }

        fun sendBroadcast(context: Context, enabled: Boolean, source: String) {
            val intent = Intent(ACTION_GLOBAL_ENABLE).apply {
                val enableAction = if (enabled) {
                    ACTION_ENABLE
                } else {
                    ACTION_DISABLE
                }
                putExtra(EXTRA_CONTROL_ACTION, enableAction)
                putExtra(EXTRA_BROADCAST_SOURCE, source)
            }
            context.sendBroadcast(intent)
        }
    }
}
