package bluestone.com.bluestone.data_model_loader

import bluestone.com.bluestone.`cache-manager`.CacheManager
import bluestone.com.bluestone.`data-model`.DisplayedPageState
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.JSON
import kotlinx.serialization.parse
import kotlinx.serialization.stringify

class DisplayedPageStateLoader(val cacheManager: CacheManager) {
    private val pageStateKey = "page_state_9256ac13-6850-43eb-b011-2169087e5d28"
    @UseExperimental(ImplicitReflectionSerializer::class)
    fun put(data: DisplayedPageState) {
        val item = JSON.unquoted.stringify(data)
        cacheManager.putString(pageStateKey, item)
    }

    @UseExperimental(ImplicitReflectionSerializer::class)
    fun get(): DisplayedPageState? {
        cacheManager.getString(pageStateKey)?.run {
            return JSON.unquoted.parse(this)
        }
        return null
    }
}