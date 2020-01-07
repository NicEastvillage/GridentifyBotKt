package east.gridentify

import kotlin.random.Random

sealed class Tile {
    class Normal(val value: Int = Random.nextInt(R) + 1) : Tile() {
        override fun values(): Set<Int> = setOf(value)
        override fun str(): String = when {
            value < 10 -> "  $value"
            value < 100 -> " $value"
            else -> "$value"
        }
        override fun toInt(): Int = value
    }

    class Wild(val depth: Int = 1) : Tile() {
        override fun values(): Set<Int> = HashSet(List(3) { (it + 1) * depth })
        override fun str(): String = when {
            depth < 10 -> " ?$depth"
            else -> "?$depth"
        }
        override fun toInt(): Int = -depth
    }

    abstract fun values(): Set<Int>
    abstract fun str(): String
    abstract fun toInt(): Int
}