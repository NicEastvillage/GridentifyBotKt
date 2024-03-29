package east.gridentify

open class Board(val tiles: MutableList<MutableList<Tile>>, var scoreMin: Int = 0, var scoreMax: Int = 0) {

    companion object {
        fun newRandom(): Board {
            return Board(Array(N) { Array(N) { Tile.Normal() as Tile }.toMutableList() }.toMutableList())
        }

        fun fromInts(values: Array<Int>): Board {
            assert(values.size == N * N) { "Wrong size" }
            var i = 0
            return Board(Array(N) {
                Array(N) {
                    if (values[i] > 0) {
                        Tile.Normal(values[i++])
                    } else {
                        Tile.Wild(-values[i++])
                    }
                }.toMutableList()
            }.toMutableList())
        }
    }

    class UndoMoveInfo(val oldTiles: Map<Pos, Tile>, val oldScoreMin: Int, val oldScoreMax: Int)
    val undoStack = ArrayList<UndoMoveInfo>()

    operator fun get(pos: Pos): Tile = this[pos.x, pos.y]
    operator fun get(x: Int, y: Int): Tile = tiles[y][x]
    operator fun set(x: Int, y: Int, tile: Tile) { tiles[y][x] = tile }
    operator fun set(pos: Pos, tile: Tile) { tiles[pos.y][pos.x] = tile }
    operator fun contains(pos: Pos): Boolean {
        return pos.x in 0 until N && pos.y in 0 until N
    }

    fun performTheoretically(move: Move) {
        undoStack.add(UndoMoveInfo(move.invseq.map { pos -> pos to this[pos] }.toMap(), scoreMin, scoreMax))
        this[move.invseq.first()] = move.result
        for (i in 1 until move.invseq.size) {
            val pos = move.invseq[i]
            this[pos] = Tile.Wild()
        }
        when (move.result) {
            is Tile.Normal -> {
                scoreMin += move.result.value
                scoreMax += move.result.value
            }
            is Tile.Wild -> {
                scoreMin += move.result.depth
                scoreMax += move.result.depth * R
            }
        }
    }

    open fun perform(move: Move) {
        if (move.result is Tile.Normal) {
            undoStack.add(UndoMoveInfo(move.invseq.map { pos -> pos to this[pos] }.toMap(), scoreMin, scoreMax))
            this[move.invseq.first()] = move.result
            for (i in 1 until move.invseq.size) {
                val pos = move.invseq[i]
                this[pos] = Tile.Normal()
            }
            scoreMin += move.result.value
            scoreMax += move.result.value
        } else {
            throw AssertionError("Don't create wilds during simulation")
        }
    }

    open fun undo() {
        val undoMoveInfo = undoStack.removeAt(undoStack.size - 1)
        for ((pos, tile) in undoMoveInfo.oldTiles) {
            this[pos] = tile
        }
        scoreMin = undoMoveInfo.oldScoreMin
        scoreMax = undoMoveInfo.oldScoreMax
    }

    fun scoreAsStr(): String {
        // "26~36"
        return if (scoreMin == scoreMax) {
            "$scoreMin"
        } else {
            "$scoreMin~$scoreMax"
        }
    }

    fun copy(): Board {
        return Board(Array(N) { y -> Array(N) { x -> this[x, y].copy() }.toMutableList() }.toMutableList(), scoreMin, scoreMax)
    }

    override fun toString(): String {
        return tiles.joinToString(postfix = "\n(score: ${scoreAsStr()})\n", separator = "\n") { row ->
            row.joinToString(prefix = "| ", postfix = " |", separator = " ") { it.str() }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Board) return false
        return tiles == other.tiles
    }

    override fun hashCode(): Int {
        return tiles.hashCode()
    }
}