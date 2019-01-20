package de.yochyo.yBooru.api

import de.yochyo.yBooru.R
import java.util.*

class Tag(val name: String, val type: String = UNKNOWN, var isFavorite: Boolean = false, val creation: Date? = null) {
    companion object {
        val GENERAL = "g"
        val CHARACTER = "c"
        val COPYPRIGHT = "cr"
        val ARTIST = "a"
        val META = "m"
        val UNKNOWN = "u"
    }

    val color: Int
        get() {
            when (type) {
                GENERAL -> return R.color.blue
                CHARACTER -> return R.color.green
                COPYPRIGHT -> return R.color.violet
                ARTIST -> return R.color.dark_red
                META -> return R.color.orange
                else -> return R.color.white
            }
        }

    override fun toString(): String {
        return "$name (type = $type) [isFavorite = $isFavorite]"
    }
}

class Subscription(val name: String, val startID: Int, var currentID: Int)