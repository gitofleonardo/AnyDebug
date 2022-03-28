package com.hhvvg.anydebug.config

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import com.hhvvg.anydebug.config.ConfigDbHelper.Companion.DB_CONFIG_TABLE_NAME

/**
 * @author hhvvg
 *
 * Provides app configurations, which supports reading only.
 */
class ConfigContentProvider : ContentProvider() {
    private lateinit var dbHelper: ConfigDbHelper
    private lateinit var rwDatabase: SQLiteDatabase

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int  = VALUE_IGNORED

    override fun getType(uri: Uri): String = DATA_TYPE

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun onCreate(): Boolean {
        dbHelper = ConfigDbHelper(context!!)
        rwDatabase = dbHelper.writableDatabase
        return true
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        return rwDatabase.query(DB_CONFIG_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder)
    }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        return rwDatabase.update(DB_CONFIG_TABLE_NAME, values, selection, selectionArgs)
    }

    companion object {
        const val DATA_TYPE = "config"
        const val VALUE_IGNORED = -1
    }
}