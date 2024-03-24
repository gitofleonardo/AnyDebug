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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hhvvg.anydebug.utils.APPLICATION_ID

private const val ACTION_REQUEST_CONFIG = "com.hhvvg.anydebug.action_request_config"

class RequestConfigReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        ConfigChangedReceiver.sendConfigBroadcast(context, AllSettings.editEnabled)
    }

    companion object {
        @JvmStatic
        fun requestConfigBroadcast(context: Context) {
            val intent = Intent(ACTION_REQUEST_CONFIG).apply {
                setPackage(APPLICATION_ID)
            }
            context.sendBroadcast(intent)
        }
    }
}