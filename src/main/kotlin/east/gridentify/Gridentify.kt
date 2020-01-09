package east.gridentify

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

typealias UtilityFunc = (Board, List<Move>?) -> Double

class GridentifyBot(val board: Board, val depth: Int, val panicAt: Int = 6) {

    fun start(utilityOfBoard: UtilityFunc) {
        var moveNum = 0
        val millis = measureTimeMillis {
            while (true) {
                println("Board:\n$board")
                val moves = findAllMoves(board)
                if (moves.isEmpty()) break

                val bestMove = moves
                        .filter { moves.size < panicAt || (it.size in smartMoveLens && it.result.toInt() in smartNumbers) }
                        .maxBy { utilityOfMove(it, utilityOfBoard) }!!
                moveNum++
                println("Move #$moveNum:\n${bestMove.asBoardString()}")
                board.perform(bestMove)
            }
        }

        println("Game over! (${formatTime(millis)})")
    }

    private fun utilityOfMove(move: Move, utilityOfBoard: UtilityFunc, currentDepth: Int = 1): Double {
        board.performTheoretically(move)

        if (currentDepth == depth) {
            // Max depth reached, return utility of current board
            val utility = utilityOfBoard(board, null)
            board.undo()
            return utility
        }

        val moves = findAllMoves(board)
        if (moves.isEmpty()) {
            // No more moves, return utility of current board
            val utility = utilityOfBoard(board, moves)
            board.undo()
            return utility
        }

        // Return highest utility of subsequent moves' utility scores
        val utility = moves
                .filter { moves.size < panicAt || (it.size in smartMoveLens && it.result.toInt() in smartNumbers) }
                .map { utilityOfMove(it, utilityOfBoard, currentDepth + 1) / it.unlikelyhood }
                .sum()
        board.undo()
        return utility
    }
}

fun main() {
    println("Hello Gridentify")

    val weights = arrayOf(
            arrayOf(6, 3, 4, 5, 6),
            arrayOf(5, 2, 1, 2, 3),
            arrayOf(4, 1, 0, 1, 4),
            arrayOf(3, 2, 1, 2, 5),
            arrayOf(6, 5, 4, 3, 6)
    ).flatten()

    val bot = GridentifyBot(Board.newRandom(), depth = 2)
    bot.start { board, chachedMoves ->
        val moves = chachedMoves ?: findAllMoves(board)
        moves.size.toDouble() + board.tiles.flatten().zip(weights).sumBy { (tile, w) -> tile.avgValue() * w }
    }
}