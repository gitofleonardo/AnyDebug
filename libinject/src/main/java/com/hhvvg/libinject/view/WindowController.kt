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

import android.animation.Animator
import android.graphics.PixelFormat
import android.graphics.PointF
import android.graphics.Rect
import android.util.FloatProperty
import android.view.GestureDetector
import android.view.GestureDetector.OnGestureListener
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.addListener
import androidx.dynamicanimation.animation.DynamicAnimation
import com.hhvvg.libinject.R
import com.hhvvg.libinject.utils.OverScroll
import com.hhvvg.libinject.utils.SpringAnimationBuilder
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class WindowController(
    private val window: Window,
    private val windowClient: WindowClient
) : OnGestureListener {

    companion object {
        private const val FLING_VELOCITY_THRESHOLD = 3000f
        private const val MAX_FLING_VELOCITY = 30000f
        private const val FLING_STIFFNESS = 200f
        private const val FLING_DAMPING_RATIO = .80f

        const val STATE_MINI_WINDOW = 0
        const val STATE_MAX_WINDOW = 1

        private const val MAX_WINDOW_WIDTH_RATIO = 3f / 4f
        private const val MAX_WINDOW_HEIGHT_RATIO = .7f

        private const val WINDOW_INIT_Y_RATIO = .25f

        private val tempRect = Rect()

        private val WINDOW_X_PROPERTY =
            object : FloatProperty<WindowController>("window_x_property") {
                override fun get(window: WindowController): Float {
                    return window.windowX.toFloat()
                }

                override fun setValue(window: WindowController, value: Float) {
                    window.windowX = value.toInt()
                    window.updateWindow()
                }
            }

        private val WINDOW_Y_PROPERTY =
            object : FloatProperty<WindowController>("window_y_property") {
                override fun get(window: WindowController): Float {
                    return window.windowY.toFloat()
                }

                override fun setValue(window: WindowController, value: Float) {
                    window.windowY = value.toInt()
                    window.updateWindow()
                }
            }

        private val WINDOW_WIDTH_PROPERTY =
            object : FloatProperty<WindowController>("window_width_property") {
                override fun get(window: WindowController): Float {
                    return if (window.windowParams.width > 0)
                        window.windowParams.width.toFloat()
                    else run {
                        window.decorView.getLocalVisibleRect(tempRect)
                        tempRect.width().toFloat()
                    }
                }

                override fun setValue(window: WindowController, value: Float) {
                    window.windowParams.width = value.toInt()
                    window.updateWindow()
                }
            }

        private val WINDOW_HEIGHT_PROPERTY =
            object : FloatProperty<WindowController>("window_width_property") {
                override fun get(window: WindowController): Float {
                    return if (window.windowParams.height > 0)
                        window.windowParams.height.toFloat()
                    else run {
                        window.decorView.getLocalVisibleRect(tempRect)
                        tempRect.height().toFloat()
                    }
                }

                override fun setValue(window: WindowController, value: Float) {
                    window.windowParams.height = value.toInt()
                    window.updateWindow()
                }
            }
    }

    private val windowParams by lazy { window.attributes }
    private val decorView: View
        get() = window.decorView
    private var windowState = STATE_MINI_WINDOW
        set(value) {
            field = value
            windowClient.onWindowStateChanged(value)
        }
    private val undampedWindowTranslation = PointF()
    private val maxFloatingWindowDragTranslation by lazy {
        parentWindowFrame.height()
    }
    private var dockToEdgeAnimationX: Animator? = null
    private var dockToEdgeAnimationY: Animator? = null
    private var widthAnimation: Animator? = null
    private var heightAnimation: Animator? = null
    private val runningDockingAnimation: Boolean
        get() = dockToEdgeAnimationX != null && dockToEdgeAnimationY != null
    private val gestureDetector = GestureDetector(window.context, this)
    private val downPoint = PointF()
    private val lastMovePoint = PointF()
    private val context by lazy { window.context }
    private val parentWindowFrame: Rect
        get() = run {
            windowClient.getParentWindowVisibleFrame()
        }
    private var windowX = 0
        set(value) {
            field = value
            updateLocationParams()
            updateWindow()
        }
    private var windowY = 0
        set(value) {
            field = value
            updateLocationParams()
            updateWindow()
        }
    private var windowOffsetX = 0
        set(value) {
            field = value
            updateLocationParams()
            updateWindow()
        }
    private var windowOffsetY = 0
        set(value) {
            field = value
            updateLocationParams()
            updateWindow()
        }

    fun configureWindowParams() = with(windowParams) {
        width = WindowManager.LayoutParams.WRAP_CONTENT
        height = WindowManager.LayoutParams.WRAP_CONTENT
        windowX = 0
        windowY = run {
            (parentWindowFrame.height() * WINDOW_INIT_Y_RATIO).toInt()
        }
        format = PixelFormat.TRANSPARENT
        gravity = Gravity.TOP or Gravity.START
        flags =
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        window.setBackgroundDrawableResource(android.R.color.transparent)
        window.setElevation(
            context.resources.getDimensionPixelSize(R.dimen.decor_elevation).toFloat()
        )
    }

    private fun updateLocationParams() = with(windowParams) {
        x = windowX + windowOffsetX
        y = windowY + windowOffsetY
    }

    private fun updateWindow() {
        windowClient.updateWindowAttributes(windowParams)
    }

    fun onTouchEvent(event: MotionEvent) {
        gestureDetector.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                cancelAllAnimations()
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (!runningDockingAnimation) {
                    if (windowState == STATE_MINI_WINDOW) {
                        dockToEdge(0f, 0f)
                    } else if (windowState == STATE_MAX_WINDOW) {
                        moveMaximizeWindowCenter()
                    }
                }
            }
        }
    }

    private fun dockToEdge(velX: Float, velY: Float) {
        animateWindowPosition(
            calcMiniWindowFinalX(velX),
            calcMiniWindowFinalY(velY),
            -velX,
            -velY
        )
    }

    private fun calcMiniWindowFinalX(velX: Float): Float {
        val parentFrame = Rect(parentWindowFrame)
        val parentWindowWidth = parentFrame.width().toFloat()
        val dockLeftX = 0f
        val miniWindowSize = calcMiniWindowSize()
        val dockRightX = parentWindowWidth - miniWindowSize.x
        if (abs(velX) >= FLING_VELOCITY_THRESHOLD) {
            return if (velX > 0) dockLeftX else dockRightX
        }
        val windowCenterX = windowX + windowParams.width / 2f
        return if (windowCenterX > parentWindowWidth / 2f) dockRightX else dockLeftX
    }

    private fun calcMiniWindowFinalY(velY: Float): Float {
        val parentFrame = Rect(parentWindowFrame)
        val parentWindowHeight = parentFrame.height().toFloat()
        val dockTopY = 0f
        val miniWindowSize = calcMiniWindowSize()
        val dockBottomY = parentWindowHeight - miniWindowSize.y
        val interpolator = DecelerateInterpolator()
        val fractionVel = min(abs(velY), MAX_FLING_VELOCITY) / MAX_FLING_VELOCITY
        val interpolatedPositionFraction = interpolator.getInterpolation(fractionVel)
        val maxTranYTop = windowY - dockTopY
        val maxTranYBottom = dockBottomY - windowY
        if (abs(velY) >= FLING_VELOCITY_THRESHOLD) {
            return if (velY > 0) {
                windowY - interpolatedPositionFraction * maxTranYTop
            } else {
                windowY + interpolatedPositionFraction * maxTranYBottom
            }
        }
        return max(dockTopY, min(windowY.toFloat(), dockBottomY))
    }

    private fun animateWindowSize(finalWidth: Float, finalHeight: Float) {
        widthAnimation?.cancel()
        heightAnimation?.cancel()
        widthAnimation = SpringAnimationBuilder(context)
            .setDampingRatio(FLING_DAMPING_RATIO)
            .setStiffness(FLING_STIFFNESS)
            .setEndValue(finalWidth)
            .setMinimumVisibleChange(DynamicAnimation.MIN_VISIBLE_CHANGE_PIXELS)
            .setStartValue(WINDOW_WIDTH_PROPERTY.get(this))
            .build(this, WINDOW_WIDTH_PROPERTY).apply {
                addListener(onEnd = {
                    widthAnimation = null
                })
                start()
            }
        heightAnimation = SpringAnimationBuilder(context)
            .setDampingRatio(FLING_DAMPING_RATIO)
            .setStiffness(FLING_STIFFNESS)
            .setEndValue(finalHeight)
            .setMinimumVisibleChange(DynamicAnimation.MIN_VISIBLE_CHANGE_PIXELS)
            .setStartValue(WINDOW_HEIGHT_PROPERTY.get(this))
            .build(this, WINDOW_HEIGHT_PROPERTY).apply {
                addListener(onEnd = {
                    heightAnimation = null
                })
                start()
            }
    }

    private fun moveMaximizeWindowCenter() {
        val maxWindowPosition = calcMaximizeWindowPosition()
        val finalX = maxWindowPosition.x
        val finalY = maxWindowPosition.y
        animateWindowPosition(finalX, finalY, 0f, 0f)
    }

    private fun animateWindowPosition(
        finalX: Float,
        finalY: Float,
        velocityX: Float,
        velocityY: Float
    ) {
        dockToEdgeAnimationX?.cancel()
        dockToEdgeAnimationY?.cancel()
        dockToEdgeAnimationX = SpringAnimationBuilder(context)
            .setDampingRatio(FLING_DAMPING_RATIO)
            .setStiffness(FLING_STIFFNESS)
            .setEndValue(finalX)
            .setMinimumVisibleChange(DynamicAnimation.MIN_VISIBLE_CHANGE_PIXELS)
            .setStartVelocity(velocityX)
            .setStartValue(WINDOW_X_PROPERTY.get(this))
            .build(this, WINDOW_X_PROPERTY).apply {
                addListener(onEnd = {
                    dockToEdgeAnimationX = null
                })
                start()
            }
        dockToEdgeAnimationY = SpringAnimationBuilder(context)
            .setDampingRatio(FLING_DAMPING_RATIO)
            .setStiffness(FLING_STIFFNESS)
            .setEndValue(finalY)
            .setMinimumVisibleChange(DynamicAnimation.MIN_VISIBLE_CHANGE_PIXELS)
            .setStartVelocity(velocityY)
            .setStartValue(WINDOW_Y_PROPERTY.get(this))
            .build(this, WINDOW_Y_PROPERTY).apply {
                addListener(onEnd = {
                    dockToEdgeAnimationY = null
                })
                start()
            }
    }

    private fun calcMaximizeWindowPosition(): PointF {
        val parentFrame = windowClient.getParentWindowVisibleFrame()
        val parentWindowWidth = parentFrame.width()
        val parentWindowHeight = parentFrame.height()
        val finalX = parentWindowWidth * (1 - MAX_WINDOW_WIDTH_RATIO) * .5f
        val finalY = parentWindowHeight * (1 - MAX_WINDOW_HEIGHT_RATIO) * .5f
        return PointF(finalX, finalY)
    }

    private fun calcMiniWindowSize(): PointF {
        val parentFrame = windowClient.getParentWindowVisibleFrame()
        val parentWindowWidth = parentFrame.width()
        val parentWindowHeight = parentFrame.height()
        val widthSpec =
            View.MeasureSpec.makeMeasureSpec(parentWindowWidth, View.MeasureSpec.AT_MOST)
        val heightSpec =
            View.MeasureSpec.makeMeasureSpec(parentWindowHeight, View.MeasureSpec.AT_MOST)
        decorView.measure(widthSpec, heightSpec)
        val size = PointF()
        decorView.let {
            size.set(it.measuredWidth.toFloat(), it.measuredHeight.toFloat())
        }
        return size
    }

    fun minimizeWindow(velocityX: Float = 0f, velocityY: Float = 0f) {
        if (windowState == STATE_MINI_WINDOW) {
            return
        }
        windowState = STATE_MINI_WINDOW
        windowParams.flags = windowParams.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        val miniWidowSize = calcMiniWindowSize()
        val finalWidth = miniWidowSize.x
        val finalHeight = miniWidowSize.y
        animateWindowSize(finalWidth, finalHeight)
        dockToEdge(velocityX, velocityY)
    }

    fun maximizeWindow() {
        if (windowState == STATE_MAX_WINDOW) {
            return
        }
        windowState = STATE_MAX_WINDOW
        windowParams.flags =
            windowParams.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
        val parentFrame = parentWindowFrame
        val parentWindowWidth = parentFrame.width()
        val parentWindowHeight = parentFrame.height()
        val finalWidth = parentWindowWidth * MAX_WINDOW_WIDTH_RATIO
        val finalHeight = parentWindowHeight * MAX_WINDOW_HEIGHT_RATIO
        animateWindowSize(finalWidth, finalHeight)
        moveMaximizeWindowCenter()
    }

    private fun cancelAllAnimations() {
        dockToEdgeAnimationX?.cancel()
        dockToEdgeAnimationY?.cancel()
        widthAnimation?.cancel()
        heightAnimation?.cancel()
    }

    override fun onDown(e: MotionEvent): Boolean {
        dockToEdgeAnimationX?.cancel()
        dockToEdgeAnimationY?.cancel()
        downPoint.set(e.rawX, e.rawY)
        lastMovePoint.set(downPoint)
        undampedWindowTranslation.set(0f, 0f)
        return false
    }

    override fun onShowPress(e: MotionEvent) {}

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return false
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        val downDiffX = e2.rawX - lastMovePoint.x
        val downDiffY = e2.rawY - lastMovePoint.y
        lastMovePoint.set(e2.rawX, e2.rawY)
        if (windowState == STATE_MINI_WINDOW) {
            windowX += downDiffX.toInt()
            windowY += downDiffY.toInt()
            updateWindow()
        } else {
            undampedWindowTranslation.set(
                undampedWindowTranslation.x + downDiffX,
                undampedWindowTranslation.y + downDiffY
            )
            val dampedTranX = OverScroll.dampedScroll(
                undampedWindowTranslation.x,
                maxFloatingWindowDragTranslation
            )
            val dampedTranY = OverScroll.dampedScroll(
                undampedWindowTranslation.y,
                maxFloatingWindowDragTranslation
            )
            val maximizeWindowPos = calcMaximizeWindowPosition()
            windowX = (maximizeWindowPos.x + dampedTranX).toInt()
            windowY = (maximizeWindowPos.y + dampedTranY).toInt()
            updateWindow()
        }
        return true
    }

    override fun onLongPress(e: MotionEvent) {}

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        if (windowState == STATE_MINI_WINDOW) {
            dockToEdge(-velocityX, -velocityY)
        } else {
            minimizeWindow(-velocityX, -velocityY)
        }
        return true
    }
}