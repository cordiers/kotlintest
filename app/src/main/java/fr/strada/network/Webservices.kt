package fr.strada.network


import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import fr.strada.models.Json4Kotlin_Base
import okhttp3.OkHttpClient
import okhttp3.OkHttpClient.Builder
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit


interface Webservices {


    @GET("strada.json")
    fun getCard(): Call<Json4Kotlin_Base>


    @POST("api/DriverFile")
    fun fileSending(@Body jsonObject: JsonObject , @Header("Authorization") token:String): Call<ResponseBody>

    companion object Factory {

        val BASE_URL : String ="https://dev.tfdapi.stradatms.net/"
        private val TAG = "ServiceGenerator"

        fun create(): Webservices
        {
            val gson = GsonBuilder().setLenient().create()
            val okhttpClientBuilder = OkHttpClient.Builder()
            okhttpClientBuilder.connectTimeout(50, TimeUnit.SECONDS)
            okhttpClientBuilder.readTimeout(50, TimeUnit.SECONDS)
            okhttpClientBuilder.writeTimeout(50, TimeUnit.SECONDS)
            okhttpClientBuilder.addInterceptor(httpLoggingInterceptor()) // used if network off OR on
            val builder = Retrofit.Builder().baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(okhttpClientBuilder.build())
            var retrofit=builder.build()
            return retrofit.create(Webservices::class.java)
        }

        private fun httpLoggingInterceptor(): HttpLoggingInterceptor {
            val httpLoggingInterceptor =
                HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { message ->
                    Log.d(TAG, "log: http log: $message")
                })
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            return httpLoggingInterceptor
        }
    }
}