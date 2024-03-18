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

import android.database.ContentObserver
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import androidx.annotation.RequiresApi
import com.hhvvg.libinject.configurations.AllSettings
import com.hhvvg.libinject.configurations.CONTENT_URI

@RequiresApi(Build.VERSION_CODES.N)
class TileControlService : TileService() {

    private var stateEnabled: Boolean
        get() = qsTile.state == Tile.STATE_ACTIVE
        set(value) {
            qsTile.state = if (value) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            qsTile.updateTile()
        }

    private val observer = object : ContentObserver(Handler(Looper.myLooper() ?: Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            stateEnabled = AllSettings.editEnabled.value
        }
    }

    override fun onCreate() {
        super.onCreate()
        contentResolver.registerContentObserver(
            CONTENT_URI,
            true,
            observer
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        contentResolver.unregisterContentObserver(observer)
    }

    override fun onTileAdded() {
        super.onTileAdded()
        stateEnabled = AllSettings.editEnabled.value
    }

    override fun onClick() {
        super.onClick()
        AllSettings.editEnabled.value = !AllSettings.editEnabled.value
    }
}