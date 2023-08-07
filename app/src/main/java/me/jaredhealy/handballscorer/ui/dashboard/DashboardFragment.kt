package me.jaredhealy.handballscorer.ui.dashboard

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.OnHierarchyChangeListener
import android.widget.ListView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.slider.RangeSlider
import me.jaredhealy.handballscorer.R
import me.jaredhealy.handballscorer.databinding.FragmentDashboardBinding
import me.jaredhealy.handballscorer.game.Competition
import me.jaredhealy.handballscorer.game.Game
import me.jaredhealy.handballscorer.game.Team
import kotlin.math.max


@SuppressLint("SetTextI18n")
class DashboardFragment : Fragment() {


    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    private val GREEN_CARD_COLOR
        get() = ResourcesCompat.getColor(resources, R.color.green_card, null)
    private val YELLOW_CARD_COLOR
        get() = ResourcesCompat.getColor(resources, R.color.yellow_card, null)
    private val RED_CARD_COLOR
        get() = ResourcesCompat.getColor(resources, R.color.red_card, null)
    private val DISABLED_TEXT_COLOR
        get() = ResourcesCompat.getColor(resources, R.color.purple_200, null)
    private val ENABLED_TEXT_COLOR
        get() = ResourcesCompat.getColor(resources, R.color.white, null)

    var visualSwap = false
    private val teamOne: Team
        get() = if (!visualSwap) game.teamOne else game.teamTwo
    private val teamTwo: Team
        get() = if (!visualSwap) game.teamTwo else game.teamOne

    val game: Game
        get() {
            return if (Competition.currentGame != null) {
                Competition.currentGame!!
            } else {
                exit()
                Game.dummyGame()
            }
        }

    private fun exit() {
        findNavController().navigate(R.id.navigation_create_game)
    }

    private fun whichPlayer(
        questionString: String, team: Team, vararg extraArgs: String, onExit: (Int) -> Unit
    ) {
        val list: ListView
        var wasPlayerOne = -1
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(questionString).setSingleChoiceItems(
            arrayOf(
                team.playerOne.name, team.playerTwo.name, *extraArgs
            ), -1
        ) { _, which ->
            wasPlayerOne = which
        }.setPositiveButton("Done") { dialog, id ->
            onExit(wasPlayerOne)
            dialog.dismiss()
        }.setNegativeButton("Cancel") { dialog, id ->
            dialog.dismiss()
        }

        val alert = builder.create()
        var count = 0
        list = alert.listView
        list.setOnHierarchyChangeListener(object : OnHierarchyChangeListener {
            override fun onChildViewAdded(parent: View, child: View) {
                val enabled =
                    !if (count == 0) team.playerOne.isCarded else if (count == 1) team.playerTwo.isCarded else false
                child.isEnabled = enabled
                count++
            }

            override fun onChildViewRemoved(view: View, view1: View) {}
        })
        alert.show()
    }

    private fun timeOut() {
        var timeRemaining = 600
        val textView = TextView(context)
        textView.text = "Select an option"
        textView.setPadding(20, 30, 20, 30)
        textView.textSize = 20f

        lateinit var thread: Thread
        val builder = AlertDialog.Builder(activity)
        builder.setCustomTitle(textView).setPositiveButton("Done") { dialog, _ ->
            thread.interrupt()
            Competition.currentGame!!.endTimeout()
            updateDisplays()
            dialog.cancel()
        }
        builder.setCancelable(false)
        val alert = builder.create()
        alert.setCanceledOnTouchOutside(false)
        alert.show()
        thread = object : Thread() {
            override fun run() {
                try {
                    while (!this.isInterrupted) {
                        sleep(100)
                        timeRemaining--
                        textView.text = "${max(0.0, timeRemaining / 10.0)} seconds left"
                    }
                } catch (_: InterruptedException) {
                }
            }
        }
        thread.start()
    }


