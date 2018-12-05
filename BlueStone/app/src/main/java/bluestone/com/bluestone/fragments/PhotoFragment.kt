package bluestone.com.bluestone.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import bluestone.com.bluestone.R
import bluestone.com.bluestone.`cache-manager`.CacheManager
import bluestone.com.bluestone.utilities.*
import com.squareup.picasso.Picasso


// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val PHOTO_URL = "param1"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [PhotoFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [PhotoFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class PhotoFragment : Fragment() {
    private var photoUrl: String? = null
    private var listener: OnFragmentInteractionListener? = null
    private val photofragmentkey = "photofragmentkey"
    private var zoomScale = 1.0f
    private lateinit var mainView:View
    private var shortAnimationDuration:Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            photoUrl = bundle.getString(PHOTO_URL)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_photo, container, false)
        shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
        initListeners(mainView)
        Picasso.get()
            .load(photoUrl)
            .noPlaceholder()
            .fit()
            .centerInside()
            .into(mainView.findViewById<ImageView>(R.id.fragment_image))
        return mainView
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        photoUrl?.let { url ->
            CacheManager.deleteKey(photofragmentkey)
            CacheManager.putString(
                photofragmentkey,
                url
            )
            printLog("saveAdapterState saved $url")
        }
    }

    private fun initListeners(targetView:View){
        targetView.setOnClickListener {thisView ->
            var reset = false
            if (zoomScale != 1.0f) {
                reset = true
                mainView.findViewById<ImageView>(R.id.fragment_image)?.visibility = View.VISIBLE
                mainView.findViewById<ImageView>(R.id.expanded_image)?.visibility = View.GONE
                zoomScale = 1.0F
            }
            else {
                zoomScale *= 3f
                mainView.findViewById<ImageView>(R.id.fragment_image)?.visibility = View.GONE
                mainView.findViewById<ImageView>(R.id.expanded_image)?.visibility = View.VISIBLE
            }
            scaleImage(mainView.findViewById(R.id.fragment_image), zoomScale, mainView.findViewById<ImageView>(R.id.expanded_image), reset)
        }
    }
    fun onButtonPressed(uri: Uri) {
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        val fragmentID = "c1b32631-4b46-4c7b-9e32-edcaf47b3e49"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param photo_url Parameter 1.
         * @return A new instance of fragment PhotoFragment.
         */
        @JvmStatic
        fun newInstance(photo_url: String) =
            PhotoFragment().apply {
                arguments = Bundle().apply {
                    putString(PHOTO_URL, photo_url)
                }
            }
    }
}
