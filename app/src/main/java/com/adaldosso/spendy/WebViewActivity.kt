package com.adaldosso.spendy

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
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

class WebViewActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var locationManager: LocationManager
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>

    private var capturedImage: Bitmap? = null
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        webView = findViewById(R.id.webView)
        webView.settings.javaScriptEnabled = true
        webView.addJavascriptInterface(WebAppInterface(), "Android")

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        // Inizializza il launcher della fotocamera
        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                capturedImage = data?.extras?.get("data") as Bitmap
                Toast.makeText(this, "Immagine catturata con successo", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Impossibile catturare l'immagine", Toast.LENGTH_SHORT).show()
            }
        }

        // Controlla i permessi
        if (!isCameraPermissionGranted()) {
            requestCameraPermission()
        }
        if (!isLocationPermissionGranted()) {
            requestLocationPermission()
        } else {
            getLocation()
        }

        // Carica la tua applicazione web
        webView.loadUrl("http://10.0.2.2:4200/")
    }

    // Controlla se il permesso della fotocamera è già stato concesso
    private fun isCameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Controlla se il permesso della posizione è già stato concesso
    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Richiede il permesso della fotocamera
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST
        )
    }

    // Richiede il permesso della posizione
    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST
        )
    }

    // Ottieni le coordinate GPS
    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
                1f,
                object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        latitude = location.latitude
                        longitude = location.longitude
                        Toast.makeText(
                            this@WebViewActivity,
                            "Posizione aggiornata: $latitude, $longitude",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }
            )
        }
    }

    // Interfaccia JavaScript per il GPS e la fotocamera
    inner class WebAppInterface {

        @JavascriptInterface
        fun openCamera() {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
                cameraLauncher.launch(cameraIntent)
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@WebViewActivity,
                        "Errore nell'aprire la fotocamera: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        @JavascriptInterface
        fun getCoordinates(): String {
            return "$latitude,$longitude"
        }

        @JavascriptInterface
        fun getCapturedImage(): String? {
            return if (capturedImage != null) {
                val outputStream = java.io.ByteArrayOutputStream()
                capturedImage?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                android.util.Base64.encodeToString(outputStream.toByteArray(), android.util.Base64.DEFAULT)
            } else {
                null
            }
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 101
        private const val LOCATION_PERMISSION_REQUEST = 102
    }
}
