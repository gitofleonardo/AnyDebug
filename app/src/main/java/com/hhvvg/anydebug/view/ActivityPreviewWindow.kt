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

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Insets
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Switch
import androidx.core.view.isVisible
import com.hhvvg.anydebug.R
import com.hhvvg.anydebug.utils.getWindowDisplayFrame
import com.hhvvg.anydebug.utils.override
import com.hhvvg.anydebug.view.remote.RemoteFactoryImpl
import de.robv.android.xposed.XC_MethodHook.Unhook
import java.util.function.Consumer

/**
 * Mod window
 */
@SuppressLint("RtlHardcoded")
class ActivityPreviewWindow(private val activity: Activity) : OnTouchListener, WindowClient,
    OnBackInvokedCallback {

    private lateinit var contentView: View
    private var activityTouchHookToken: Unhook? = null
    private val dragBar: View by lazy { contentView.findViewById(R.id.bottom_drag_bar) }
    private val editSwitch: Switch by lazy { contentView.findViewById(R.id.edit_switch) }
    private val maxWindowView: ViewSettingsContainer by lazy { contentView.findViewById(R.id.max_window_container) }
    private val miniWindowView: View by lazy { contentView.findViewById(R.id.mini_window_container) }
    private val previewList: PreviewList by lazy { contentView.findViewById(R.id.preview_list) }

    private val remoteInflater by lazy {
        RemoteFactoryImpl()
    }

    private val windowController by lazy {
        WindowController(this, this)
    }

    private val onPreviewClickListener: OnClickListener = OnClickListener {
        maxWindowView.setTargetView(it)
        windowController.maximizeWindow()
    }

    private val onViewCommitListener: Runnable = Runnable {
        windowController.minimizeWindow()
    }

    private val onViewRemoveListener: Consumer<View> = Consumer {
        previewList.removePreviewView(it)
        windowController.minimizeWindow()
    }

    private val tempLoc = IntArray(2)

    private var created = false
    private var shown = false

    private val windowManager by lazy {
        activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }
    internal val windowParams: WindowManager.LayoutParams = WindowManager.LayoutParams()
    internal val context: Context = activity
    internal val decorView: DecorView = DecorView(activity)

    private fun onCreate() {
        contentView = onCreateWindowContent(activity)
        decorView.addView(contentView)
        decorView.setOnBackInvokedCallback(this)
        dragBar.setOnTouchListener(this@ActivityPreviewWindow)
        editSwitch.setOnCheckedChangeListener { _, isChecked ->
            handleActivityTouchStateChanged(isChecked)
        }
        previewList.setOnPreviewClickListener(onPreviewClickListener)
        maxWindowView.setOnViewRemoveListener(onViewRemoveListener)
        maxWindowView.setOnCommitListener(onViewCommitListener)
        setRenderers(mutableListOf(activity.window.decorView))
        windowController.configureWindowParams()
    }

    fun show() {
        if (activity.isFinishing || shown) {
            return
        }
        if (!created) {
            created = true
            onCreate()
        }
        shown = true
        windowManager.addView(decorView, windowParams)
    }

    fun dismiss() {
        shown = false
        activityTouchHookToken?.unhook()
    }

    override fun onBackInvoked() {
        windowController.minimizeWindow()
    }

    override fun getParentWindowFrame(): Rect {
        return Rect().apply {
            activity.window?.decorView?.getWindowDisplayFrame(this)
        }
    }

    override fun onRequestMaxWindowSize(width: Int, height: Int) {
        maxWindowView.let {
            val params = it.layoutParams
            params.width = width
            params.height = height
            it.layoutParams = params

            it.pivotX = 0f
            it.pivotY = 0f
        }
    }

    override fun onStateSizeAnimationEnd(state: Int) {
        when (state) {
            WindowController.STATE_MINI_WINDOW -> {
                maxWindowView.isVisible = false
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        windowController.onTouchEvent(event)
        return true
    }

    override fun onWindowHeightChanged(
        startHeight: Float,
        endHeight: Float,
        minHeight: Float,
        maxHeight: Float,
        height: Float
    ) {
        val scaleY = (1 / maxHeight) * height
        maxWindowView.let {
            View.SCALE_Y.set(it, scaleY)
        }
    }

    override fun onWindowInsetsChanged(insets: Insets) = with(contentView.findFocus()) {
        if (this == null || windowController.currentState != WindowController.STATE_MAX_WINDOW) {
            return@with
        }
        val maxWindowSize = windowController.getMaxWindowSize()
        val height = (maxWindowSize.y - insets.bottom).toInt()
        updateWindowContent(maxWindowSize.x.toInt(), height)
        onRequestMaxWindowSize(maxWindowSize.x.toInt(), height)
    }

    override fun onWindowStateChanged(state: Int) {
        when (state) {
            WindowController.STATE_MINI_WINDOW -> {
                // For animation
                maxWindowView.isVisible = true
            }

            WindowController.STATE_MAX_WINDOW -> {
                maxWindowView.isVisible = true
                editSwitch.isChecked = false
            }
        }
    }

    override fun onWindowWidthChanged(
        startWidth: Float,
        endWidth: Float,
        minWidth: Float,
        maxWidth: Float,
        width: Float
    ) {
        val alphaFraction = (width - startWidth) / (endWidth - startWidth)
        val realAlphaFraction = when (windowController.currentState) {
            WindowController.STATE_MINI_WINDOW -> {
                1 - alphaFraction
            }

            WindowController.STATE_MAX_WINDOW -> {
                alphaFraction
            }

            else -> return
        }
        maxWindowView.let {
            View.ALPHA.set(it, realAlphaFraction)
        }
        miniWindowView.let {
            View.ALPHA.set(it, 1 - realAlphaFraction)
        }
        val scaleX = (1 / maxWidth) * width
        maxWindowView.let {
            View.SCALE_X.set(it, scaleX)
        }
    }

    override fun updateWindowAttributes(attr: WindowManager.LayoutParams) {
        if (decorView.isAttachedToWindow) {
            windowManager.updateViewLayout(decorView, attr)
        }
    }

    override fun updateWindowContent(width: Int, height: Int) {
        contentView.layoutParams.let {
            it.width = width
            it.height = height
            contentView.layoutParams = it
        }
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

    private fun isEventInChild(event: MotionEvent, child: View): Boolean {
        child.getLocationOnScreen(tempLoc)
        return event.rawX >= tempLoc[0] &&
                event.rawX <= tempLoc[0] + child.width &&
                event.rawY >= tempLoc[1] &&
                event.rawY <= tempLoc[1] + child.height
    }

    private fun onActivityTouchEvent(event: MotionEvent) {
        val targets = findEventTargets(event)
        setRenderers(targets)
    }

    private fun onCreateWindowContent(context: Context): View {
        return remoteInflater.onInflateView(context, "layout_display_window")
    }

    private fun setRenderers(renderers: List<View>) {
        previewList.updatePreviewItems(renderers)
    }
}