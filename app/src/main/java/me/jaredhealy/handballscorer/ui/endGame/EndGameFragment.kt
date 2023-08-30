package me.jaredhealy.handballscorer.ui.endGame

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.squareup.picasso.Picasso
import me.jaredhealy.handballscorer.R
import me.jaredhealy.handballscorer.databinding.FragmentEndGameBinding
import me.jaredhealy.handballscorer.game.Competition
import me.jaredhealy.handballscorer.game.ServerInteractions


@SuppressLint("SetTextI18n")
class EndGameFragment : Fragment() {

    private var _binding: FragmentEndGameBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private fun showControlPanel() {
        findNavController().navigate(R.id.navigation_create_game)
        findNavController()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)

        _binding = FragmentEndGameBinding.inflate(inflater, container, false)

        binding.winner.text = "Winner" + Competition.currentGame.getWinningTeam()!!.teamName
        Picasso.get()
            .load("http://handball-tourney.zapto.org/api/teams/image?name=${Competition.currentGame.getWinningTeam()!!.niceName}")
            .into(binding.imageView)

        binding.playerOneTeamOne.text = Competition.currentGame.teamOne.playerOne.name
        binding.playerTwoTeamOne.text = Competition.currentGame.teamOne.playerTwo.name
        binding.playerOneTeamTwo.text = Competition.currentGame.teamTwo.playerOne.name
        binding.playerTwoTeamTwo.text = Competition.currentGame.teamTwo.playerTwo.name

        binding.button.setOnClickListener {
            val player = if (binding.playerOneTeamOne.isChecked) {
                Competition.currentGame.teamOne.playerOne
            } else if (binding.playerTwoTeamOne.isChecked) {
                Competition.currentGame.teamOne.playerTwo
            } else if (binding.playerOneTeamTwo.isChecked) {
                Competition.currentGame.teamTwo.playerOne
            } else {
                Competition.currentGame.teamTwo.playerTwo
            }
            ServerInteractions.end(player)
            Thread.sleep(1000)
            showControlPanel()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}