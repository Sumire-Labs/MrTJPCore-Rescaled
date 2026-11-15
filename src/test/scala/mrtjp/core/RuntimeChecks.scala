package mrtjp.core

import net.minecraftforge.fml.common.Loader

object RuntimeChecks {
    def verify():Unit = {
        val expected = Reference.SCALA_VERSION
        val actual = scala.util.Properties.versionNumberString
        assert(actual == expected, s"Expected Scala $expected at runtime, found $actual")
        val scalar = Loader.instance().getIndexedModList.get("scalar")
        assert(scalar != null && scalar.getVersion == expected,
            s"Expected the Scalar $expected runtime provider")
        println(s"MRTJPCORE_SCALA_RUNTIME_PASS: Scala $actual, Scalar ${scalar.getVersion}")
    }
}
