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
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.PointF
import android.graphics.Rect
import android.util.FloatProperty
import android.view.GestureDetector
import android.view.GestureDetector.OnGestureListener
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.View.MeasureSpec
import android.view.View.OnClickListener
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.Switch
import androidx.core.animation.addListener
import androidx.core.view.isVisible
import androidx.dynamicanimation.animation.DynamicAnimation
import com.hhvvg.libinject.R
import com.hhvvg.libinject.utils.Logger
import com.hhvvg.libinject.utils.OverScroll
import com.hhvvg.libinject.utils.SpringAnimationBuilder
import com.hhvvg.libinject.utils.call
import com.hhvvg.libinject.utils.override
import com.hhvvg.libinject.view.remote.RemoteViewFactoryLoader
import de.robv.android.xposed.XC_MethodHook.Unhook
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@SuppressLint("RtlHardcoded")
class ActivityPreviewWindow(private val activity: Activity) : OnTouchListener, OnGestureListener {

    companion object {
        private val TAG = ActivityPreviewWindow::class.java.simpleName

        private val WINDOW_X_PROPERTY =
            object : FloatProperty<ActivityPreviewWindow>("window_x_property") {
                override fun get(window: ActivityPreviewWindow): Float {
                    return window.windowParams.x.toFloat()
                }

                override fun setValue(window: ActivityPreviewWindow, value: Float) {
                    window.windowParams.x = value.toInt()
                    window.updateWindow()
                }
            }

        private val WINDOW_Y_PROPERTY =
            object : FloatProperty<ActivityPreviewWindow>("window_y_property") {
                override fun get(window: ActivityPreviewWindow): Float {
                    return window.windowParams.y.toFloat()
                }

                override fun setValue(window: ActivityPreviewWindow, value: Float) {
                    window.windowParams.y = value.toInt()
                    window.updateWindow()
                }
            }

        private val WINDOW_WIDTH_PROPERTY =
            object : FloatProperty<ActivityPreviewWindow>("window_width_property") {
                override fun get(window: ActivityPreviewWindow): Float {
                    return if (window.windowParams.width > 0)
                        window.windowParams.width.toFloat()
                    else run {
                        window.decorView?.getLocalVisibleRect(window.tempRect)
                        window.tempRect.width().toFloat()
                    }
                }

                override fun setValue(window: ActivityPreviewWindow, value: Float) {
                    window.windowParams.width = value.toInt()
                    window.updateWindow()
                }
            }

        private val WINDOW_HEIGHT_PROPERTY =
            object : FloatProperty<ActivityPreviewWindow>("window_width_property") {
                override fun get(window: ActivityPreviewWindow): Float {
                    return if (window.windowParams.height > 0)
                        window.windowParams.height.toFloat()
                    else run {
                        window.decorView?.getLocalVisibleRect(window.tempRect)
                        window.tempRect.height().toFloat()
                    }
                }

                override fun setValue(window: ActivityPreviewWindow, value: Float) {
                    window.windowParams.height = value.toInt()
                    window.updateWindow()
                }
            }

        private const val FLING_VELOCITY_THRESHOLD = 3000f
        private const val MAX_FLING_VELOCITY = 30000f
        private const val FLING_STIFFNESS = 200f
        private const val FLING_DAMPING_RATIO = .80f

        private const val STATE_MINI_WINDOW = 0
        private const val STATE_MAX_WINDOW = 1

        private const val MAX_WINDOW_WIDTH_RATIO = 3f / 4f
        private const val MAX_WINDOW_HEIGHT_RATIO = .7f
    }

