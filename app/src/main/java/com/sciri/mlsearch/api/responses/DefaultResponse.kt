package com.sciri.mlsearch.api.responses

import com.squareup.moshi.Json

class DefaultResponse(
    val message: String,
    @Json(name = "is_success") val isSuccess: Boolean
)