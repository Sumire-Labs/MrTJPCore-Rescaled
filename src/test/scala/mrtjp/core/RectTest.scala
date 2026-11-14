package mrtjp.core

import mrtjp.core.vec.Rect
import org.junit.jupiter.api.Test

class RectTest {
    @Test def intersections(): Unit = {
        val horizontal = new Rect(0, 4, 10, 2)
        val vertical = new Rect(4, 0, 2, 10)
        assert(horizontal.intersects(vertical) && vertical.intersects(horizontal))
        assert(horizontal.intersects(new Rect(10, 4, 2, 2)))
        assert(horizontal.intersects(new Rect(1, 4, 1, 1)))
        assert(!horizontal.intersects(new Rect(11, 4, 2, 2)))
        assert(!horizontal.intersects(new Rect(0, 7, 2, 2)))
    }
}
