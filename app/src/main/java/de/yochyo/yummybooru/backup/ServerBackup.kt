package de.yochyo.yummybooru.backup

import android.content.Context
import de.yochyo.yummybooru.api.entities.Server
import de.yochyo.yummybooru.database.db
import de.yochyo.yummybooru.utils.general.Logger
import de.yochyo.json.JSONObject

object ServerBackup : BackupableEntity<Server> {
    override fun toJSONObject(e: Server, context: Context): JSONObject {
        val json = JSONObject()
        json.put("name", e.name)
        json.put("api", e.apiName)
        json.put("url", e.url)
        json.put("userName", e.username)
        json.put("password", e.password)
        json.put("id", e.id)
        return json
    }

    override suspend fun restoreEntity(json: JSONObject, context: Context) {
        try {
            val server = Server(json.getString("name"), json.getString("url"), json.getString("api"),
                     json.getString("userName"), json.getString("password"), json.getInt("id"))
            context.db.serverDao.insertWithID(server, server.id)
        } catch (e: Exception) {
            Logger.log(e)
            e.printStackTrace()
        }
    }
}