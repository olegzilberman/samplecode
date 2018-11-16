package bluerock.com.bluerock.server

import retrofit2.Retrofit
import retrofit2.Retrofit.Builder
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor


class NetworkService(base:String) {
    private var retrofit: Retrofit? = null
    private var api: NetworkApi? = null
    var httpClient = OkHttpClient.Builder()
    val logging = HttpLoggingInterceptor()
    var baseUrl:String
    init {
        //Minimum log output for http calls
        logging.level = HttpLoggingInterceptor.Level.NONE
        httpClient.addInterceptor(logging)
        baseUrl = base

    }
// set your desired log level
    private fun getRetrofit(): Retrofit? {
        if (retrofit == null) {
            retrofit = Builder()
                .baseUrl(baseUrl)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build()
        }
        return retrofit
    }

    fun getApi(): NetworkApi? {
        if (api == null) {
            api = getRetrofit()?.create<NetworkApi>(NetworkApi::class.java)
        }
        return api
    }
}