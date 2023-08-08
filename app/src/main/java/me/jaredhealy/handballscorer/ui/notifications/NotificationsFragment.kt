package me.jaredhealy.handballscorer.ui.notifications

import android.R
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import me.jaredhealy.handballscorer.databinding.FragmentNotificationsBinding
import me.jaredhealy.handballscorer.game.Competition
import me.jaredhealy.handballscorer.game.Team

@SuppressLint("SetTextI18n")
class NotificationsFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private var reloading = false
    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var selectedTeam: Team


    private fun update() {
        binding.statsPlayed.text = selectedTeam.played.toString()
        binding.statsWon.text = selectedTeam.wins.toString()
        binding.statsLosses.text = selectedTeam.losses.toString()
        if (selectedTeam.played > 0) {
            binding.statsPercentage.text =
                "${(1000 * selectedTeam.wins / selectedTeam.played) / 10}%"
        } else {
            binding.statsPercentage.text = "NAN"
        }
        binding.statsCards.text = selectedTeam.cards.toString()
        binding.statsPlayerOne.text = selectedTeam.playerOne.name
        binding.statsScoredOne.text = selectedTeam.playerOne.goalsScored.toString()
        binding.statsAcesOne.text = selectedTeam.playerOne.aces.toString()
        binding.statsGreenOne.text = selectedTeam.playerOne.greenCards.toString()
        binding.statsYellowOne.text = selectedTeam.playerOne.yellowCards.toString()
        binding.statsRedOne.text = selectedTeam.playerOne.redCards.toString()
        binding.statsTimeCardedOne.text = "${selectedTeam.playerOne.roundsCarded} points"
        binding.statsPlayerTwo.text = selectedTeam.playerTwo.name
        binding.statsScoredTwo.text = selectedTeam.playerTwo.goalsScored.toString()
        binding.statsAcesTwo.text = selectedTeam.playerTwo.aces.toString()
        binding.statsGreenTwo.text = selectedTeam.playerTwo.greenCards.toString()
        binding.statsYellowTwo.text = selectedTeam.playerTwo.yellowCards.toString()
        binding.statsRedTwo.text = selectedTeam.playerTwo.redCards.toString()
        binding.statsTimeCardedTwo.text = "${selectedTeam.playerTwo.roundsCarded} points"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        if (Competition.offlineMode || Competition.onlineGame == null || Competition.teams.isEmpty()) {
            findNavController().navigate(me.jaredhealy.handballscorer.R.id.navigation_home)
            return binding.root
        }
        selectedTeam = Competition.teams[0]
        val list = arrayListOf<String>()
        list.addAll(Competition.teams.map { it.toString() })
        val root: View = binding.root
        val spinner = binding.statsTeamSelect
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.simple_spinner_item, list
        )
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this

        binding.reloadStats.setOnClickListener {
            reloading = true
            Competition.getTeamsFromApi()

        }
        Competition.addCallback {
            if (reloading) {
                findNavController().navigate(me.jaredhealy.handballscorer.R.id.navigation_create_game)
                findNavController().navigate(me.jaredhealy.handballscorer.R.id.navigation_notifications)
                reloading = false
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        selectedTeam = Competition.teams[position]
        update()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }
}