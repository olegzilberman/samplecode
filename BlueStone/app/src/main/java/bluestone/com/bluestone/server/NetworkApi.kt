package bluestone.com.bluestone.server

import io.reactivex.Observable
import retrofit2.http.GET
import bluestone.com.bluestone.`data-model`.PhotoDataModel
import io.reactivex.Single
import retrofit2.http.Path
import retrofit2.http.Query

interface NetworkApi {
   @GET("q{search_string}&image_type{image_type}&editors_choice=1&safesearch=1")
   fun fetchUsingSearchObservable(@Path("search_string")search_string:String,
                                 @Path("image_type")image_type:String,
                                 @Path("category")category:String) : Observable<PhotoDataModel>
    @GET("?key=10583771-d9cdc2ef7dce85b4c6299413f&editors_choice=1")
    fun fetchAll(@Query("per_page") per_page:Int) : Single<PhotoDataModel>

    @GET("?key=10583771-d9cdc2ef7dce85b4c6299413f&editors_choice=1")
    fun fetchNextPage(@Query("page") page:Int, @Query("per_page") per_page:Int) : Observable<PhotoDataModel>
}