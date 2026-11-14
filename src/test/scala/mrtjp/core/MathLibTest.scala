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
    @Test def weightedSelection(): Unit = {
        val boundary = new java.util.Random(0) {
            override def nextLong(bound:Long):Long = {
                assert(bound == 2L*Int.MaxValue)
                Int.MaxValue.toLong
            }
        }
        assert(MathLib.weightedRandom(Seq("first" -> Int.MaxValue, "second" -> Int.MaxValue), boundary) == "second")
        for (seed <- 0 until 20) {
            assert(MathLib.weightedRandom(Seq("zero" -> 0, "positive" -> 10), new java.util.Random(seed)) == "positive")
        }
        val expected = new java.util.Random(42)
        val actual = new java.util.Random(42)
        for (_ <- 0 until 20) {
            val draw = expected.nextInt(3)
            assert(MathLib.weightedRandom(Seq("first" -> 1, "second" -> 2), actual) == (if draw == 0 then "first" else "second"))
        }
        for (invalid <- Seq(Seq.empty[(String, Int)], Seq("zero" -> 0), Seq("negative" -> -1, "positive" -> 2))) {
            org.junit.jupiter.api.Assertions.assertThrows(classOf[IllegalArgumentException], () => {
                MathLib.weightedRandom(invalid); ()
            })
        }
    }
}
