package east.gridentify

import org.junit.Test

class MoveTest {
    @Test
    fun wildcardMoves01() {
        val board = Board.fromInts(arrayOf(
                8, 9, -1, 9, 8,
                9, 8, 1, 8, 9,
                8, 9, 8, 9, 8,
                9, 8, 9, 8, 9,
                8, 9, 8, 9, 8
        ))
        val moves = findAllMoves(board)
        assert(moves.toString() == "[[(2, 1), (2, 0): result=2, p=1/3], [(2, 0), (2, 1): result=2, p=1/3]]")
    }

    @Test
    fun wildcardMoves02() {
        val board = Board.fromInts(arrayOf(
                8, 9, 8, 9, 8,
                9, -1, -1, 8, 9,
                8, 9, 8, 9, 8,
                9, 8, 9, 8, 9,
                8, 9, 8, 9, 8
        ))
        val moves = findAllMoves(board)
        assert(moves.toString() == "[[(2, 1), (1, 1): result=-2, p=1/3], [(1, 1), (2, 1): result=-2, p=1/3]]")
    }

    @Test
    fun wildcardMoves03() {
        val board = Board.fromInts(arrayOf(
                8, 9, 8, 9, 8,
                9, 8, 9, 8, 9,
                8, 9, -1, 9, 8,
                9, 8, -2, 8, 9,
                8, 9, 8, 9, 8
        ))
        val moves = findAllMoves(board)
        assert(moves.toString() == "[[(2, 3), (2, 2): result=4, p=1/9], [(2, 2), (2, 3): result=4, p=1/9]]")
    }

    @Test
    fun wildcardMoves04() {
        val board = Board.fromInts(arrayOf(
                8, 9, 8, 9, 8,
                9, 8, 9, 8, 9,
                8, 9, 8, 9, 8,
                9, 8, 9, 8, 9,
                8, 9, -1, -3, -2
        ))
        val moves = findAllMoves(board)
        assert(moves.toString() == "[[(3, 4), (2, 4): result=6, p=1/9], [(4, 4), (3, 4): result=12, p=1/9], [(2, 4), (3, 4): result=6, p=1/9], [(3, 4), (4, 4): result=12, p=1/9]]")
    }

    @Test
    fun wildcardMoves05() {
        val board = Board.fromInts(arrayOf(
                8, 9, 8, 9, 8,
                9, 8, 9, 8, 9,
                8, 9, 8, 9, 8,
                -1, 3, -3, 8, 9,
                8, 9, 8, 9, 8
        ))
        val moves = findAllMoves(board)
        assert(moves.last().toString() == "[(0, 3), (1, 3), (2, 3): result=9, p=1/9]")
    }

    @Test
    fun wildcardMoves06() {
        val board = Board.fromInts(arrayOf(
                88, 99, 88, -2, 88,
                99, 88, 99, -3, -6,
                88, 99, 88, 99, 88,
                99, 88, 99, 88, 99,
                88, 99, 88, 99, 88
        ))
        val moves = findAllMoves(board)
        assert(moves.last().toString() == "[(3, 0), (3, 1), (4, 1): result=18, p=1/27]")
    }

    @Test
    fun wildcardMovePerformed01() {
        val board = Board.fromInts(arrayOf(
                8, 9, 8, 9, 8,
                9, 8, 9, 8, 9,
                8, -3, 3, -1, 8,
                9, 8, 9, 8, 9,
                8, 9, 8, 9, 8
        ))
        val move = findAllMoves(board).last()
        assert(move.toString() == "[(1, 2), (2, 2), (3, 2): result=9, p=1/9]")

        board.performTheoretically(move)
        assert(board.toString() == """
            |   8   9   8   9   8 |
            |   9   8   9   8   9 |
            |   8   9  ?1  ?1   8 |
            |   9   8   9   8   9 |
            |   8   9   8   9   8 |
            (score: 9)
            
        """.trimIndent())

        board.undo()
        assert(board.toString() == """
            |   8   9   8   9   8 |
            |   9   8   9   8   9 |
            |   8  ?3   3  ?1   8 |
            |   9   8   9   8   9 |
            |   8   9   8   9   8 |
            (score: 0)
            
        """.trimIndent())
    }
}