package com.adaldosso.spendy

import android.webkit.WebSettings
import android.webkit.WebView
import androidx.test.core.app.ActivityScenario
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class WebViewActivityTest {

    private lateinit var webView: WebView
    private lateinit var webSettings: WebSettings

    @Before
    fun setUp() {
        ActivityScenario.launch(WebViewActivity::class.java).onActivity { activity ->
            webView = activity.findViewById(R.id.webView)
            webSettings = webView.settings
        }
    }

    @Test
    fun testWebViewLoadsCorrectUrl() {
        val expectedUrl = "http://10.0.2.2:4200/"
        assertEquals(expectedUrl, webView.url)
    }

    @Test
    fun testWebViewHasJavaScriptEnabled() {
        assertTrue(webSettings.javaScriptEnabled)
    }

    @Test
    fun testWebViewClientIsSet() {
        assertNotNull(webView.webViewClient)
    }
}
