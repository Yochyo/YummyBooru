package de.yochyo.yummybooru.api.downloads

import android.widget.ImageView
import com.bumptech.glide.Glide
import de.yochyo.yummybooru.utils.Logger
import de.yochyo.yummybooru.utils.mimeType
import de.yochyo.yummybooru.utils.toBitmap
import java.io.*

class Resource(val resource: ByteArray, val type: Int) : Serializable {
    companion object {
        const val IMAGE = 0
        const val ANIMATED = 1
        const val VIDEO = 2

        fun getTypeFromURL(url: String): Int {
            val mimeType = url.mimeType
            println("Mime: $mimeType")
            if (mimeType != null) {
                return when(mimeType){
                    "gif" -> ANIMATED
                    "mp4", "webm" -> VIDEO
                    else -> IMAGE
                }
            }
            return IMAGE
        }

        fun fromFile(file: File): Resource? {
            try {
                val fileStream = file.inputStream()
                val inputStream = ObjectInputStream(fileStream)
                val obj = inputStream.readObject() as Resource
                fileStream.close()
                inputStream.close()
                return obj
            } catch (e: Exception) {
                e.printStackTrace()
                Logger.log(e, file.name)
            }
            return null
        }
    }

    fun loadInto(imageView: ImageView) {

        when (type) {
            IMAGE -> imageView.setImageBitmap(resource.toBitmap())
            ANIMATED -> Glide.with(imageView).load(resource).into(imageView)
        }
    }

    fun loadInto(file: File) {
        val outputStream = ByteArrayOutputStream()
        try {
            val objStream = ObjectOutputStream(outputStream)
            objStream.writeObject(this)
            file.writeBytes(outputStream.toByteArray())
            objStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Logger.log(e, file.name)
        } finally {
            outputStream.close()
        }
    }
}
