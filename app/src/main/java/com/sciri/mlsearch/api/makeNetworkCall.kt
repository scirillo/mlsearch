package com.sciri.mlsearch.api

import com.sciri.mlsearch.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.UnknownHostException

suspend fun <T> makeNetworkCall(
    call: suspend () -> T
): ApiResponseStatus<T> = withContext(Dispatchers.IO) {
    try {
        ApiResponseStatus.Success(call())
    } catch (e: UnknownHostException) {
        ApiResponseStatus.Error(R.string.error_msg_no_internet)
    } catch (e: HttpException) {
        val errorMessage = if (e.code() == 401) {
            R.string.wrong_user_or_password
        } else {
            R.string.error_msg_unknokn
        }
        ApiResponseStatus.Error(errorMessage)
    } catch (e: Exception) {
        val errorMessage = when (e.message) {
            "sign_up_error" -> R.string.error_sign_up
            "sign_in_error" -> R.string.error_sign_in
            "user_already_exists" -> R.string.user_already_exists
            "error_adding_dog" -> R.string.error_adding_dog
            else -> R.string.error_msg_unknokn
        }
        ApiResponseStatus.Error(errorMessage)
    }
}
