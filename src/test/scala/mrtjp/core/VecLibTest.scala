package mrtjp.core

import codechicken.lib.vec.{Cuboid6, Vector3}
import mrtjp.core.vec.VecLib
import org.junit.jupiter.api.Test

class VecLibTest {
    @Test def rectangularCubeGrid(): Unit = {
        for ((columns, rows) <- Seq((3, 2), (2, 3), (4, 1), (1, 4))) {
            val cells = VecLib.buildCubeArray(columns, rows,
                new Cuboid6(0, 0, 0, 16*columns, 16, 16*rows), Vector3.zero.copy())
            assert(cells.length == columns*rows)
            for (i <- cells.indices) {
                assert(cells(i).min.x == i%columns)
                assert(cells(i).min.z == i/columns)
                assert(cells(i).max.x == i%columns+1)
                assert(cells(i).max.z == i/columns+1)
            }
        }
    }
}
