package bluerock.com.bluerock.interfaces

import android.support.v4.app.Fragment
import bluerock.com.bluerock.`data-model`.FragmentCreationDescriptor
import io.reactivex.subjects.PublishSubject

interface FragmentCreationInterface {
    fun callbackSubject() : PublishSubject<FragmentCreationDescriptor>
    fun fragment() : Fragment
}