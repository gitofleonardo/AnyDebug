package com.hhvvg.anydebug.util.glide

import android.graphics.Bitmap
import android.view.View
import androidx.core.view.drawToBitmap
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher

class ViewDataFetcher(private val view: View) : DataFetcher<Bitmap> {
    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Bitmap>) {
        try {
            val bitmap = view.drawToBitmap()
            callback.onDataReady(bitmap)
        }catch (e: Exception) {
            callback.onLoadFailed(e)
        }
    }

    override fun cleanup() {
    }

    override fun cancel() {
    }

    override fun getDataClass(): Class<Bitmap> = Bitmap::class.java

    override fun getDataSource(): DataSource = DataSource.MEMORY_CACHE
}