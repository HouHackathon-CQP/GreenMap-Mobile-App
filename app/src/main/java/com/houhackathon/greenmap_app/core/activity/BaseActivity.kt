package com.houhackathon.greenmap_app.core.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.houhackathon.greenmap_app.extension.window.hideNavigationBar
import com.houhackathon.greenmap_app.extension.window.hideSystemBars
import java.util.Locale


abstract class BaseActivity : AppCompatActivity() {
    protected abstract fun updateUI(savedInstanceState: Bundle?)
    protected abstract fun createContentView(savedInstanceState: Bundle?): View

    @ColorRes
    open fun getStatusBarColor(): Int? = null

    open fun isLightStatusBar(): Boolean = false

    open fun isHideSystemBar(): Boolean = false

    @SuppressLint("SourceLockedOrientationActivity")
    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewRoot = createContentView(savedInstanceState)
        val statusBarColor = getStatusBarColor()
        if (statusBarColor != null) {
            setStatusBarColor(ContextCompat.getColor(this, statusBarColor))
        } else {
            setIsAppearanceLightStatusBars(isLightStatusBar())
        }
        setContentView(viewRoot)
        supportActionBar?.hide()
        actionBar?.hide()
        updateUI(savedInstanceState)
    }

    fun setStatusBarColor(@ColorInt color: Int) {
        if (window.statusBarColor == color) return
        window.statusBarColor = color
        setIsAppearanceLightStatusBars(isLightStatusBar())
    }

    fun setIsAppearanceLightStatusBars(isLight: Boolean) {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = isLight
        }
    }

    private fun isColorDark(@ColorInt color: Int): Boolean {
        val darkness =
            1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return darkness >= 0.5
    }

    private fun isColorLight(@ColorInt color: Int): Boolean {
        return !isColorDark(color)
    }

    override fun onResume() {
        super.onResume()
        if (isHideSystemBar()) {
            window.hideSystemBars()
        } else {
            window.hideNavigationBar()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (isHideSystemBar()) {
            window.hideSystemBars()
        } else {
            window.hideNavigationBar()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
    }

    private fun updateLocale(context: Context, language: String): Context {
        return try {
            val locale = Locale.forLanguageTag(language)
            Locale.setDefault(locale)
            val config = Configuration(context.resources.configuration)
            config.setLocale(locale)
            context.createConfigurationContext(config)
        } catch (e: Exception) {
            context
        }
    }

    fun showMessage(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    fun showMessage(@StringRes messageRes: Int) {
        runOnUiThread {
            Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show()
        }
    }
}