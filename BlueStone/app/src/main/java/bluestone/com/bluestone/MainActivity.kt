package bluestone.com.bluestone

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import bluestone.com.bluestone.`cache-manager`.CacheManager
import bluestone.com.bluestone.`data-model`.FragmentCreationDescriptor
import bluestone.com.bluestone.`data_model_loader`.MainAppStateLoader
import bluestone.com.bluestone.fragments.CensusFragment
import bluestone.com.bluestone.fragments.PhotoFragment
import bluestone.com.bluestone.fragments.RecyclerViewFragment
import bluestone.com.bluestone.interfaces.FragmentCreationInterface
import io.reactivex.disposables.Disposables
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.*


private var fragmentCallback = PublishSubject.create<FragmentCreationDescriptor>()

enum class ViewIdentifier {
    PIXABAY_VIEW,
    CENSUS_VIEW,
    MAP_VIEW
}

class MainActivity : AppCompatActivity(), PhotoFragment.OnFragmentInteractionListener {

    override fun onFragmentInteraction(uri: Uri) {
        Toast.makeText(applicationContext, uri.toString(), Toast.LENGTH_SHORT).show()
    }

    private var currentFragmentSelection = ViewIdentifier.PIXABAY_VIEW
    private var disposable = Disposables.disposed()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CacheManager.initialize(applicationContext, "masterDB")
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        val loader = MainAppStateLoader(CacheManager)
        if (savedInstanceState == null) {
            loader.get()?.let { savedState ->
                mapFragmentIDToFragment(savedState.fragmentID, savedState.payload)
                displayNextFragment(
                    mapFragmentIDToFragment(savedState.fragmentID, savedState.payload) as Fragment,
                    savedState.fragmentID
                )
            }?: mapFragmentIDToFragment(RecyclerViewFragment.fragmentID, null)?.let {
                displayNextFragment(it as Fragment, it.getFragmentId())
            }

        } else {
            mapFragmentIDToFragment(RecyclerViewFragment.fragmentID, null)?.let {
                displayNextFragment(it as Fragment, it.getFragmentId())
            }
        }
    }


    private fun notImplemented(): Boolean {
        Toast.makeText(applicationContext, "Feature not implemented", Toast.LENGTH_SHORT).show()
        return false
    }

    private fun getMenuIdFromFragmentId(fragmentID: String): Int? = when {
        RecyclerViewFragment.fragmentID.equals(fragmentID) -> R.id.action_pictures_data
        CensusFragment.fragmentID.equals(fragmentID) -> R.id.action_census_data
        else -> null
    }


    private fun switchViews(viewSelector: ViewIdentifier) =
        when (viewSelector) {
            ViewIdentifier.PIXABAY_VIEW -> notImplemented()
            ViewIdentifier.CENSUS_VIEW -> notImplemented()
            ViewIdentifier.MAP_VIEW -> notImplemented()
        }

    private fun cleanDB() {
        CacheManager.deleteDB()
    }

    private fun displayNextFragment(fragment: Fragment, fragmentID: String) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment, fragmentID)
            .addToBackStack(null)
            .commit()
    }

    private fun mapFragmentIDToFragment(fragmentID: String, payload: String?): FragmentCreationInterface? =
        when {
            fragmentID.equals(RecyclerViewFragment.fragmentID) -> {
                currentFragmentSelection = ViewIdentifier.PIXABAY_VIEW
                RecyclerViewFragment.newInstance(fragmentCallback, payload)
            }
            fragmentID == CensusFragment.fragmentID -> {
                currentFragmentSelection = ViewIdentifier.CENSUS_VIEW
                CensusFragment.newInstance(fragmentCallback, payload)
            }
            else -> null
        }

    override fun onStart() {
        super.onStart()
        disposable = fragmentCallback.subscribeWith(object : DisposableObserver<FragmentCreationDescriptor>() {
            override fun onComplete() {}
            override fun onNext(fragmentDescriptor: FragmentCreationDescriptor) {
                displayNextFragment(fragmentDescriptor.fragment, fragmentDescriptor.fragmentTag)
            }

            override fun onError(e: Throwable) {
                Log.e("MainActivity", e.localizedMessage)
            }
        })
    }

    override fun onStop() {
        super.onStop()
        disposable.dispose()
    }

    override fun onPrepareOptionsMenu(menu: Menu) : Boolean {
        supportFragmentManager.fragments.forEach { fragment ->
            if (fragment is FragmentCreationInterface) {
                val target = fragment as FragmentCreationInterface
                if (fragment.isVisible) {
                    target?.run {
                        getMenuIdFromFragmentId(getFragmentId())?.let {
                            menu.findItem(it)?.isEnabled = false
                        }
                        return@forEach
                    }
                }
            }
        }
        return true
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
            R.id.action_restart -> {
                val intent = baseContext.packageManager
                    .getLaunchIntentForPackage(baseContext.packageName)
                intent?.run {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                    cleanDB()
                }
                Toast.makeText(applicationContext, "database deleted", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_census_data -> switchViews(ViewIdentifier.CENSUS_VIEW)
            R.id.action_map_data -> switchViews(ViewIdentifier.MAP_VIEW)
            R.id.action_pictures_data -> switchViews(ViewIdentifier.PIXABAY_VIEW)
            else -> super.onOptionsItemSelected(item)
        }
    }
}
