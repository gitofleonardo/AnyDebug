package com.hhvvg.anydebug.util

import android.app.Activity
import android.app.Application
import android.os.Bundle

/**
 * @author hhvvg
 */
class MyLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
    private val postCreatedCallbacks = ArrayList<ActivityBundleStateCallback>()
    private val resumedCallbacks = ArrayList<ActivityCallback>()
    private val destroyCallbacks = ArrayList<ActivityCallback>()

    fun addPostCreatedAction(callback: (Activity, Bundle?) -> Unit) {
        postCreatedCallbacks.add(object : ActivityBundleStateCallback {
            override fun onActivityAction(activity: Activity, savedInstanceState: Bundle?) {
                callback.invoke(activity, savedInstanceState)
            }
        })
    }

    fun addResumedAction(callback: (Activity) -> Unit) {
        resumedCallbacks.add(object : ActivityCallback {
            override fun onActivityAction(activity: Activity) {
                callback.invoke(activity)
            }
        })
    }

    fun addDestroyedAction(callback: (Activity) -> Unit) {
        destroyCallbacks.add(object : ActivityCallback {
            override fun onActivityAction(activity: Activity) {
                callback.invoke(activity)
            }
        })
    }

    override fun onActivityPostCreated(activity: Activity, savedInstanceState: Bundle?) {
        postCreatedCallbacks.forEach {
            it.onActivityAction(activity, savedInstanceState)
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
        resumedCallbacks.forEach {
            it.onActivityAction(activity)
        }
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
        destroyCallbacks.forEach {
            it.onActivityAction(activity)
        }
    }

    interface ActivityBundleStateCallback {
        fun onActivityAction(activity: Activity, savedInstanceState: Bundle?)
    }

    interface ActivityCallback {
        fun onActivityAction(activity: Activity)
    }
}