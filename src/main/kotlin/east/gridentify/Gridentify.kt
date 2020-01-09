package east.gridentify

import java.util.*
import kotlin.collections.HashSet
import kotlin.system.measureTimeMillis

const val N = 5
const val R = 3

val smartNumbers = setOf(-2, -1, 1, 2, 3, 6, 12, 24, 48, 96, 192, 384, 768, 1536, 3072, 6144, 12288, 24578, 49152)
val smartMoveLens = setOf(2, 3, 4, 6, 8, 12)
val maxMoveLen = smartMoveLens.max()!!

fun findAllMoves(board: Board): List<Move> {
    fun findMovesStartingFrom(
            pos: Pos,
            draggedValue: Set<Int>,
            visited: BoardMask = BoardMask(),
            moveLen: Int = 1,
            unlikelyhoodArg: Int = 1
    ): List<Move> {

        var unlikelyhood = unlikelyhoodArg
        if (board[pos] is Tile.Wild) {
            unlikelyhood *= R
        }

        visited[pos] = true

        val allChildMoves = mutableListOf<Move>()
        if (moveLen in smartMoveLens) {
            // We should include move that ends on this tile
            val valueSize = moveLen * draggedValue.min()!! // Works for both Normal and Wild tiles. E.g. [3] or [3/6/9]
            val result = if (draggedValue.size == 1) {
                Tile.Normal(valueSize)
            } else {
                Tile.Wild(valueSize)
            }
            allChildMoves.add(Move(mutableListOf(pos), result, unlikelyhood / draggedValue.size))
        }

        if (moveLen < maxMoveLen) {
            for (dir in Direction.values()) {
                val neiPos = pos + dir.delta
                if (neiPos in board && !visited[neiPos]) {
                    val neiTile = board[neiPos]
                    val sharedValues = draggedValue.intersect(neiTile.values())
                    if (sharedValues.isNotEmpty()) {
                        val movesGoingThroughNei = findMovesStartingFrom(neiPos, sharedValues, visited.copy(), moveLen + 1, unlikelyhood)
                        // Append this pos, then add them all to allChildMoves
                        for (move in movesGoingThroughNei) {
                            move.invseq.add(pos)
                        }
                        allChildMoves.addAll(movesGoingThroughNei)
                    }
                }
            }
        }
        return allChildMoves
    }

    val allMoves = mutableListOf<Move>()
    for (y in 0 until N) {
        for (x in 0 until N) {
            val draggedValues = board[Pos(x, y)].values()
            val moves = findMovesStartingFrom(Pos(x, y), draggedValues)
            allMoves.addAll(moves)
        }
    }
    return allMoves
}

fun findNeighbourPairsSum(board: Board): Int {
    var sum = 0
    for (y in 0 until N) {
        for (x in y until N) {
            if (y < N - 1 && board[x, y] == board[x, y + 1])
                sum += board[x, y].avgValue() * 2
            if (x < N - 1 && board[x, y] == board[x + 1, y])
                sum += board[x, y].avgValue() * 2
        }
    }
    return sum
}

typealias UtilityFunc = (Board, List<Move>?) -> Double

class GridentifyBot(val board: Board, val depth: Int, val panicAt: Int = 6, val print: Boolean = true) {

    data class Result(val finalBoard: Board, val timeMillis: Long)

    val transpositionTable = HashMap<Board, List<Move>>()

    fun start(utilityOfBoard: UtilityFunc): Result {
        var moveNum = 0
        val millis = measureTimeMillis {
            while (true) {
                if (print) println("Board:\n$board")
                val moves = findAllMoves(board)
                if (moves.isEmpty()) break

                val bestMove = moves
                        .filter { moves.size < panicAt || (it.size in smartMoveLens && it.result.toInt() in smartNumbers) }
                        .maxBy { utilityOfMove(it, utilityOfBoard) } ?: moves.maxBy { utilityOfMove(it, utilityOfBoard) }!!
                moveNum++
                if (print) println("Move #$moveNum:\n${bestMove.asBoardString()}")
                board.perform(bestMove)
            }
        }

        if (print) println("Game over! (${formatTime(millis)})")

        return Result(board.copy(), millis)
    }

    private fun utilityOfMove(move: Move, utilityOfBoard: UtilityFunc, currentDepth: Int = 1): Double {
        board.performTheoretically(move)

        if (currentDepth == depth) {
            // Max depth reached, return utility of current board
            val utility = utilityOfBoard(board, null)
            board.undo()
            return utility
        }

        val moves = transpositionTable[board] ?: findAllMoves(board).also { transpositionTable[board] = it }
        if (moves.isEmpty()) {
            // No more moves, return utility of current board
            val utility = utilityOfBoard(board, moves)
            board.undo()
            return utility
        }

        // Return the weighted sum of subsequent moves' utility scores
        val utility = moves
                .filter { moves.size < panicAt || (it.size in smartMoveLens && it.result.toInt() in smartNumbers) }
                .map { utilityOfMove(it, utilityOfBoard, currentDepth + 1) / it.unlikelyhood }
                .sum()
        board.undo()
        return utility
    }
}

fun main() {
    val weights = arrayOf(
            arrayOf(4, 3, 1, 3, 4),
            arrayOf(3, 2, 1, 2, 3),
            arrayOf(2, 1, 0, 1, 2),
            arrayOf(3, 2, 1, 2, 3),
            arrayOf(4, 3, 2, 3, 4)
    ).flatten().map { it * it }

    val bots = 20

    val results = (0 until bots).map { i ->
        val bot = GridentifyBot(Board.newRandom(), depth = 2, print = false)
        bot.start { board, cachedMoves ->
            //val moves = cachedMoves ?: findAllMoves(board)
            //moves.size.toDouble() + board.tiles.flatten().zip(weights).sumBy { (tile, w) -> tile.avgValue() * w }
            8 * findNeighbourPairsSum(board) + board.tiles.flatten().zip(weights).sumBy { (tile, w) -> tile.avgValue() * w }.toDouble()
        }.also { (board, millis) ->
            println("Bot ${i + 1}/$bots ::: score: ${board.scoreMin}, time: ${formatTime(millis)}")
        }
    }

    val bestScore = results.maxBy { (board, _) -> board.scoreMin }!!.finalBoard.scoreMin
    val avgScore = results.map { (board, _) -> board.scoreMin }.average()
    val avgMillis = results.map { (_, millis) -> millis }.average().toLong()

    println("\n$bots runs done!")
    println("Best score: $bestScore")
    println("Average score: $avgScore")
    println("Average time: ${formatTime(avgMillis)}")
}