package com.teckudos.demoapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.teckudos.demoapplication.network.PlacesApi
import com.teckudos.demoapplication.network.PlacesApiFilter
import com.teckudos.demoapplication.network.PlacesDTO
import com.teckudos.demoapplication.view.DEFAULT_LOCATION
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

class MapsViewModel : ViewModel() {

    private val _status = MutableLiveData<PlacesApiStatus>()

    val status: LiveData<PlacesApiStatus>
        get() = _status

    private val _places = MutableLiveData<PlacesDTO>()

    val places: LiveData<PlacesDTO>
        get() = _places

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    init {
//        getPlaces(PlacesApiFilter.SHOW_ALL)
    }

    fun updateFilter(filter: PlacesApiFilter) {
        getPlaces(filter)
    }

    private fun getPlaces(filter: PlacesApiFilter){
        coroutineScope.launch {
            val getPropertiesDeferred = PlacesApi.retrofitService.getPlaces(DEFAULT_LOCATION, "150000",
                filter.value, "AIzaSyDo5mUCwS1ubz5hhqIOmItxdC5QmK6P_c0")
            try {
                _status.value = PlacesApiStatus.LOADING
                val listResult = getPropertiesDeferred.await()
                Timber.i("$listResult")
                _status.value = PlacesApiStatus.DONE
                if (listResult.results.isNotEmpty()) {
                    _places.value = listResult
                }
            } catch (e: Exception) {
                _status.value = PlacesApiStatus.ERROR
//                _places.value = Any()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}

enum class PlacesApiStatus { LOADING, ERROR, DONE }
