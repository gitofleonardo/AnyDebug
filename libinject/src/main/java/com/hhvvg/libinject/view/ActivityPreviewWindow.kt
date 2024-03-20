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

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.PointF
import android.graphics.Rect
import android.view.GestureDetector
import android.view.GestureDetector.OnGestureListener
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.Switch
import androidx.core.view.isVisible
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.FloatPropertyCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.hhvvg.libinject.R
import com.hhvvg.libinject.utils.Logger
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
            object : FloatPropertyCompat<ActivityPreviewWindow>("window_x_property") {
                override fun getValue(window: ActivityPreviewWindow): Float {
                    return window.windowParams.x.toFloat()
                }

                override fun setValue(window: ActivityPreviewWindow, value: Float) {
                    window.windowParams.x = value.toInt()
                    window.updateWindow()
                }
            }

        private val WINDOW_Y_PROPERTY =
            object : FloatPropertyCompat<ActivityPreviewWindow>("window_y_property") {
                override fun getValue(window: ActivityPreviewWindow): Float {
                    return window.windowParams.y.toFloat()
                }

                override fun setValue(window: ActivityPreviewWindow, value: Float) {
                    window.windowParams.y = value.toInt()
                    window.updateWindow()
                }
            }

        private val WINDOW_WIDTH_PROPERTY =
            object : FloatPropertyCompat<ActivityPreviewWindow>("window_width_property") {
                override fun getValue(window: ActivityPreviewWindow): Float {
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
            object : FloatPropertyCompat<ActivityPreviewWindow>("window_width_property") {
                override fun getValue(window: ActivityPreviewWindow): Float {
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
        private const val FLING_STIFFNESS = 300f
        private const val FLING_DAMPING_RATIO = .85f
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
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        }
    }
    private val gestureDetector = GestureDetector(activity, this)
    private val downPoint = PointF()
    private val lastMovePoint = PointF()
    private var activityTouchHookToken: Unhook? = null
    private val tempLoc = IntArray(2)
    private var dockToEdgeAnimationX: SpringAnimation? = null
    private var dockToEdgeAnimationY: SpringAnimation? = null
    private var widthAnimation: SpringAnimation? = null
    private var heightAnimation: SpringAnimation? = null
    private val tempRect = Rect()
    private val runningDockingAnimation: Boolean
        get() = dockToEdgeAnimationX != null && dockToEdgeAnimationY != null
    private val runningSizeAnimation: Boolean
        get() = widthAnimation != null && heightAnimation != null
    private val onPreviewClickListener: OnClickListener = OnClickListener { v -> onPreviewClick(v) }

    fun show() {
        if (attachedToWindow || activity.isFinishing) {
            return
        }
        decorView = onCreateWindowContent(activity).apply {
            findViewById<View>(R.id.bottom_drag_bar).setOnTouchListener(this@ActivityPreviewWindow)
            findViewById<Switch>(R.id.edit_switch).setOnCheckedChangeListener { _, isChecked ->
                handleActivityTouchStateChanged(isChecked)
            }

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
        dockToEdgeAnimationX?.cancel()
        dockToEdgeAnimationY?.cancel()
        val animX = SpringAnimation(this, WINDOW_X_PROPERTY).apply {
            val finalX = calcWindowFinalX(velX)
            spring = SpringForce()
                .setStiffness(FLING_STIFFNESS)
                .setDampingRatio(FLING_DAMPING_RATIO)
                .setFinalPosition(finalX)
            minimumVisibleChange = DynamicAnimation.MIN_VISIBLE_CHANGE_PIXELS
            setStartVelocity(-velX)
            addEndListener { _, _, _, _ ->
                dockToEdgeAnimationX = null
            }
            start()
        }
        val animY = SpringAnimation(this, WINDOW_Y_PROPERTY).apply {
            val finalY = calcWindowFinalY(velY)
            spring = SpringForce()
                .setStiffness(FLING_STIFFNESS)
                .setDampingRatio(FLING_DAMPING_RATIO)
                .setFinalPosition(finalY)
            minimumVisibleChange = DynamicAnimation.MIN_VISIBLE_CHANGE_PIXELS
            setStartVelocity(-velY)
            addEndListener { _, _, _, _ ->
                dockToEdgeAnimationY = null
            }
            start()
        }
        dockToEdgeAnimationX = animX
        dockToEdgeAnimationY = animY
    }

    private fun calcWindowFinalX(velX: Float): Float {
        activity.window.decorView.getWindowVisibleDisplayFrame(tempRect)
        val parentWindowWidth = tempRect.width().toFloat()
        val dockLeftX = 0f
        decorView?.getLocalVisibleRect(tempRect)
        val dockRightX = parentWindowWidth - tempRect.width()
        if (abs(velX) >= FLING_VELOCITY_THRESHOLD) {
            return if (velX > 0) dockLeftX else dockRightX
        }
        val windowCenterX = windowParams.x + windowParams.width / 2f
        return if (windowCenterX > parentWindowWidth / 2f) dockRightX else dockLeftX
    }

    private fun calcWindowFinalY(velY: Float): Float {
        activity.window.decorView.getWindowVisibleDisplayFrame(tempRect)
        val parentWindowHeight = tempRect.height().toFloat()
        val dockTopY = 0f
        decorView?.getLocalVisibleRect(tempRect)
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
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (!runningDockingAnimation) {
                    dockToEdge(0f, 0f)
                }
            }
        }
        return true
    }

    override fun onDown(e: MotionEvent): Boolean {
        dockToEdgeAnimationX?.cancel()
        dockToEdgeAnimationY?.cancel()
        downPoint.set(e.rawX, e.rawY)
        lastMovePoint.set(downPoint)
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
        windowParams.x += downDiffX.toInt()
        windowParams.y += downDiffY.toInt()
        decorView?.let {
            windowManager.updateViewLayout(it, windowParams)
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
        dockToEdge(-velocityX, -velocityY)
        return true
    }

    private fun onPreviewClick(view: View) {
        maximizeWindow()
    }

    private fun maximizeWindow() {
        activity.window.decorView.getWindowVisibleDisplayFrame(tempRect)
        val parentWindowWidth = tempRect.width()
        val parentWindowHeight = tempRect.height()
        val finalWidth = parentWindowWidth * (2f / 3f)
        val finalHeight = parentWindowHeight * .5f
        widthAnimation?.cancel()
        heightAnimation?.cancel()
        widthAnimation = SpringAnimation(this, WINDOW_WIDTH_PROPERTY).apply {
            spring = SpringForce(finalWidth)
                .setStiffness(FLING_STIFFNESS)
                .setDampingRatio(FLING_DAMPING_RATIO)
            minimumVisibleChange = DynamicAnimation.MIN_VISIBLE_CHANGE_PIXELS
            addEndListener { _, _, _, _ ->
                widthAnimation = null
            }
            start()
        }
        heightAnimation = SpringAnimation(this, WINDOW_HEIGHT_PROPERTY).apply {
            spring = SpringForce(finalHeight)
                .setStiffness(FLING_STIFFNESS)
                .setDampingRatio(FLING_DAMPING_RATIO)
            minimumVisibleChange = DynamicAnimation.MIN_VISIBLE_CHANGE_PIXELS
            addEndListener { _, _, _, _ ->
                heightAnimation = null
            }
            start()
        }

        val finalX = parentWindowWidth * (1f / 3f) * .5f
        val finalY = parentWindowHeight * .25f
        dockToEdgeAnimationX?.cancel()
        dockToEdgeAnimationY?.cancel()
        dockToEdgeAnimationX = SpringAnimation(this, WINDOW_X_PROPERTY).apply {
            spring = SpringForce()
                .setStiffness(FLING_STIFFNESS)
                .setDampingRatio(FLING_DAMPING_RATIO)
                .setFinalPosition(finalX)
            minimumVisibleChange = DynamicAnimation.MIN_VISIBLE_CHANGE_PIXELS
            addEndListener { _, _, _, _ ->
                dockToEdgeAnimationX = null
            }
            start()
        }
        dockToEdgeAnimationY = SpringAnimation(this, WINDOW_Y_PROPERTY).apply {
            spring = SpringForce()
                .setStiffness(FLING_STIFFNESS)
                .setDampingRatio(FLING_DAMPING_RATIO)
                .setFinalPosition(finalY)
            minimumVisibleChange = DynamicAnimation.MIN_VISIBLE_CHANGE_PIXELS
            addEndListener { _, _, _, _ ->
                dockToEdgeAnimationY = null
            }
            start()
        }
    }

    private fun minimizeWindow() {

    }
}