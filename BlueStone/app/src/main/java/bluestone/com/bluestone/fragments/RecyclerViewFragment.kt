package bluestone.com.bluestone.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import bluestone.com.bluestone.R
import bluestone.com.bluestone.`data-model`.FragmentCreationDescriptor
import bluestone.com.bluestone.`data-model`.PhotoDataModel
import bluestone.com.bluestone.`item-detail`.ItemDetail
import bluestone.com.bluestone.`recyclerview-adapters`.PhotoAdapter
import bluestone.com.bluestone.`touch-handlers`.RecyclerItemClickListenr
import bluestone.com.bluestone.`touch-handlers`.RecyclerViewScrollHandler
import bluestone.com.bluestone.interfaces.FragmentCreationInterface
import bluestone.com.bluestone.server.NetworkService
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import bluestone.com.bluestone.utilities.printLog

class RecyclerViewFragment : Fragment(), FragmentCreationInterface {
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: PhotoAdapter
    private var serverCall = NetworkService("https://pixabay.com/api/")
    private var savedStatePresent = false
    private val disposables = CompositeDisposable()
    private val bundleKey = "recyclerviewfragment"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedStatePresent = savedInstanceState != null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Toast.makeText(context, "Saved state = $savedStatePresent", Toast.LENGTH_SHORT).show()
        // If there is no saved state, initiate a server request to populate the recycler view adapter
        val view = inflater.inflate(R.layout.fragment_recycler, container, false)
        initializeRecycler(view)
        context?.also { context ->
            fetchServerDataAsSingle(context) { adapterData -> mAdapter.update(adapterData) }
        }
        savedInstanceState?.run {
            if (containsKey(bundleKey)) {
                populateAdapterFromBundle(this)
            }
        }

        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.let { bundle ->
            mAdapter?.let { adapter ->
                bundle.putInt(bundleKey, adapter.itemCount)
                printLog("onSaveInstanceState bundleKey = $bundleKey itemCount = ${mAdapter.itemCount}")
                for (index in 0 until mAdapter.itemCount) {
                    bundle.putSerializable(bundleKey + "_" + index.toString(), mAdapter.getItemDetailByPosition(index))
                }
            }
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.run {
            if (containsKey(bundleKey)) {
                printLog("onViewStateRestored = $bundleKey itemCount = ${getInt(bundleKey)}")
                populateAdapterFromBundle(this)
            } else {
                printLog("Bundle is NULL")
            }
        }
    }

    override fun callbackSubject() = RecyclerViewFragment.fragmentSubject
    override fun fragment() = this

    override fun onDetach() {
        super.onDetach()

        disposables.clear()
    }

    private fun populateAdapterFromBundle(bundle: Bundle) {
        disposables.add(
            createBundleReader(bundle)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<ItemDetail>() {
                    val itemList = ArrayList<ItemDetail>()
                    override fun onComplete() {
                        printLog("Received onComplete item count = ${itemList.size}")
                        mAdapter.update(itemList)
                    }

                    override fun onError(e: Throwable) {
                        Log.e("populateAdapter", e.localizedMessage)
                    }

                    override fun onNext(detail: ItemDetail) {
                        itemList.add(detail)
                    }
                })
        )
    }

    private fun createBundleReader(bundle: Bundle): Observable<ItemDetail> =
        Observable.create<ItemDetail> { emitter ->
            val count =
                bundle.getInt(bundleKey) //the caller guarantees the existence of this key. Will not be called otherwise
            for (index in 0 until count) {
                val key = bundleKey + "_" + index
                emitter.onNext(bundle.getSerializable(key) as ItemDetail)
            }
            emitter.onComplete()
        }

    private fun fetchServerDataAsSingle(context: Context, callback: (ArrayList<ItemDetail>) -> Unit) {
        disposables.add(
            serverCall.getApi()!!.fetchAll(5)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(object : DisposableSingleObserver<PhotoDataModel>() {
                    override fun onSuccess(data: PhotoDataModel) {
                        val itemList = ArrayList<ItemDetail>()
                        for (item in data.hits) {
                            itemList.add(ItemDetail(item.tags, item.largeImageURL, item.likes, item.user))
                        }
                        callback(itemList)
                    }

                    override fun onError(e: Throwable) {
                        Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
                })
        )
    }

    private fun displayDetailPhoto(url: String) {
        val photoView = PhotoFragment.newInstance(url)
        RecyclerViewFragment.fragmentSubject.onNext(FragmentCreationDescriptor(photoView, 0, "nothing"))
    }

    private fun initializeRecycler(view: View) {
        view.let { view ->
            mRecyclerView = view.findViewById(R.id.recycler_view)
            mAdapter = PhotoAdapter()
            val mLayoutManager = LinearLayoutManager(context)
            mRecyclerView.layoutManager = mLayoutManager
            mRecyclerView.itemAnimator = DefaultItemAnimator()
            mRecyclerView.adapter = mAdapter
            context?.let { context ->
                mRecyclerView.addOnScrollListener(RecyclerViewScrollHandler(context, mAdapter, serverCall))
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
        private lateinit var fragmentSubject: PublishSubject<FragmentCreationDescriptor>
        @JvmStatic
        fun newInstance(subject: PublishSubject<FragmentCreationDescriptor>): FragmentCreationInterface {
            RecyclerViewFragment.fragmentSubject = subject
            return RecyclerViewFragment()
        }
    }
}