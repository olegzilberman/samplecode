package bluestone.com.bluestone.`data-model`
import com.google.gson.annotations.SerializedName
class PhotoData(
    @SerializedName("pageURL") val pageURL : String,
    @SerializedName("previewURL") val previewURL : String,
    @SerializedName("previewWidth") val previewWidth : Int,
    @SerializedName("previewHeight") val previewHeight : Int,
    @SerializedName("largeImageURL") val largeImageURL : String,
    @SerializedName("imageWidth") val imageWidth : Int,
    @SerializedName("imageHeight") val imageHeight : Int,
    @SerializedName("likes") val likes : Int,
    @SerializedName("user") val user : String,
    @SerializedName("userImageURL") val userImageURL : String,
    @SerializedName("tags") val tags : String
)

class PhotoDataModel(
    @SerializedName("total") val id:Int,
    @SerializedName("totalHits") val totalHits:Int,
    @SerializedName("hits") val hits:List<PhotoData>
)

