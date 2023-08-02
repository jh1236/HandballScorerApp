package me.jaredhealy.handballscorer.game

import kotlin.math.max

@Suppress("UNCHECKED_CAST")
class Team(var teamName: String, val leftPlayer: Player, val rightPlayer: Player) {
    companion object {
        fun fromMap(teamName: String, map: Map<String, *>): Team {
            val played = map["played"] as Int
            val wins = map["wins"] as Int
            val losses = map["losses"] as Int
            val cards = map["cards"] as Int
            val left = Player.fromMap(map["leftPlayer"] as Map<String, *>)
            val right = Player.fromMap(map["rightPlayer"] as Map<String, *>)
            val team = Team(teamName, left, right)
            team.played = played
            team.wins = wins
            team.losses = losses
            team.cards = cards
            return team
        }
    }

    init {
        leftPlayer.isLeft = true
        rightPlayer.isLeft = false
    }


    constructor(teamName: String, leftPlayer: String, rightPlayer: String) : this(
        teamName, Player(leftPlayer), Player(rightPlayer)
    )

    private var timeOutsRemaining = 2

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
    private var cardCountPlayerLeft = 0
    private var cardCountPlayerRight = 0
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
            if (cardCountPlayerLeft == -1 || cardCountPlayerRight == -1) {
                return -1
            }
            return max(cardCountPlayerRight, cardCountPlayerLeft)
        }

    val server: Player
        get() {
            return if ((game.serving.isLeft && !leftPlayer.isCarded) || rightPlayer.isCarded) {
                leftPlayer
            } else {
                rightPlayer
            }
        }

    fun start() {
        if (Game.recordStats) {
            played++
        }
        leftPlayer.redCarded = false
        rightPlayer.redCarded = false
        leftPlayer.isCarded = false
        rightPlayer.isCarded = false
        greenCarded = false
        cardCountPlayerLeft = 0
        cardCountPlayerRight = 0
        timeOutsRemaining = 2
    }

    fun callTimeout() {
        ServerInteractions.timeout(this)
        timeOutsRemaining--
        game.startTimeout()
    }

    fun allowedTimeout(): Boolean {
        return timeOutsRemaining > 0
    }

    fun addScore(leftPlayer: Boolean? = null) {
        addScoreInternal(leftPlayer)

    }

    private fun addScoreInternal(leftPlayer: Boolean? = null, ace: Boolean = false) {
        score++
        val player = when (leftPlayer) {
            true -> {
                this.leftPlayer
            }

            false -> {
                this.rightPlayer
            }

            else -> null
        }
        if (player != null) {
            player.scoreGoal(ace)
            ServerInteractions.score(this, player.isLeft, ace)
        }

        if (!serving) {
            game.cycleService()
        }
        game.nextPoint()
    }

    fun greenCard(leftPlayer: Boolean) {
        greenCarded = true
        cards++
        ServerInteractions.greenCard(this, leftPlayer)
        if (leftPlayer) {
            this.leftPlayer.greenCard()
        } else {
            this.rightPlayer.greenCard()
        }
    }

    fun yellowCard(leftPlayer: Boolean, time: Int = 3) {
        cards++
        ServerInteractions.yellowCard(this, leftPlayer)
        if (leftPlayer) {
            this.leftPlayer.yellowCard()
            if (cardCountPlayerLeft >= 0) {
                cardCountPlayerLeft += time
            }
        } else {
            this.rightPlayer.yellowCard()
            if (cardCountPlayerRight >= 0) {
                cardCountPlayerRight += time
            }
        }
        while (cardCountPlayerLeft != 0 && cardCountPlayerRight != 0 && !game.isOver()) {
            opponent.addScore()
        }
    }

    fun redCard(leftPlayer: Boolean) {
        cards++
        ServerInteractions.redCard(this, leftPlayer)
        if (leftPlayer) {
            this.leftPlayer.redCard()
            cardCountPlayerLeft = -1
        } else {
            this.rightPlayer.redCard()
            cardCountPlayerRight = -1
        }
        while (cardCountPlayerLeft != 0 && cardCountPlayerRight != 0 && !game.isOver()) {
            opponent.addScore()
        }
    }

    internal fun nextPoint() {
        leftPlayer.nextPoint()
        rightPlayer.nextPoint()
        if (cardCountPlayerLeft > 0) {
            cardCountPlayerLeft -= 1
        }
        if (cardCountPlayerRight > 0) {
            cardCountPlayerRight -= 1
        }
        leftPlayer.isCarded = cardCountPlayerLeft != 0
        rightPlayer.isCarded = cardCountPlayerRight != 0
    }

    fun ace(leftPlayer: Boolean? = null) {
        addScoreInternal(leftPlayer ?: this.server.isLeft, true)
    }

}