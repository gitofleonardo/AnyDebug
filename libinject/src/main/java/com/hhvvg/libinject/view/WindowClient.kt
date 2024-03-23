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

package com.hhvvg.libinject.view

import android.graphics.Insets
import android.graphics.Rect
import android.view.WindowManager

interface WindowClient {
    fun updateWindowAttributes(attr: WindowManager.LayoutParams)
    fun getParentWindowVisibleFrame(): Rect
    fun onWindowStateChanged(state: Int)
    fun onWindowInsetsChanged(insets: Insets)
    fun onRequestMaxWindowSize(width: Int, height: Int)
    fun onWindowWidthChanged(
        startWidth: Float,
        endWidth: Float,
        minWidth: Float,
        maxWidth: Float,
        width: Float
    )

    fun onWindowHeightChanged(
        startHeight: Float,
        endHeight: Float,
        minHeight: Float,
        maxHeight: Float,
        height: Float
    )

    fun onStateSizeAnimationEnd(state: Int)
}