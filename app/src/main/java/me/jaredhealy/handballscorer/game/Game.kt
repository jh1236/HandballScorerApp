package me.jaredhealy.handballscorer.game

import kotlin.math.abs

class Game(var teamOne: Team, var teamTwo: Team, private val goalsToWin: Int = 11) {

    companion object {

        var recordStats = true

        fun loadFromMap(map: Map<String, *>): Game? {
            if (Competition.teams.isEmpty()) return null
            val teamOne = Competition.teams.first { it.teamName == map["teamOne"] }
            val teamTwo = Competition.teams.first { it.teamName == map["teamTwo"] }
            recordStats = false
            val game = Game(teamOne, teamTwo)
            if (!(map["started"] as Boolean)) {
                recordStats = true
                return game
            }
            game.startGame(
                map["swapped"] as Boolean,
                map["swapTeamOne"] as Boolean,
                map["swapTeamTwo"] as Boolean
            )
            val string = map["game"].toString()
            for (i in string.chunked(2)) {
                val team = if (i[1].isUpperCase()) game.teamOne else game.teamTwo
                val isFirstPlayer = i[1].uppercase() == "L"
                when (i[0].lowercase()) {
                    "s" -> {
                        team.addScore(isFirstPlayer)
                    }

                    "a" -> {
                        team.ace(isFirstPlayer)
                    }

                    "g" -> {
                        team.greenCard(isFirstPlayer)
                    }

                    "y" -> {
                        team.yellowCard(isFirstPlayer)
                    }

                    "v" -> {
                        team.redCard(isFirstPlayer)
                    }

                    "t" -> {
                        team.callTimeout()
                        game.endTimeout()
                    }

                    else -> {
                        if (i[0].isDigit()) {
                            if (i[0] == '0') {
                                team.yellowCard(isFirstPlayer, 10)
                            } else {
                                team.yellowCard(isFirstPlayer, i[0].toString().toInt())
                            }
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

    var serveLeft = true
        private set
    private var startTime: Long = -1
    private var swapped: Boolean = false

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


    var serviceCounter = 0

    enum class State {
        PLAYING, BEFORE_GAME, GAME_WON, TIMEOUT
    }

    var state = State.BEFORE_GAME

    var serving: Team = teamOne

    fun startGame(swapServe: Boolean, swapTeamOne: Boolean, swapTeamTwo: Boolean) {
        startTime = System.currentTimeMillis()
        ServerInteractions.start(swapServe, swapTeamOne, swapTeamTwo, startTime)
        swapped = swapServe
        if (swapServe) {
            teamTwo.serving = true
            teamOne.serving = false
            this.serving = teamTwo
        } else{
            teamOne.serving = true
            teamTwo.serving = false
            this.serving = teamOne
        }
        state = State.PLAYING
        teamOne.start(swapTeamOne)
        teamTwo.start(swapTeamTwo)
    }

    fun startTimeout() {
        state = State.TIMEOUT
    }

    fun endTimeout() {
        state = State.PLAYING
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