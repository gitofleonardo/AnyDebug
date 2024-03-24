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

package com.hhvvg.anydebug

import android.annotation.SuppressLint
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.hhvvg.anydebug.configurations.ConfigChangedReceiver
import com.hhvvg.anydebug.configurations.ModuleSettings.Companion.moduleSettings

class TileControlService : TileService() {

    private var stateEnabled: Boolean
        get() = qsTile.state == Tile.STATE_ACTIVE
        set(value) {
            qsTile.state = if (value) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            qsTile.updateTile()
        }

    private val settings by lazy {
        moduleSettings
    }
    private val configReceiver = ConfigChangedReceiver {
        stateEnabled = settings.editEnabled
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate() {
        super.onCreate()
        ConfigChangedReceiver.registerConfigReceiver(this, configReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(configReceiver)
    }

    override fun onTileAdded() {
        super.onTileAdded()
        stateEnabled = settings.editEnabled
    }

    override fun onClick() {
        super.onClick()
        settings.editEnabled = !settings.editEnabled
        ConfigChangedReceiver.sendConfigBroadcast(this, settings.editEnabled)
    }
}