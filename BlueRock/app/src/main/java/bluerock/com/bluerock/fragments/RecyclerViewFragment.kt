package bluerock.com.bluerock.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import bluerock.com.bluerock.R
import bluerock.com.bluerock.`data-model`.FragmentCreationDescriptor
import bluerock.com.bluerock.`data-model`.PhotoDataModel
import bluerock.com.bluerock.`item-detail`.ItemDetail
import bluerock.com.bluerock.`recyclerview-adapters`.PhotoAdapter
import bluerock.com.bluerock.`touch-handlers`.RecyclerItemClickListenr
import bluerock.com.bluerock.`touch-handlers`.RecyclerViewScrollHandler
import bluerock.com.bluerock.interfaces.FragmentCreationInterface
import bluerock.com.bluerock.server.NetworkService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.SingleSubject

class RecyclerViewFragment : Fragment(), FragmentCreationInterface {
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: PhotoAdapter
    private lateinit var inflater: LayoutInflater
    private var disposable = Disposables.disposed()
    private var serverCall = NetworkService("https://pixabay.com/api/")
    private var savedStatePresent = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedStatePresent = savedInstanceState != null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        Toast.makeText(context, "Saved state = $savedStatePresent", Toast.LENGTH_SHORT).show()
        if (!savedStatePresent)
            context?.let {
                serverConnection(it)
            }
        return inflater.inflate(R.layout.fragment_recycler, container, false)
    }

    override fun callbackSubject() = RecyclerViewFragment.fragmentSubject
    override fun fragment() = this

    override fun onResume() {
        super.onResume()
    }

    override fun onDetach() {
        super.onDetach()
        disposable.dispose()
    }

    private fun serverConnection(context: Context) {
        disposable = serverCall.getApi()!!.fetchAll()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribeWith(object : DisposableObserver<PhotoDataModel>() {
                override fun onNext(data: PhotoDataModel) {
                    val itemList = ArrayList<ItemDetail>()
                    for (item in data.hits) {
                        itemList.add(ItemDetail(item.tags, item.largeImageURL, item.likes, item.user))
                    }
                    initializeRecycler()
                    mAdapter.update(itemList)
                }

                override fun onError(e: Throwable) {
                    Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT).show()
                }

                override fun onComplete() {
                }

            })
    }

    private fun displayDetailPhoto(url: String) {
        val photoView = PhotoFragment.newInstance(url)
        RecyclerViewFragment.fragmentSubject.onNext(FragmentCreationDescriptor(photoView, 0, "nothing"))
    }

    private fun initializeRecycler() {
        view?.let { view ->
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