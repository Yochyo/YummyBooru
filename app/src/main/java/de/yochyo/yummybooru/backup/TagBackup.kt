package de.yochyo.yummybooru.backup

import android.content.Context
import de.yochyo.booruapi.api.TagType
import de.yochyo.json.JSONObject
import de.yochyo.yummybooru.api.entities.Tag
import de.yochyo.yummybooru.utils.general.sendFirebase
import java.util.*

object TagBackup : BackupableEntity<Tag> {
    override fun toJSONObject(e: Tag, context: Context): JSONObject {
        val json = JSONObject()
        json.put("name", e.name)
        json.put("type", e.type.value)
        json.put("isFavorite", e.isFavorite)
        json.put("creation", e.creation.time)
        json.put("serverID", e.serverId)
        json.put("lastID", e.lastId ?: -1)
        json.put("lastCount", e.lastCount ?: -1)
        return json
    }

    override suspend fun restoreEntity(json: JSONObject, context: Context) {
        TODO()
    }

    fun restoreEntity2(json: JSONObject, context: Context): Tag? {
        return try {
            val lastId = json.getInt("lastID").let { if (it == -1) null else it }
            val lastCount = json.getInt("lastCount").let { if (it == -1) null else it }
            Tag(
                json.getString("name"), TagType.valueOf(json.getInt("type")), json.getBoolean("isFavorite"),
                0, lastCount, lastId, Date(json.getLong("creation")), json.getInt("serverID")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            e.sendFirebase()
            null
        }
    }

}