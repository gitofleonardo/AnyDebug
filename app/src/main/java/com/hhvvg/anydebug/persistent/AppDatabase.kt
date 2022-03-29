package com.hhvvg.anydebug.persistent

import android.app.AndroidAppHelper
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [ViewRule::class], version = 1)
@TypeConverters(RuleTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun viewRuleDao(): ViewRuleDao

    companion object {
        private const val DB_NAME = "local_view_rules_database.db"

        @JvmStatic
        val instance by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            val context = AndroidAppHelper.currentApplication().applicationContext
            Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME).build()
        }

        @JvmStatic
        val viewRuleDao by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            instance.viewRuleDao()
        }
    }
}