package rs.gospaleks.waterspot.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import rs.gospaleks.waterspot.data.local.ThemePreferences
import rs.gospaleks.waterspot.domain.model.AppTheme
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themePreferences: ThemePreferences
) : ViewModel() {

    private val _appTheme = MutableStateFlow(AppTheme.SYSTEM)
    val appTheme: StateFlow<AppTheme> = _appTheme.asStateFlow()

    init {
        observeTheme()
    }

    private fun observeTheme() {
        viewModelScope.launch {
            themePreferences.selectedTheme.collect { savedTheme ->
                _appTheme.value = savedTheme
            }
        }
    }

    fun onThemeSelected(theme: AppTheme) {
        viewModelScope.launch {
            themePreferences.saveTheme(theme)
        }
    }
}
