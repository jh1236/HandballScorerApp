package me.jaredhealy.handballscorer.ui.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.squareup.picasso.Picasso
import me.jaredhealy.handballscorer.R
import me.jaredhealy.handballscorer.databinding.FragmentHomeBinding
import me.jaredhealy.handballscorer.game.Competition
import me.jaredhealy.handballscorer.toMap
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException


data class DisplayTeam(
    val name: String,
    val leftPlayer: String,
    val rightPlayer: String,
    val score: Int,
    val cards: Int,
    val cardDuration: Int,
    val green: Boolean
)

private val lastActions = arrayListOf<String>()

@Suppress("UNCHECKED_CAST")
class HomeFragment : Fragment() {

    private var open = true
    private val GREEN_CARD_COLOR
        get() = ResourcesCompat.getColor(resources, R.color.green_card, null)
    private val YELLOW_CARD_COLOR
        get() = ResourcesCompat.getColor(resources, R.color.yellow_card, null)
    private val RED_CARD_COLOR
        get() = ResourcesCompat.getColor(resources, R.color.red_card, null)

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!


    private var teamOne = DisplayTeam("Team One", "Player One", "Player Two", 0, 0, 3, false)
    private var teamTwo = DisplayTeam("Team Two", "Player Three", "Player Four", 0, 0, 3, false)
    private var rounds = -1
    private var firstServing = true
    private var swapVisual = false
    private var serverName: String = ""
    private val actionTexts = arrayListOf<TextView>()


