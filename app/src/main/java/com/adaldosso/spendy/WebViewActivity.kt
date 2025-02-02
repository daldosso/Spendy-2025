package com.adaldosso.spendy

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging

class WebViewActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var locationManager: LocationManager
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    private var capturedImage: Bitmap? = null
    private var latitude = 0.0
    private var longitude = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        initializeWebView()
        initializeLocationManager()
        initializeCameraLauncher()
        checkAndRequestPermissions()
        initializeFirebaseMessaging()
    }

    private fun initializeWebView() {
        webView = findViewById<WebView>(R.id.webView).apply {
            settings.javaScriptEnabled = true
            addJavascriptInterface(WebAppInterface(), "Android")
            loadUrl("http://10.0.2.2:4200/")
        }
    }

    private fun initializeLocationManager() {
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
    }

    private fun initializeCameraLauncher() {
        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                capturedImage = result.data?.extras?.get("data") as? Bitmap
                showToast("Immagine catturata con successo")
            } else {
                showToast("Impossibile catturare l'immagine")
            }
        }
    }

    private fun checkAndRequestPermissions() {
        if (!isPermissionGranted(Manifest.permission.CAMERA)) requestPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_REQUEST)
        if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, LOCATION_PERMISSION_REQUEST)
        else getLocation()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (!isGranted) println("❌ Permesso POST_NOTIFICATIONS negato dall'utente.")
            }
            if (!isPermissionGranted(Manifest.permission.POST_NOTIFICATIONS)) requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun initializeFirebaseMessaging() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) println("FCM Token: ${task.result}")
            else println("Errore nel recupero del token FCM")
        }
    }

    private fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
    }

    private fun getLocation() {
        if (isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
                1f,
                object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        latitude = location.latitude
                        longitude = location.longitude
                        showToast("Posizione aggiornata: $latitude, $longitude")
                    }

                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }
            )
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    inner class WebAppInterface {
        @JavascriptInterface
        fun openCamera() {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
                cameraLauncher.launch(cameraIntent)
            } catch (e: Exception) {
                runOnUiThread { showToast("Errore nell'aprire la fotocamera: ${e.message}") }
            }
        }

        @JavascriptInterface
        fun getCoordinates(): String = "$latitude,$longitude"

        @JavascriptInterface
        fun getCapturedImage(): String? {
            return capturedImage?.let {
                val outputStream = java.io.ByteArrayOutputStream()
                it.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                android.util.Base64.encodeToString(outputStream.toByteArray(), android.util.Base64.DEFAULT)
            }
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 101
        private const val LOCATION_PERMISSION_REQUEST = 102
    }
}
