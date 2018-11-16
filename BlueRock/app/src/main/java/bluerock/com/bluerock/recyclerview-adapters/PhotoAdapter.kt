package bluerock.com.bluerock.`recyclerview-adapters`
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import bluerock.com.bluerock.R
import bluerock.com.bluerock.`item-detail`.ItemDetail
import com.squareup.picasso.Picasso

class PhotoAdapter : RecyclerView.Adapter<PhotoAdapter.PhotoHolder>() {
    private val itemList = ArrayList<ItemDetail>()
    inner class PhotoHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(itemDetail: ItemDetail){
            itemView.findViewById<TextView>(R.id.author_name).text = itemDetail.authorName
            itemView.findViewById<TextView>(R.id.image_name).text = itemDetail.imageName
            itemView.findViewById<TextView>(R.id.like_count).text = itemDetail.like_count.toString()
            val item = itemView.findViewById<ImageView>(R.id.photo_item)
            if (item.tag == null || item.tag != itemDetail.imageUrl) {
                item.tag = itemDetail.imageUrl
                Picasso.get()
                    .load(itemDetail.imageUrl)
                    .noPlaceholder()
                    .fit()
                    .into(itemView.findViewById<ImageView>(R.id.photo_item))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_detail, parent, false)

        return PhotoHolder(itemView)
    }

    override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    fun updateEndOfList(newItems: List<ItemDetail>){
        itemList.addAll(newItems)
        notifyItemInserted(itemList.size-1)
    }
    fun update(newItems:List<ItemDetail>){
        itemList.clear()
        itemList.addAll(newItems)
        notifyDataSetChanged()
    }
    fun getItemDetailByPosition(index:Int) : ItemDetail {
        return itemList[index]
    }
}