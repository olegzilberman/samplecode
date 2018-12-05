package bluestone.com.bluestone.`data-model`
import bluestone.com.bluestone.`item-detail`.ItemDetail
import kotlinx.serialization.SerialId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DisplayedPageState(@SerialId(1) val nextPage:Int,
                              @SerialId(2)val firstVisible:Int,
                              @SerialId(3)val items:List<ItemDetail>) {
}