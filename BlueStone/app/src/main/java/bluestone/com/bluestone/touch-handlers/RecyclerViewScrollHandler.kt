package bluestone.com.bluestone.`touch-handlers`

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.AbsListView
import android.widget.Toast
import bluestone.com.bluestone.`data-model`.PhotoDataModel
import bluestone.com.bluestone.`item-detail`.ItemDetail
import bluestone.com.bluestone.`recyclerview-adapters`.PhotoAdapter
import bluestone.com.bluestone.server.NetworkService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class RecyclerViewScrollHandler(private val context: Context,
                                private val adapter: PhotoAdapter,
                                private val serverCall:NetworkService) : RecyclerView.OnScrollListener(){
    private var disposable = Disposables.disposed()
    private var nextPage = 0
    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        if (newState != AbsListView.OnScrollListener.SCROLL_STATE_IDLE)
            return
        val last = (recyclerView.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()
        if (last < adapter.itemCount-3)
            return
        disposable.dispose()
        disposable = serverCall.getApi()!!.fetchNextPage(++nextPage, 5)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribeWith(object : DisposableObserver<PhotoDataModel>() {
                override fun onNext(data: PhotoDataModel) {
                    val itemList = ArrayList<ItemDetail>()
                    for (item in data.hits) {
                        itemList.add(ItemDetail(item.tags, item.largeImageURL, item.likes, item.user))
                    }
                    adapter.updateEndOfList(itemList)
                }

                override fun onError(e: Throwable) {
                    Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT).show()
                }

                override fun onComplete() {
                }
            })
    }
}