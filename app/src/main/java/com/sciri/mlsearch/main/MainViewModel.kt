package com.sciri.mlsearch.main

import androidx.camera.core.ImageProxy
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sciri.mlsearch.api.ApiResponseStatus
import com.sciri.mlsearch.doglist.DogRepository
import com.sciri.mlsearch.domains.Dog
import com.sciri.mlsearch.machinelearning.Classifier
import com.sciri.mlsearch.machinelearning.ClassifierRepository
import com.sciri.mlsearch.machinelearning.DogRecognition
import kotlinx.coroutines.launch
import java.nio.MappedByteBuffer

class MainViewModel: ViewModel() {
    private lateinit var classifier: Classifier
    private lateinit var classifierRepository: ClassifierRepository

    private val _dog = MutableLiveData<Dog>()
    val dog: LiveData<Dog?>
        get() = _dog

    private val _status = MutableLiveData<ApiResponseStatus<Any>>()
    val status: LiveData<ApiResponseStatus<Any>>
        get() = _status

    private val _dogRecognition = MutableLiveData<DogRecognition>()
    val dogRecognition: LiveData<DogRecognition>
        get() = _dogRecognition

    fun setUpClassifier(tfLiteModel: MappedByteBuffer, labels: List<String>) {
        classifier = Classifier(tfLiteModel, labels)
        classifierRepository = ClassifierRepository(classifier)
    }

    fun recognizeImage(imageProxy: ImageProxy) {
        viewModelScope.launch {
            _dogRecognition.value = classifierRepository.recognizeImage(imageProxy)
        }
    }

    private val dogRepository = DogRepository()
    fun getDogByMlId(mlDogId: String){
        viewModelScope.launch {
            _status.value = ApiResponseStatus.Loading()
            handleResponseStatus(dogRepository.getDogByMlId(mlDogId))
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun handleResponseStatus(apiResponseStatus: ApiResponseStatus<Dog>) {
        if (apiResponseStatus is ApiResponseStatus.Success) {
            _dog.value = apiResponseStatus.data!!
        }
        _status.value = apiResponseStatus as ApiResponseStatus<Any>
    }
}