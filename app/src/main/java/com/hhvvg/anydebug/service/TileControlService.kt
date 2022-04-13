package com.hhvvg.anydebug.service

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.hhvvg.anydebug.BuildConfig
import com.hhvvg.anydebug.R
import com.hhvvg.anydebug.config.ConfigDbHelper
import com.hhvvg.anydebug.config.ConfigPreferences
import com.hhvvg.anydebug.receiver.EditControlReceiver

/**
 * @author hhvvg
 */
@RequiresApi(Build.VERSION_CODES.N)
class TileControlService : TileService() {
    private val configSp by lazy { ConfigPreferences(this) }
    private var isEditEnabled
        get() = qsTile.state == Tile.STATE_ACTIVE
        set(value) {
            qsTile.state = if (value) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                qsTile.subtitle = if (value) getString(R.string.enabled) else getString(R.string.disabled)
            }
            qsTile.updateTile()
        }
    private val stateReceiver by lazy {
        EditControlReceiver { enabled, source ->
            if (source != SOURCE) {
                isEditEnabled = enabled
            }
        }
    }
    private val appSp by lazy {
        getSharedPreferences("${BuildConfig.PACKAGE_NAME}_preferences", MODE_PRIVATE)
    }

    override fun onTileAdded() {
        // Load initial value
        isEditEnabled = configSp.getBoolean(ConfigDbHelper.CONFIG_EDIT_ENABLED_COLUMN, false)
    }

    override fun onCreate() {
        super.onCreate()
        EditControlReceiver.register(stateReceiver, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(stateReceiver)
    }

    override fun onClick() {
        isEditEnabled = !isEditEnabled
        val edit = configSp.edit()
        edit.putBoolean(ConfigDbHelper.CONFIG_EDIT_ENABLED_COLUMN, isEditEnabled)
        edit.apply()

        // Update local sp as well
        val appEdit = appSp.edit()
        appEdit.putBoolean(getString(R.string.global_edit_enable_key), isEditEnabled)
        appEdit.apply()

        EditControlReceiver.sendBroadcast(this, isEditEnabled, SOURCE)
    }

    companion object {
        private const val SOURCE = "TileControlServiceSource"
    }
}
