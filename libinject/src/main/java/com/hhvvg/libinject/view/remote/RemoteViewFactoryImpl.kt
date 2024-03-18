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

package com.hhvvg.libinject.view.remote

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.hhvvg.libinject.utils.APPLICATION_ID

class RemoteViewFactoryImpl : RemoteViewFactory {

    override fun onInflateView(context: Context, name: String): View {
        val packageContext = context.createPackageContext(APPLICATION_ID,
            Context.CONTEXT_INCLUDE_CODE or Context.CONTEXT_IGNORE_SECURITY)
        val resId = packageContext.resources
            .getIdentifier(name, "layout", packageContext.packageName)
        if (resId > 0) {
            return LayoutInflater.from(packageContext).inflate(resId, null, false)
        }
        throw RuntimeException("Error inflating remote layout.")
    }
}