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

package com.hhvvg.anydebug.view.factory.command

import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import com.hhvvg.anydebug.utils.ltrbPattern

/**
 * Command for settings margins
 */
class MarginLtrbCommand(view: View, private val ltrb: CharSequence) : BaseCommand<View>(view) {

    override fun onApply() {
        val params = targetView.layoutParams
        if (params !is MarginLayoutParams) {
            return
        }
        val matcher = ltrbPattern.matcher(ltrb)
        if (matcher.find()) {
            params.leftMargin = matcher.group(1)?.toInt() ?: 0
            params.topMargin = matcher.group(2)?.toInt() ?: 0
            params.rightMargin = matcher.group(3)?.toInt() ?: 0
            params.bottomMargin = matcher.group(4)?.toInt() ?: 0
        }
        targetView.layoutParams = params
    }
}