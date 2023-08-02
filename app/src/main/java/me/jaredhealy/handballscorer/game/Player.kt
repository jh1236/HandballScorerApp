package me.jaredhealy.handballscorer.game

class Player(var name: String) {
    companion object {

        fun fromMap(map: Map<String, *>): Player {
            val name = map["name"] as String
            val goals = map["goals"] as Int
            val green = map["greenCards"] as Int
            val yellow = map["yellowCards"] as Int
            val red = map["redCards"] as Int
            val rounds = map["roundsPlayed"] as Int
            val roundsCarded = map["roundsCarded"] as Int
            val aces = map["aces"] as Int
            val player = Player(name)
            player.goalsScored = goals
            player.greenCards = green
            player.yellowCards = yellow
            player.redCards = red
            player.roundsPlayed = rounds
            player.roundsCarded = roundsCarded
            player.aces = aces
            return player
        }
    }

    var isLeft = false
    var aces = 0
        private set
    var redCarded: Boolean = false
    var goalsScored = 0
        private set
    var greenCards = 0
        private set
    var yellowCards = 0
        private set
    var redCards = 0
        private set
    var roundsPlayed = 0
        private set
    var roundsCarded = 0
        private set
    var isCarded = false
        internal set;

    fun scoreGoal(ace: Boolean) {
        if (Game.recordStats) {
            if (ace) {
                aces++
            }
            goalsScored++
        }
    }

    fun greenCard() {
        if (Game.recordStats) {
            greenCards++
        }
    }

    fun yellowCard() {
        if (Game.recordStats) {
            yellowCards++
        }
        isCarded = true
    }

    fun redCard() {
        if (Game.recordStats) {
            redCards++
        }
        isCarded = true
        redCarded = true
    }

    fun nextPoint() {
        if (Game.recordStats) {
            if (isCarded) {
                roundsCarded++
            }
            roundsPlayed++
        }
    }


    fun reset() {
        goalsScored = 0
        greenCards = 0
        yellowCards = 0
        redCards = 0
        roundsPlayed = 0
        roundsCarded = 0
        aces = 0
    }


}