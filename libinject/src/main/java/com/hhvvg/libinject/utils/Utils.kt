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

import android.content.Context
import android.graphics.Point
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import java.util.regex.Pattern


const val APPLICATION_ID = "com.hhvvg.anydebug"

/**
 * Regex pattern for parsing padding/margin
 */
val ltrbPattern: Pattern = Pattern.compile("^\\[(-?\\d+),(-?\\d+),(-?\\d+),(-?\\d+)]$")

/**
 * Padding format in string
 */
val View.paddingLtrb: String
    get() = "[${paddingLeft},${paddingTop},${paddingRight},${paddingBottom}]"

/**
 * Margin format in string
 */
val ViewGroup.MarginLayoutParams.ltrb: String
    get() = "[${leftMargin},${topMargin},${rightMargin},${bottomMargin}]"

/**
 * Find ancestor for target view with a specific class.
 */
fun <T : ViewGroup> View.findTargetAncestor(targetClass: Class<T>): T? {
    var currParent = parent
    while (currParent != null && !targetClass.isInstance(currParent)) {
        currParent = currParent.parent
    }
    return if (currParent == null || !targetClass.isInstance(currParent)) null else currParent as T
}

/**
 * Create package context of this app
 */
fun Context.createRemotePackageContext(): Context {
    val packageContext = createPackageContext(
        APPLICATION_ID,
        Context.CONTEXT_INCLUDE_CODE or Context.CONTEXT_IGNORE_SECURITY
    )
    val themeResId =
        packageContext.resources.getIdentifier("AppTheme", "style", packageContext.packageName)
    return ContextThemeWrapper(packageContext, themeResId)
}

/**
 * Get drawable resource id
 */
fun Context.drawableResId(name: String): Int {
    return resources.getIdentifier(name, "drawable", packageName)
}

/**
 * Get dimen resource id
 */
fun Context.dimenResId(name: String): Int {
    return resources.getIdentifier(name, "dimen", packageName)
}

/**
 * Get default display screen size
 */
fun Context.screenSize(): Point {
    val display = (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
    val outPoint = Point()
    display.getRealSize(outPoint)
    return outPoint
}