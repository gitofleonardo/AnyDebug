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

import android.content.Context
import android.os.Bundle
import java.lang.IllegalArgumentException

class SettingItem<T>(
    val key: String,
    private val type: Class<*>,
    private val context: Context
) {

    var value: T
        get() = getAsType()
        set(value) = setAsType(value)

    private fun getAsType(): T {
        return when (type) {
            Int::class.java -> {
                (context.contentResolver.call(CONTENT_URI, key, METHOD_GET, null)
                    ?.getInt(KEY_RESULT, 0) ?: 0) as T
            }

            Boolean::class.java -> {
                (context.contentResolver.call(CONTENT_URI, key, METHOD_GET, null)
                    ?.getBoolean(KEY_RESULT, false) ?: false) as T
            }

            Long::class.java -> {
                (context.contentResolver.call(CONTENT_URI, key, METHOD_GET, null)
                    ?.getLong(KEY_RESULT, 0) ?: 0) as T
            }

            String::class.java -> {
                (context.contentResolver.call(CONTENT_URI, key, METHOD_GET, null)
                    ?.getString(KEY_RESULT, "")) as T
            }

            else -> throw IllegalArgumentException()
        }
    }

    private fun setAsType(value: T) {
        when (type) {
            Int::class.java -> {
                context.contentResolver.call(CONTENT_URI, key, METHOD_SET, Bundle()
                    .apply { putInt(KEY_ARG, value as Int) })
            }

            Boolean::class.java -> {
                context.contentResolver.call(CONTENT_URI, key, METHOD_SET, Bundle()
                    .apply { putBoolean(KEY_ARG, value as Boolean) })
            }

            Long::class.java -> {
                context.contentResolver.call(CONTENT_URI, key, METHOD_SET, Bundle()
                    .apply { putLong(KEY_ARG, value as Long) })
            }

            String::class.java -> {
                context.contentResolver.call(CONTENT_URI, key, METHOD_SET, Bundle()
                    .apply { putString(KEY_ARG, value as String) })
            }

            else -> throw IllegalArgumentException()
        }
    }

}