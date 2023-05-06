package east.gridentify

import java.util.concurrent.Executors

fun runManyBots(bots: Int=30, threadCount: Int=6) {

    val executor = Executors.newFixedThreadPool(threadCount)
    val results = mutableListOf<GridentifyBot.Result>()

    for (i in 0 until bots) {
        executor.execute {
            val bot = GridentifyBot(Board.newRandom(), depth = 2) { board, cachedMoves ->
//                val moves = cachedMoves ?: findAllMoves(board)
//                moves.size.toDouble() + board.tiles.flatten().zip(weights).sumBy { (tile, w) -> tile.avgValue() * w }
                8 * findNeighbourPairsSum(board) + board.tiles.flatten().zip(weights).sumOf { (tile, w) -> tile.avgValue() * w }.toDouble()
            }
            val res = runLocalToCompletion(bot, print = false)
            results.add(res)
            println("Bot ${results.size}/$bots | #${i + 1} ::: score: ${res.finalBoard.scoreMin}, time: ${formatTime(res.timeMillis)}")
        }
    }

    executor.shutdown()
    while (!executor.isTerminated) { }

    printSummary(results)
}

@ExperimentalStdlibApi
fun runHytakServerBots(bots: Int=5) {
    val results = mutableListOf<GridentifyBot.Result>()

    for (i in 0 until bots) {
        val bot = GridentifyBot(Board.newRandom(), depth = 2) { board, cachedMoves ->
            val moves = cachedMoves ?: findAllMoves(board)
            8 * moves.size.toDouble() + board.tiles.flatten().zip(weights).sumOf { (tile, w) -> tile.avgValue() * w }
            //8 * findNeighbourPairsSum(board) + board.tiles.flatten().zip(weights).sumBy { (tile, w) -> tile.avgValue() * w }.toDouble()
        }
        val res = runOlineToCompletion(bot, "wss://server.lucasholten.com", 21212)
        if (res != null) {
            results.add(res)
            println("Bot ${results.size}/$bots | #${i + 1} ::: score: ${res.finalBoard.scoreMin}, time: ${formatTime(res.timeMillis)}")
        } else {
            println("Bot ${results.size}/$bots | #${i + 1} ::: DNF")
        }
    }

    printSummary(results)
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
    runHytakServerBots()
//    runManyBots()
}