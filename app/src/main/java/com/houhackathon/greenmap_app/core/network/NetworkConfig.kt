package com.houhackathon.greenmap_app.core.network

import com.houhackathon.greenmap_app.BuildConfig

object NetworkConfig {
    private const val DEFAULT_BASE_URL = "https://example.com/"
    const val DEFAULT_TIMEOUT_SECONDS = 30L

    val baseUrl: String
        get() = BuildConfig.API_BASE_URL.takeIf { it.isNotBlank() } ?: DEFAULT_BASE_URL
}
