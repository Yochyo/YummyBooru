package de.yochyo.yummybooru.backup

import android.content.Context
import de.yochyo.yummybooru.api.entities.Subscription
import de.yochyo.yummybooru.database.db
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.*

object SubscriptionBackup : BackupableEntity<Subscription> {
    override fun toJSONObject(e: Subscription, context: Context): JSONObject {
        val json = JSONObject()
        json.put("name", e.name)
        json.put("type", e.type)
        json.put("lastID", e.lastID)
        json.put("lastCount", e.lastCount)
        json.put("isFavorite", e.isFavorite)
        json.put("creation", e.creation.time)
        json.put("serverID", e.serverID)
        return json
    }

    override fun toEntity(json: JSONObject, context: Context) {

        GlobalScope.launch {
            db.subDao.insert(
                    Subscription(json.getString("name"), json.getInt("type"), json.getInt("lastID"), json.getInt("lastCount"),
                            json.getBoolean("isFavorite"), Date(json.getLong("creation")), json.getInt("serverID")))
        }
    }
}