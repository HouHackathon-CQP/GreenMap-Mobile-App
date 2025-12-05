package com.houhackathon.greenmap_app.extension.window

import android.view.Window
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat


fun Window.hideSystemBars() {
    runCatching {
        val windowInsetsController =
            WindowCompat.getInsetsController(this, this.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }.getOrElse {
        println("Failed to hide system bars: ${it.message}")
    }
}

fun Window.hideNavigationBar() {
    runCatching {
        val windowInsetsController =
            WindowCompat.getInsetsController(this, this.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())
    }.getOrElse {
        println("Failed to hide navigation bar: ${it.message}")
    }
}

fun Window.detectNavigationBarVisible(onVisible: (Window) -> Unit) {
    val window = this
    if (AndroidProvider.isVirtualNavigation) {
        val wic = WindowInsetsControllerCompat(window, window.decorView)
        wic.hide(WindowInsetsCompat.Type.navigationBars())
        wic.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { view, insets ->
            val isSoftInputShow = insets.isVisible(WindowInsetsCompat.Type.ime())
            val isNavBarVisible = insets.isVisible(WindowInsetsCompat.Type.navigationBars())
            if (isNavBarVisible) {
                onVisible(window)
            }
            val currentBottomPadding = view.paddingBottom
            if (!isSoftInputShow) {
                view.setPadding(
                    view.paddingLeft,
                    view.paddingTop,
                    view.paddingRight,
                    (-AndroidProvider.navigationHeight)
                )
            } else {
                view.setPadding(
                    view.paddingLeft,
                    view.paddingTop,
                    view.paddingRight,
                    currentBottomPadding
                )
            }
            insets
        }
    }
}