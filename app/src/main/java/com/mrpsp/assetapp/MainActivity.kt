package com.mrpsp.assetapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.TextView
import android.view.Gravity
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private lateinit var web: WebView
    private val siteUrl = "https://kolkatametroproject-web.github.io/firebase-excel/"

    private val cameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) loadSite() else loadSite() // Site remains usable; camera features may be unavailable.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(7, 59, 112)
        window.navigationBarColor = Color.rgb(7, 59, 112)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            loadSite()
        } else {
            cameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun loadSite() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
        }
        val header = TextView(this).apply {
            text = "MRPSP  •  ASSET CHECKOUT / RETURN"
            setTextColor(Color.WHITE)
            textSize = 16f
            gravity = Gravity.CENTER_VERTICAL
            setPadding(18, 16, 18, 16)
            setBackgroundColor(Color.rgb(7, 59, 112))
        }
        web = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.allowFileAccess = false
            settings.allowContentAccess = true
            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val uri = request?.url ?: return true
                    val host = uri.host ?: return true
                    val allowed = host == "kolkatametroproject-web.github.io" ||
                        host == "firebase-excel.web.app" || host == "firebase-excel.firebaseapp.com" ||
                        host == "metro-railway-project.firebaseapp.com"
                    return !(uri.scheme == "https" && allowed)
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onPermissionRequest(request: PermissionRequest) {
                    runOnUiThread {
                        val cameraOnly = request.resources.all { it == PermissionRequest.RESOURCE_VIDEO_CAPTURE }
                        if (cameraOnly && ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            request.grant(arrayOf(PermissionRequest.RESOURCE_VIDEO_CAPTURE))
                        } else request.deny()
                    }
                }
            }
        }
        root.addView(header, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(web, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        setContentView(root)
        web.loadUrl(siteUrl)
    }

    @Deprecated("Deprecated by Android; retained for compatibility")
    override fun onBackPressed() {
        if (::web.isInitialized && web.canGoBack()) web.goBack() else super.onBackPressed()
    }
}
