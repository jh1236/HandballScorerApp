package me.jaredhealy.handballscorer.game

import android.util.Log
import me.jaredhealy.handballscorer.ui.home.DisplayTeam
import kotlin.math.max

@Suppress("UNCHECKED_CAST")
class Team(var teamName: String, val playerOne: Player, val playerTwo: Player) {
    companion object {
        fun fromMap(teamName: String, map: Map<String, *>): Team {
            val played = map["played"] as Int
            val wins = map["wins"] as Int
            val losses = map["losses"] as Int
            val cards = map["cards"] as Int
            val left = Player.fromMap(map["playerOne"] as Map<String, *>)
            val right = Player.fromMap(map["playerTwo"] as Map<String, *>)
            val team = Team(teamName, left, right)
            team.played = played
            team.wins = wins
            team.losses = losses
            team.cards = cards
            return team
        }
    }

    init {
        playerOne.isPlayerOne = true
        playerTwo.isPlayerOne = false
    }

    private fun addToGameString(c: String) {
        if (this.firstTeam) {
            game.gameString += c.uppercase()
        } else {
            game.gameString += c.lowercase()
        }
    }

    override operator fun equals(other: Any?): Boolean {
        return this.teamName == (other as? Team)?.teamName
    }

    constructor(teamName: String, leftPlayer: String, rightPlayer: String) : this(
        teamName, Player(leftPlayer), Player(rightPlayer)
    )

    var swapped = false
    var timeOutsRemaining = 2
        private set

    var firstTeam = false
        internal set
    var serving = false
        internal set
    var greenCarded = false
        private set
    lateinit var game: Game
    lateinit var opponent: Team
    var score = 0
        private set
    private var cardDurationPlayerOne = 3
    private var cardDurationPlayerTwo = 3
    private var cardCountPlayerOne = 0
    private var cardCountPlayerTwo = 0
    var cards = 0
        private set
    var played = 0
        private set
    var wins = 0
        internal set
    var losses = 0
        internal set


    override fun toString() = teamName
    val cardCount: Int
        get() {
            if (cardCountPlayerOne == -1 || cardCountPlayerTwo == -1) {
                return -1
            }
            return max(cardCountPlayerTwo, cardCountPlayerOne)
        }
    val cardDuration: Int
        get() {
            return if (cardCountPlayerOne > cardCountPlayerTwo) {
                cardDurationPlayerOne
            } else {
                cardDurationPlayerTwo
            }
        }

    var server: Player = playerOne

    fun start(swapPlayers: Boolean) {
        swapped = swapPlayers
        if (Game.recordStats) {
            played++
        }
        server = if (!(swapPlayers xor serving)) {
            playerTwo
        } else {
            playerOne
        }
        Log.i("asd", "Game started, server is ${server.name} ($swapPlayers)")
        playerOne.redCarded = false
        playerTwo.redCarded = false
        playerOne.isCarded = false
        playerTwo.isCarded = false
        greenCarded = false
        cardCountPlayerOne = 0
        cardCountPlayerTwo = 0
        timeOutsRemaining = 2
    }

    fun callTimeout() {
        timeOutsRemaining--
        Log.i("timeouts", timeOutsRemaining.toString())
        addToGameString("TT")
        if (Game.recordStats) {
            ServerInteractions.timeout(this)
        }
        game.startTimeout()
    }

    fun allowedTimeout(): Boolean {
        return timeOutsRemaining > 0
    }

    fun addScore(firstPlayer: Boolean? = null) {
        addScoreInternal(firstPlayer)
    }

    private fun addScoreInternal(firstPlayer: Boolean? = null, ace: Boolean = false) {
        score++
        val player = when (firstPlayer) {
            true -> {
                if (ace) {
                    addToGameString("AL")
                } else {
                    addToGameString("SL")
                }
                this.playerOne
            }

            false -> {
                if (ace) {
                    addToGameString("AR")
                } else {
                    addToGameString("SR")
                }
                this.playerTwo
            }

            else -> null
        }
        if (player != null) {
            player.scoreGoal(ace)
            if (Game.recordStats) {
                ServerInteractions.score(this, player.isPlayerOne, ace)
            }
        }
        if (!serving) {
            setServer()
        }
        game.nextPoint()
    }

    private fun setServer() {
        game.serving = this
        serving = true
        opponent.serving = false
        if (this.server == this.playerOne) {
            this.server = playerTwo
        } else {
            this.server = playerOne
        }
    }

    fun greenCard(firstPlayer: Boolean) {
        greenCarded = true
        cards++
        if (Game.recordStats) {
            ServerInteractions.greenCard(this, firstPlayer)
        }
        if (firstPlayer) {
            addToGameString("GL")
            this.playerOne.greenCard()
        } else {
            addToGameString("GR")
            this.playerTwo.greenCard()
        }
    }

    fun yellowCard(firstPlayer: Boolean, time: Int = 3) {
        cards++
        if (Game.recordStats) {
            ServerInteractions.yellowCard(this, firstPlayer, time)
        }
        if (firstPlayer) {
            this.playerOne.yellowCard()
            if (time == 3) {
                addToGameString("YL")
            } else {
                addToGameString("${time}L")
            }
            if (cardCountPlayerOne >= 0) {
                cardCountPlayerOne += time
                cardDurationPlayerOne = cardCountPlayerOne
            }
        } else {
            this.playerTwo.yellowCard()
            if (time == 3) {
                addToGameString("YR")
            } else {
                addToGameString("${time}R")
            }
            if (cardCountPlayerTwo >= 0) {
                cardCountPlayerTwo += time
                cardDurationPlayerTwo = cardCountPlayerTwo
            }
        }
        Log.i("game", "$cardDuration")
        while (cardCountPlayerOne != 0 && cardCountPlayerTwo != 0 && !game.isOver()) {
            opponent.addScore()
        }
    }

    fun redCard(firstPlayer: Boolean) {
        cards++
        if (Game.recordStats) {
            ServerInteractions.redCard(this, firstPlayer)
        }
        if (firstPlayer) {
            this.playerOne.redCard()
            addToGameString("VL")
            cardCountPlayerOne = -1
        } else {
            this.playerTwo.redCard()
            addToGameString("VR")
            cardCountPlayerTwo = -1
        }
        while (cardCountPlayerOne != 0 && cardCountPlayerTwo != 0 && !game.isOver()) {
            opponent.addScore()
        }
    }

    internal fun nextPoint() {
        playerOne.nextPoint()
        playerTwo.nextPoint()
        if (cardCountPlayerOne > 0) {
            cardCountPlayerOne -= 1
        } else {
            cardDurationPlayerOne = 3
        }
        if (cardCountPlayerTwo > 0) {
            cardCountPlayerTwo -= 1
        } else {
            cardDurationPlayerTwo = 3
        }
        playerOne.isCarded = cardCountPlayerOne != 0
        playerTwo.isCarded = cardCountPlayerTwo != 0
    }

    fun ace(firstPlayer: Boolean? = null) {
        addScoreInternal(firstPlayer ?: this.server.isPlayerOne, true)
    }

    override fun hashCode(): Int {
        return teamName.hashCode()
    }

    fun asDisplayTeam(): DisplayTeam {
        return DisplayTeam(
            teamName,
            playerOne.name,
            playerTwo.name,
            score,
            cardCount,
            cardDuration,
            greenCarded
        )
    }

}