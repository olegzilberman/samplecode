package bluestone.com.bluestone

import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import bluestone.com.bluestone.`data-model`.FragmentCreationDescriptor
import bluestone.com.bluestone.fragments.PhotoFragment
import bluestone.com.bluestone.fragments.RecyclerViewFragment
import bluestone.com.bluestone.interfaces.FragmentCreationInterface
import io.reactivex.disposables.Disposables
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.*
private lateinit var fragmentCallback: PublishSubject<FragmentCreationDescriptor>

class MainActivity : AppCompatActivity(), PhotoFragment.OnFragmentInteractionListener {

    override fun onFragmentInteraction(uri: Uri) {
        Toast.makeText(applicationContext, uri.toString(), Toast.LENGTH_SHORT).show()
    }

    private var disposable = Disposables.disposed()
    private lateinit var firstFragment: FragmentCreationInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        initFragmentCallback()
    }

    private fun initFragmentCallback(){
        fragmentCallback = PublishSubject.create()
        disposable = fragmentCallback.subscribeWith(object : DisposableObserver<FragmentCreationDescriptor>() {
            override fun onComplete() {}
            override fun onNext(fragmentDescriptor: FragmentCreationDescriptor) {
                displayNextFragment(fragmentDescriptor.fragment)
            }

            override fun onError(e: Throwable) {
                Log.e("MainActivity", e.localizedMessage)
            }
        })
        firstFragment = RecyclerViewFragment.newInstance(fragmentCallback)
        displayNextFragment(firstFragment.fragment())
    }
    private fun displayNextFragment(fragment: Fragment) {
                                                                        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
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
