package xyz.zephr.demo.presentation.map.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import xyz.zephr.demo.data.model.Place
import xyz.zephr.demo.domain.repository.AuthRepository
import xyz.zephr.demo.domain.repository.PlacesRepository
import xyz.zephr.demo.presentation.map.model.PlacesUiState
import javax.inject.Inject

@HiltViewModel
class PlacesViewModel @Inject constructor(
    private val placesRepository: PlacesRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlacesUiState())
    val uiState: StateFlow<PlacesUiState> = _uiState.asStateFlow()

    private var hasInitialized = false
    private var isInitializing = false

    init {
        viewModelScope.launch {
            placesRepository.getAllPlaces().collect { places ->
                _uiState.value = _uiState.value.copy(
                    places = places,
                    highlightedPlaceIds = places.filter { it.isInFOV }.map { it.id }.toSet()
                )
            }
        }
    }

    fun initializeWithLocation(location: LatLng) {
        if (hasInitialized || isInitializing) {
            return
        }

        isInitializing = true
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                if (!authRepository.isAuthenticated()) {
                    authRepository.authenticate().getOrThrow()
                }
                placesRepository.initializeWithLocation(location)
                _uiState.value = _uiState.value.copy(isLoading = false)
                hasInitialized = true
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
                hasInitialized = false
            } finally {
                isInitializing = false
            }
        }
    }

    fun refreshPlaces() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                if (!authRepository.isAuthenticated()) {
                    authRepository.authenticate().getOrThrow()
                }
                val result = placesRepository.refreshPlaces()
                if (result.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun selectPlace(place: Place) {
        _uiState.value = _uiState.value.copy(selectedPlace = place)
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedPlace = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
