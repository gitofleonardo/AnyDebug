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
import android.view.GestureDetector
import android.view.GestureDetector.OnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Switch
import androidx.core.view.isVisible
import com.hhvvg.libinject.R
import com.hhvvg.libinject.utils.Logger
import com.hhvvg.libinject.utils.override
import com.hhvvg.libinject.view.remote.RemoteViewFactoryLoader
import de.robv.android.xposed.XC_MethodHook.Unhook
import de.robv.android.xposed.XposedHelpers

class ActivityPreviewWindow(private val activity: Activity): OnTouchListener, OnGestureListener {

    companion object {
        private val TAG = ActivityPreviewWindow::class.java.simpleName
    }

    private var floatingView: View? = null
    private val windowManager by lazy {
        activity.getSystemService(WindowManager::class.java)
    }
    private val attachedToWindow
        get() = floatingView != null
    private val remoteInflater by lazy {
        RemoteViewFactoryLoader(activity).getRemoteFactory()
    }
    private val windowParams by lazy {
        WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            x = 0
            y = 0
            format = PixelFormat.TRANSPARENT
            type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL
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
    private var renderer: View? = null

    fun show() {
        if (attachedToWindow || activity.isFinishing) {
            return
        }
        floatingView = onCreateWindowContent(activity).apply {
            setOnTouchListener(this@ActivityPreviewWindow)
            findViewById<Switch>(R.id.edit_switch).setOnCheckedChangeListener { _, isChecked ->
                handleActivityTouchStateChanged(isChecked)
            }

            windowManager.addView(this, windowParams)
            Logger.log(TAG, "addView to window, wm=${windowManager}.")
        }
        floatingView?.let {
            setRenderer(activity.window.decorView)
        }
    }

    fun hide() {
        if (!attachedToWindow) {
            return
        }
        floatingView?.let { windowManager.removeView(it) }
        floatingView = null
        activityTouchHookToken?.unhook()
        Logger.log(TAG, "removeView from window.")
    }

    private fun handleActivityTouchStateChanged(interceptTouch: Boolean) {
        if (!interceptTouch) {
            activityTouchHookToken?.unhook()
            activityTouchHookToken = null
            return
        }
        activityTouchHookToken = Activity::class.override("dispatchTouchEvent",
            MotionEvent::class.java) {
            onActivityTouchEvent(it.args[0] as MotionEvent)
            true
        }
    }

    private fun onActivityTouchEvent(event: MotionEvent) {
        val target = findEventTarget(event)
        setRenderer(target)
    }

    private fun findEventTarget(event: MotionEvent): View {
        return findEventTarget(activity.window.decorView, event)
    }

    private fun findEventTarget(root: View, event: MotionEvent): View {
        if (root !is ViewGroup || root.childCount == 0) {
            return root
        }
        val childCount = root.childCount
        for (i in 0..childCount) {
            val child = root.getChildAt(i)
            if (child != null && child.isVisible && isEventInChild(event, child)) {
                return findEventTarget(child, event)
            }
        }
        return root
    }

    private fun isEventInChild(event: MotionEvent, child: View): Boolean {
        child.getLocationOnScreen(tempLoc)
        return event.rawX >= tempLoc[0] &&
                event.rawX <= tempLoc[0] + child.width &&
                event.rawY >= tempLoc[1] &&
                event.rawY <= tempLoc[1] + child.height
    }

    private fun setRenderer(renderer: View) {
        this.renderer = renderer
        floatingView?.let {
            val previewView = it.findViewById<View>(R.id.preview_view)
            XposedHelpers.callMethod(
                previewView,
                "setRenderer",
                renderer
            )
        }
    }

    private fun onCreateWindowContent(context: Context): View {
        return remoteInflater.onInflateView(context, "layout_display_window")
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    override fun onDown(e: MotionEvent): Boolean {
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
        floatingView?.let {
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
        return false
    }
}