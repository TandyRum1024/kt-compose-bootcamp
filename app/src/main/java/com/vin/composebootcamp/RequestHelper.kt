package com.vin.composebootcamp

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

// Refer: https://dev-cho.tistory.com/27

// Singleton for Retrofit2 handler
object RetrofitHandler {
    private var baseUrl: String? = null
    var instance: Retrofit? = null
        get() {
            if (field == null) {
                field = Retrofit.Builder()
                    .baseUrl(this.baseUrl!!)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return field
        }

    fun getInstance (baseUrl: String?): Retrofit {
        if (baseUrl != null) setUrl(baseUrl)
        return instance!!
    }

    fun setUrl (baseUrl: String) {
        this.baseUrl = baseUrl
        this.instance = null
    }
}

fun RequestUrl (baseUrl: String) {
    val service = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}