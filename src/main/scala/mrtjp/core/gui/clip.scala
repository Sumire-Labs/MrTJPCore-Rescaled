/*
 * Copyright (c) 2015.
 * Created by MrTJP.
 * All rights reserved.
 */
package mrtjp.core.gui

import mrtjp.core.vec.{Point, Rect, Size}
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.opengl.GL11
import org.lwjgl.system.MemoryStack

class ClipNode extends TNode
:
    var size = Size.zeroSize
    override def frame = Rect(position, size)

    override protected[gui] def drawBack(mouse:Point, rframe:Float): Unit =
        if !hidden then
            val dp = mouse-position
            for n <- familyByZ do
                if n == this then drawBack_Impl(mouse, rframe)
                else
                    onChildPredraw()
                    translateTo()
                    try n.drawBack(dp, rframe)
                    finally
                        translateFrom()
                        onChildPostdraw()

    override protected[gui] def drawFront(mouse:Point, rframe:Float): Unit =
        if !hidden then
            val dp = mouse-position
            for n <- familyByZ do
                if n == this then drawFront_Impl(mouse, rframe)
                else
                    onChildPredraw()
                    translateTo()
                    try n.drawFront(dp, rframe)
                    finally
                        translateFrom()
                        onChildPostdraw()

    private def onChildPredraw(): Unit =
        val scaleRes = new ScaledResolution(mcInst)
        val scale = scaleRes.getScaleFactor

        val absPos = parent.convertPointToScreen(position)
        val sFrame = new Rect(absPos.x*scale, mcInst.displayHeight-(absPos.y*scale)-size.height*scale,
            size.width*scale, size.height*scale)

        GL11.glPushAttrib(GL11.GL_SCISSOR_BIT)
        val clipped = if GL11.glIsEnabled(GL11.GL_SCISSOR_TEST) then
            val stack = MemoryStack.stackPush()
            try
                val parentBox = stack.mallocInt(4)
                GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX, parentBox)
                val left = math.max(sFrame.x, parentBox.get(0))
                val bottom = math.max(sFrame.y, parentBox.get(1))
                val right = math.min(sFrame.maxX, parentBox.get(0)+parentBox.get(2))
                val top = math.min(sFrame.maxY, parentBox.get(1)+parentBox.get(3))
                new Rect(left, bottom, math.max(0, right-left), math.max(0, top-bottom))
            finally stack.close()
        else sFrame
        GL11.glEnable(GL11.GL_SCISSOR_TEST)
        GL11.glScissor(clipped.x, clipped.y, math.max(0, clipped.width), math.max(0, clipped.height))

    private def onChildPostdraw(): Unit =
        GL11.glPopAttrib()

    override def traceHit(absPoint:Point) = !super.traceHit(absPoint)//only let hits within frame pass through

    override def mouseScrolled_Impl(p:Point, dir:Int, consumed:Boolean) = !frame.contains(p)
    override def mouseClicked_Impl(p:Point, button:Int, consumed:Boolean) = !frame.contains(p)

object ClipNode
:
    def tempDisableScissoring(): Unit =
        GL11.glPushAttrib(GL11.GL_SCISSOR_BIT)
        GL11.glDisable(GL11.GL_SCISSOR_TEST)

    def tempEnableScissoring(): Unit =
        GL11.glPopAttrib()
