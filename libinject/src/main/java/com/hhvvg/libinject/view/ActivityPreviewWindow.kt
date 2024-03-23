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
import android.app.Dialog
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Switch
import androidx.core.view.isVisible
import com.hhvvg.libinject.R
import com.hhvvg.libinject.utils.Logger
import com.hhvvg.libinject.utils.call
import com.hhvvg.libinject.utils.override
import com.hhvvg.libinject.view.remote.RemoteViewFactoryLoader
import de.robv.android.xposed.XC_MethodHook.Unhook

@SuppressLint("RtlHardcoded")
class ActivityPreviewWindow(private val activity: Activity) : Dialog(activity),
    OnTouchListener, WindowClient {

    companion object {
        private val TAG = ActivityPreviewWindow::class.java.simpleName
    }

    private val remoteInflater by lazy {
        RemoteViewFactoryLoader(activity).getRemoteFactory()
    }

    private var activityTouchHookToken: Unhook? = null
    private val tempLoc = IntArray(2)
    private val onPreviewClickListener: OnClickListener = OnClickListener { v -> onPreviewClick(v) }

    private val miniWindowView: View?
        get() = contentView?.findViewById(R.id.mini_window_container)
    private val maxWindowView: View?
        get() = contentView?.findViewById(R.id.max_window_container)
    private var contentView: View? = null
    private val windowController by lazy {
        WindowController(window!!, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contentView = onCreateWindowContent(activity).apply {
            findViewById<View>(R.id.bottom_drag_bar).setOnTouchListener(this@ActivityPreviewWindow)
            findViewById<Switch>(R.id.edit_switch).setOnCheckedChangeListener { _, isChecked ->
                handleActivityTouchStateChanged(isChecked)
            }
            findViewById<View>(R.id.preview_list)?.call(
                "setOnPreviewClickListener",
                onPreviewClickListener
            )
            setContentView(this)
        }
        setRenderers(mutableListOf(activity.window.decorView))
        windowController.configureWindowParams()
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        Logger.log(TAG, "addView to window.")
    }

    override fun show() {
        if (activity.isFinishing) {
            return
        }
        super.show()
    }

    override fun dismiss() {
        super.dismiss()
        contentView = null
        activityTouchHookToken?.unhook()
        Logger.log(TAG, "removeView from window.")
    }

    private fun handleActivityTouchStateChanged(interceptTouch: Boolean) {
        if (!interceptTouch) {
            activityTouchHookToken?.unhook()
            activityTouchHookToken = null
            return
        }
        activityTouchHookToken = Activity::class.override(
            "dispatchTouchEvent", MotionEvent::class.java
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
        contentView?.findViewById<View>(R.id.preview_list)?.call("updatePreviewItems", renderers)
    }

    private fun onCreateWindowContent(context: Context): View {
        return remoteInflater.onInflateView(context, "layout_display_window")
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        windowController.onTouchEvent(event)
        return true
    }

    private fun onPreviewClick(view: View) {
        maxWindowView?.call(
            "setTargetView", view
        )
        windowController.maximizeWindow()
    }

    override fun updateWindowAttributes(attr: WindowManager.LayoutParams) {
        onWindowAttributesChanged(attr)
    }

    override fun getParentWindowVisibleFrame(): Rect {
        val rect = Rect()
        activity.window?.decorView?.getWindowVisibleDisplayFrame(rect)
        return rect
    }

    override fun onWindowStateChanged(state: Int) {
        when (state) {
            WindowController.STATE_MINI_WINDOW -> {
                miniWindowView?.isVisible = true
                maxWindowView?.isVisible = false
            }

            WindowController.STATE_MAX_WINDOW -> {
                miniWindowView?.isVisible = false
                maxWindowView?.isVisible = true
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        windowController.minimizeWindow()
    }
}