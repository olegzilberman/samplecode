package bluerock.com.bluerock.server

import io.reactivex.Observable
import retrofit2.http.GET
import bluerock.com.bluerock.`data-model`.PhotoDataModel
import retrofit2.http.Path

interface ImageAccessInterface {
    @GET("{end_point}")
    fun fetchImage(@Path("end_point") endPoint:String) : Observable<PhotoDataModel>
}