package com.hhvvg.anydebug.config

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * @author hhvvg
 */
class ConfigDbHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_TABLE)
        // Init table with default values
        db?.execSQL("INSERT INTO $DB_CONFIG_TABLE_NAME values(0, 0, 0)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $DB_CONFIG_TABLE_NAME")
        onCreate(db)
    }

    companion object {
        const val DB_NAME = "ConfigDatabase"
        const val DB_CONFIG_TABLE_NAME = "ConfigTable"
        const val CONFIG_ID_COLUMN = "config_id"
        const val CONFIG_EDIT_ENABLED_COLUMN = "global_edit_enable_key"
        const val CONFIG_PERSISTENT_ENABLED_COLUMN = "edit_persistent_key"
        const val DB_VERSION = 1

        const val CREATE_TABLE = "CREATE TABLE $DB_CONFIG_TABLE_NAME ($CONFIG_ID_COLUMN int, $CONFIG_EDIT_ENABLED_COLUMN int, $CONFIG_PERSISTENT_ENABLED_COLUMN int)"
    }
}