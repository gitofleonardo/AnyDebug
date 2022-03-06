package com.hhvvg.anydebug.util.glide

import android.graphics.Bitmap
import android.view.View
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory

class ViewLoaderFactory : ModelLoaderFactory<View, Bitmap> {
    override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<View, Bitmap> {
        return ViewModelLoader()
    }

    override fun teardown() {
    }
}