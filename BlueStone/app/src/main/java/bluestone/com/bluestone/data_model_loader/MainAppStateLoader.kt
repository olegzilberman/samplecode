package bluestone.com.bluestone.data_model_loader

import bluestone.com.bluestone.`cache-manager`.CacheManager
import bluestone.com.bluestone.`data-model`.MainAppSaveState
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.JSON
import kotlinx.serialization.stringify

class MainAppStateLoader(val cacheManager: CacheManager) {
    private val mainStateKey     = "main_app_state_19b7572b-839e-4118-bbc1-4a3e6198d032"
    fun get() : MainAppSaveState?{
        cacheManager.getItem(mainStateKey)?.run {
            return MainAppSaveState(this[0], this[1])
        }
        return null
    }

    @UseExperimental(ImplicitReflectionSerializer::class)
    fun put(data:MainAppSaveState){
        val item_1 = JSON.unquoted.stringify(data.fragmentID)
        val item_2 =  JSON.unquoted.stringify(data.payload)
        val data = arrayOf(item_1, item_2)
        cacheManager.putItem(mainStateKey, data)
    }
}