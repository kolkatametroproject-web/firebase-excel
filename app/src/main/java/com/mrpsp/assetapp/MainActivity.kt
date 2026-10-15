package com.mrpsp.assetapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

/** MRPSP Asset App: WebView wrapper for the hosted asset workflow. */
class MainActivity : ComponentActivity() {
    private lateinit var web: WebView
    private val hostingUrl = "https://kolkatametroproject-web.github.io/firebase-excel/"
    private val releaseApi = "https://api.github.com/repos/kolkatametroproject-web/MRPSP-Asset-App/releases/latest"
    private val executor = Executors.newSingleThreadExecutor()

    private val cameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) loadSite() else showCameraRequired()
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
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(Color.rgb(7, 59, 112))
        }
        val title = TextView(this).apply {
            text = "MRPSP  •  ASSET CHECKOUT / RETURN"
            setTextColor(Color.WHITE)
            textSize = 15f
            setPadding(14, 16, 8, 16)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        val updateButton = Button(this).apply {
            text = "Check updates"
            setOnClickListener { checkForUpdate(true) }
        }
        header.addView(title)
        header.addView(updateButton)

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
                    val allowedHost = uri.host == "kolkatametroproject-web.github.io" ||
                        uri.host == "firebase-excel.web.app" ||
                        uri.host == "firebase-excel.firebaseapp.com" ||
                        uri.host == "metro-railway-project.firebaseapp.com"
                    return !(uri.scheme == "https" && allowedHost)
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onPermissionRequest(request: PermissionRequest) {
                    runOnUiThread {
                        val wantsVideo = request.resources.any { it == PermissionRequest.RESOURCE_VIDEO_CAPTURE }
                        if (wantsVideo && ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            request.grant(arrayOf(PermissionRequest.RESOURCE_VIDEO_CAPTURE))
                        } else request.deny()
                    }
                }
            }
        }
        root.addView(header, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(web, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        setContentView(root)
        web.loadUrl(hostingUrl)
        checkForUpdate(false)
    }

    private fun checkForUpdate(showCurrent: Boolean) {
        executor.execute {
            var message = "Could not check for updates. Check your internet connection."
            try {
                val connection = (URL(releaseApi).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 8000
                    readTimeout = 8000
                    setRequestProperty("Accept", "application/vnd.github+json")
                    setRequestProperty("User-Agent", "MRPSP-Asset-App")
                }
                val body = connection.inputStream.bufferedReader().use { it.readText() }
                connection.disconnect()
                val release = JSONObject(body)
                val tag = release.optString("tag_name", "")
                val assets = release.optJSONArray("assets")
                var apkUrl: String? = null
                if (assets != null) for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    if (asset.optString("name").endsWith(".apk", true)) {
                        apkUrl = asset.optString("browser_download_url")
                        break
                    }
                }
                val currentCode = packageManager.getPackageInfo(packageName, 0).longVersionCode
                val releaseCode = tag.removePrefix("v").substringBefore("-").split(".")
                    .mapNotNull { it.toIntOrNull() }.let { parts ->
                        if (parts.isEmpty()) 0L else (parts.getOrElse(0) { 0 } * 10000L) +
                            (parts.getOrElse(1) { 0 } * 100L) + parts.getOrElse(2) { 0 }
                    }
                message = when {
                    apkUrl.isNullOrBlank() -> "A GitHub Release exists, but it has no APK asset attached."
                    releaseCode > currentCode -> "Update $tag is available. Download it from GitHub Releases: ${release.optString("html_url")}"
                    showCurrent -> "You are using the latest available version ($tag)."
                    else -> ""
                }
            } catch (_: Exception) { }
            if (message.isNotEmpty()) runOnUiThread {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showCameraRequired() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Camera required")
            .setMessage("MRPSP Asset App needs camera access for face verification and QR/barcode scanning. Grant camera permission to continue.")
            .setCancelable(false)
            .setPositiveButton("Try again") { _, _ -> cameraPermission.launch(Manifest.permission.CAMERA) }
            .setNegativeButton("Exit") { _, _ -> finish() }
            .show()
    }

    @Deprecated("Deprecated by Android; retained for compatibility")
    override fun onBackPressed() {
        if (::web.isInitialized && web.canGoBack()) web.goBack() else super.onBackPressed()
    }

    override fun onDestroy() {
        executor.shutdownNow()
        if (::web.isInitialized) web.destroy()
        super.onDestroy()
    }
}
