package bluestone.com.bluestone.`data-model`

import kotlinx.serialization.SerialId

data class MainAppSaveState(@SerialId(1) val fragmentID:String,
                            @SerialId(2)val payload:String)
