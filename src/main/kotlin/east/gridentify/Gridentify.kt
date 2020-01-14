package east.gridentify

import java.util.concurrent.Executors

fun runManyBots() {
    val bots = 40

    val executor = Executors.newFixedThreadPool(5)
    val results = mutableListOf<GridentifyBot.Result>()

    for (i in 0 until bots) {
        executor.execute {
            val bot = GridentifyBot(Board.newRandom(), depth = 2, print = false)
            bot.start { board, cachedMoves ->
                //val moves = cachedMoves ?: findAllMoves(board)
                //moves.size.toDouble() + board.tiles.flatten().zip(weights).sumBy { (tile, w) -> tile.avgValue() * w }
                8 * findNeighbourPairsSum(board) + board.tiles.flatten().zip(weights).sumBy { (tile, w) -> tile.avgValue() * w }.toDouble()
            }.also {
                val (board, millis) = it
                results.add(it)
                println("Bot ${results.size}/$bots | #${i + 1} ::: score: ${board.scoreMin}, time: ${formatTime(millis)}")
            }
        }
    }

    executor.shutdown()
    while (!executor.isTerminated) { }

    val bestScore = results.maxBy { (board, _) -> board.scoreMin }!!.finalBoard.scoreMin
    val avgScore = results.map { (board, _) -> board.scoreMin }.average()
    val avgMillis = results.map { (_, millis) -> millis }.average().toLong()

    println("\n${results.size} runs done!")
    println("Best score: $bestScore")
    println("Average score: $avgScore")
    println("Average time: ${formatTime(avgMillis)}")
}

val weights = arrayOf(
        arrayOf(4, 3, 1, 3, 4),
        arrayOf(3, 2, 1, 2, 3),
        arrayOf(2, 1, 0, 1, 2),
        arrayOf(3, 2, 1, 2, 3),
        arrayOf(4, 3, 2, 3, 4)
).flatten().map { it * it }

@ExperimentalStdlibApi
fun main() {
    for (x in 0..1) {
        HytakServerBoard("East", "35.193.192.221", 32123).use { hytakBoard ->
            val bot = GridentifyBot(hytakBoard, depth = 2)
            bot.start { board, cachedMoves ->
                //val moves = cachedMoves ?: findAllMoves(board)
                //moves.size.toDouble() + board.tiles.flatten().zip(weights).sumBy { (tile, w) -> tile.avgValue() * w }
                8 * findNeighbourPairsSum(board) + board.tiles.flatten().zip(weights).sumBy { (tile, w) -> tile.avgValue() * w }.toDouble()
            }
        }
        Thread.sleep(20)
    }
}