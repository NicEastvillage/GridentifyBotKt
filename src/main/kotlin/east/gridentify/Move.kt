package east.gridentify

class Move(val invseq: MutableList<Pos>, val result: Tile, var unlikelyhood: Int) {

    val size: Int get() = invseq.size

    override fun toString(): String = invseq.joinToString(prefix = "[", postfix = ": result=${result.toInt()}, p=1/$unlikelyhood]")

    fun asBoardString(): String {
        val grid = Array(N) { y ->
            Array(N) { x ->
                val index = invseq.size - invseq.indexOf(Pos(x, y))
                if (index > invseq.size) "  ." else when {
                    index < 10 -> "  $index"
                    index < 100 -> " $index"
                    else -> "$index"
                }
            }
        }
        return grid.joinToString(postfix = "\n", separator = "\n") { row ->
            row.joinToString(prefix = "| ", postfix = " |", separator = " ")
        }
    }
}

fun List<Move>.asBoardStrings() = this.joinToString(prefix = "[\n", separator = ",\n", postfix = "]") { it.asBoardString() }