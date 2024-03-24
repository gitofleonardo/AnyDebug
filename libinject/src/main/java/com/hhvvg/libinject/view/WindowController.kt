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
import android.animation.AnimatorSet
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.util.FloatProperty
import android.view.GestureDetector
import android.view.GestureDetector.OnGestureListener
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager.LayoutParams
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.addListener
import androidx.dynamicanimation.animation.DynamicAnimation
import com.hhvvg.libinject.R
import com.hhvvg.libinject.utils.OverScroll
import com.hhvvg.libinject.utils.SpringAnimationBuilder
import com.hhvvg.libinject.utils.createRemotePackageContext
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Controls position and size of window
 */
class WindowController(
    private val window: Window,
    private val windowClient: WindowClient,
) : OnGestureListener {

    private var dockToEdgeAnimationX: Animator? = null
    private var dockToEdgeAnimationY: Animator? = null
    private var sizeChangeAnimator: AnimatorSet? = null

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

    private var windowState = STATE_MINI_WINDOW
        set(value) {
            field = value
            windowClient.onWindowStateChanged(value)
        }

    private var windowX = 0
        set(value) {
            field = value
            updateLocationParams()
            updateWindow()
        }

    private var windowContentWidth = 0
        set(value) {
            field = value
            updateWindowContent()
        }
    private var windowContentHeight = 0
        set(value) {
            field = value
            updateWindowContent()
        }

    private val context by lazy { window.context }

    private val maxFloatingWindowDragTranslation by lazy {
        parentWindowFrame.height()
    }

    private val remoteContext by lazy { context.createRemotePackageContext() }
    private val windowParams by lazy { window.attributes }

    private val decorView: View
        get() = window.decorView

    private val downPoint = PointF()
    private val gestureDetector = GestureDetector(window.context, this)
    private val lastMovePoint = PointF()

    private val parentWindowFrame: Rect
        get() = run {
            windowClient.getParentWindowVisibleFrame()
        }

    private val runningDockingAnimation: Boolean
        get() = dockToEdgeAnimationX != null && dockToEdgeAnimationY != null

    private val undampedWindowTranslation = PointF()

    var windowY = 0
        set(value) {
            field = value
            updateLocationParams()
            updateWindow()
        }

    val currentState: Int
        get() = windowState

    init {
        decorView.setOnApplyWindowInsetsListener { _, insets ->
            val winInsets = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                insets.getInsets(WindowInsets.Type.ime())
            } else {
                insets.systemWindowInsets
            }
            windowClient.onWindowInsetsChanged(winInsets)
            return@setOnApplyWindowInsetsListener insets
        }
    }

    override fun onDown(e: MotionEvent): Boolean {
        dockToEdgeAnimationX?.cancel()
        dockToEdgeAnimationY?.cancel()
        downPoint.set(e.rawX, e.rawY)
        lastMovePoint.set(downPoint)
        undampedWindowTranslation.set(0f, 0f)
        return false
    }

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

    override fun onLongPress(e: MotionEvent) {
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
            updateWindowContent()
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
            updateWindowContent()
        }
        return true
    }

    override fun onShowPress(e: MotionEvent) {
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return false
    }

    fun getMaxWindowSize(): PointF {
        val parentFrame = parentWindowFrame
        val parentWindowWidth = parentFrame.width()
        val parentWindowHeight = parentFrame.height()
        val finalWidth = parentWindowWidth * MAX_WINDOW_WIDTH_RATIO
        val finalHeight = parentWindowHeight * MAX_WINDOW_HEIGHT_RATIO
        return PointF(
            finalWidth,
            finalHeight
        )
    }

    fun getMiniWindowSize(): PointF {
        return PointF(
            remoteContext.resources.getDimensionPixelSize(R.dimen.config_mini_window_width)
                .toFloat(),
            remoteContext.resources.getDimensionPixelSize(R.dimen.config_mini_window_height)
                .toFloat()
        )
    }

    fun configureWindowParams() = with(windowParams) {
        val miniWindowSize = getMiniWindowSize()
        width = miniWindowSize.x.toInt()
        height = miniWindowSize.y.toInt()
        windowContentWidth = width
        windowContentHeight = height
        windowX = 0
        windowY = (parentWindowFrame.height() * WINDOW_INIT_Y_RATIO).toInt()
        format = PixelFormat.TRANSPARENT
        gravity = Gravity.TOP or Gravity.START
        flags = LayoutParams.FLAG_NOT_TOUCH_MODAL or
                LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                LayoutParams.FLAG_NOT_FOCUSABLE
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            setCanPlayMoveAnimation(false)
        }
    }

    fun maximizeWindow() {
        if (windowState == STATE_MAX_WINDOW) {
            return
        }
        windowState = STATE_MAX_WINDOW
        windowParams.flags =
            windowParams.flags and LayoutParams.FLAG_NOT_FOCUSABLE.inv()
        val size = getMaxWindowSize()
        val finalWidth = size.x
        val finalHeight = size.y
        windowClient.onRequestMaxWindowSize(finalWidth.toInt(), finalHeight.toInt())
        windowParams.apply {
            width = finalWidth.toInt()
            height = finalHeight.toInt()
        }
        windowClient.updateWindowAttributes(windowParams)
        animateWindowSize(finalWidth, finalHeight)
        moveMaximizeWindowCenter()
    }

    fun minimizeWindow(velocityX: Float = 0f, velocityY: Float = 0f) {
        if (windowState == STATE_MINI_WINDOW) {
            return
        }
        windowState = STATE_MINI_WINDOW
        decorView.findFocus()?.clearFocus()
        windowParams.flags = windowParams.flags or LayoutParams.FLAG_NOT_FOCUSABLE
        val miniWidowSize = getMiniWindowSize()
        val finalWidth = miniWidowSize.x
        val finalHeight = miniWidowSize.y
        animateWindowSize(finalWidth, finalHeight)
        dockToEdge(velocityX, velocityY)
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

    private fun animateWindowSize(finalWidth: Float, finalHeight: Float) {
        sizeChangeAnimator?.cancel()
        val miniSize = getMiniWindowSize()
        val maxSize = getMaxWindowSize()
        val startWidth = WINDOW_WIDTH_PROPERTY.get(this)
        val widthAnimationBuilder = SpringAnimationBuilder(context)
            .setDampingRatio(SIZE_CHANGE_DAMPING_RATIO)
            .setStiffness(SIZE_CHANGE_STIFFNESS)
            .setEndValue(finalWidth)
            .setMinimumVisibleChange(DynamicAnimation.MIN_VISIBLE_CHANGE_PIXELS)
            .setStartValue(startWidth)
        val widthAnimation = widthAnimationBuilder.build(this, WINDOW_WIDTH_PROPERTY)
        widthAnimation.addUpdateListener {
            val interpolatedWidth = widthAnimationBuilder.getInterpolatedValue(it.animatedFraction)
            windowClient.onWindowWidthChanged(
                startWidth,
                finalWidth,
                miniSize.x,
                maxSize.x,
                interpolatedWidth
            )
        }
        val startHeight = WINDOW_HEIGHT_PROPERTY.get(this)
        val heightAnimationBuilder = SpringAnimationBuilder(context)
            .setDampingRatio(SIZE_CHANGE_DAMPING_RATIO)
            .setStiffness(SIZE_CHANGE_STIFFNESS)
            .setEndValue(finalHeight)
            .setMinimumVisibleChange(DynamicAnimation.MIN_VISIBLE_CHANGE_PIXELS)
            .setStartValue(startHeight)
        val heightAnimation = heightAnimationBuilder.build(this, WINDOW_HEIGHT_PROPERTY)
        heightAnimation.addUpdateListener {
            val interpolatedHeight =
                heightAnimationBuilder.getInterpolatedValue(it.animatedFraction)
            windowClient.onWindowHeightChanged(
                startHeight,
                finalHeight,
                miniSize.y,
                maxSize.y,
                interpolatedHeight
            )
        }
        val animatorSet = AnimatorSet().apply {
            play(widthAnimation)
            play(heightAnimation)
            addListener(onEnd = {
                onSizeAnimationEnd()
            })
        }
        sizeChangeAnimator = animatorSet.apply {
            start()
        }
    }

    private fun onSizeAnimationEnd() {
        sizeChangeAnimator = null
        windowParams.apply {
            width = windowContentWidth
            height = windowContentHeight
        }
        windowClient.updateWindowAttributes(windowParams)
        windowClient.onStateSizeAnimationEnd(currentState)
    }

    private fun calcMaximizeWindowPosition(): PointF {
        val parentFrame = windowClient.getParentWindowVisibleFrame()
        val parentWindowWidth = parentFrame.width()
        val parentWindowHeight = parentFrame.height()
        val finalX = parentWindowWidth * (1 - MAX_WINDOW_WIDTH_RATIO) * .5f
        val finalY = parentWindowHeight * (1 - MAX_WINDOW_HEIGHT_RATIO) * .5f
        return PointF(finalX, finalY)
    }

    private fun calcMiniWindowFinalX(velX: Float): Float {
        val parentFrame = Rect(parentWindowFrame)
        val parentWindowWidth = parentFrame.width().toFloat()
        val dockLeftX = 0f
        val miniWindowSize = getMiniWindowSize()
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
        val miniWindowSize = getMiniWindowSize()
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

    private fun cancelAllAnimations() {
        dockToEdgeAnimationX?.cancel()
        dockToEdgeAnimationY?.cancel()
        sizeChangeAnimator?.cancel()
    }

    private fun dockToEdge(velX: Float, velY: Float) {
        animateWindowPosition(
            calcMiniWindowFinalX(velX),
            calcMiniWindowFinalY(velY),
            -velX,
            -velY
        )
    }

    private fun moveMaximizeWindowCenter() {
        val maxWindowPosition = calcMaximizeWindowPosition()
        val finalX = maxWindowPosition.x
        val finalY = maxWindowPosition.y
        animateWindowPosition(finalX, finalY, 0f, 0f)
    }

    private fun updateLocationParams() = with(windowParams) {
        x = windowX + windowOffsetX
        y = windowY + windowOffsetY
    }

    private fun updateWindowContent() {
        windowClient.updateWindowContent(windowContentWidth, windowContentHeight)
    }

    private fun updateWindow() {
        windowClient.updateWindowAttributes(windowParams)
    }

    companion object {
        private const val FLING_VELOCITY_THRESHOLD = 3000f
        private const val MAX_FLING_VELOCITY = 30000f
        private const val FLING_STIFFNESS = 300f
        private const val FLING_DAMPING_RATIO = .9f
        private const val SIZE_CHANGE_STIFFNESS = 300f
        private const val SIZE_CHANGE_DAMPING_RATIO = .95f

        const val STATE_MINI_WINDOW = 0
        const val STATE_MAX_WINDOW = 1

        private const val MAX_WINDOW_WIDTH_RATIO = 3f / 4f
        private const val MAX_WINDOW_HEIGHT_RATIO = .7f

        private const val WINDOW_INIT_Y_RATIO = .25f

        private val WINDOW_X_PROPERTY =
            object : FloatProperty<WindowController>("window_x_property") {
                override fun get(window: WindowController): Float {
                    return window.windowX.toFloat()
                }

                override fun setValue(window: WindowController, value: Float) {
                    window.windowX = value.toInt()
                }
            }

        private val WINDOW_Y_PROPERTY =
            object : FloatProperty<WindowController>("window_y_property") {
                override fun get(window: WindowController): Float {
                    return window.windowY.toFloat()
                }

                override fun setValue(window: WindowController, value: Float) {
                    window.windowY = value.toInt()
                }
            }

        private val WINDOW_WIDTH_PROPERTY =
            object : FloatProperty<WindowController>("window_width_property") {
                override fun get(window: WindowController): Float {
                    return window.windowContentWidth.toFloat()
                }

                override fun setValue(window: WindowController, value: Float) {
                    window.windowContentWidth = value.toInt()
                }
            }

        private val WINDOW_HEIGHT_PROPERTY =
            object : FloatProperty<WindowController>("window_width_property") {
                override fun get(window: WindowController): Float {
                    return window.windowContentHeight.toFloat()
                }

                override fun setValue(window: WindowController, value: Float) {
                    window.windowContentHeight = value.toInt()
                }
            }
    }

}