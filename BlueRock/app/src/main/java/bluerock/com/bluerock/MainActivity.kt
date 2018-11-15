package bluerock.com.bluerock

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import bluerock.com.bluerock.`data-model`.PhotoDataModel
import bluerock.com.bluerock.`item-detail`.ItemDetail
import bluerock.com.bluerock.`recyclerview-adapters`.PhotoAdapter
import bluerock.com.bluerock.`touch-handlers`.RecyclerItemClickListenr
import bluerock.com.bluerock.`touch-handlers`.RecyclerViewScrollHandler
import bluerock.com.bluerock.fragments.PhotoFragment
import bluerock.com.bluerock.server.NetworkService

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), PhotoFragment.OnFragmentInteractionListener {
    override fun onFragmentInteraction(uri: Uri) {
        Toast.makeText(applicationContext, uri.toString(), Toast.LENGTH_SHORT).show()
    }

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: PhotoAdapter
    private var disposable = Disposables.disposed()
    private var serverCall = NetworkService("https://pixabay.com/api/")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        serverConnection()
    }

    private fun serverConnection() {
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
                    Toast.makeText(applicationContext, e.localizedMessage, Toast.LENGTH_SHORT).show()
                }

                override fun onComplete() {
                }

            })
    }

    private fun displayDetailPhoto(url:String){
        val photoView = PhotoFragment.newInstance(url)
        val fragmentTransaction = supportFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.relative_layout_content_main, photoView)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    private fun initializeRecycler() {
        mRecyclerView = findViewById(R.id.recycler_view)
        mAdapter = PhotoAdapter()
        val mLayoutManager = LinearLayoutManager(applicationContext)
        mRecyclerView.layoutManager = mLayoutManager
        mRecyclerView.itemAnimator = DefaultItemAnimator()
        mRecyclerView.adapter = mAdapter
        mRecyclerView.addOnScrollListener(RecyclerViewScrollHandler(applicationContext, mAdapter, serverCall))
        mRecyclerView.addOnItemTouchListener(
            RecyclerItemClickListenr(
                this,
                mRecyclerView,
                object : RecyclerItemClickListenr.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        displayDetailPhoto(mAdapter.getItemDetailByPosition(position).imageUrl)
                    }

                    override fun onItemLongClick(view: View?, position: Int) {
                        Toast.makeText(
                            applicationContext,
                            "got long click position $position item count ${mAdapter.itemCount}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        )
    }

    override fun onStop() {
        super.onStop()
        disposable.dispose()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
