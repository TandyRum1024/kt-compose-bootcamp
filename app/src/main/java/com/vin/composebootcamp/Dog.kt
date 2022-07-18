package com.vin.composebootcamp

import android.util.Log
import android.widget.Toast
import com.google.gson.JsonDeserializer
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.create
import retrofit2.http.GET

const val DOG_API_URL = "https://dog.ceo"

/*
    Dog API models
 */
data class Dog(val id: String, val name: String = id) {
    val imgUrl: String
        get() = "https://picsum.photos/id/237/200"
}

data class DogBreedsResponse (
    @SerializedName("message")
    val breedMap: Map<String, List<String>>,
    @SerializedName("status")
    val status: String,
)

/*
    Dog API interface
 */
object DogApi {
    val api = RetrofitHandler.getInstance(DOG_API_URL).create(DogApiService::class.java)
}
interface DogApiService {
    @GET("/api/breeds/list/all")
    fun getBreeds(): Call<DogBreedsResponse>
}

fun FetchAllDogBreeds(): List<String>? {
    var list: MutableList<String>? = null

    // Request dog breed list
    val response = DogApi.api.getBreeds()
    response.enqueue(object: Callback<DogBreedsResponse> {
        override fun onResponse(
            call: Call<DogBreedsResponse>,
            response: Response<DogBreedsResponse>
        ) {
            if (response.isSuccessful() && response.body()!!.status == "success") { // HTTP Success
                Log.e("DOG", "${"=".repeat(42)} getBreeds() API CALL ${"=".repeat(42)}")

                list = mutableListOf()
                for ((breed, subBreed) in response.body()!!.breedMap) {
                    list!!.add(breed)
                    Log.e("DOG", "\tbreed: $breed => ${subBreed.joinToString(prefix = "[", postfix = "]")}")
                }
                Log.e("DOG", "${"=".repeat(42)} //getBreeds() API CALL// ${"=".repeat(42)}")

                Log.e("DOG", "result: ${list!!.joinToString(prefix = "[", postfix = "]")}")
            } else { // HTTP Failed
                Log.e("DOG", "getBreeds() API CALL FAILED! (failed response)")
            }
        }

        override fun onFailure(call: Call<DogBreedsResponse>, t: Throwable) {
            Log.e("DOG", "getBreeds() API CALL FAILED!")
        }

    })

    return list?.toList()
}