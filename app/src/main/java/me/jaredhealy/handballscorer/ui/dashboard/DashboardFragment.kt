package me.jaredhealy.handballscorer.ui.dashboard

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.OnHierarchyChangeListener
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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
        var wasLeftPlayer = -1
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(questionString).setSingleChoiceItems(
            arrayOf(
                team.leftPlayer.name, team.rightPlayer.name, *extraArgs
            ), -1
        ) { _, which ->
            wasLeftPlayer = which
        }.setPositiveButton("Done") { dialog, id ->
            onExit(wasLeftPlayer)
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
                    !if (count == 0) team.leftPlayer.isCarded else if (count == 1) team.rightPlayer.isCarded else false
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
        binding.teamNameOne.text = game.teamOne.toString()
        binding.teamNameTwo.text = game.teamTwo.toString()
        binding.scoreTeamOne.text = game.teamOne.score.toString()
        binding.scoreTeamTwo.text = game.teamTwo.score.toString()
        binding.totalRounds.text = game.roundCount.toString()
        binding.cardsTeamOne.visibility = View.INVISIBLE
        binding.cardsTeamTwo.visibility = View.INVISIBLE
        binding.timeOutOneBtn.setTextColor(if (game.teamOne.allowedTimeout()) ENABLED_TEXT_COLOR else DISABLED_TEXT_COLOR)
        binding.timeOutTwoBtn.setTextColor(if (game.teamTwo.allowedTimeout()) ENABLED_TEXT_COLOR else DISABLED_TEXT_COLOR)
        binding.timeOutOneBtn.text = "Timeout ${game.teamOne} (${game.teamOne.timeOutsRemaining})"
        binding.timeOutTwoBtn.text = "Timeout ${game.teamTwo} (${game.teamTwo.timeOutsRemaining})"
        if (game.isOver()) {
            binding.totalRounds.text = "${game.getWinningTeam()!!} Wins!"
        }
        if (game.teamOne.cardCount != 0) {
            binding.cardsTeamOne.visibility = View.VISIBLE
            if (game.teamOne.cardCount == -1) {
                binding.cardsTeamOne.setBackgroundColor(RED_CARD_COLOR)
                binding.cardsTeamOne.progress = 0
            } else {
                binding.cardsTeamOne.setBackgroundColor(YELLOW_CARD_COLOR)
                binding.cardsTeamOne.progress = 3 - game.teamOne.cardCount
            }
        } else if (game.teamOne.greenCarded) {
            binding.cardsTeamOne.visibility = View.VISIBLE
            binding.cardsTeamOne.setBackgroundColor(GREEN_CARD_COLOR)
            binding.cardsTeamOne.progress = 0
        }
        if (game.teamTwo.cardCount != 0) {
            binding.cardsTeamTwo.visibility = View.VISIBLE
            if (game.teamTwo.cardCount == -1) {
                binding.cardsTeamTwo.setBackgroundColor(RED_CARD_COLOR)
                binding.cardsTeamTwo.progress = 0
            } else {
                binding.cardsTeamTwo.setBackgroundColor(YELLOW_CARD_COLOR)
                binding.cardsTeamTwo.progress = 3 - game.teamTwo.cardCount
            }
        } else if (game.teamTwo.greenCarded) {
            binding.cardsTeamTwo.visibility = View.VISIBLE
            binding.cardsTeamTwo.setBackgroundColor(GREEN_CARD_COLOR)
            binding.cardsTeamTwo.progress = 0
        }

        if (game.serving.teamOne) {
            binding.serverOne.visibility = View.VISIBLE
            binding.serverTwo.visibility = View.INVISIBLE
            binding.serverOne.text = game.teamOne.server.name
        } else {
            binding.serverOne.visibility = View.INVISIBLE
            binding.serverTwo.visibility = View.VISIBLE
            binding.serverTwo.text = game.teamTwo.server.name
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        if (Competition.currentGame == null) {
            Competition.getTeamsFromApi()
            exit()
            return binding.root
        }
        updateDisplays()
        val root: View = binding.root
        binding.scoreOneBtn.text = "Score ${game.teamOne}"
        binding.scoreTwoBtn.text = "Score ${game.teamTwo}"
        binding.greenOneBtn.text = "Green card ${game.teamOne}"
        binding.greenTwoBtn.text = "Green card ${game.teamTwo}"
        binding.yellowOneBtn.text = "Yellow card ${game.teamOne}"
        binding.yellowTwoBtn.text = "Yellow card ${game.teamTwo}"
        binding.redOneBtn.text = "Red card ${game.teamOne}"
        binding.redTwoBtn.text = "Red card ${game.teamTwo}"
        binding.timeOutOneBtn.text = "Timeout ${game.teamOne} (${game.teamOne.timeOutsRemaining})"
        binding.timeOutTwoBtn.text = "Timeout ${game.teamTwo} (${game.teamTwo.timeOutsRemaining})"
        binding.scoreOneBtn.setOnClickListener {
            if (game.state == Game.State.PLAYING) {
                var extraArgs = arrayOf<String>()
                if (game.teamOne.serving) {
                    extraArgs = arrayOf("Ace")
                }
                whichPlayer("Which Player", game.teamOne, *extraArgs) {
                    if (it == 2) {
                        game.teamOne.ace()
                    } else {
                        game.teamOne.addScore(it == 0)
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
                if (game.teamTwo.serving) {
                    extraArgs = arrayOf("Ace")
                }
                whichPlayer("Which Player", game.teamTwo, *extraArgs) {
                    if (it == 2) {
                        game.teamTwo.ace()
                    } else {
                        game.teamTwo.addScore(it == 0)
                    }
                    updateDisplays()
                }
            } else {
                exit()
            }

        }
        binding.greenOneBtn.setOnClickListener {
            if (game.state == Game.State.PLAYING) {
                whichPlayer("Which Player", game.teamOne) {
                    game.teamOne.greenCard(it == 0)
                    updateDisplays()
                }
            } else {
                exit()
            }

        }
        binding.greenTwoBtn.setOnClickListener {
            if (game.state == Game.State.PLAYING) {
                whichPlayer("Which Player", game.teamTwo) {
                    game.teamTwo.greenCard(it == 0)
                    updateDisplays()
                }
            } else {
                exit()
            }

        }
        binding.yellowOneBtn.setOnClickListener {
            if (game.state == Game.State.PLAYING) {
                whichPlayer("Which Player", game.teamOne) {
                    game.teamOne.yellowCard(it == 0)
                    updateDisplays()
                }
            } else {
                exit()
            }
        }
        binding.yellowTwoBtn.setOnClickListener {
            if (game.state == Game.State.PLAYING) {
                whichPlayer("Which Player", game.teamTwo) {
                    game.teamTwo.yellowCard(it == 0)
                    updateDisplays()
                }
            } else {
                exit()
            }
        }
        binding.yellowOneBtn.setOnLongClickListener {
            if (game.state == Game.State.PLAYING) {
                whichPlayer("Which Player", game.teamOne) {
                    val builder = AlertDialog.Builder(activity)
                    val input = EditText(context);
                    input.inputType = InputType.TYPE_CLASS_NUMBER
                    input.setText("3")
                    builder.setTitle("How many rounds").setPositiveButton("Done") { dialog, id ->
                        game.teamOne.yellowCard(it == 0, input.text.toString().toInt())
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
                whichPlayer("Which Player", game.teamTwo) {
                    val builder = AlertDialog.Builder(activity)
                    val input = EditText(context);
                    input.inputType = InputType.TYPE_CLASS_NUMBER
                    input.setText("3")
                    builder.setTitle("How many rounds").setPositiveButton("Done") { dialog, id ->
                        game.teamTwo.yellowCard(it == 0, input.text.toString().toInt())
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
                whichPlayer("Which Player", game.teamOne) {
                    game.teamOne.redCard(it == 0)
                    updateDisplays()
                }

            } else {
                exit()
            }
        }
        binding.redTwoBtn.setOnClickListener {
            if (game.state == Game.State.PLAYING) {
                whichPlayer("Which Player", game.teamTwo) {
                    game.teamTwo.redCard(it == 0)
                    updateDisplays()
                }
            } else {
                exit()
            }
        }
        binding.timeOutOneBtn.setOnClickListener {
            if (game.state == Game.State.PLAYING && game.teamOne.allowedTimeout()) {
                game.teamOne.callTimeout()
                timeOut()
            } else {
                exit()
            }
        }
        binding.timeOutTwoBtn.setOnClickListener {
            if (game.state == Game.State.PLAYING && game.teamTwo.allowedTimeout()) {
                game.teamTwo.callTimeout()
                timeOut()
            } else {
                exit()
            }
        }
        binding.timeOutOneBtn.setOnLongClickListener {
            game.teamOne.callTimeout()
            timeOut()
            true
        }
        binding.timeOutTwoBtn.setOnLongClickListener {
            game.teamTwo.callTimeout()
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