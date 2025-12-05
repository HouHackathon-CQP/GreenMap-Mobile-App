package com.houhackathon.greenmap_app.extension.window

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean


object AndroidProvider {
    private var currentActivity: WeakReference<Activity>? = null
    private var lifecycleCallbacks: Application.ActivityLifecycleCallbacks? = null
    private var isInitialized = false
    var isVirtualNavigation = true
        private set

    var navigationHeight: Int = 0

    /**
     * Register activity lifecycle callbacks to track the current activity
     */
    fun init(application: Application) {
        // Unregister previous callbacks if already initialized
        application.unregisterActivityLifecycleCallbacks(lifecycleCallbacks)

        lifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                if (currentActivity == null) {
                    detectNavigationBar(activity)
                }
            }

            override fun onActivityStarted(activity: Activity) {
                // Not used
            }

            override fun onActivityResumed(activity: Activity) {
                currentActivity = WeakReference(activity)
            }

            override fun onActivityPaused(activity: Activity) {
                // Not used
            }

            override fun onActivityStopped(activity: Activity) {
                // Not used
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                // Not used
            }

            override fun onActivityDestroyed(activity: Activity) {
                if (currentActivity?.get() === activity) {
                    currentActivity = null
                }
            }
        }

        application.registerActivityLifecycleCallbacks(lifecycleCallbacks)
        isInitialized = true
    }

    private fun detectNavigationBar(activity: Activity) {
        val window = activity.window
        val decorView = window.decorView
        val wic = WindowInsetsControllerCompat(window, decorView)

        // Initially show navigation bars to measure insets
        wic.show(WindowInsetsCompat.Type.navigationBars())

        var defaultVisible: Boolean? = null
        val hasDetectedNavigation = AtomicBoolean(false)
        ViewCompat.setOnApplyWindowInsetsListener(decorView) { _, insets ->
            val isNavBarVisible = insets.isVisible(WindowInsetsCompat.Type.navigationBars())

            if (defaultVisible == null) {
                defaultVisible = isNavBarVisible
            }

            if (isNavBarVisible && !hasDetectedNavigation.get()) {
                detectVirtualNavigationFromInsets(insets, wic, defaultVisible)
                hasDetectedNavigation.set(true)
                ViewCompat.setOnApplyWindowInsetsListener(decorView, null)
            } else if (!hasDetectedNavigation.get()) {
                // Keep navigation bar visible until detection is complete
                wic.show(WindowInsetsCompat.Type.navigationBars())
            }
            insets
        }
    }

    private fun detectVirtualNavigationFromInsets(
        insets: WindowInsetsCompat,
        wic: WindowInsetsControllerCompat,
        defaultVisible: Boolean?,
    ) {
        val navigationInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
        val bottom = navigationInsets.bottom
        val left = navigationInsets.left
        val right = navigationInsets.right

        // A device with virtual 3-button navigation has significant bottom inset
        // while gesture navigation typically has small or zero inset values
        isVirtualNavigation = bottom > 0 && ((left == 0 && right == 0) || bottom > left + right)
        navigationHeight = bottom

        // Restore default visibility state
        defaultVisible?.let { isVisible ->
            if (isVisible) {
                wic.show(WindowInsetsCompat.Type.navigationBars())
            } else {
                wic.hide(WindowInsetsCompat.Type.navigationBars())
            }
        }
    }
    /**
     * Get the current activity in foreground
     * @return Current activity or null if no activity is in foreground
     */
    fun getCurrentActivity(): Activity? {
        return currentActivity?.get()
    }

    /**
     * Check if the app is in foreground
     * @return True if app is in foreground, false otherwise
     */
    fun isAppInForeground(): Boolean {
        return currentActivity?.get() != null
    }
}