package east.gridentify

data class Pos(val x: Int, val y: Int) {
    operator fun plus(other: Pos) = Pos(x + other.x, y + other.y)
    operator fun minus(other: Pos) = Pos(x - other.x, y - other.y)
    override fun toString(): String {
        return "($x, $y)"
    }
}

enum class Direction(val delta: Pos) {
    UP(Pos(0, 1)),
    DOWN(Pos(0, -1)),
    RIGHT(Pos(1, 0)),
    LEFT(Pos(-1, 0));
}