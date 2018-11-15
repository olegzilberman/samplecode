package bluerock.com.bluerock.`touch-handlers`

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.AbsListView
import android.widget.Toast
import bluerock.com.bluerock.`data-model`.PhotoDataModel
import bluerock.com.bluerock.`item-detail`.ItemDetail
import bluerock.com.bluerock.`recyclerview-adapters`.PhotoAdapter
import bluerock.com.bluerock.server.NetworkService
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
        disposable = serverCall.getApi()!!.fetchNextPage(++nextPage)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribeWith(object : DisposableObserver<PhotoDataModel>() {
                override fun onNext(data: PhotoDataModel) {
                    val itemList = ArrayList<ItemDetail>()
                    for (item in data.hits) {
                        itemList.add(ItemDetail(item.tags, item.largeImageURL, item.likes, item.user))
                    }
                    //TODO non-trivial implementation of this function is pending.
                    adapter.updateEndOfList(itemList)
                }

                override fun onError(e: Throwable) {
                    Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT).show()
                }

                override fun onComplete() {
                }
            })
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        val total = adapter.itemCount
        val first = (recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
        val last = (recyclerView.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()
    }

}