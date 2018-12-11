package bluestone.com.bluestone.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import bluestone.com.bluestone.`data-model`.FragmentCreationDescriptor
import bluestone.com.bluestone.interfaces.FragmentCreationInterface
import io.reactivex.subjects.PublishSubject

private const val CENSUS_INITIAL_DATA = "param1"
class CensusFragment : Fragment(), FragmentCreationInterface {
    override fun getFragmentId(): String {
        return CensusFragment.fragmentID
    }

    override fun fragment(): Fragment = this


    override fun callbackSubject(): PublishSubject<FragmentCreationDescriptor> {
        return fragmentSubject
    }

    companion object {
        val fragmentID = "1d4975b2-f2e8-40cd-87a5-431dafe9d445"
        private lateinit var fragmentSubject: PublishSubject<FragmentCreationDescriptor>

        @JvmStatic
        fun newInstance(subject: PublishSubject<FragmentCreationDescriptor>, initialData: String?) : FragmentCreationInterface {
            fragmentSubject = subject
            return CensusFragment().apply {
                arguments = Bundle().apply {
                    initialData.apply {
                        putString(CENSUS_INITIAL_DATA, this)
                    }
                }
            }
        }
    }
}