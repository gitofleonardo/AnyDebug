package com.hhvvg.anydebug.service

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.hhvvg.anydebug.IConfigurationService
import com.hhvvg.anydebug.R
import com.hhvvg.anydebug.receiver.EditControlReceiver
import com.hhvvg.anydebug.util.CONFIGURATION_SERVICE
import com.kaisar.xservicemanager.XServiceManager

/**
 * @author hhvvg
 */
@RequiresApi(Build.VERSION_CODES.N)
class TileControlService : TileService() {
    private val confService by lazy {
        val binder = XServiceManager.getService(CONFIGURATION_SERVICE)
        IConfigurationService.Stub.asInterface(binder)
    }
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

    override fun onTileAdded() {
        // Load initial value
        isEditEnabled = confService.isEditEnabled
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
        confService.isEditEnabled = isEditEnabled

        EditControlReceiver.sendBroadcast(this, isEditEnabled, SOURCE)
    }

    companion object {
        private const val SOURCE = "TileControlServiceSource"
    }
}