    private fun updateDisplays() {
        if (!open) return
        binding.teamOneScoreboard.text = teamOne.name
        binding.teamTwoScoreboard.text = teamTwo.name
        binding.scoreOneScoreboard.text = teamOne.score.toString()
        binding.scoreTwoScoreboard.text = teamTwo.score.toString()
        binding.totalRoundsScoreboard.text = rounds.toString()
        binding.cardsOneScoreboard.visibility = View.INVISIBLE
        binding.cardsTwoScoreboard.visibility = View.INVISIBLE
        if (teamOne.cards != 0) {
            binding.cardsOneScoreboard.visibility = View.VISIBLE
            if (teamOne.cards == -1) {
                binding.cardsOneScoreboard.setBackgroundColor(RED_CARD_COLOR)
                binding.cardsOneScoreboard.progress = 0
            } else {
                binding.cardsOneScoreboard.setBackgroundColor(YELLOW_CARD_COLOR)
                binding.cardsOneScoreboard.progress = teamOne.cardDuration - teamOne.cards
                binding.cardsOneScoreboard.max = teamOne.cardDuration
            }
        } else if (teamOne.green) {
            binding.cardsOneScoreboard.visibility = View.VISIBLE
            binding.cardsOneScoreboard.setBackgroundColor(GREEN_CARD_COLOR)
            binding.cardsOneScoreboard.progress = 0
        }
        if (teamTwo.cards != 0) {
            binding.cardsTwoScoreboard.visibility = View.VISIBLE
            if (teamTwo.cards == -1) {
                binding.cardsTwoScoreboard.setBackgroundColor(RED_CARD_COLOR)
                binding.cardsTwoScoreboard.progress = 0
            } else {
                binding.cardsTwoScoreboard.setBackgroundColor(YELLOW_CARD_COLOR)
                binding.cardsTwoScoreboard.progress = teamTwo.cardDuration - teamTwo.cards
                binding.cardsTwoScoreboard.max = teamTwo.cardDuration
            }
        } else if (teamTwo.green) {
            binding.cardsTwoScoreboard.visibility = View.VISIBLE
            binding.cardsTwoScoreboard.setBackgroundColor(GREEN_CARD_COLOR)
            binding.cardsTwoScoreboard.progress = 0
        }

        if (firstServing) {
            binding.serverOneScoreboard.visibility = View.VISIBLE
            binding.serverTwoScoreboard.visibility = View.INVISIBLE
            binding.serverOneScoreboard.text = serverName
        } else {
            binding.serverOneScoreboard.visibility = View.INVISIBLE
            binding.serverTwoScoreboard.visibility = View.VISIBLE
            binding.serverTwoScoreboard.text = serverName
        }
        if (Competition.onlineGame != null && !Competition.offlineMode) {
            binding.teamOneImgDisplay.visibility = View.VISIBLE
            binding.teamTwoImgDisplay.visibility = View.VISIBLE
            Picasso.get()
                .load("http://handball-tourney.zapto.org/api/teams/image?name=${Competition.currentGame.teamOne.niceName}")
                .into(binding.teamOneImgDisplay)
            Picasso.get()
                .load("http://handball-tourney.zapto.org/api/teams/image?name=${Competition.currentGame.teamTwo.niceName}")
                .into(binding.teamTwoImgDisplay)


        } else {
            binding.teamOneImgDisplay.visibility = View.INVISIBLE
            binding.teamTwoImgDisplay.visibility = View.INVISIBLE
        }
        for ((i, j) in lastActions zip actionTexts) {
            j.text = i
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        open = true
        actionTexts.clear()
        actionTexts.addAll(
            arrayOf(
                binding.lineOne,
                binding.lineTwo,
                binding.lineThree,
                binding.lineFour,
                binding.lineFive
            )
        )
        binding.swapButtonDisplay.setOnClickListener {
            val temp = teamOne
            teamOne = teamTwo
            teamTwo = temp
            swapVisual = !swapVisual
            firstServing = !firstServing
            updateDisplays()
        }
        val root: View = binding.root
        updateInternals()
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(object : Runnable {
            override fun run() {
                if (open) {
                    updateInternals()
                    mainHandler.postDelayed(this, 1000)
                }
            }
        })
        return root
    }

    fun createLastN(string: String) {
        lastActions.clear()
        lastActions.addAll(arrayOf("", "", "", "", "Game Started"))
        for (i in string.takeLast(10).chunked(2)) {
            val team = if (i[1].isUpperCase() xor swapVisual) teamOne else teamTwo
            val player = if (i[1].uppercase() == "L") team.leftPlayer else team.rightPlayer
            when (i[0].lowercase()) {
                "s" -> {
                    lastActions.add("$player scored")
                }

                "a" -> {
                    lastActions.add("$player scored an ace!")
                }

                "g" -> {
                    lastActions.add("$player was green carded")
                }

                "y" -> {
                    lastActions.add("$player was yellow carded")
                }

                "v" -> {
                    lastActions.add("$player was red carded!")
                }

                "t" -> {
                    lastActions.add("${team.name} called a timeout")
                }

                else -> {
                    if (i[0].isDigit()) {
                        lastActions.add("$player was yellow carded")
                    }
                }

            }
        }
        lastActions.reverse()
    }


    fun updateInternals(attempts: Int = 10) {
        val request =
            Request.Builder().url("http://handball-tourney.zapto.org/api/games/display").build()
        if (Competition.onlineGame == null || Competition.offlineMode) {
            teamOne = Competition.currentGame.teamOne.asDisplayTeam()
            teamTwo = Competition.currentGame.teamTwo.asDisplayTeam()
            if (swapVisual) {
                val temp = teamOne
                teamOne = teamTwo
                teamTwo = temp
            }
            serverName = Competition.currentGame.serving.server.name
            firstServing = Competition.currentGame.serving.firstTeam xor swapVisual
            rounds = Competition.currentGame.roundCount
            val mainHandler = Handler(Looper.getMainLooper())
            mainHandler.post {
                updateDisplays()
            }
            return
        }
        Competition.client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.i("api", e.stackTraceToString())
                teamOne = Competition.currentGame.teamOne.asDisplayTeam()
                teamTwo = Competition.currentGame.teamTwo.asDisplayTeam()
                if (swapVisual) {
                    val temp = teamOne
                    teamOne = teamTwo
                    teamTwo = temp
                }
                serverName = Competition.currentGame.serving.server.name
                firstServing = Competition.currentGame.serving.firstTeam xor swapVisual
                rounds = Competition.currentGame.roundCount
                val mainHandler = Handler(Looper.getMainLooper())
                mainHandler.post {
                    updateDisplays()
                }
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
                        val mapOne = map["teamOne"] as Map<String, *>
                        val mapTwo = map["teamTwo"] as Map<String, *>
                        val teamOneName = mapOne["name"] as String
                        val playerOneLeft = mapOne["playerOne"] as String
                        val playerOneRight = mapOne["playerTwo"] as String
                        val teamTwoName = mapTwo["name"] as String
                        val playerTwoLeft = mapTwo["playerOne"] as String
                        val playerTwoRight = mapTwo["playerTwo"] as String
                        val cardsOne = mapOne["cards"] as Int
                        val cardsTwo = mapTwo["cards"] as Int
                        val cardDurationOne = mapOne["cardDuration"] as Int
                        val cardDurationTwo = mapTwo["cardDuration"] as Int
                        val greenOne = mapOne["greenCard"] as Boolean
                        val greenTwo = mapTwo["greenCard"] as Boolean
                        val scoreOne = mapOne["score"] as Int
                        val scoreTwo = mapTwo["score"] as Int
                        teamOne = DisplayTeam(
                            teamOneName,
                            playerOneLeft,
                            playerOneRight,
                            scoreOne,
                            cardsOne,
                            cardDurationOne,
                            greenOne
                        )
                        teamTwo = DisplayTeam(
                            teamTwoName,
                            playerTwoLeft,
                            playerTwoRight,
                            scoreTwo,
                            cardsTwo,
                            cardDurationTwo,
                            greenTwo
                        )
                        if (swapVisual) {
                            val temp = teamOne
                            teamOne = teamTwo
                            teamTwo = temp
                        }
                        rounds = map["rounds"] as Int
                        firstServing = (map["firstTeamServing"] as Boolean) xor swapVisual
                        serverName = map["serverName"] as String
                        createLastN(map["game"] as String)
                        val mainHandler = Handler(Looper.getMainLooper())
                        mainHandler.post {
                            updateDisplays()
                        }
                    }
                } else {
                    if (attempts > 0) {
                        updateInternals(attempts - 1)
                    } else {
                        teamOne = Competition.currentGame.teamOne.asDisplayTeam()
                        teamTwo = Competition.currentGame.teamTwo.asDisplayTeam()
                        if (swapVisual) {
                            val temp = teamOne
                            teamOne = teamTwo
                            teamTwo = temp
                        }
                        serverName = Competition.currentGame.serving.server.name
                        firstServing = Competition.currentGame.serving.firstTeam xor swapVisual
                        rounds = Competition.currentGame.roundCount
                        val mainHandler = Handler(Looper.getMainLooper())
                        mainHandler.post {
                            updateDisplays()
                        }
                    }
                }
            }
        })
    }

    override fun onDestroyView() {
        open = false
        super.onDestroyView()
        _binding = null
    }


}