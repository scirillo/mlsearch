package com.sciri.mlsearch.api.responses

import com.squareup.moshi.Json

class DogApiResponse (
    val message: String,
    @Json(name = "is_success") val isSuccess: Boolean,
    val data: DogResponse
)