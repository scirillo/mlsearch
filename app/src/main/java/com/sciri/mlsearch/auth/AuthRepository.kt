package com.sciri.mlsearch.auth

import com.sciri.mlsearch.api.ApiResponseStatus
import com.sciri.mlsearch.api.DogsApi
import com.sciri.mlsearch.api.dtos.LoginDTO
import com.sciri.mlsearch.api.dtos.SignUpDTO
import com.sciri.mlsearch.api.dtos.UserDTOMapper
import com.sciri.mlsearch.api.makeNetworkCall
import com.sciri.mlsearch.domains.User

class AuthRepository {
    suspend fun signUp(
        email: String,
        password: String,
        passwordConfirmation: String
    ): ApiResponseStatus<User> = makeNetworkCall {
        val signUpDTO = SignUpDTO(email, password, passwordConfirmation)
        val signUpApiResponse = DogsApi.retrofitService.signUp(signUpDTO)
        if (!signUpApiResponse.isSuccess && signUpApiResponse.data.user == null) {
            throw Exception(signUpApiResponse.message)
        }
        val userDTO = signUpApiResponse.data.user
        UserDTOMapper().fromUserDTOToUserDomain(userDTO)
    }

    suspend fun login(
        email: String,
        password: String
    ): ApiResponseStatus<User> = makeNetworkCall {
        val loginDTO = LoginDTO(email, password)
        val loginResponse = DogsApi.retrofitService.login(loginDTO)
        if (!loginResponse.isSuccess && loginResponse.data.user == null) {
            throw Exception(loginResponse.message)
        }
        val userDTO = loginResponse.data.user
        UserDTOMapper().fromUserDTOToUserDomain(userDTO)
    }
}