package east.gridentify

import com.beust.klaxon.Klaxon
import com.beust.klaxon.json
import kotlinx.coroutines.delay
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.Closeable
import java.net.Socket
import java.util.*

@ExperimentalStdlibApi
class HytakServerBoard(nickname: String, host: String, port: Int) : Board(Array(N) { Array(N) { Tile.Normal() as Tile }.toMutableList() }.toMutableList()), Closeable {

    private val klaxjson = Klaxon()
    private val socket = Socket(host, port) // Socket("35.193.192.221", 32123)
    private val reader = BufferedInputStream(socket.getInputStream())
    private val writer = BufferedOutputStream(socket.getOutputStream())

    init {
        writer.write("\"$nickname\"".toByteArray())
        send()
        println("Succesfully connected to $host:$port with nickname \"$nickname\"")
        updateBoardFromJson(read())
        println("Received initial board!")
    }

    private fun send() {
        writer.write(0)
        writer.flush()
    }

    private fun read(): String {
        val bytes = ByteArray(256)
        var i = 0
        var byte = 255
        while (true) {
            byte = reader.read()
            if (byte == 0) break
            bytes[i++] = byte.toByte()
        }

        val msg = bytes.sliceArray(0 until i).decodeToString()
        println("Recieved: $msg\n")
        return msg
    }

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
                this[x, y] = Tile.from(tiles[y * N + x])
            }
        }
    }

    override fun perform(move: Move) {
        if (move.result is Tile.Normal) {

            val moveJson = move.toJson()
            println("Sending: $moveJson")
            writer.write(moveJson.toByteArray())
            send()
            updateBoardFromJson(read())

            scoreMin += move.result.value
            scoreMax += move.result.value
        } else {
            throw AssertionError("Don't create wilds during simulation")
        }
    }

    override fun close() {
        reader.close()
        writer.close()
        socket.close()
    }
}