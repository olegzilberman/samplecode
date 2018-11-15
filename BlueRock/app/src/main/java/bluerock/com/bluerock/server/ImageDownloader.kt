package bluerock.com.bluerock.server

import retrofit2.Retrofit
import retrofit2.Retrofit.Builder
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor


class ImageDownloader(base:String) {
    private var retrofit: Retrofit? = null
    private var api: ImageAccessInterface? = null
    var httpClient = OkHttpClient.Builder()
    val logging = HttpLoggingInterceptor()
    var baseUrl:String
    init {
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClient.addInterceptor(logging)
        baseUrl = base

    }
    // set your desired log level
    private fun getRetrofit(): Retrofit? {
        if (retrofit == null) {
            retrofit = Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build()
        }
        return retrofit
    }

    fun getApi(): ImageAccessInterface? {
        if (api == null) {
            api = getRetrofit()?.create<ImageAccessInterface>(ImageAccessInterface::class.java)
        }
        return api
    }
}