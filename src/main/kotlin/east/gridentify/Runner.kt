package east.gridentify

import com.beust.klaxon.Klaxon
import com.beust.klaxon.json
import okhttp3.*
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

fun runLocalToCompletion(bot: GridentifyBot, print: Boolean = true): GridentifyBot.Result {
    var moveNum = 0
    val millis = measureTimeMillis {
        while (true) {
            if (print) println("Board:\n${bot.board}")
            val move = bot.bestMove() ?: break
            moveNum++
            if (print) println("Move #$moveNum:\n${move.asBoardString()}")
            bot.board.perform(move)
        }
    }

    if (print) println("Game over! (${formatTime(millis)})")

    return GridentifyBot.Result(bot.board.copy(), millis)
}

fun runOlineToCompletion(bot: GridentifyBot, host: String, port: Int, print: Boolean = true, nickname: String = "East [bot]"): GridentifyBot.Result? {
    val client = OkHttpClient.Builder().readTimeout(0, TimeUnit.MILLISECONDS).build()
    val request = Request.Builder().url("$host:$port").build()
    val runner = WebSocketRunner(bot, print, nickname)
    val millis = measureTimeMillis {
        client.newWebSocket(request, runner) // Starts thread
        client.dispatcher.executorService.awaitTermination(24, TimeUnit.HOURS)
    }
    return if (runner.gameOver) {
        GridentifyBot.Result(bot.board.copy(), millis)
    } else {
        null
    }
}

class WebSocketRunner(val bot: GridentifyBot, val print: Boolean, val nickname: String) : WebSocketListener() {

    private val klaxjson = Klaxon()
    var gameOver = false

    private fun Move.toJson(): String {
        return json {
            array(this@toJson.invseq.reversed().map { (x, y) ->
                N * y + x
            })
        }.toJsonString()
    }

    private fun updateBoardFromJson(json: String) {
        val tiles = klaxjson.parseArray<Int>(json)!!
        for (y in 0 until N) {
            for (x in 0 until N) {
                bot.board[x, y] = Tile.from(tiles[y * N + x])
            }
        }
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        webSocket.send("\"$nickname\"")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        println("MESSAGE FROM HYTAK: $text")
        updateBoardFromJson(text)
        val move = bot.bestMove()
        if (move != null) {
            val score = (move.result as Tile.Normal).value
            bot.board.scoreMin += score
            bot.board.scoreMax += score
            webSocket.send(move.toJson())
        } else {
            gameOver = true
            webSocket.close(1000, null) // FIXME Shutdown is not graceful, maybe Hytak is not sending a close frame
        }
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        println("CLOSING: $code $reason")
        webSocket.close(1000, null)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        t.printStackTrace()
        println("FAILURE")
        webSocket.cancel()
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        println("CLOSED")
    }
}
