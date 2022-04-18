package com.hhvvg.anydebug.config

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.hhvvg.anydebug.config.ConfigDbHelper.Companion.CONFIG_EDIT_ENABLED_COLUMN
import com.hhvvg.anydebug.config.ConfigDbHelper.Companion.CONFIG_PERSISTENT_ENABLED_COLUMN

/**
 * @author hhvvg
 */
class ConfigPreferences(private val context: Context) : SharedPreferences {

    /**
     * Read from ContentProvider only when first accessing it.
     */
    private val configurations: MutableMap<String, Any> by lazy {
        val resolver = context.contentResolver
        val url = "content://$AUTHORITIES/config"
        val uri = Uri.parse(url)
        val cursor =
            resolver.query(uri, null, "${ConfigDbHelper.CONFIG_ID_COLUMN}=?", arrayOf("0"), null) ?: return@lazy mutableMapOf()
        if (cursor.moveToFirst()) {
            val editEnabled = cursor.getInt(cursor.getColumnIndexOrThrow(CONFIG_EDIT_ENABLED_COLUMN))
            val persistentEnabled = cursor.getInt(cursor.getColumnIndexOrThrow(
                CONFIG_PERSISTENT_ENABLED_COLUMN))
            cursor.close()
            return@lazy mutableMapOf(
                CONFIG_EDIT_ENABLED_COLUMN to (editEnabled == 1),
                CONFIG_PERSISTENT_ENABLED_COLUMN to (persistentEnabled == 1)
            )
        }
        cursor.close()
        mutableMapOf()
    }

    override fun getAll(): MutableMap<String, *> = configurations

    override fun getString(key: String?, defValue: String?): String? {
        val c = configurations[key]
        return c as String? ?: defValue
    }

    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? {
        throw IllegalArgumentException("StringSet not supported")
    }

    override fun getInt(key: String?, defValue: Int): Int {
        val c = configurations[key]
        return c as Int? ?: defValue
    }

    override fun getLong(key: String?, defValue: Long): Long {
        val c = configurations[key]
        return c as Long? ?: defValue
    }

    override fun getFloat(key: String?, defValue: Float): Float {
        val c = configurations[key]
        return c as Float? ?: defValue
    }

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        val c = configurations[key]
        return c as Boolean? ?: defValue
    }

    override fun contains(key: String?): Boolean {
        return configurations.containsKey(key)
    }

    override fun edit(): SharedPreferences.Editor {
        return ConfigEditImpl(context.contentResolver)
    }

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
    }

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
    }

    companion object {
        private const val AUTHORITIES = "com.hhvvg.anydebug.config"
    }

    class ConfigEditImpl(private val resolver: ContentResolver) : SharedPreferences.Editor {
        private val contentValues = ContentValues()

        override fun putString(key: String?, value: String?): SharedPreferences.Editor {
            contentValues.put(key, value)
            return this
        }

        override fun putStringSet(
            key: String?,
            values: MutableSet<String>?
        ): SharedPreferences.Editor {
            throw IllegalArgumentException("StringSet not supported")
        }

        override fun putInt(key: String?, value: Int): SharedPreferences.Editor {
            contentValues.put(key, value)
            return this
        }

        override fun putLong(key: String?, value: Long): SharedPreferences.Editor {
            contentValues.put(key, value)
            return this
        }

        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor {
            contentValues.put(key, value)
            return this
        }

        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor {
            contentValues.put(key, value)
            return this
        }

        override fun remove(key: String?): SharedPreferences.Editor {
            contentValues.remove(key)
            return this
        }

        override fun clear(): SharedPreferences.Editor {
            contentValues.clear()
            return this
        }

        override fun commit(): Boolean {
            update()
            return true
        }

        override fun apply() {
            update()
        }

        private fun update() {
            val url = "content://$AUTHORITIES/config"
            val uri = Uri.parse(url)
            resolver.update(uri, contentValues, "${ConfigDbHelper.CONFIG_ID_COLUMN}=?", arrayOf("0"))
        }
    }
}