package mrtjp.core

import mrtjp.core.math.MathLib
import org.junit.jupiter.api.Test

class MathLibTest {
    @Test def bitPositions(): Unit = {
        for (bit <- 0 until 32) {
            val mask = 1 << bit
            assert(MathLib.leastSignificant(mask) == bit)
            assert(MathLib.mostSignificant(mask) == bit)
        }
        assert(MathLib.leastSignificant(0) == 0)
        assert(MathLib.leastSignificant(-1) == 0)
        assert(MathLib.leastSignificant(0x80000010) == 4)
    }
}
