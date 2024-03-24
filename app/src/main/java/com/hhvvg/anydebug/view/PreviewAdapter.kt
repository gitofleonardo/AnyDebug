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

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.hhvvg.anydebug.R
import com.hhvvg.anydebug.view.remote.RemoteFactoryLoader

/**
 * Adapter for PreviewList
 */
class PreviewAdapter(context: Context) : Adapter<PreviewHolder>() {

    private var onPreviewClickListener: OnClickListener? = null
    private val previewItems = mutableListOf<ViewItem>()
    private val remoteInflater = RemoteFactoryLoader(context).getRemoteFactory()

    override fun getItemCount(): Int {
        return previewItems.size
    }

    override fun onBindViewHolder(holder: PreviewHolder, position: Int) {
        val item = previewItems[position]
        holder.previewView.setRenderer(item.view)
        holder.previewView.setOnClickListener {
            onPreviewClickListener?.onClick(holder.previewView.getRenderer())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreviewHolder {
        val view =
            remoteInflater.onInflateView(parent.context, "layout_preview_item", parent, false)
        return PreviewHolder(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun removePreviewView(view: View) {
        previewItems.removeIf {
            it.view == view
        }
        notifyDataSetChanged()
    }

    fun setOnClickListener(listener: OnClickListener) {
        onPreviewClickListener = listener
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updatePreviewItems(items: List<ViewItem>) {
        previewItems.clear()
        previewItems.addAll(items)
        notifyDataSetChanged()
    }

}

class PreviewHolder(view: View) : ViewHolder(view) {
    val previewView: PreviewView by lazy {
        view.findViewById<PreviewView>(R.id.preview_item)
    }

}

data class ViewItem(
    val view: View,
)