package com.adaldosso.spendy

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class WebViewActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var capturedImage: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        webView = findViewById(R.id.webView)
        webView.settings.javaScriptEnabled = true
        webView.addJavascriptInterface(WebAppInterface(), "Android")

        // Carica la tua applicazione web
        webView.loadUrl("http://10.0.2.2:4200/")

        // Controlla i permessi
        if (!isCameraPermissionGranted()) {
            requestCameraPermission()
        }
    }

    // Controlla se il permesso della fotocamera è già stato concesso
    private fun isCameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    // Richiede il permesso della fotocamera
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST
        )
    }

    // Gestione della risposta ai permessi
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permesso fotocamera concesso", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permesso fotocamera negato", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Interfaccia JavaScript per l'accesso alla fotocamera
    inner class WebAppInterface {

        @JavascriptInterface
        fun openCamera() {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
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

    // Gestione del risultato della fotocamera
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            capturedImage = data?.extras?.get("data") as Bitmap
            Toast.makeText(this, "Immagine catturata con successo", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Impossibile catturare l'immagine", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val CAMERA_REQUEST_CODE = 100
        private const val CAMERA_PERMISSION_REQUEST = 101
    }
}
