package me.jaredhealy.handballscorer.ui.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.jaredhealy.handballscorer.R
import me.jaredhealy.handballscorer.databinding.FragmentHomeBinding
import me.jaredhealy.handballscorer.game.Competition
import me.jaredhealy.handballscorer.game.Team
import kotlin.math.max


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
    private fun updateDisplays() {
        val game = Competition.currentGame ?: return
        binding.teamOneScoreboard.text = game.teamOne.toString()
        binding.teamTwoScoreboard.text = game.teamTwo.toString()
        binding.scoreOneScoreboard.text = game.teamOne.score.toString()
        binding.scoreTwoScoreboard.text = game.teamTwo.score.toString()
        binding.totalRoundsScoreboard.text = game.roundCount.toString()
        binding.cardsOneScoreboard.visibility = View.INVISIBLE
        binding.cardsTwoScoreboard.visibility = View.INVISIBLE
        if (game.teamOne.cardCount != 0) {
            binding.cardsOneScoreboard.visibility = View.VISIBLE
            if (game.teamOne.cardCount == -1) {
                binding.cardsOneScoreboard.setBackgroundColor(RED_CARD_COLOR)
                binding.cardsOneScoreboard.progress = 0
            } else {
                binding.cardsOneScoreboard.setBackgroundColor(YELLOW_CARD_COLOR)
                binding.cardsOneScoreboard.progress = 3 - game.teamOne.cardCount
            }
        } else if (game.teamOne.greenCarded) {
            binding.cardsOneScoreboard.visibility = View.VISIBLE
            binding.cardsOneScoreboard.setBackgroundColor(GREEN_CARD_COLOR)
            binding.cardsOneScoreboard.progress = 0
        }
        if (game.teamTwo.cardCount != 0) {
            binding.cardsTwoScoreboard.visibility = View.VISIBLE
            if (game.teamTwo.cardCount == -1) {
                binding.cardsTwoScoreboard.setBackgroundColor(RED_CARD_COLOR)
                binding.cardsTwoScoreboard.progress = 0
            } else {
                binding.cardsTwoScoreboard.setBackgroundColor(YELLOW_CARD_COLOR)
                binding.cardsTwoScoreboard.progress = 3 - game.teamTwo.cardCount
            }
        } else if (game.teamTwo.greenCarded) {
            binding.cardsTwoScoreboard.visibility = View.VISIBLE
            binding.cardsTwoScoreboard.setBackgroundColor(GREEN_CARD_COLOR)
            binding.cardsTwoScoreboard.progress = 0
        }

        if (game.serving.teamOne) {
            binding.serverOneScoreboard.visibility = View.VISIBLE
            binding.serverTwoScoreboard.visibility = View.INVISIBLE
            binding.serverOneScoreboard.text = game.teamOne.server.name
        } else {
            binding.serverOneScoreboard.visibility = View.INVISIBLE
            binding.serverTwoScoreboard.visibility = View.VISIBLE
            binding.serverTwoScoreboard.text = game.teamTwo.server.name
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        open = true
        val root: View = binding.root
        updateDisplays()
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(object : Runnable {
            override fun run() {
                if (open) {
                    updateDisplays()
                    Competition.getTeamsFromApi()
                    mainHandler.postDelayed(this, 1000)
                }
            }
        })
        return root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        open = false
        _binding = null
    }


}