package com.app.nebulaiqtask.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

/**
 * Creates a DataStore<Preferences> instance according to the official Android Jetpack KMP guidance:
 * https://developer.android.com/kotlin/multiplatform/datastore
 */
fun createDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() }
    )

const val USER_PREFERENCES_DATASTORE_FILE = "user_preferences.preferences_pb"
