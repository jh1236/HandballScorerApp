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
    }

    private fun updateServeIndicators() {
        if (!swappedService) {
            binding.serveIndicatorOne.visibility = View.VISIBLE
            binding.serveIndicatorTwo.visibility = View.INVISIBLE
        } else {
            binding.serveIndicatorOne.visibility = View.INVISIBLE
            binding.serveIndicatorTwo.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        _binding = FragmentCreateGameBinding.inflate(inflater, container, false)
        updateServeIndicators()

        if (Competition.currentGame == null) {
            Competition.getTeamsFromApi()
            findNavController().navigate(R.id.navigation_home)
            return binding.root
        }
        val state = (Competition.currentGame?.state ?: Game.State.BEFORE_GAME)
        if (state == Game.State.PLAYING || state == Game.State.TIMEOUT) {
            showControlPanel()
            return binding.root
        }
        binding.nextTeamOne.text = Competition.currentGame!!.teamOne.teamName
        binding.nextTeamTwo.text = Competition.currentGame!!.teamTwo.teamName
        binding.swapService.setOnClickListener {
            swappedService = !swappedService
            updateServeIndicators()
        }
        binding.startGame.setOnClickListener {
            Competition.currentGame?.startGame(swappedService)
            showControlPanel()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}