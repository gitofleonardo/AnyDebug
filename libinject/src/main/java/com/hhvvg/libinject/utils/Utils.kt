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

package com.hhvvg.libinject.utils

import android.view.View
import android.view.ViewGroup
import java.util.regex.Pattern

const val APPLICATION_ID = "com.hhvvg.anydebugv2"

val ltrbPattern = Pattern.compile("^\\[(-?\\d+),(-?\\d+),(-?\\d+),(-?\\d+)]$")

fun <T : ViewGroup> View.findTargetAncestor(targetClass: Class<T>): T? {
    var currParent = parent
    while (currParent != null && !targetClass.isInstance(currParent)) {
        currParent = currParent.parent
    }
    return if (currParent == null || !targetClass.isInstance(currParent)) null else currParent as T
}