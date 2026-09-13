package com.app.nebulaiqtask

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.app.nebulaiqtask.data.auth.FirebaseAuthManager
import com.app.nebulaiqtask.data.session.UserSessionManager
import com.app.nebulaiqtask.service.GeofenceForegroundService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val userSessionManager: UserSessionManager by inject()
    private val firebaseAuthManager: FirebaseAuthManager by inject()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        if (fineLocationGranted) {
            startGeofenceService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Sign in with Firebase Anonymous Auth and initialize the session
        initializeUserSession()

        checkAndRequestPermissions()

        setContent {
            App()
        }
    }

    /**
     * Performs Firebase Anonymous Auth on every launch.
     * The UID is stable and persists across sessions — Firebase re-uses
     * the same anonymous account once created on the device.
     * After auth, UserSessionManager is initialized with the real UID
     * and any saved display name from SharedPreferences.
     */
    private fun initializeUserSession() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val uid = firebaseAuthManager.signInAnonymously()
                val savedName = firebaseAuthManager.getDisplayName() ?: "User ${uid.take(4)}"
                userSessionManager.initialize(userId = uid, displayName = savedName)
            } catch (e: Exception) {
                // If Firebase is unreachable, fall back to a device-local ID
                val fallbackId = firebaseAuthManager.getCurrentUserId()
                    ?: "local_${android.os.Build.FINGERPRINT.hashCode().toString(16)}"
                val savedName = firebaseAuthManager.getDisplayName() ?: "Operator"
                userSessionManager.initialize(userId = fallbackId, displayName = savedName)
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            startGeofenceService()
        }
    }

    private fun startGeofenceService() {
        try {
            val serviceIntent = Intent(this, GeofenceForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}