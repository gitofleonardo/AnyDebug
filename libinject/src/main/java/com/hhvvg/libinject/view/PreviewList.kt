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

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.hhvvg.libinject.R
import com.rbrooks.indefinitepagerindicator.IndefinitePagerIndicator
import java.util.function.Consumer

class PreviewList(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    RecyclerView(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?)

            : this(context, attrs, 0)

    constructor(context: Context)
            : this(context, null)

    private val previewAdapter = PreviewAdapter(context)
    private val indicatorView: IndefinitePagerIndicator?
        get() = (parent as View).findViewById(R.id.page_indicator)

    init {
        adapter = previewAdapter
        layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
        LinearSnapHelper().attachToRecyclerView(this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        indicatorView?.attachToRecyclerView(this)
    }

    fun updatePreviewItems(items: List<View>) {
        previewAdapter.updatePreviewItems(items.map { ViewItem(it) })
        indicatorView?.requestLayout()
    }

    fun setOnPreviewClickListener(listener: OnClickListener) {
        previewAdapter.setOnClickListener(listener)
    }
}