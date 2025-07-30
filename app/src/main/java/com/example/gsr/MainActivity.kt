package com.example.gsr

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.addCallback

class MainActivity : ComponentActivity() {

    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private lateinit var myWebView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        myWebView = findViewById(R.id.webview)
        myWebView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                android.widget.Toast.makeText(
                    this@MainActivity,
                    "Error loading page: $description",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
        myWebView.settings.javaScriptEnabled = true
        myWebView.settings.domStorageEnabled = true
        myWebView.settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT

        myWebView.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (customView != null) {
                    callback?.onCustomViewHidden()
                    return
                }
                customView = view
                customViewCallback = callback
                window.decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        )
                findViewById<ViewGroup>(R.id.main_content).visibility = View.GONE
                findViewById<ViewGroup>(R.id.fullscreen_container).addView(view)
                findViewById<ViewGroup>(R.id.fullscreen_container).visibility = View.VISIBLE
            }

            override fun onHideCustomView() {
                customView?.let {
                    findViewById<ViewGroup>(R.id.fullscreen_container).removeView(it)
                    customView = null
                }
                findViewById<ViewGroup>(R.id.main_content).visibility = View.VISIBLE
                findViewById<ViewGroup>(R.id.fullscreen_container).visibility = View.GONE
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                customViewCallback?.onCustomViewHidden()
            }
        }

        myWebView.loadUrl("https://undressbaby.com")

        // Handle back press
        onBackPressedDispatcher.addCallback(this) {
            if (customView != null) {
                myWebView.webChromeClient?.onHideCustomView()
            } else if (myWebView.canGoBack()) {
                myWebView.goBack()
            } else {
                finish()
            }
        }
    }
}