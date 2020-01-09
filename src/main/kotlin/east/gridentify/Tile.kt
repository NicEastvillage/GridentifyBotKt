package east.gridentify

import kotlin.random.Random

sealed class Tile {
    companion object {
        fun from(num: Int): Tile {
            return when {
                num > 0 -> Normal(num)
                num < 0 -> Wild(-num)
                else -> throw IllegalAccessException("0 is not a valid tile")
            }
        }
    }

    data class Normal(val value: Int = Random.nextInt(R) + 1) : Tile() {
        override fun values(): Set<Int> = setOf(value)
        override fun str(): String = when {
            value < 10 -> "  $value"
            value < 100 -> " $value"
            else -> "$value"
        }
        override fun toInt(): Int = value
        override fun avgValue(): Int = value
        override fun minValue(): Int = value
    }

    data class Wild(val depth: Int = 1) : Tile() {
        override fun values(): Set<Int> = HashSet(List(3) { (it + 1) * depth })
        override fun str(): String = when {
            depth < 10 -> " ?$depth"
            else -> "?$depth"
        }
        override fun toInt(): Int = -depth
        override fun avgValue(): Int = 2 * depth
        override fun minValue(): Int = depth
        override fun hashCode(): Int = -depth
    }

    abstract fun values(): Set<Int>
    abstract fun str(): String
    abstract fun toInt(): Int
    abstract fun avgValue(): Int
    abstract fun minValue(): Int

    fun copy() = from(this.toInt())
}