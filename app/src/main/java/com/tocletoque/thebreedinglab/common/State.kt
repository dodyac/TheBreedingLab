package com.tocletoque.thebreedinglab.common

import android.content.Context
import android.content.SharedPreferences
import com.acxdev.commonFunction.utils.Preference
import com.acxdev.commonFunction.utils.ext.toClass
import com.tocletoque.thebreedinglab.model.Player
import androidx.core.content.edit
import com.acxdev.commonFunction.utils.ext.view.json
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import com.tocletoque.thebreedinglab.model.Dog
import java.time.LocalDate

class State(private val context: Context) {

    fun getPrefs(): SharedPreferences {
        return Preference(context).get("game_state")
    }

    fun getPlayer(): Player {
       return getPrefs().getString("player", null)?.toClass(Player::class.java) ?: Player()
    }

    fun savePlayer(player: Player) {
        getPrefs().edit { putString("player", player.json) }
    }

    fun getPuppies(): List<Dog> {
        val json = getPrefs().getString("puppies", null) ?: return emptyList()

        val gson = GsonBuilder()
            .registerTypeAdapter(LocalDate::class.java, JsonDeserializer { json, _, _ ->
                LocalDate.parse(json.asString) // for "2025-08-09"
            })
            .create()

        return try {
            gson.fromJson(json, Array<Dog>::class.java).toList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun savePuppies(puppies: List<Dog>) {
        val gson = GsonBuilder()
            .registerTypeAdapter(LocalDate::class.java, JsonSerializer<LocalDate> { src, _, _ ->
                JsonPrimitive(src.toString()) // will print "2025-08-09"
            })
            .create()

        val json = gson.toJson(puppies)
        getPrefs().edit { putString("puppies", json) }
    }

    fun restart() {
        getPrefs().edit { clear() }
    }
}