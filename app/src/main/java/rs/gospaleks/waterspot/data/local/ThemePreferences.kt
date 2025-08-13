package rs.gospaleks.waterspot.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import rs.gospaleks.waterspot.domain.model.AppTheme
import javax.inject.Inject

class ThemePreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val THEME_KEY = stringPreferencesKey("app_theme")
    }

    val selectedTheme: Flow<AppTheme> = dataStore.data.map { preferences ->
        val theme = preferences[THEME_KEY] ?: AppTheme.SYSTEM.name
        AppTheme.valueOf(theme)
    }

    suspend fun saveTheme(theme: AppTheme) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.name
        }
    }
}
