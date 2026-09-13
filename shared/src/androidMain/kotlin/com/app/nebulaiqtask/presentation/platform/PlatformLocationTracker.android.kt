package com.app.nebulaiqtask.presentation.platform

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import com.app.nebulaiqtask.domain.model.LocationCoordinate
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

actual class PlatformLocationTracker(
    private val context: Context
) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    private var activeListener: LocationListener? = null

    @SuppressLint("MissingPermission")
    actual fun startLocationUpdates(): Flow<LocationCoordinate> = callbackFlow {
        if (locationManager == null) {
            close()
            return@callbackFlow
        }

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                trySend(
                    LocationCoordinate(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracyMeters = location.accuracy,
                        timestamp = location.time
                    )
                )
            }
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
        activeListener = listener

        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    3000L,
                    2.0f,
                    listener,
                    Looper.getMainLooper()
                )
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    3000L,
                    2.0f,
                    listener,
                    Looper.getMainLooper()
                )
            }

            // Immediately emit last known location if available
            getCurrentLocation()?.let { trySend(it) }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        awaitClose {
            stopLocationUpdates()
        }
    }

    actual fun stopLocationUpdates() {
        activeListener?.let {
            try {
                locationManager?.removeUpdates(it)
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
            activeListener = null
        }
    }

    @SuppressLint("MissingPermission")
    actual fun getCurrentLocation(): LocationCoordinate? {
        val lm = locationManager ?: return null
        return try {
            val lastGps = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val lastNetwork = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val best = when {
                lastGps != null && lastNetwork != null -> if (lastGps.time > lastNetwork.time) lastGps else lastNetwork
                lastGps != null -> lastGps
                else -> lastNetwork
            }

            best?.let {
                LocationCoordinate(
                    latitude = it.latitude,
                    longitude = it.longitude,
                    accuracyMeters = it.accuracy,
                    timestamp = it.time
                )
            }
        } catch (e: SecurityException) {
            null
        }
    }
}
