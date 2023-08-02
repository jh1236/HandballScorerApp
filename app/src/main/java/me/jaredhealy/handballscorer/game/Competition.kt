package me.jaredhealy.handballscorer.game

import android.util.Log
import me.jaredhealy.handballscorer.toMap
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException


object Competition {
    private val client = OkHttpClient()
    fun getTeamsFromApi() {
        teams.clear()
        val request = Request.Builder()
            .url("http://handball-tourney.zapto.org/api/teams")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.i("api", e.stackTraceToString())
            }

            override fun onResponse(call: Call, response: Response) {
                val string = response.body()?.string()
                if (string != null) {
                    processTeams(string)
                    getCurrentGame()
                }
            }
        })
    }

    fun getCurrentGame(tries_remaining: Int = 20) {
        currentGame = null
        val request = Request.Builder()
            .url("http://handball-tourney.zapto.org/api/games/current")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.i("api", e.stackTraceToString())
            }

            override fun onResponse(call: Call, response: Response) {
                val string = response.body()?.string()
                if (string != null) {
                    if (string != "none") {
                        val map = JSONObject(string).toMap()
                        Log.i("api", map.toString())
                        currentGame = Game.loadFromString(
                            map["teamOne"] as String,
                            map["teamTwo"] as String,
                            map["game"] as String
                        )
                        currentGame!!.state =
                            if (map["started"] as Boolean) Game.State.PLAYING else Game.State.BEFORE_GAME
                    } else {
                        if (tries_remaining > 0) {
                            getCurrentGame(tries_remaining - 1)
                        }
                    }
                }
            }
        })
    }


    var currentGame: Game? = null

    val teams = arrayListOf<Team>()

    @Suppress("UNCHECKED_CAST")
    private fun processTeams(string: String) {
        Log.i("api", string)
        val map = JSONObject(string).toMap()
        for ((teamName, v) in map) {
            teams.add(
                Team.fromMap(teamName, v as Map<String, *>)
            )
        }
    }

}