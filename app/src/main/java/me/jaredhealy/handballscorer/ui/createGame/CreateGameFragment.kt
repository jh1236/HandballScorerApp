package me.jaredhealy.handballscorer.ui.createGame

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import me.jaredhealy.handballscorer.R
import me.jaredhealy.handballscorer.databinding.FragmentCreateGameBinding
import me.jaredhealy.handballscorer.game.Competition
import me.jaredhealy.handballscorer.game.Game


@SuppressLint("SetTextI18n")
class CreateGameFragment : Fragment() {

    private var _binding: FragmentCreateGameBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var swappedService = false

    private fun showControlPanel() {
        findNavController().navigate(R.id.navigation_dashboard)
        findNavController()
    }

    private fun updateDisplay() {
        if (!swappedService) {
            binding.serveIndicatorOne.visibility = View.VISIBLE
            binding.serveIndicatorTwo.visibility = View.INVISIBLE
        } else {
            binding.serveIndicatorOne.visibility = View.INVISIBLE
            binding.serveIndicatorTwo.visibility = View.VISIBLE
        }
        if (Competition.onlineGame == null) {
            binding.offlineMode.isChecked = true
            binding.offlineMode.isEnabled = false
        }
        binding.leftPlayerOne.text = Competition.currentGame.teamOne.playerOne.name
        binding.rightPlayerOne.text = Competition.currentGame.teamOne.playerTwo.name
        binding.leftPlayerTwo.text = Competition.currentGame.teamTwo.playerOne.name
        binding.rightPlayerTwo.text = Competition.currentGame.teamTwo.playerTwo.name
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        _binding = FragmentCreateGameBinding.inflate(inflater, container, false)
        updateDisplay()

        val state = Competition.currentGame.state
        if (state == Game.State.PLAYING || state == Game.State.TIMEOUT) {
            showControlPanel()
            return binding.root
        } else if (state == Game.State.GAME_WON && (Competition.onlineGame == null || Competition.offlineMode)) {
            Competition.resetOfflineGame()
        }
        binding.nextTeamOne.text = Competition.currentGame.teamOne.teamName
        binding.nextTeamTwo.text = Competition.currentGame.teamTwo.teamName
        binding.swapService.setOnClickListener {
            swappedService = !swappedService
            updateDisplay()
        }
        binding.startGame.setOnClickListener {
            Competition.currentGame.startGame(
                swappedService,
                binding.rightPlayerOne.isChecked,
                binding.rightPlayerTwo.isChecked
            )
            showControlPanel()
        }
        binding.offlineMode.setOnCheckedChangeListener { _, value ->
            Competition.offlineMode = value
            updateDisplay()
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}