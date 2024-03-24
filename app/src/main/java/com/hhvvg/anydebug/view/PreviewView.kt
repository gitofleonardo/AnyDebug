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
import android.graphics.Canvas
import android.graphics.RenderNode
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

/**
 * Capture and render other View
 */
class PreviewView(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
    View(context, attrs, defStyleAttr, defStyleRes) {

    private var renderer: View? = null
    private val renderNode: RenderNode = RenderNode("PreviewViewNode")

    constructor(context: Context)
            : this(context, null)

    constructor(context: Context, attrs: AttributeSet?)

            : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : this(context, attrs, defStyleAttr, 0)

    override fun onDraw(canvas: Canvas) {
        renderer?.let { drawRenderer(canvas, it) }
    }

    fun getRenderer(): View? {
        return renderer
    }

    fun setRenderer(view: View?) {
        renderer = view
        renderNode.discardDisplayList()
        invalidate()
    }

    private fun buildDisplayList(renderer: View) {
        val canvas = renderNode.beginRecording(width, height)
        renderNode.setPosition(0, 0, width, height)
        val widthScale = width / renderer.width.toFloat()
        val heightScale = height / renderer.height.toFloat()
        val finalScale = min(widthScale, heightScale)
        val tranX = (width - renderer.width * finalScale) / 2
        val tranY = (height - renderer.height * finalScale) / 2
        try {
            canvas.save()
            canvas.translate(tranX, tranY)
            canvas.scale(finalScale, finalScale)
            renderer.draw(canvas)
            canvas.restore()
        } finally {
            renderNode.endRecording()
        }
    }

    private fun drawRenderer(canvas: Canvas, renderer: View) {
        if (!renderNode.hasDisplayList()) {
            buildDisplayList(renderer)
        }
        if (renderNode.hasDisplayList() && canvas.isHardwareAccelerated) {
            canvas.drawRenderNode(renderNode)
        }
    }

}