package com.example.gsr

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private lateinit var myWebView: WebView
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private val REQUEST_CODE_FILE_CHOOSER = 101
    private val REQUEST_CODE_PERMISSIONS = 100

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermissions()

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

            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                this@MainActivity.filePathCallback = filePathCallback

                if (ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU)
                            android.Manifest.permission.READ_MEDIA_IMAGES
                        else
                            android.Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU)
                                android.Manifest.permission.READ_MEDIA_IMAGES
                            else
                                android.Manifest.permission.READ_EXTERNAL_STORAGE
                        ),
                        REQUEST_CODE_PERMISSIONS
                    )
                    return false
                }

                val fileIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "image/*"
                }
                startActivityForResult(Intent.createChooser(fileIntent, "Select Photos"), REQUEST_CODE_FILE_CHOOSER)
                return true
            }
        }

        myWebView.loadUrl("https://undressbaby.com?utm_source=github&utm_medium=apk&utm_campaign=FaceSwapAI-00")

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

    private fun checkPermissions() {
        val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU)
            android.Manifest.permission.READ_MEDIA_IMAGES
        else
            android.Manifest.permission.READ_EXTERNAL_STORAGE

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), REQUEST_CODE_PERMISSIONS)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Permission Grant
            } else {
                android.widget.Toast.makeText(
                    this,
                    "Storage permission is required to upload photos",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_FILE_CHOOSER && filePathCallback != null) {
            if (resultCode == RESULT_OK) {
                val result = data?.data?.let { arrayOf(it) } ?: arrayOf()
                filePathCallback?.onReceiveValue(result)
            } else {
                filePathCallback?.onReceiveValue(null)
            }
            filePathCallback = null
        }
    }
}