package bluestone.com.bluestone.`cache-manager`

import android.content.Context
import bluestone.com.bluestone.`data-model`.DisplayedPageState
import bluestone.com.bluestone.`item-detail`.ItemDetailListDescriptor
import bluestone.com.bluestone.utilities.printLog
import com.snappydb.DB
import com.snappydb.SnappyDB
import com.snappydb.SnappydbException
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.JSON
import kotlinx.serialization.parse
import kotlinx.serialization.stringify


object CacheManager {
    fun initialize(context: Context, dbname: String) : CacheManager {
        if (initialized)
            return this
        initialized = true
        database = SnappyDB.Builder(context)
            .directory(context.getExternalFilesDir(dbname).absolutePath) //optional
            .name("viewState")//optional
            .build()
        //TODO:OZ replace with a version check and the appropriate migration code.
        database.put(rootKey, dbVersion)
        return  this
    }
    private var initialized=false
    private lateinit var database: DB
    private lateinit var dbnmae:String
    private val rootKey = "__CacheManager__"
    private val dbVersion = "1.0.0"

    @UseExperimental(ImplicitReflectionSerializer::class)
    fun putItemList(key: String, items: ItemDetailListDescriptor) {
        val data = JSON.unquoted.stringify(items)
        database.put(key, data)
    }

    @UseExperimental(ImplicitReflectionSerializer::class)
    fun getItemList(key: String): ItemDetailListDescriptor? {
        if (database.exists(key)) {
            return JSON.unquoted.parse(database.get(key))
        }
        return null
    }

    @UseExperimental(ImplicitReflectionSerializer::class)
    fun putDisplayedPageState(key:String, item: DisplayedPageState) {
        val data = JSON.unquoted.stringify(item)
        database.put(key, data)
    }

    @UseExperimental(ImplicitReflectionSerializer::class)
    fun getDisplayedPageState(key:String) : DisplayedPageState?{
        if (database.exists(key))
            return JSON.unquoted.parse(database.get(key))
        return null
    }

    @UseExperimental(ImplicitReflectionSerializer::class)
    fun putString(key:String, value:String) {
        database.put(key, value)
    }

    @UseExperimental(ImplicitReflectionSerializer::class)
    fun getString(key:String) : String? {
        if (!database.exists(key))
            return null
        return database.get(key)
    }

    @UseExperimental(ImplicitReflectionSerializer::class)
    fun getItem(key:String) : Array<String>? {
        if (database.exists(key)){
            return JSON.unquoted.parse(database.get(key))
        }
        return null
    }

    @UseExperimental(ImplicitReflectionSerializer::class)
    fun putItem(key:String, item:Array<String>) {
        database.put(key, item)
    }

    fun deleteKey(key: String) = try {
        database.del(key)
    } catch (e: SnappydbException) {
        printLog(e.localizedMessage)
    }

    fun deleteDB() {
        if(!database.isOpen)
            return
        database.destroy()
    }

    fun exists(key: String): Boolean{
      if (!database.isOpen)
          return false
        return database.exists(key)
    }
}