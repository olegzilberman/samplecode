package bluestone.com.bluestone.interfaces

import android.support.v4.app.Fragment
import bluestone.com.bluestone.`data-model`.FragmentCreationDescriptor
import io.reactivex.subjects.PublishSubject

interface FragmentCreationInterface {
    fun callbackSubject(): PublishSubject<FragmentCreationDescriptor>
    fun fragment(): Fragment
    fun getFragmentId(): String
}