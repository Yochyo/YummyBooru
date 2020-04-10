package de.yochyo.yummybooru.backup

import android.content.Context
import de.yochyo.json.JSONArray
import de.yochyo.json.JSONObject
import de.yochyo.yummybooru.BuildConfig
import de.yochyo.yummybooru.database.db
import de.yochyo.yummybooru.utils.general.Logger
import de.yochyo.yummybooru.utils.general.configPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

object BackupUtils {
    //TODO ObjectSerializer benutzen, Eine Preferences Klasse schreiben
    val directory = "$configPath/backup"

    val dir = File(directory)

    init {
        dir.mkdirs()
    }

    fun createBackup(context: Context) {
        GlobalScope.launch(Dispatchers.IO) {
            val f = createBackupFile()
            val json = JSONObject()
            val tagArray = JSONArray()
            val serverArray = JSONArray()
            for (tag in context.db.tagDao.selectAll())
                tagArray.put(TagBackup.toJSONObject(tag, context))
            for (server in context.db.serverDao.selectAll())
                serverArray.put(ServerBackup.toJSONObject(server, context))
            json.put("tags", tagArray)
            json.put("servers", serverArray)
            json.put("preferences", PreferencesBackup.toJSONObject("", context))
            json.put("version", BuildConfig.VERSION_CODE)


            f.writeBytes(json.toString().toByteArray())
        }
    }

    suspend fun restoreBackup(byteArray: ByteArray, context: Context) {
        try {
            context.db.deleteEverything()
            var obj = updateRestoreObject(JSONObject(String(byteArray)))
            val tags = obj["tags"] as JSONArray
            val servers = obj["servers"] as JSONArray
            PreferencesBackup.restoreEntity(obj["preferences"] as JSONObject, context)
            for (i in 0 until servers.length())
                ServerBackup.restoreEntity(servers[i] as JSONObject, context)
            for (i in 0 until tags.length())
                TagBackup.restoreEntity(tags[i] as JSONObject, context)
        } catch (e: Exception) {
            Logger.log(e)
            e.printStackTrace()
        }
    }

    fun updateRestoreObject(json: JSONObject): JSONObject{
        val version = json["version"] as Int
        if(version < 9){
            json.getJSONObject("preferences").put("downloadWebm", true)
            val subs = json.getJSONArray("subs")
            val tags = json.getJSONArray("tags")
            for(tag  in tags){
                val tag = tag as JSONObject
                val name = tag.getString("name")
                val sub = subs.find { (it as JSONObject).getString("name") == name } as JSONObject?
                tag.put("lastID", sub?.getString("lastID") ?: -1)
                tag.put("lastCount", sub?.getString("lastCount") ?: -1)
            }
            for(sub in subs){
                val sub = sub as JSONObject
                val name = sub.getString("name")
                val tag = tags.find { (it as JSONObject).getString("name") == name } as JSONObject?
                if(tag == null){
                    tags.put(sub)
                }
            }
        }
        return json
    }

    private fun createBackupFile(): File {
        val file = File("$directory/backup" + System.currentTimeMillis() + ".yBooru")
        file.createNewFile()
        return file
    }
}