    private var decorView: View? = null
    private val windowManager by lazy {
        activity.getSystemService(WindowManager::class.java)
    }
    private val attachedToWindow
        get() = decorView != null
    private val remoteInflater by lazy {
        RemoteViewFactoryLoader(activity).getRemoteFactory()
    }
    private val windowParams by lazy {
        WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            x = 0
            y = run {
                activity.window.decorView.getWindowVisibleDisplayFrame(tempRect)
                (tempRect.height() * .25f).toInt()
            }
            format = PixelFormat.TRANSPARENT
            type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL
            gravity = Gravity.TOP or Gravity.LEFT
            flags =
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        }
    }
    private val gestureDetector = GestureDetector(activity, this)
    private val downPoint = PointF()
    private val lastMovePoint = PointF()
    private var activityTouchHookToken: Unhook? = null
    private val tempLoc = IntArray(2)
    private var dockToEdgeAnimationX: Animator? = null
    private var dockToEdgeAnimationY: Animator? = null
    private var widthAnimation: Animator? = null
    private var heightAnimation: Animator? = null
    private val tempRect = Rect()
    private val runningDockingAnimation: Boolean
        get() = dockToEdgeAnimationX != null && dockToEdgeAnimationY != null
    private val onPreviewClickListener: OnClickListener = OnClickListener { v -> onPreviewClick(v) }
    private var windowState = STATE_MINI_WINDOW
    private val undampedWindowTranslation = PointF()
    private val maxFloatingWindowDragTranslation by lazy {
        activity.window.decorView.getWindowVisibleDisplayFrame(tempRect)
        tempRect.height()
    }
    private val miniWindowView: View?
        get() = decorView?.findViewById(R.id.mini_window_container)
    private val maxWindowView: View?
        get() = decorView?.findViewById(R.id.max_window_container)

    fun show() {
        if (attachedToWindow || activity.isFinishing) {
            return
        }
        decorView = onCreateWindowContent(activity).apply {
            findViewById<View>(R.id.bottom_drag_bar).setOnTouchListener(this@ActivityPreviewWindow)
            findViewById<Switch>(R.id.edit_switch).setOnCheckedChangeListener { _, isChecked ->
                handleActivityTouchStateChanged(isChecked)
            }
            elevation = activity.resources.getDimensionPixelSize(R.dimen.decor_elevation).toFloat()
            windowManager.addView(this, windowParams)
            Logger.log(TAG, "addView to window, wm=${windowManager}.")
        }
        decorView?.let {
            setRenderers(mutableListOf(activity.window.decorView))
            it.findViewById<View>(R.id.preview_list)
                ?.call("setOnPreviewClickListener", onPreviewClickListener)
        }
    }

    fun hide() {
        if (!attachedToWindow) {
            return
        }
        decorView?.let { windowManager.removeView(it) }
        decorView = null
        activityTouchHookToken?.unhook()
        Logger.log(TAG, "removeView from window.")
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
        activity.window.decorView.getWindowVisibleDisplayFrame(tempRect)
        val parentWindowWidth = tempRect.width().toFloat()
        val dockLeftX = 0f
        decorView?.findViewById<View>(R.id.mini_window_container)?.getLocalVisibleRect(tempRect)
        val dockRightX = parentWindowWidth - tempRect.width()
        if (abs(velX) >= FLING_VELOCITY_THRESHOLD) {
            return if (velX > 0) dockLeftX else dockRightX
        }
        val windowCenterX = windowParams.x + windowParams.width / 2f
        return if (windowCenterX > parentWindowWidth / 2f) dockRightX else dockLeftX
    }

    private fun calcMiniWindowFinalY(velY: Float): Float {
        activity.window.decorView.getWindowVisibleDisplayFrame(tempRect)
        val parentWindowHeight = tempRect.height().toFloat()
        val dockTopY = 0f
        decorView?.findViewById<View>(R.id.mini_window_container)?.getLocalVisibleRect(tempRect)
        val dockBottomY = parentWindowHeight - tempRect.height()
        val interpolator = DecelerateInterpolator()
        val fractionVel = min(abs(velY), MAX_FLING_VELOCITY) / MAX_FLING_VELOCITY
        val interpolatedPositionFraction = interpolator.getInterpolation(fractionVel)
        val maxTranYTop = windowParams.y - dockTopY
        val maxTranYBottom = dockBottomY - windowParams.y
        if (abs(velY) >= FLING_VELOCITY_THRESHOLD) {
            return if (velY > 0) {
                windowParams.y - interpolatedPositionFraction * maxTranYTop
            } else {
                windowParams.y + interpolatedPositionFraction * maxTranYBottom
            }
        }
        return max(dockTopY, min(windowParams.y.toFloat(), dockBottomY))
    }

    private fun updateWindow() {
        decorView?.let { windowManager.updateViewLayout(it, windowParams) }
    }

    private fun handleActivityTouchStateChanged(interceptTouch: Boolean) {
        if (!interceptTouch) {
            activityTouchHookToken?.unhook()
            activityTouchHookToken = null
            return
        }
        activityTouchHookToken = Activity::class.override(
            "dispatchTouchEvent",
            MotionEvent::class.java
        ) {
            onActivityTouchEvent(it.args[0] as MotionEvent)
            true
        }
    }

    private fun onActivityTouchEvent(event: MotionEvent) {
        val targets = findEventTargets(event)
        setRenderers(targets)
    }

    private fun findEventTargets(event: MotionEvent): List<View> {
        val items = mutableListOf<View>()
        findEventTargets(activity.window.decorView, event, items)
        return items
    }

    private fun findEventTargets(root: View, event: MotionEvent, outList: MutableList<View>) {
        if (root !is ViewGroup || root.childCount == 0) {
            outList.add(root)
            return
        }
        val childCount = root.childCount
        val touchedTargetChildren = mutableListOf<View>()
        for (i in 0..childCount) {
            val child = root.getChildAt(i)
            if (child != null && child.isVisible && isEventInChild(event, child)) {
                findEventTargets(child, event, touchedTargetChildren)
            }
        }
        if (touchedTargetChildren.isEmpty()) {
            touchedTargetChildren.add(root)
        }
        outList.addAll(touchedTargetChildren)
    }

    private fun isEventInChild(event: MotionEvent, child: View): Boolean {
        child.getLocationOnScreen(tempLoc)
        return event.rawX >= tempLoc[0] &&
                event.rawX <= tempLoc[0] + child.width &&
                event.rawY >= tempLoc[1] &&
                event.rawY <= tempLoc[1] + child.height
    }

    private fun setRenderers(renderers: List<View>) {
        decorView?.findViewById<View>(R.id.preview_list)?.call("updatePreviewItems", renderers)
    }

    private fun onCreateWindowContent(context: Context): View {
        return remoteInflater.onInflateView(context, "layout_display_window")
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
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
        return true
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
            windowParams.x += downDiffX.toInt()
            windowParams.y += downDiffY.toInt()
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
            windowParams.x = (maximizeWindowPos.x + dampedTranX).toInt()
            windowParams.y = (maximizeWindowPos.y + dampedTranY).toInt()
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

    private fun onPreviewClick(view: View) {
        maxWindowView?.call(
            "setTargetView",
            view
        )
        maximizeWindow()
    }

    private fun animateWindowSize(finalWidth: Float, finalHeight: Float) {
        widthAnimation?.cancel()
        heightAnimation?.cancel()
        widthAnimation = SpringAnimationBuilder(activity)
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
        heightAnimation = SpringAnimationBuilder(activity)
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
        dockToEdgeAnimationX = SpringAnimationBuilder(activity)
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
        dockToEdgeAnimationY = SpringAnimationBuilder(activity)
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
        activity.window.decorView.getWindowVisibleDisplayFrame(tempRect)
        val parentWindowWidth = tempRect.width()
        val parentWindowHeight = tempRect.height()
        val finalX = parentWindowWidth * (1 - MAX_WINDOW_WIDTH_RATIO) * .5f
        val finalY = parentWindowHeight * (1 - MAX_WINDOW_HEIGHT_RATIO) * .5f
        return PointF(finalX, finalY)
    }

    private fun calcMiniWindowSize(): PointF {
        activity.window.decorView.getWindowVisibleDisplayFrame(tempRect)
        val parentWindowWidth = tempRect.width()
        val parentWindowHeight = tempRect.height()
        val widthSpec = MeasureSpec.makeMeasureSpec(parentWindowWidth, MeasureSpec.AT_MOST)
        val heightSpec = MeasureSpec.makeMeasureSpec(parentWindowHeight, MeasureSpec.AT_MOST)
        decorView?.measure(widthSpec, heightSpec)
        val size = PointF()
        decorView?.let {
            size.set(it.measuredWidth.toFloat(), it.measuredHeight.toFloat())
        }
        return size
    }

    private fun minimizeWindow(velocityX: Float, velocityY: Float) {
        if (windowState == STATE_MINI_WINDOW) {
            return
        }
        windowState = STATE_MINI_WINDOW
        windowParams.flags = windowParams.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        miniWindowView?.isVisible = true
        maxWindowView?.isVisible = false
        val miniWidowSize = calcMiniWindowSize()
        val finalWidth = miniWidowSize.x
        val finalHeight = miniWidowSize.y
        animateWindowSize(finalWidth, finalHeight)
        dockToEdge(velocityX, velocityY)
    }

    private fun maximizeWindow() {
        if (windowState == STATE_MAX_WINDOW) {
            return
        }
        windowState = STATE_MAX_WINDOW
        windowParams.flags =
            windowParams.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
        miniWindowView?.isVisible = false
        maxWindowView?.isVisible = true
        activity.window.decorView.getWindowVisibleDisplayFrame(tempRect)
        val parentWindowWidth = tempRect.width()
        val parentWindowHeight = tempRect.height()
        val finalWidth = parentWindowWidth * MAX_WINDOW_WIDTH_RATIO
        val finalHeight = parentWindowHeight * MAX_WINDOW_HEIGHT_RATIO
        animateWindowSize(finalWidth, finalHeight)
        moveMaximizeWindowCenter()
    }
}