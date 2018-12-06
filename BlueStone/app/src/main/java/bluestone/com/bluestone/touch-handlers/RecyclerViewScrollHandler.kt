package bluestone.com.bluestone.`touch-handlers`

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.AbsListView
import android.widget.Toast
import bluestone.com.bluestone.`data-model`.PhotoDataModel
import bluestone.com.bluestone.`item-detail`.ItemDetail
import bluestone.com.bluestone.`recyclerview-adapters`.PhotoAdapter
import bluestone.com.bluestone.data_model_loader.DisplayedPageStateLoader
import bluestone.com.bluestone.server.NetworkService
import bluestone.com.bluestone.utilities.printLog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers

class RecyclerViewScrollHandler(
    private val context: Context,
    private val adapter: PhotoAdapter,
    private val serverCall: NetworkService,
    private val pageStateLoader: DisplayedPageStateLoader
) : RecyclerView.OnScrollListener() {
    private val topPageBuffer: ArrayList<ItemDetail> = arrayListOf<ItemDetail>()
    private var disposable = Disposables.disposed()
    private var scrollAccumulator = 0
    private var nextPage = 1
    private var prevPage = 1
    private var scrollDirection = 0
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        if (dy < 0)
            scrollDirection = -1
        else
            scrollDirection = 1
    }

    private fun advancePageCounter(page: Int, first: Int, last: Int, delta:Int): Int {
        var nextPage = page
        when {
            delta < 0 ->
                if (last >= PhotoAdapter.maxAdapterSize - 1) {
                    ++nextPage
                    printLog("scrollAccumulator = $scrollAccumulator SCROLLING DOWN first = $first nextPage = $nextPage")
                }
            delta > 0 ->
                if (first == 0 && nextPage > 0) {
                    --nextPage
                    printLog("scrollAccumulator = $scrollAccumulator SCROLLING UP first = $first nextPage = $nextPage")
                }
            else ->
                printLog("No scroll action taken")
        }
        return nextPage
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        if (newState != AbsListView.OnScrollListener.SCROLL_STATE_IDLE)
            return
        val last = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
        val first = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        nextPage = advancePageCounter(nextPage, first, last, scrollDirection)

        if (prevPage == nextPage)
            return
        prevPage = nextPage
        serverCall.getApi()?.run {
            disposable.dispose()
            printLog("Count before scroll ${adapter.itemCount}")
            disposable = fetchNextPage(nextPage, PhotoAdapter.maxPageSize)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(object : DisposableObserver<PhotoDataModel>() {
                    override fun onNext(data: PhotoDataModel) {
                        val itemList = ArrayList<ItemDetail>()
                        for (item in data.hits) {
                            itemList.add(ItemDetail(item.tags, item.largeImageURL, item.likes, item.user))
                        }
                        topPageBuffer.addAll(adapter.getAllItems())
                        if (adapter.itemCount < PhotoAdapter.maxAdapterSize) {
                            adapter.updateEndOfList(itemList)
                        } else if (scrollDirection < 0){
                            adapter.removeFirstNItems(itemList)
                        } else if (scrollDirection > 0){
                            adapter.removeLastNItems(itemList)
                        }
                    }

                    override fun onError(e: Throwable) {
                        Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT).show()
                    }

                    override fun onComplete() {
                    }
                })
        }
    }

}