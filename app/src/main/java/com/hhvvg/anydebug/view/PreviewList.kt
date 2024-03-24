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

package com.hhvvg.anydebug.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.hhvvg.anydebug.R
import com.rbrooks.indefinitepagerindicator.IndefinitePagerIndicator

/**
 * Preview view for a set of views
 */
class PreviewList(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    RecyclerView(context, attrs, defStyleAttr) {

    private val indicatorView: IndefinitePagerIndicator?
        get() = (parent as View).findViewById(R.id.page_indicator)

    private val previewAdapter = PreviewAdapter(context)

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?)

            : this(context, attrs, 0)

    init {
        adapter = previewAdapter
        layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
        LinearSnapHelper().attachToRecyclerView(this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        indicatorView?.attachToRecyclerView(this)
    }

    fun removePreviewView(view: View) {
        previewAdapter.removePreviewView(view)
        indicatorView?.requestLayout()
    }

    fun setOnPreviewClickListener(listener: OnClickListener) {
        previewAdapter.setOnClickListener(listener)
    }

    fun updatePreviewItems(items: List<View>) {
        previewAdapter.updatePreviewItems(items.map { ViewItem(it) })
        indicatorView?.requestLayout()
    }

}