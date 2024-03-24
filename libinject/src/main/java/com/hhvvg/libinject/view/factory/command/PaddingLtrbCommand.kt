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

package com.hhvvg.libinject.view.factory.command

import android.view.View
import com.hhvvg.libinject.utils.ltrbPattern

/**
 * Command for setting paddings
 */
class PaddingLtrbCommand(view: View, private val ltrb: CharSequence) : BaseCommand<View>(view) {

    override fun onApply() {
        val matcher = ltrbPattern.matcher(ltrb)
        var l = 0
        var t = 0
        var r = 0
        var b = 0
        if (matcher.find()) {
            l = matcher.group(1)?.toInt() ?: 0
            t = matcher.group(2)?.toInt() ?: 0
            r = matcher.group(3)?.toInt() ?: 0
            b = matcher.group(4)?.toInt() ?: 0
        }
        targetView.setPadding(l, t, r, b)
    }
}