    private fun updateDisplays() {

        binding.teamNameOne.text = teamOne.toString()
        binding.teamNameTwo.text = teamTwo.toString()
        binding.scoreTeamOne.text = teamOne.score.toString()
        binding.scoreTeamTwo.text = teamTwo.score.toString()
        binding.totalRounds.text = game.roundCount.toString()
        binding.cardsTeamOne.visibility = View.INVISIBLE
        binding.cardsTeamTwo.visibility = View.INVISIBLE
        binding.timeOutOneBtn.setTextColor(if (teamOne.allowedTimeout()) ENABLED_TEXT_COLOR else DISABLED_TEXT_COLOR)
        binding.timeOutTwoBtn.setTextColor(if (teamTwo.allowedTimeout()) ENABLED_TEXT_COLOR else DISABLED_TEXT_COLOR)
        binding.teamNameOne.text = teamOne.toString()
        binding.teamNameTwo.text = teamTwo.toString()
        binding.scoreTeamOne.text = teamOne.score.toString()
        binding.scoreTeamTwo.text = teamTwo.score.toString()
        binding.totalRounds.text = game.roundCount.toString()
        binding.cardsTeamOne.visibility = View.INVISIBLE
        binding.cardsTeamTwo.visibility = View.INVISIBLE
        binding.timeOutOneBtn.setTextColor(if (teamOne.allowedTimeout()) ENABLED_TEXT_COLOR else DISABLED_TEXT_COLOR)
        binding.timeOutTwoBtn.setTextColor(if (teamTwo.allowedTimeout()) ENABLED_TEXT_COLOR else DISABLED_TEXT_COLOR)
        binding.scoreOneBtn.text = "Score $teamOne"
        binding.scoreTwoBtn.text = "Score $teamTwo"
        binding.greenOneBtn.text = "Green card $teamOne"
        binding.greenTwoBtn.text = "Green card $teamTwo"
        binding.yellowOneBtn.text = "Yellow card $teamOne"
        binding.yellowTwoBtn.text = "Yellow card $teamTwo"
        binding.redOneBtn.text = "Red card $teamOne"
        binding.redTwoBtn.text = "Red card $teamTwo"
        binding.timeOutOneBtn.text = "Timeout $teamOne (${teamOne.timeOutsRemaining})"
        binding.timeOutTwoBtn.text = "Timeout $teamTwo (${teamTwo.timeOutsRemaining})"
        if (game.isOver()) {
            binding.totalRounds.text = "${game.getWinningTeam()!!} Wins!"
        }
        if (teamOne.cardCount != 0) {
            binding.cardsTeamOne.visibility = View.VISIBLE
            if (teamOne.cardCount == -1) {
                binding.cardsTeamOne.setBackgroundColor(RED_CARD_COLOR)
                binding.cardsTeamOne.progress = 0
            } else {
                binding.cardsTeamOne.setBackgroundColor(YELLOW_CARD_COLOR)
                binding.cardsTeamOne.progress = teamOne.cardDuration - teamOne.cardCount
                binding.cardsTeamOne.max = teamOne.cardDuration
            }
        } else if (teamOne.greenCarded) {
            binding.cardsTeamOne.visibility = View.VISIBLE
            binding.cardsTeamOne.setBackgroundColor(GREEN_CARD_COLOR)
            binding.cardsTeamOne.progress = 0
        }
        if (teamTwo.cardCount != 0) {
            binding.cardsTeamTwo.visibility = View.VISIBLE
            if (teamTwo.cardCount == -1) {
                binding.cardsTeamTwo.setBackgroundColor(RED_CARD_COLOR)
                binding.cardsTeamTwo.progress = 0
            } else {
                binding.cardsTeamTwo.setBackgroundColor(YELLOW_CARD_COLOR)
                binding.cardsTeamTwo.progress = teamTwo.cardDuration - teamTwo.cardCount
                binding.cardsTeamTwo.max = teamTwo.cardDuration
            }
        } else if (teamTwo.greenCarded) {
            binding.cardsTeamTwo.visibility = View.VISIBLE
            binding.cardsTeamTwo.setBackgroundColor(GREEN_CARD_COLOR)
            binding.cardsTeamTwo.progress = 0
        }

        if (game.serving.firstTeam == teamOne.firstTeam) {
            binding.serverOne.visibility = View.VISIBLE
            binding.serverTwo.visibility = View.INVISIBLE
            binding.serverOne.text = teamOne.server.name
        } else {
            binding.serverOne.visibility = View.INVISIBLE
            binding.serverTwo.visibility = View.VISIBLE
            binding.serverTwo.text = teamTwo.server.name
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        visualSwap = false
        if (Competition.currentGame?.state != Game.State.PLAYING) {
            Competition.getTeamsFromApi()
            exit()
            visualSwap = false
            return binding.root
        }
        updateDisplays()
        val root: View = binding.root
        binding.swapButton.setOnClickListener{
            visualSwap = !visualSwap
            updateDisplays()
        }
        binding.scoreOneBtn.setOnClickListener {
            if (game.state == Game.State.PLAYING) {
                var extraArgs = arrayOf<String>()
                if (teamOne.serving) {
                    extraArgs = arrayOf("Ace")
                }
                whichPlayer("Which Player", teamOne, *extraArgs) {
                    if (it == 2) {
                        teamOne.ace()
                    } else {
                        teamOne.addScore(it == 0)
                    }
                    updateDisplays()
                }
            } else {
                exit()
            }
        }
        binding.scoreTwoBtn.setOnClickListener {
            if (game.state == Game.State.PLAYING) {
                var extraArgs = arrayOf<String>()
                if (teamTwo.serving) {
                    extraArgs = arrayOf("Ace")
                }
                whichPlayer("Which Player", teamTwo, *extraArgs) {
                    if (it == 2) {
                        teamTwo.ace()
                    } else {
                        teamTwo.addScore(it == 0)
                    }
                    updateDisplays()
                }
            } else {
                exit()
            }

        }
        binding.greenOneBtn.setOnClickListener {
            if (game.state == Game.State.PLAYING) {
                whichPlayer("Which Player", teamOne) {
                    teamOne.greenCard(it == 0)
                    updateDisplays()
                }
            } else {
                exit()
            }

        }
        binding.greenTwoBtn.setOnClickListener {
            if (game.state == Game.State.PLAYING) {
                whichPlayer("Which Player", teamTwo) {
                    teamTwo.greenCard(it == 0)
                    updateDisplays()
                }
            } else {
                exit()
            }

        }
        binding.yellowOneBtn.setOnClickListener {
            if (game.state == Game.State.PLAYING) {
                whichPlayer("Which Player", teamOne) {
                    teamOne.yellowCard(it == 0)
                    updateDisplays()
                }
            } else {
                exit()
            }
        }
        binding.yellowTwoBtn.setOnClickListener {
            if (game.state == Game.State.PLAYING) {
                whichPlayer("Which Player", teamTwo) {
                    teamTwo.yellowCard(it == 0)
                    updateDisplays()
                }
            } else {
                exit()
            }
        }
        binding.yellowOneBtn.setOnLongClickListener {
            if (game.state == Game.State.PLAYING) {
                whichPlayer("Which Player", teamOne) {
                    val builder = AlertDialog.Builder(activity)
                    val input = RangeSlider(requireContext())
                    input.stepSize = 1.0f
                    input.setLabelFormatter { it.toInt().toString() }
                    input.setValues(3.0f)
                    input.valueFrom = 1.0f
                    input.valueTo = 10.0f
                    builder.setTitle("How many rounds").setPositiveButton("Done") { dialog, _ ->
                        teamOne.yellowCard(it == 0, input.values[0].toInt())
                        updateDisplays()
                        dialog.dismiss()
                    }.setNegativeButton("Cancel") { dialog, id ->
                        dialog.dismiss()
                    }.setView(input)
                    builder.create().show()
                }
            } else {
                exit()
            }
            true
        }
        binding.yellowTwoBtn.setOnLongClickListener {
            if (game.state == Game.State.PLAYING) {
                whichPlayer("Which Player", teamTwo) {
                    val builder = AlertDialog.Builder(activity)
                    val input = RangeSlider(requireContext())
                    input.stepSize = 1.0f
                    input.setLabelFormatter { it.toInt().toString() }
                    input.setValues(3.0f)
                    input.valueFrom = 1.0f
                    input.valueTo = 10.0f
                    builder.setTitle("How many rounds").setPositiveButton("Done") { dialog, id ->
                        teamTwo.yellowCard(it == 0, input.values[0].toInt())
                        updateDisplays()
                        dialog.dismiss()
                    }.setNegativeButton("Cancel") { dialog, id ->
                        dialog.dismiss()
                    }.setView(input)
                    builder.create().show()
                }
            } else {
                exit()
            }
            true
        }
        binding.redOneBtn.setOnClickListener {
            if (game.state == Game.State.PLAYING) {
                whichPlayer("Which Player", teamOne) {
                    teamOne.redCard(it == 0)
                    updateDisplays()
                }

            } else {
                exit()
            }
        }
        binding.redTwoBtn.setOnClickListener {
            if (game.state == Game.State.PLAYING) {
                whichPlayer("Which Player", teamTwo) {
                    teamTwo.redCard(it == 0)
                    updateDisplays()
                }
            } else {
                exit()
            }
        }
        binding.timeOutOneBtn.setOnClickListener {
            if (game.state == Game.State.PLAYING && teamOne.allowedTimeout()) {
                teamOne.callTimeout()
                timeOut()
            } else {
                exit()
            }
        }
        binding.timeOutTwoBtn.setOnClickListener {
            if (game.state == Game.State.PLAYING && teamTwo.allowedTimeout()) {
                teamTwo.callTimeout()
                timeOut()
            } else {
                exit()
            }
        }
        binding.timeOutOneBtn.setOnLongClickListener {
            teamOne.callTimeout()
            timeOut()
            true
        }
        binding.timeOutTwoBtn.setOnLongClickListener {
            teamTwo.callTimeout()
            timeOut()
            true
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}