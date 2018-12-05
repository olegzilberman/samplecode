package bluestone.com.bluestone.`item-detail`

import kotlinx.serialization.SerialId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemDetail(
    @SerialName("imageName") val imageName: String,
    @SerialName("imageUrl") val imageUrl: String,
    @SerialName("like_count") val like_count: Int, val authorName: String
)

@Serializable
data class ItemDetailListDescriptor(
    @SerialId(1) val firstVisible: Int,
    @SerialId(2) val itemList: List<ItemDetail>
)