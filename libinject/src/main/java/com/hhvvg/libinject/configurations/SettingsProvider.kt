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

package com.hhvvg.libinject.configurations

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.net.Uri
import android.os.Bundle

const val AUTHORITY = "com.hhvvg.anydebug.provider"

const val METHOD_EDIT_ENABLED = "method_edit_enabled"
const val METHOD_GET = "get"
const val METHOD_SET = "set"

const val KEY_RESULT = "result"
const val KEY_ARG = "arg"

val CONTENT_URI: Uri = Uri.parse("content://${AUTHORITY}/settings")

class SettingsProvider : ContentProvider() {

    private lateinit var preferences: SettingsPreferenceWrapper

    override fun onCreate(): Boolean {
        preferences = SettingsPreferenceWrapper(context!!)
        return true
    }

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        if (arg == null) {
            return null
        }
        return when (arg) {
            METHOD_GET -> handleGetMethod(method, extras)
            METHOD_SET -> handleSetMethod(method, extras)
            else -> super.call(method, arg, extras)
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0

    private fun handleGetMethod(method: String, extras: Bundle?): Bundle? {
        return when (method) {
            METHOD_EDIT_ENABLED -> {
                handleGetMethodEditEnabled()
            }

            else -> null
        }
    }

    private fun handleGetMethodEditEnabled(): Bundle {
        return Bundle().apply {
            putBoolean(KEY_RESULT, preferences.editEnabled)
        }
    }

    private fun handleSetMethod(method: String, extras: Bundle?): Bundle? {
        return when (method) {
            METHOD_EDIT_ENABLED -> {
                handleSetMethodEditEnabled(extras)
                null
            }

            else -> null
        }
    }

    private fun handleSetMethodEditEnabled(extras: Bundle?) {
        val enabled: Boolean = extras?.getBoolean(KEY_ARG, false) ?: return
        preferences.editEnabled = enabled
        context?.contentResolver?.notifyChange(CONTENT_URI, null, false)
    }

}