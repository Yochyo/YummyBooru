package de.yochyo.ybooru.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import kotlinx.coroutines.delay
import java.io.ByteArrayOutputStream
import java.io.File

abstract class Cache(context: Context) {
    private val memory = MemoryCache()
    private val disk = DiskCache(context.cacheDir)

    companion object {
        private var _instance: Cache? = null
        fun getInstance(context: Context): Cache {
            if (_instance == null) _instance = object : Cache(context) {}
            return _instance!!
        }
    }

    fun getCachedBitmap(id: String): Bitmap? {
        memory.getBitmap(id)?.apply { return this }
                ?: disk.getBitmap(id)?.apply { return this }
        return null
    }

    fun cacheBitmap(id: String, bitmap: Bitmap, onStorage: Boolean = true) {
        memory.addBitmap(id, bitmap)
        if (onStorage)
            disk.addBitmap(id, ByteArrayOutputStream().apply { bitmap.compress(Bitmap.CompressFormat.PNG, 100, this) }.toByteArray())
    }

    fun removeBitmap(id: String) {
        memory.remove(id)
        disk.removeBitmap(id)
    }


    suspend fun awaitPicture(id: String): Bitmap {
        var bitmap = getCachedBitmap(id)
        var i = 0
        while (bitmap == null) {
            delay(50)
            bitmap = getCachedBitmap(id)
            i++
            if (i > 100) throw Exception("awaited picture [$id] does not appear in cache")
        }
        return bitmap
    }

    private fun clearMemory() {
        memory.evictAll()
    }

    private fun clearDisk() {
        disk.clearCache()
    }
}


private class MemoryCache : LruCache<String, Bitmap>((Runtime.getRuntime().maxMemory() / 1024).toInt() / 8) {
    override fun sizeOf(key: String, bitmap: Bitmap) = bitmap.byteCount / 1024

    fun getBitmap(id: String): Bitmap? {
        get(id)?.apply { return this }
        return null
    }

    fun addBitmap(id: String, bitmap: Bitmap) {
        put(id, bitmap)
    }
}

private class DiskCache(val cache: File) {
    private val bitmaps = ArrayList<String>(200)

    init {
        clearCache()
    }

    fun addBitmap(id: String, byteArray: ByteArray) {
        bitmaps += id
        File("${cache.absolutePath}/$id").apply { createNewFile();writeBytes(byteArray) }
    }

    fun clearCache() {
        bitmaps.clear()
        cache.listFiles().forEach {
            if (it.isFile)
                it.delete()//TODO on exit delete all
        }
    }

    fun removeBitmap(id: String) {
        with(File("${cache.absolutePath}/$id")) {
            if (exists())
                File("${cache.absolutePath}/$id").delete()
        }
    }

    fun getBitmap(id: String): Bitmap? {
        return if (bitmaps.contains(id)) {
            BitmapFactory.decodeStream(File("${cache.absolutePath}/$id").inputStream())
        } else
            null
    }
}

val Context.cache: Cache
    get() = Cache.getInstance(this)