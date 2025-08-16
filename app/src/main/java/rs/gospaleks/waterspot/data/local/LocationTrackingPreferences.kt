package rs.gospaleks.waterspot.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocationTrackingPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val IS_TRACKING_ENABLED_KEY = booleanPreferencesKey("is_location_tracking_enabled")
        private val NEARBY_RADIUS_METERS_KEY = intPreferencesKey("nearby_radius_meters")
        private const val DEFAULT_RADIUS_METERS = 100
    }

    val isTrackingEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_TRACKING_ENABLED_KEY] ?: false
    }

    val nearbyRadiusMeters: Flow<Int> = dataStore.data.map { preferences ->
        preferences[NEARBY_RADIUS_METERS_KEY] ?: DEFAULT_RADIUS_METERS
    }

    suspend fun setTrackingEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_TRACKING_ENABLED_KEY] = enabled
        }
    }

    suspend fun setNearbyRadiusMeters(radiusMeters: Int) {
        val clamped = radiusMeters.coerceIn(1, 1000)
        dataStore.edit { preferences ->
            preferences[NEARBY_RADIUS_METERS_KEY] = clamped
        }
    }
}
