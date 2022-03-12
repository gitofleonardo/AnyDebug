package com.hhvvg.anydebug.glide

import android.app.AndroidAppHelper
import android.graphics.Bitmap
import android.os.Message
import android.view.View
import androidx.core.view.drawToBitmap
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import com.hhvvg.anydebug.util.*

/**
 * @author hhvvg
 *
 * Fetch bitmap from view.
 * Caution: This core method to fetch data runs in main thread to avoid concurrency exception,
 * so ensure that you don't have lots of views to be drawn to bitmap at a time to avoid ANR.
 */
class ViewDataFetcher(private val view: View) : DataFetcher<Bitmap>, HandlerCallback {
    private val handler = WeakHandler(this)
    private var callback: DataFetcher.DataCallback<in Bitmap>? = null

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Bitmap>) {
        this.callback = callback
        handler.sendEmptyMessage(0)
    }

    override fun cleanup() {
        //Do nothing
    }

    override fun cancel() {
        // Do nothing
    }

    override fun getDataClass(): Class<Bitmap> = Bitmap::class.java

    override fun getDataSource(): DataSource = DataSource.MEMORY_CACHE

    override fun onHandleMessage(msg: Message) {
        try {
            // Disable bounds
            view.drawLayoutBounds(drawEnabled = false, traversalChildren = true, invalidate = false)
            val bitmap = view.drawToBitmap()

            //Restore origin bounds state
            val drawBounds = AndroidAppHelper.currentApplication().getInjectedField(
                APP_FIELD_SHOW_BOUNDS, false
            ) ?: false
            view.drawLayoutBounds(
                drawEnabled = drawBounds,
                traversalChildren = true,
                invalidate = false
            )
            callback?.onDataReady(bitmap)
        } catch (e: Exception) {
            callback?.onLoadFailed(e)
        }
    }
}
