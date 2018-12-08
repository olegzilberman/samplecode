package bluestone.com.bluestone.`recyclerview-adapters`

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import bluestone.com.bluestone.R
import bluestone.com.bluestone.`item-detail`.ItemDetail
import com.squareup.picasso.Picasso

class PhotoAdapter : RecyclerView.Adapter<PhotoAdapter.PhotoHolder>() {
    private val itemList = ArrayList<ItemDetail>()

    inner class PhotoHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(itemDetail: ItemDetail) {
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

    fun removeFirstNItems(newItems: List<ItemDetail>) {
        for (  i in 0 until newItems.size)
            if (i < itemList.size)
                itemList.removeAt(i)
            else
                break

        itemList.addAll(itemList.size, newItems)
        notifyItemRangeRemoved(0, newItems.size)
    }

    fun removeLastNItems(newItems:List<ItemDetail>) {
        val start = itemList.size - newItems.size
        if (start < 0)
            return
        val originalSize = itemList.size
        for (i in start until originalSize)
            itemList.removeAt(itemList.size-1)
        itemList.addAll(0, newItems)
        notifyItemRangeInserted(0, newItems.size)
    }

    fun update(newItems: List<ItemDetail>) {
        itemList.clear()
        itemList.addAll(newItems)
        notifyDataSetChanged()
    }
    fun update(){
        notifyDataSetChanged()
    }
    fun getItemDetailByPosition(index: Int): ItemDetail {
        return itemList[index]
    }

    fun getAllItems(): List<ItemDetail> {
        return itemList.toMutableList()
    }
    companion object {
        val maxAdapterSize = 15
        val maxPageSize = 5
    }
}