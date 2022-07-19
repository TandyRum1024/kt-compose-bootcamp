package com.vin.composebootcamp

import android.R
import android.content.res.Resources
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.res.stringResource
import com.google.gson.JsonDeserializer
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Path

const val DOG_API_URL = "https://dog.ceo"



/*
    Dog API models
 */
data class Dog(val id: String, val name: String = id) {
    val imgUrl: String
        get() = "https://dog.ceo/api/breed/$id/images/random"
}

data class DogBreedsResponse (
    @SerializedName("message")
    val breedMap: Map<String, List<String>>,
    @SerializedName("status")
    val status: String,
)

data class DogImageResponse (
    @SerializedName("message")
    val url: String,
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
    @GET("/api/breed/{dogId}/images/random")
    fun getRandomBreedImageUrl(@Path("dogId") dogId: String): Call<DogImageResponse>
}

fun FetchAllDogBreeds(onFetchSuccess: (List<String>) -> Unit = {}, onFetchFail: () -> Unit): List<String>? {
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

                onFetchSuccess(list!!)
                Log.e("DOG", "result: ${list!!.joinToString(prefix = "[", postfix = "]")}")
            } else { // HTTP Failed
                Log.e("DOG", "getBreeds() API CALL FAILED! (failed response)")
                onFetchFail()
            }
        }

        override fun onFailure(call: Call<DogBreedsResponse>, t: Throwable) {
            Log.e("DOG", "getBreeds() API CALL FAILED!")
            onFetchFail()
        }

    })

    return list?.toList()
}

/*
    'Cached' dog data singleton, fetched from the external DB (accessible via Dog API)
    For now it only stores a list of dog breeds
 */
object DogData {
    var dogBreeds: List<String>? = null

    fun updateBreeds(onUpdateDone: (List<String>) -> Unit = {}, onFail: () -> Unit = {}) {
        dogBreeds = FetchAllDogBreeds(onUpdateDone, onFail)
    }
}

/*
    Helper function for 'translating' the dog name from dog ID
 */
fun translateDogIdToStringRes(id: String): Int {
    return Resources.getSystem().getIdentifier(id, null, null)
}