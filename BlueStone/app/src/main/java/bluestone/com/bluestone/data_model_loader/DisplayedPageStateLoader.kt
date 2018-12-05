package bluestone.com.bluestone.data_model_loader

import bluestone.com.bluestone.`cache-manager`.CacheManager
import bluestone.com.bluestone.`data-model`.DisplayedPageState

class DisplayedPageStateLoader (val cacheManager: CacheManager){
    private val pageStateKey     = "mainKey_9256ac13-6850-43eb-b011-2169087e5d28"
    private var data:DisplayedPageState? = null
    init {
        if (cacheManager.exists(pageStateKey)) {
            data = cacheManager.getDisplayedPageState(pageStateKey) as DisplayedPageState
        }
    }
    fun put(data:DisplayedPageState){
        cacheManager.putDisplayedPageState(pageStateKey,data)
    }
    fun get()  = data
}