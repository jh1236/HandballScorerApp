package me.jaredhealy.handballscorer.game

import android.os.Handler
import android.os.Looper
import android.util.Log
import me.jaredhealy.handballscorer.toMap
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception


object Competition {
    val client = OkHttpClient()
    var offlineMode = false
    var offlineGame = Game(
        Team("Team One", "Player One", "Player Two"),
        Team("Team Two", "Player One", "Player Two")
    )


    val currentGame: Game
        get() {
            return if (offlineMode || onlineGame == null) {
                offlineGame
            } else {
                onlineGame!!
            }
        }

    var onlineGame: Game? = null

    val teams = arrayListOf<Team>()
    private val callback = arrayListOf<() -> Unit>()
    fun getTeamsFromApi() {
        val request = Request.Builder().url("http://handball-tourney.zapto.org/api/teams").build()

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

    fun addCallback(func: () -> Unit) {
        callback.add(func)
    }

    fun getCurrentGame(triesRemaining: Int = 20) {
        val request =
            Request.Builder().url("http://handball-tourney.zapto.org/api/games/current").build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.i("api", e.stackTraceToString())
            }

            override fun onResponse(call: Call, response: Response) {
                val string: String? = try {
                    response.body()?.string()
                } catch (e: Exception) {
                    null
                }
                if (string != null) {
                    if (string != "none") {
                        val map = JSONObject(string).toMap()
                        Log.i("api", map.toString())
                        onlineGame = Game.loadFromMap(map)
                        for (i in callback) {
                            val mainHandler = Handler(Looper.getMainLooper())
                            mainHandler.post(i)
                        }
                    }
                } else if (triesRemaining > 0) {
                    getCurrentGame(triesRemaining - 1)
                }
            }
        })
    }


    @Suppress("UNCHECKED_CAST")
    private fun processTeams(string: String) {
        Log.i("api", string)
        val map = JSONObject(string).toMap()
        teams.clear()
        for ((teamName, v) in map) {
            teams.add(
                Team.fromMap(teamName, v as Map<String, *>)
            )
        }
    }

    fun resetOfflineGame() {
        this.offlineGame = Game(
            Team("Team One", "Player One", "Player Two"),
            Team("Team Two", "Player One", "Player Two")
        )
    }

}