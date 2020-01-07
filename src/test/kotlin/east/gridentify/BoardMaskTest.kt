package east.gridentify

import org.junit.Test

internal class BoardMaskTest {
    @Test
    fun boardMarker01() {
        val mask = BoardMask()
        mask[Pos(0, 3)] = true
        mask[Pos(1, 4)] = true
        mask[Pos(2, 2)] = true

        assert(mask[Pos(0, 3)])
        assert(mask[Pos(1, 4)])
        assert(mask[Pos(2, 2)])
    }

    @Test
    fun boardMarker02() {
        val mask = BoardMask()
        mask[Pos(4, 4)] = true
        mask[Pos(1, 2)] = true
        mask[Pos(4, 4)] = false

        assert(mask[Pos(1, 2)])
        assert(!mask[Pos(4, 4)])
    }

    @Test
    fun boardMarker03() {
        val mask = BoardMask()
        mask[Pos(0, 2)] = true
        mask.toggle(Pos(0, 2))
        mask.toggle(Pos(1, 3))

        assert(!mask[Pos(1, 2)])
        assert(mask[Pos(1, 3)])
    }
}