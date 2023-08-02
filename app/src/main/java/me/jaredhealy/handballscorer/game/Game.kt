package me.jaredhealy.handballscorer.game

import kotlin.math.abs

class Game(var teamOne: Team, var teamTwo: Team, val goalsToWin: Int = 11) {

    companion object {

        var recordStats = true

        fun loadFromString(team1: String, team2: String, string: String): Game? {
            if (Competition.teams.isEmpty()) return null
            val teamOne = Competition.teams.first { it.teamName == team1 }
            val teamTwo = Competition.teams.first { it.teamName == team2 }
            recordStats = false
            val game = Game(teamOne, teamTwo)
            game.startGame(false)
            for (i in string.chunked(2)) {
                val team = if (i[0].isUpperCase()) game.teamOne else game.teamTwo
                val isLeft = i[1] == 'L'
                when (i[0].lowercase()) {
                    "s" -> {
                        team.addScore(isLeft)
                    }

                    "a" -> {
                        team.ace(isLeft)
                    }

                    "g" -> {
                        team.greenCard(isLeft)
                    }

                    "y" -> {
                        team.yellowCard(isLeft)
                    }

                    "v" -> {
                        team.redCard(isLeft)
                    }

                    "t" -> {
                        team.callTimeout()
                        game.endTimeout()
                    }

                    else -> {
                        if (i[0].isDigit()) {
                            team.yellowCard(isLeft, i[0].toString().toInt())
                        }
                    }

                }
            }
            recordStats = true
            return game
        }

        fun dummyGame(): Game {
            return Game(
                Team("Team One", "Player One", "Player Two"),
                Team("Team Two", "Player One", "Player Two")
            )
        }
    }

    var swapped = false
    private var startTime: Long = -1

    var roundCount = 0
        private set

    init {
        teamOne.game = this
        teamTwo.game = this
        teamOne.firstTeam = true
        teamTwo.firstTeam = false
        teamOne.opponent = teamTwo
        teamTwo.opponent = teamOne
        teamOne.serving = true
    }

    enum class Service(val teamOne: Boolean, val isLeft: Boolean) {
        TEAM_ONE_LEFT(true, true), TEAM_TWO_LEFT(false, true), TEAM_ONE_RIGHT(
            true, false
        ),
        TEAM_TWO_RIGHT(false, false)
    }

    enum class State {
        PLAYING, BEFORE_GAME, GAME_WON, TIMEOUT
    }

    var state = State.BEFORE_GAME

    fun startGame(swapServe: Boolean) {
        startTime = System.currentTimeMillis()
        ServerInteractions.start(swapServe, startTime)
        state = State.PLAYING
        teamOne.start()
        teamTwo.start()
    }

    fun startTimeout() {
        state = State.TIMEOUT
    }

    fun endTimeout() {
        state = State.PLAYING
    }

    var serving = Service.TEAM_ONE_LEFT
        private set


    internal fun cycleService() {
        serving = Service.values()[(serving.ordinal + 1) % Service.values().size]
        teamOne.serving = serving.teamOne
        teamTwo.serving = !serving.teamOne
    }

    internal fun nextPoint() {
        roundCount++
        teamOne.nextPoint()
        teamTwo.nextPoint()
        if (isOver()) {
            state = State.GAME_WON
            if (recordStats) {
                getWinningTeam()!!.wins++
                getLosingTeam()!!.losses++
            }
        }
    }

    fun isOver(): Boolean {
        return (teamOne.score >= goalsToWin || teamTwo.score >= goalsToWin) && abs(teamOne.score - teamTwo.score) > 1
    }

    fun getWinningTeam(): Team? {
        if (!isOver()) return null
        return if (teamOne.score > teamTwo.score) teamOne else teamTwo
    }

    fun getLosingTeam(): Team? {
        if (!isOver()) return null
        return if (teamOne.score < teamTwo.score) teamOne else teamTwo
    }


}