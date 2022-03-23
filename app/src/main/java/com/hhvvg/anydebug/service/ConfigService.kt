package com.hhvvg.anydebug.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.hhvvg.anydebug.IConfigManager

/**
 * @author hhvvg
 *
 * Service for providing configurations.
 */
class ConfigService : Service() {
    private lateinit var binder: IBinder
    private val context: Context
        get() = applicationContext

    override fun onCreate() {
        super.onCreate()
        binder = ConfigManagerImpl()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    private inner class ConfigManagerImpl : IConfigManager.Stub() {
        override fun isEditEnable(): Boolean {
            val sp = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
            return sp.getBoolean("global_edit_enable_key", false)
        }

        override fun isPersistentEnable(): Boolean {
            val sp = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
            return sp.getBoolean("edit_persistent_key", false)
        }
    }
}