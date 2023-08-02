package me.jaredhealy.handballscorer.game

import android.os.Handler
import android.os.Looper
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

object ServerInteractions {
    private val client = OkHttpClient()

    private fun post(method: String, content: Map<Any, Any?>, recall: Boolean = true) {
        if (!Game.recordStats) return
        val body = RequestBody.create(
            MediaType.parse("application/json"), JSONObject(content).toString()
        )
        val request = Request.Builder()
            .method("POST", body)
            .url("http://handball-tourney.zapto.org/api/$method")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
            }
        })
        if (!recall) return
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.postDelayed({
            Competition.getTeamsFromApi()
        }, 500)
    }

    fun score(team: Team, isLeft: Boolean?, ace: Boolean) {
        post(
            "games/update/score",
            mapOf(
                "firstTeam" to team.firstTeam,
                "leftPlayer" to isLeft,
                "ace" to ace
            )
        )
    }

    fun start(swapServe: Boolean, time: Long) {
        post(
            "games/update/start",
            mapOf(
                "startTime" to time,
                "swap" to swapServe
            )
        )
    }


    fun greenCard(team: Team, left: Boolean) {
        post(
            "games/update/card",
            mapOf(
                "color" to "green",
                "firstTeam" to team.firstTeam,
                "leftPlayer" to left,
            )
        )
    }

    fun yellowCard(team: Team, left: Boolean, time: Int) {
        post(
            "games/update/card",
            mapOf(
                "color" to "yellow",
                "firstTeam" to team.firstTeam,
                "leftPlayer" to left,
                "time" to time
            )
        )
    }

    fun redCard(team: Team, left: Boolean) {
        post(
            "games/update/card",
            mapOf(
                "color" to "red",
                "firstTeam" to team.firstTeam,
                "leftPlayer" to left,
            )
        )
    }

    fun timeout(team: Team) {
        post(
            "games/update/timeout",
            mapOf(
                "firstTeam" to team.firstTeam
            ), false
        )
    }
}