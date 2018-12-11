package bluestone.com.bluestone.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import bluestone.com.bluestone.R
import bluestone.com.bluestone.`cache-manager`.CacheManager
import bluestone.com.bluestone.`data-model`.DisplayedPageState
import bluestone.com.bluestone.`data-model`.FragmentCreationDescriptor
import bluestone.com.bluestone.`item-detail`.ItemDetail
import bluestone.com.bluestone.`recyclerview-adapters`.PhotoAdapter
import bluestone.com.bluestone.`touch-handlers`.RecyclerItemClickListenr
import bluestone.com.bluestone.`touch-handlers`.RecyclerViewScrollHandler
import bluestone.com.bluestone.data_model_loader.DisplayedPageStateLoader
import bluestone.com.bluestone.interfaces.FragmentCreationInterface
import bluestone.com.bluestone.server.NetworkService
import bluestone.com.bluestone.utilities.printLog
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

private const val PIXABAY_INITIAL_DATA = "param1"
class RecyclerViewFragment : Fragment(), FragmentCreationInterface {
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: PhotoAdapter
    private var serverCall = NetworkService("https://pixabay.com/api/")
    private var savedStatePresent = false
    private val disposables = CompositeDisposable()
    private lateinit var pageLoader: DisplayedPageStateLoader
    private var nextPage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedStatePresent = savedInstanceState != null
        context?.let { context ->
            val cacheManager = CacheManager.initialize(context, "masterDB")
            pageLoader = DisplayedPageStateLoader(cacheManager)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        printLog("RecyclerViewFragment.onCreateView")
        // If there is no saved state, initiate a server request to populate the recycler view adapter
        val view = inflater.inflate(R.layout.fragment_recycler, container, false)

        initializeRecycler(view, pageLoader) //Minimal initialization. No server call
        return view
    }

    override fun callbackSubject() = RecyclerViewFragment.fragmentSubject
    override fun fragment() = this
    override fun getFragmentId() = RecyclerViewFragment.fragmentID

    override fun onResume() {
        super.onResume()
        printLog("RecyclerViewFragment.onResume")
            var disposable = populateAdapterFromSavedState()!!
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(object : DisposableSingleObserver<DisplayedPageState>() {
                    override fun onSuccess(itemDetails: DisplayedPageState) {
                        mAdapter.update(itemDetails.items)
                        mRecyclerView.layoutManager?.scrollToPosition(itemDetails.firstVisible)
                        mAdapter.update()
                        printLog("initial count = ${mAdapter.itemCount}")
                    }

                    override fun onError(e: Throwable) {
                        printLog(e.localizedMessage)
                        // Do Nothing. Should never reach this logic
                    }

                })
            disposables.add(disposable)
    }

    override fun onPause(){
        super.onPause()
        saveAdapterState()
        disposables.clear()
    }

    override fun onDetach() {
        super.onDetach()
        disposables.clear()
        printLog("From RecyclerViewFragment.onDetach clearing disposables")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveAdapterState()
        printLog("onSaveInstanceState")
    }

    private fun saveAdapterState() {
        mAdapter.let { adapterData ->
            if (adapterData.itemCount == 0)
                return
            val currentState = DisplayedPageStateLoader(CacheManager)
            val first = (mRecyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
            val nextPage:Int = currentState.get()?.nextPage ?: 0
            currentState.put(DisplayedPageState(nextPage, first, adapterData.getAllItems()))

            printLog("saveAdapterState saved ${adapterData.itemCount}")
        }
    }

    private fun populateAdapterFromSavedState(): Single<DisplayedPageState>? {
        val loader = DisplayedPageStateLoader(CacheManager)
        val items = loader.get()
        return if (items != null)
            savedStateReader()
        else {
            fetchServerDataAsSingle()
        }

    }

    private fun savedStateReader(): Single<DisplayedPageState>? =
        Single.create<DisplayedPageState> { emitter ->
            val items = DisplayedPageStateLoader(CacheManager)
            items.get()?.let { itemListDescriptor ->
                emitter.onSuccess(itemListDescriptor)
            }
        }

    private fun fetchServerDataAsSingle(): Single<DisplayedPageState>? {
        return serverCall.getApi()?.run {
            nextPage = PhotoAdapter.maxAdapterSize/PhotoAdapter.maxPageSize
            fetchAll(PhotoAdapter.maxAdapterSize)
                .flatMap { data ->
                    val itemList = mutableListOf<ItemDetail>()
                    for (item in data.hits) {
                        itemList.add(ItemDetail(item.tags, item.largeImageURL, item.likes, item.user))
                    }
                    Single.just(DisplayedPageState(1, 0, itemList))
                }
        }
    }

    private fun displayDetailPhoto(url: String) {
        val photoView = PhotoFragment.newInstance(url)
        RecyclerViewFragment.fragmentSubject.onNext(FragmentCreationDescriptor(photoView, PhotoFragment.fragmentID))
    }

    private fun initializeRecycler(view: View, pageLoader: DisplayedPageStateLoader) {
        view.let { recyclerView ->
            mRecyclerView = recyclerView.findViewById(R.id.recycler_view)
            mAdapter = PhotoAdapter()
            val mLayoutManager = LinearLayoutManager(context)
            mRecyclerView.layoutManager = mLayoutManager
            mRecyclerView.itemAnimator = DefaultItemAnimator()
            mRecyclerView.adapter = mAdapter

            context?.let { context ->
                mRecyclerView.addOnScrollListener(
                    RecyclerViewScrollHandler(
                        context,
                        mAdapter,
                        serverCall,
                        nextPage
                    )
                )

                mRecyclerView.addOnItemTouchListener(
                    RecyclerItemClickListenr(
                        context,
                        mRecyclerView,
                        object : RecyclerItemClickListenr.OnItemClickListener {
                            override fun onItemClick(view: View, position: Int) {
                                displayDetailPhoto(mAdapter.getItemDetailByPosition(position).imageUrl)
                            }

                            override fun onItemLongClick(view: View?, position: Int) {
                                Toast.makeText(
                                    context,
                                    "got long click position $position item count ${mAdapter.itemCount}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                )
            }
        }
    }

    companion object {
        const val fragmentID = "5a5e870f-01db-4e44-977e-0212501177a5"
        private lateinit var fragmentSubject: PublishSubject<FragmentCreationDescriptor>
        @JvmStatic
        fun newInstance(subject: PublishSubject<FragmentCreationDescriptor>, initialData: String?) :FragmentCreationInterface{
            fragmentSubject = subject
            return RecyclerViewFragment().apply {
                arguments = Bundle().apply {
                    initialData?.apply {
                        putString(PIXABAY_INITIAL_DATA, this)
                    }
                }
            }
        }
    }
}