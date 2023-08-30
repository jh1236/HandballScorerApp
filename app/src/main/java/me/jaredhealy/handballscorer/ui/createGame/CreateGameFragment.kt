package me.jaredhealy.handballscorer.ui.createGame

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.squareup.picasso.Picasso
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

    private fun toOfflineMode() {
        binding.playersOne.text = "Players:"
        binding.playersTwo.text = "Players:"
        binding.leftPlayerOne.visibility = View.INVISIBLE
        binding.rightPlayerOne.visibility = View.INVISIBLE
        binding.teamOneCreateImg.visibility = View.INVISIBLE
        binding.teamTwoCreateImg.visibility = View.INVISIBLE
        binding.leftPlayerTwo.visibility = View.INVISIBLE
        binding.rightPlayerTwo.visibility = View.INVISIBLE
        binding.leftPlayerNameOne.visibility = View.VISIBLE
        binding.rightPlayerNameOne.visibility = View.VISIBLE
        binding.leftPlayerNameTwo.visibility = View.VISIBLE
        binding.rightPlayerNameTwo.visibility = View.VISIBLE
        binding.leftPlayerOne.isChecked = true
        binding.rightPlayerOne.isChecked = false
        binding.leftPlayerTwo.isChecked = true
        binding.rightPlayerTwo.isChecked = false
    }

    private fun toOnlineMode() {
        binding.playersOne.text = "Left Player:"
        binding.playersTwo.text = "Left Player:"
        binding.leftPlayerOne.visibility = View.VISIBLE
        binding.rightPlayerOne.visibility = View.VISIBLE
        binding.leftPlayerTwo.visibility = View.VISIBLE
        binding.rightPlayerTwo.visibility = View.VISIBLE
        binding.teamOneCreateImg.visibility = View.VISIBLE
        binding.teamTwoCreateImg.visibility = View.VISIBLE
        binding.leftPlayerNameOne.visibility = View.INVISIBLE
        binding.rightPlayerNameOne.visibility = View.INVISIBLE
        binding.leftPlayerNameTwo.visibility = View.INVISIBLE
        binding.rightPlayerNameTwo.visibility = View.INVISIBLE
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
            toOfflineMode()
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
        toOnlineMode()
        updateDisplay()
        if (!Competition.offlineMode && Competition.onlineGame != null) {
            Picasso.get()
                .load("http://handball-tourney.zapto.org/api/teams/image?name=${Competition.currentGame.teamOne.niceName}")
                .into(binding.teamOneCreateImg)
            Picasso.get()
                .load("http://handball-tourney.zapto.org/api/teams/image?name=${Competition.currentGame.teamTwo.niceName}")
                .into(binding.teamTwoCreateImg)
        }
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
            if (Competition.offlineMode) {
                Competition.currentGame.teamOne.playerOne.name =
                    binding.leftPlayerNameOne.text.toString()
                Competition.currentGame.teamOne.playerTwo.name =
                    binding.rightPlayerNameOne.text.toString()
                Competition.currentGame.teamTwo.playerOne.name =
                    binding.leftPlayerNameTwo.text.toString()
                Competition.currentGame.teamTwo.playerTwo.name =
                    binding.rightPlayerNameTwo.text.toString()
            }
            Competition.currentGame.startGame(
                swappedService,
                binding.rightPlayerOne.isChecked,
                binding.rightPlayerTwo.isChecked
            )
            showControlPanel()
        }
        binding.offlineMode.setOnCheckedChangeListener { _, value ->
            Competition.offlineMode = value
            if (value) {
                toOfflineMode()
            } else {
                toOnlineMode()
            }
            updateDisplay()
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}