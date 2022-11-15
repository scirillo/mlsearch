package com.sciri.mlsearch.api.responses

import com.sciri.mlsearch.api.responses.DigListResponse
import com.squareup.moshi.Json

class DogListApiResponse(
    val message: String,
    @Json(name = "is_success") val isSuccess: Boolean,
    val data: DigListResponse
)