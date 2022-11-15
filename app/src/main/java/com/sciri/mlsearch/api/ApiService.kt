package com.sciri.mlsearch.api

import com.sciri.mlsearch.api.dtos.AddDogToUserDTO
import com.sciri.mlsearch.api.dtos.LoginDTO
import com.sciri.mlsearch.api.dtos.SignUpDTO
import com.sciri.mlsearch.api.interceptors.ApiServiceInterceptor
import com.sciri.mlsearch.api.responses.AuthApiResponse
import com.sciri.mlsearch.api.responses.DefaultResponse
import com.sciri.mlsearch.api.responses.DogApiResponse
import com.sciri.mlsearch.api.responses.DogListApiResponse
import com.sciri.mlsearch.*
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

private val okkHttpBuilder = OkHttpClient.Builder().addInterceptor(ApiServiceInterceptor).build()

private val retrofit =
    Retrofit.Builder().client(okkHttpBuilder).baseUrl(BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create()).build()

interface ApiService {
    @GET(GET_ALL_DOGS_URL)
    suspend fun getAllDogs(): DogListApiResponse

    @POST(SIGN_UP_URL)
    suspend fun signUp(@Body signUpDTO: SignUpDTO): AuthApiResponse

    @POST(LOGIN_URL)
    suspend fun login(@Body loginDTO: LoginDTO): AuthApiResponse

    @Headers("${ApiServiceInterceptor.NEEDS_AUTH_HEADER_KEY}: true")
    @POST(ADD_DOG_TO_USER_URL)
    suspend fun addDogToUser(@Body addDogToUserDTO: AddDogToUserDTO): DefaultResponse

    @Headers("${ApiServiceInterceptor.NEEDS_AUTH_HEADER_KEY}: true")
    @GET(GET_USER_DOGS_URL)
    suspend fun getUserDogs(): DogListApiResponse

    @GET(GET_DOG_BY_ML_ID_URL)
    suspend fun getDogByMlId(@Query("ml_id") mlId: String): DogApiResponse
}

object DogsApi {
    val retrofitService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}