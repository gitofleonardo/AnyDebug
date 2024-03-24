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

package com.hhvvg.anydebug.configurations

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.service.quicksettings.TileService
import java.util.function.Consumer

const val ACTION_CONFIG_CHANGED = "com.hhvvg.anydebug.config_changed"
const val ACTION_KEY_EDIT_ENABLED = "key_edit_enabled"

class ConfigChangedReceiver(private val callback: Consumer<Boolean>) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action == ACTION_CONFIG_CHANGED) {
            val enabled = intent.getBooleanExtra(ACTION_KEY_EDIT_ENABLED, false)
            callback.accept(enabled)
        }
    }

    companion object {
        @JvmStatic
        fun sendConfigBroadcast(context: Context, enabled: Boolean) {
            val intent = Intent(ACTION_CONFIG_CHANGED).apply {
                putExtra(ACTION_KEY_EDIT_ENABLED, enabled)
            }
            context.sendBroadcast(intent)
        }

        @JvmStatic
        fun getConfigIntentFilter(): IntentFilter {
            return IntentFilter(ACTION_CONFIG_CHANGED)
        }

        @SuppressLint("UnspecifiedRegisterReceiverFlag")
        @JvmStatic
        fun registerConfigReceiver(context: Context, receiver: ConfigChangedReceiver) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(
                    receiver, getConfigIntentFilter(),
                    TileService.RECEIVER_EXPORTED
                )
            } else {
                context.registerReceiver(receiver, getConfigIntentFilter())
            }
        }
    }
}