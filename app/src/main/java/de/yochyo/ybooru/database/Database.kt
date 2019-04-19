package de.yochyo.ybooru.database

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.arch.persistence.room.migration.Migration
import android.content.Context
import android.content.SharedPreferences
import de.yochyo.ybooru.database.converter.DateConverter
import de.yochyo.ybooru.database.entities.*
import de.yochyo.ybooru.database.liveData.LiveTree
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@android.arch.persistence.room.Database(entities = [Tag::class, Subscription::class, Server::class], version = 2)
@TypeConverters(DateConverter::class)
abstract class Database : RoomDatabase() {
    private lateinit var prefs: SharedPreferences

    companion object {
        var instance: Database? = null
        fun initDatabase(context: Context): Database {
            if (instance == null) instance = Room.databaseBuilder(context.applicationContext,
                    Database::class.java, "db")
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            for (s in DefaultServerExeq.all)
                                db.execSQL(s)
                        }
                    })
                    .addMigrations(*Migrations.all).build()
            instance!!.prefs = context.getSharedPreferences("default", Context.MODE_PRIVATE)
            instance!!.initialize()

            return instance!!
        }

    }

    val servers = LiveTree<Server>()
    val tags = LiveTree<Tag>()
    val subs = LiveTree<Subscription>()

    fun initialize() {
        var se: List<Server>? = null
        runBlocking {
            val job = GlobalScope.launch {
                se = serverDao.getAllServers()
            }
            job.join()
        }
        servers += se!!
        Server.currentServer.select()
    }

    fun getAllTags(serverID: Int): List<Tag> {
        var t: List<Tag>? = null
        runBlocking {
            val job = GlobalScope.launch {
                t = tagDao.getAllTags().filter { it.serverID == serverID }
            }
            job.join()
        }
        return t!!
    }

    fun getTag(name: String) = tags.find { it.name == name }
    fun addTag(tag: Tag) {
        if (getTag(tag.name) == null) {
            tags += tag
            GlobalScope.launch { tagDao.insert(tag) }
        }
    }

    fun deleteTag(name: String) {
        val tag = tags.find { it.name == name }
        if (tag != null) {
            tags.remove(tag)
            GlobalScope.launch { tagDao.delete(tag) }
        }
    }

    fun changeTag(changedTag: Tag): Boolean {
        val tag = tags.find { it.name == changedTag.name }
        return if (tag != null) {
            tags.remove(tag)
            tags.add(changedTag)
            GlobalScope.launch { tagDao.update(changedTag) }
            true
        } else false
    }

    fun getAllSubscriptions(serverID: Int): List<Subscription> {
        var s: List<Subscription>? = null
        runBlocking {
            val job = GlobalScope.launch {
                s = subDao.getAllSubscriptions().filter { it.serverID == serverID }
            }
            job.join()
        }
        return s!!
    }

    fun getSubscription(name: String) = subs.find { it.name == name }
    fun addSubscription(sub: Subscription) {
        if (getSubscription(sub.name) == null) {
            subs += sub
            GlobalScope.launch { subDao.insert(sub) }
        }
    }

    fun deleteSubscription(name: String) {
        val sub = subs.find { it.name == name }
        if (sub != null) {
            subs.remove(sub)
            GlobalScope.launch { subDao.delete(sub) }
        }
    }

    fun changeSubscription(changedSub: Subscription): Boolean {
        val sub = subs.find { it.name == changedSub.name }
        return if (sub != null) {
            subs.remove(sub)
            subs.add(changedSub)
            GlobalScope.launch { subDao.update(changedSub) }
            true
        } else false
    }

    fun getServer(id: Int) = servers.find { it.id == id }
    fun addServer(server: Server) {
        if (getServer(server.id) == null) {
            servers += server
            GlobalScope.launch { serverDao.insert(server) }
        }
    }

    fun deleteServer(id: Int) {
        val s = servers.find { id == it.id }
        if (s != null) {
            if (currentServerID == id) s.unselect()
            servers.remove(s)
            GlobalScope.launch { serverDao.delete(s) }
        }
    }

    fun changeServer(server: Server) {
        val s = servers.find { it.id == server.id }
        if (s != null) {
            servers -= s
            servers += server
            GlobalScope.launch { serverDao.update(server) }
        }
    }

    private var _nextServerID: Int? = null
    var nextServerID: Int
        get() {
            if (_nextServerID == null)
                _nextServerID = prefs.getInt("nextServerID", DefaultServerExeq.all.size)
            return _nextServerID!!
        }
        set(v) {
            _nextServerID = v
            with(prefs.edit()) {
                putInt("nextServerID", v)
                apply()
            }
        }


    private var _limit: Int? = null
    var limit: Int
        get() {
            if (_limit == null) _limit = prefs.getInt("limit", 30)
            return _limit!!
        }
        set(value) {
            _limit = value
            with(prefs.edit()) {
                putInt("limit", value)
                apply()
            }
        }

    private var _currentServerID: Int? = null
    var currentServerID: Int
        get() {
            if (_currentServerID == null) _currentServerID = prefs.getInt("currentServer", 0)
            return _currentServerID!!
        }
        set(v) {
            _currentServerID = v
            with(prefs.edit()) {
                putInt("currentServer", v)
                apply()
            }
        }


    private var _sortTags: String? = null
    var sortTags: String
        get() {
            if (_sortTags == null) _sortTags = prefs.getString("sortTags", "00")
            return _sortTags!!
        }
        set(value) {
            _sortTags = value
            with(prefs.edit()) {
                putString("sortTags", value)
                apply()
            }
        }
    private var _sortSubs: String? = null
    var sortSubs: String
        get() {
            if (_sortSubs == null) _sortSubs = prefs.getString("sortSubs", "00")
            return _sortSubs!!
        }
        set(value) {
            _sortSubs = value
            with(prefs.edit()) {
                putString("sortSubs", value)
                apply()
            }
        }
    var sortTagsByFavorite: Boolean
        get() = sortTags.first() == '1'
        set(v) {
            sortTags = "${sortTags.first()}$v"
        }
    var sortTagsByAlphabet: Boolean
        get() = sortTags.last() == '1'
        set(v) {
            sortTags = "$v${sortTags.last()}"
        }

    var sortSubsByFavorite: Boolean
        get() = sortSubs.first() == '1'
        set(v) {
            sortSubs = "${sortSubs.first()}$v"
        }
    var sortSubsByAlphabet: Boolean
        get() = sortSubs.last() == '1'
        set(v) {
            sortSubs = "$v${sortSubs.last()}"
        }

    abstract val subDao: SubscriptionDao
    abstract val tagDao: TagDao
    abstract val serverDao: ServerDao
}

val db: Database
    get() = Database.instance!!


private object Migrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
        }
    }


    val all = arrayOf(MIGRATION_1_2)
}

object DefaultServerExeq {
    val all = ArrayList<String>()

    init {
        all += "INSERT INTO servers (name,api,url,userName,password,enableR18Filter,id) VALUES ('Danbooru', 'danbooru', 'https://danbooru.donmai.us/', '', '', 1, 0);"
        all += "INSERT INTO servers (name,api,url,userName,password,enableR18Filter,id) VALUES ('Konachan', 'moebooru', 'https://konachan.com/', '', '', 1, 1);"
        all += "INSERT INTO servers (name,api,url,userName,password,enableR18Filter,id) VALUES ('Yande.re', 'moebooru', 'https://yande.re/', '', '', 1, 2);"
    }
}