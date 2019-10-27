package de.yochyo.yummybooru.utils.network

import de.yochyo.yummybooru.api.api.Api
import de.yochyo.yummybooru.api.downloads.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object DownloadUtils {
    fun getUrlResponseCode(url: String): Int {
        val u = URL(Api.instance!!.urlGetPosts(1, arrayOf("*"), 1))
        val conn = u.openConnection() as HttpURLConnection
        conn.addRequestProperty("User-Agent", "Mozilla/5.00");conn.requestMethod = "GET"
        return conn.responseCode
    }

    suspend fun getUrlLines(urlToRead: String): Collection<String> {
        return withContext(Dispatchers.IO) {
            val result = ArrayList<String>()
            try {
                val stream = getUrlInputStream(urlToRead)
                if (stream != null) {
                    BufferedReader(InputStreamReader(stream, "UTF-8")).use { bufferedReader ->
                        var inputLine: String? = bufferedReader.readLine()
                        while (inputLine != null) {
                            result.add(inputLine)
                            inputLine = bufferedReader.readLine()
                        }
                        stream.close()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            result
        }
    }

    suspend fun getJson(urlToRead: String): JSONArray? {
        var array: JSONArray? = null
        try {
            array = JSONArray(getUrlLines(urlToRead).joinToString(""))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return array
    }

    suspend fun downloadResource(url: String, type: Int = Resource.getTypeFromURL(url)): Resource? {
        return withContext(Dispatchers.IO) {
            try {
                val stream = DownloadUtils.getUrlInputStream(url)
                if (stream != null) {
                    val res = Resource(stream.readBytes(), type)
                    stream.close()
                    return@withContext res
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            null
        }
    }

    private suspend fun getUrlInputStream(url: String): InputStream? {
        return withContext(Dispatchers.IO) {
            return@withContext try {
                val conn = URL(url).openConnection() as HttpURLConnection
                conn.addRequestProperty("User-Agent", "Mozilla/5.00")
                conn.requestMethod = "GET"
                conn.inputStream
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}