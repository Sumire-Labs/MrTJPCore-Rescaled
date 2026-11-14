package mrtjp.core.gui

import mrtjp.core.vec.{Point, Rect, Size}
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11

object ClipChecks {
    private def scissorBox():Seq[Int] = {
        val buffer = BufferUtils.createIntBuffer(4)
        GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX, buffer)
        (0 until 4).map(buffer.get)
    }

    def run(mc:Minecraft): Unit = {
        val root = new NodeGui(100, 100)
        root.position = Point(10, 10)
        val outer = new ClipNode
        outer.position = Point(20, 20)
        outer.size = Size(100, 100)
        val inner = new ClipNode
        inner.position = Point(80, 80)
        inner.size = Size(100, 100)
        class Probe extends TNode {
            var box = Seq.empty[Int]
            var fail = false
            override def frame = Rect.zeroRect
            override def drawBack_Impl(p:Point, frame:Float): Unit = {
                assert(GL11.glIsEnabled(GL11.GL_SCISSOR_TEST))
                box = scissorBox()
                if (fail) throw new IllegalStateException("clip fixture")
            }
        }
        val probe = new Probe
        root.addChild(outer); outer.addChild(inner); inner.addChild(probe)
        val scale = new ScaledResolution(mc).getScaleFactor
        GL11.glPushAttrib(GL11.GL_SCISSOR_BIT)
        try {
            GL11.glDisable(GL11.GL_SCISSOR_TEST)
            GL11.glScissor(1, 2, 3, 4)
            outer.drawBack(Point.zeroPoint, 0)
            assert(probe.box == Seq(110*scale, mc.displayHeight-130*scale, 20*scale, 20*scale))
            assert(!GL11.glIsEnabled(GL11.GL_SCISSOR_TEST))
            assert(scissorBox() == Seq(1, 2, 3, 4))
            probe.fail = true
            try {
                outer.drawBack(Point.zeroPoint, 0)
                throw new AssertionError("Expected the child rendering exception")
            } catch { case e:IllegalStateException => assert(e.getMessage == "clip fixture") }
            assert(!GL11.glIsEnabled(GL11.GL_SCISSOR_TEST))
            assert(scissorBox() == Seq(1, 2, 3, 4))
        } finally { GL11.glPopAttrib() }
    }
}
