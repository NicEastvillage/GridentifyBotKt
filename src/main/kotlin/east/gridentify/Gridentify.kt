package east.gridentify

const val N = 5
const val R = 3

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
        if (moveLen >= 2) {
            // We should include move that ends on this tile
            val valueSize = moveLen * draggedValue.min()!! // Works for both Normal and Wild tiles. E.g. [3] or [3/6/9]
            val result = if (draggedValue.size == 1) {
                Tile.Normal(valueSize)
            } else {
                Tile.Wild(valueSize)
            }
            allChildMoves.add(Move(mutableListOf(pos), result, unlikelyhood / draggedValue.size))
        }

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

fun main() {
    println("Hello Gridentify")
}