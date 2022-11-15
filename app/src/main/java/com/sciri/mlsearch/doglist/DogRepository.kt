package com.sciri.mlsearch.doglist

import com.sciri.mlsearch.api.ApiResponseStatus
import com.sciri.mlsearch.api.DogsApi
import com.sciri.mlsearch.api.dtos.AddDogToUserDTO
import com.sciri.mlsearch.api.dtos.DogDTOMapper
import com.sciri.mlsearch.api.makeNetworkCall
import com.sciri.mlsearch.domains.Dog
import com.sciri.mlsearch.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class DogRepository {

    suspend fun getDogsCollection(): ApiResponseStatus<List<Dog>> {
        return withContext(Dispatchers.IO) {
            val allDogsListDeferred = async { downloadDogs() }
            val userDogsListDeferred = async { getUserDogs() }

            val allDogsListResponse = allDogsListDeferred.await()
            val userDogsListResponse = userDogsListDeferred.await()

            if (allDogsListResponse is ApiResponseStatus.Error) {
                allDogsListResponse
            } else if (userDogsListResponse is ApiResponseStatus.Error) {
                userDogsListResponse
            } else if (allDogsListResponse is ApiResponseStatus.Success &&
                userDogsListResponse is ApiResponseStatus.Success
            ) {
                ApiResponseStatus.Success(
                    getCollectionList(
                        allDogsListResponse.data,
                        userDogsListResponse.data
                    )
                )
            } else {
                ApiResponseStatus.Error(R.string.error_msg_unknokn)
            }
        }
    }

    private fun getCollectionList(
        allDogsListResponse: List<Dog>,
        userDogListResponse: List<Dog>
    ): List<Dog> {
        return allDogsListResponse.map {
            if (userDogListResponse.contains(it)) {
                it
            } else {
                Dog(
                    0, it.index, "", "", "", "",
                    "", "", "", "", "",
                    inCollection = false
                )
            }
        }.sorted()
    }

    suspend fun addDogToUser(dogId: Long): ApiResponseStatus<Any> = makeNetworkCall {
        val defaultResponse = DogsApi.retrofitService.addDogToUser(AddDogToUserDTO(dogId))
        if (!defaultResponse.isSuccess) {
            throw Exception(defaultResponse.message)
        }
    }

    private suspend fun getUserDogs(): ApiResponseStatus<List<Dog>> = makeNetworkCall {
        val dogListApiResponse = DogsApi.retrofitService.getUserDogs()
        val dogDTOList = dogListApiResponse.data.dogs
        val dogDTOMapper = DogDTOMapper()
        dogDTOMapper.formDogDTOListToDogDomainList(dogDTOList)
    }

    private suspend fun downloadDogs(): ApiResponseStatus<List<Dog>> = makeNetworkCall {
        val dogListApiResponse = DogsApi.retrofitService.getAllDogs()
        val dogDTOList = dogListApiResponse.data.dogs
        val dogDTOMapper = DogDTOMapper()
        dogDTOMapper.formDogDTOListToDogDomainList(dogDTOList)
    }

    suspend fun getDogByMlId(mlDogId: String): ApiResponseStatus<Dog> = makeNetworkCall {
        val dogResponse = DogsApi.retrofitService.getDogByMlId(mlDogId)
        if (!dogResponse.isSuccess && dogResponse.data == null) {
            throw Exception(dogResponse.message)
        }
        val dogDTOMapper = DogDTOMapper()
        dogDTOMapper.fromDogDTOToDogDomain(dogResponse.data.dog)
    }
}