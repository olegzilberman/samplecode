package bluestone.com.bluestone.`cache-manager`

class CacheManager {
    fun put(key:String, data:String){

    }
    fun put(data:String) : String {
        return java.util.UUID.randomUUID().toString()
    }

    fun get(key:String) : String?{
        return null
    }
}