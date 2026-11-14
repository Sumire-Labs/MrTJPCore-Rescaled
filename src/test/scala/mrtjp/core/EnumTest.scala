package mrtjp.core
import org.junit.jupiter.api.Test
import mrtjp.core.util.Enum

object TestValues extends Enum {
    type EnumVal = Entry
    class Entry(val name:String) extends Value
    val a = new Entry("a")
    val b = new Entry("b")
    val c = new Entry("c")
}
class EnumTest {
    @Test def collectionOperations(): Unit = {
        import TestValues.*
        val original = ValSet(a, c)
        assert(original.incl(b).contains(b))
        assert(!original.excl(a).contains(a))
        assert(original.contains(a) && !original.contains(b))
        assert(original.iteratorFrom(b).toList == List(c))
        assert(ValSet(a, b, c).rangeImpl(Some(b), Some(c)).toList == List(b))
        assert(ValSet.empty.iteratorFrom(a).isEmpty)
        assert(a.to(c).toList == List(a, b, c))
    }
}
