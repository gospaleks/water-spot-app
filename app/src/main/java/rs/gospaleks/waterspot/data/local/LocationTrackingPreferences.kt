package rs.gospaleks.waterspot.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocationTrackingPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val IS_TRACKING_ENABLED_KEY = booleanPreferencesKey("is_location_tracking_enabled")
    }

    val isTrackingEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_TRACKING_ENABLED_KEY] ?: false
    }

    suspend fun setTrackingEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_TRACKING_ENABLED_KEY] = enabled
        }
    }
}