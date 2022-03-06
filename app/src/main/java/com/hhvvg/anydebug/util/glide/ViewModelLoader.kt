package com.hhvvg.anydebug.util.glide

import android.graphics.Bitmap
import android.view.View
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader

class ViewModelLoader : ModelLoader<View, Bitmap> {
    override fun buildLoadData(
        model: View,
        width: Int,
        height: Int,
        options: Options
    ): ModelLoader.LoadData<Bitmap> {
        val fetcher = ViewDataFetcher(model)
        return ModelLoader.LoadData(options, fetcher)
    }

    override fun handles(model: View): Boolean {
        return model.isLaidOut
    }
}