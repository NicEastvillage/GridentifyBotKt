package east.gridentify

data class BoardMask(var mask: Int = 0) {
    operator fun get(pos: Pos): Boolean = mask and (1 shl (pos.x + pos.y * N)) > 0
    operator fun set(pos: Pos, flag: Boolean) {
        if (flag)
            mask = mask or (1 shl (pos.x + pos.y * N))
        else
            mask = mask and (1 shl (pos.x + pos.y * N)).inv()
    }

    fun toggle(pos: Pos) {
        mask = mask xor (1 shl (pos.x + pos.y * N))
    }

    override fun toString(): String {
        val grid = Array(N) { y ->
            Array(N) { x ->
                if (this[Pos(x, y)]) 1 else 0
            }
        }
        return grid.joinToString(postfix = "\n", separator = "\n") { row ->
            row.joinToString(prefix = "| ", postfix = " |", separator = " ")
        }
    }
}