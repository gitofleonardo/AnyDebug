package com.hhvvg.anydebug.config

import android.os.Handler
import android.os.HandlerThread
import com.google.gson.Gson
import com.hhvvg.anydebug.IConfigurationService
import java.io.File

/**
 * @author hhvvg
 *
 * Provides configurations for hooked apps.
 */
class ConfigurationManagerService : IConfigurationService.Stub() {
    private val configurations: AppConfiguration
    private val gson = Gson()
    private val workDir by lazy {
        val f = File(WORKING_DIR)
        if (!f.exists()) {
            f.mkdir()
        }
        f
    }
    private val jsonFile by lazy {
        val f = File(workDir, CONF_FILE_NAME)
        if (!f.exists()) {
            f.createNewFile()
        }
        f
    }

    private val workThread by lazy {
        val t = HandlerThread(THREAD_TAG)
        t.start()
        t
    }

    private val workHandler by lazy {
        Handler(workThread.looper)
    }

    init {
        val src = readFromFile(jsonFile)
        configurations = if (src != null) {
            readFromJson(src) ?: AppConfiguration()
        } else {
            AppConfiguration()
        }
    }

    private fun readFromJson(src: String): AppConfiguration? {
        return try {
            gson.fromJson(src, AppConfiguration::class.java)
        }catch (e: Exception) {
            null
        }
    }

    private fun readFromFile(file: File): String? {
        if (!file.exists()) {
            file.createNewFile()
            return null
        }
        return try {
            file.readText()
        }catch (e :Exception) {
            null
        }
    }

    override fun isEditEnabled(): Boolean = configurations.editEnabled

    override fun isPersistentEnabled(): Boolean = configurations.persistentEnabled

    override fun setEditEnabled(enabled: Boolean) {
        configurations.editEnabled = enabled
        apply(configurations)
    }

    override fun setPersistentEnabled(enabled: Boolean) {
        configurations.persistentEnabled = enabled
        apply(configurations)
    }

    private fun apply(conf: AppConfiguration) {
        workHandler.post {
            synchronized(jsonFile) {
                val confJson = gson.toJson(conf)
                jsonFile.writeText(confJson)
            }
        }
    }

    companion object {
        private const val THREAD_TAG = "ConfigurationManagerServiceWorkThread"
        private const val WORKING_DIR = "/data/system/anydebug"
        private const val CONF_FILE_NAME = "anydebug_conf.json"
    }
}
