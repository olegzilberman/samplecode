package bluestone.com.bluestone.`touch-handlers`

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.AbsListView
import android.widget.Toast
import bluestone.com.bluestone.`data-model`.PhotoDataModel
import bluestone.com.bluestone.`data-model`.PixabayKey
import bluestone.com.bluestone.`item-detail`.ItemDetail
import bluestone.com.bluestone.`recyclerview-adapters`.PhotoAdapter
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
    private var nextPage : Int
    ) : RecyclerView.OnScrollListener() {
    private var disposable = Disposables.disposed()
    private var scrollAccumulator = 0
    private var prevPage = nextPage

    private enum class ScrollDirection { SCROLL_DOWN, SCROLL_UP, IDLE }

    private var scrollDirection = ScrollDirection.IDLE
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        scrollDirection = when {
            dy < 0 -> ScrollDirection.SCROLL_DOWN
            dy > 0 -> ScrollDirection.SCROLL_UP
            else -> ScrollDirection.IDLE
        }
    }

    private fun advancePageCounter(page: Int, first: Int, last: Int, direction: ScrollDirection): Int {
        var nextPage = page
        when (direction) {
            ScrollDirection.SCROLL_DOWN ->
                if (first == 0 && nextPage > 1) {
                    --nextPage
                    printLog("scrollAccumulator = $scrollAccumulator SCROLLING DOWN first = $first nextPage = $nextPage")
                }
            ScrollDirection.SCROLL_UP ->
                if (last >= PhotoAdapter.maxAdapterSize - 1) {
                    ++nextPage
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
            disposable = fetchTest(PixabayKey, nextPage, PhotoAdapter.maxPageSize)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(object : DisposableObserver<PhotoDataModel>() {
                    override fun onNext(data: PhotoDataModel) {
                        val itemList = ArrayList<ItemDetail>()
                        for (item in data.hits) {
                            itemList.add(ItemDetail(item.tags, item.largeImageURL, item.likes, item.user))
                        }
                        when (scrollDirection) {
                            ScrollDirection.SCROLL_UP -> if (last == PhotoAdapter.maxAdapterSize - 1) adapter.removeFirstNItems(
                                itemList
                            )
                            ScrollDirection.SCROLL_DOWN -> if (first == 0) adapter.removeLastNItems(itemList)
                            else -> printLog("no need to update paging")
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