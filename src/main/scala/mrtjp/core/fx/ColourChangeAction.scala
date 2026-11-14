/*
 * Copyright (c) 2015.
 * Created by MrTJP.
 * All rights reserved.
 */
package mrtjp.core.fx

import codechicken.lib.vec.Vector3
import mrtjp.core.fx.particles.CoreParticle

trait TColourParticle extends CoreParticle
:
    var rgb = Vector3.one.copy

    def red = rgb.x
    def green = rgb.y
    def blue = rgb.z

    def red_=(r:Double): Unit ={rgb.x = r.toFloat}
    def green_=(g:Double): Unit ={rgb.y = g.toFloat}
    def blue_=(b:Double): Unit ={rgb.z = b.toFloat}

    def setRGB(r:Double, g:Double, b:Double): Unit =
        red = r
        green = g
        blue = b

class ColourChangeToAction extends ParticleAction
:
    var target = Vector3.zero.copy
    var duration = 0.0

    override def canOperate(p:CoreParticle) = p.isInstanceOf[TColourParticle]

    override def operate(p:CoreParticle, time:Double): Unit =
        val c = p.asInstanceOf[TColourParticle]

        if time < duration then
            val drgb = target.copy.subtract(c.rgb)
            val speed = drgb.copy.multiply(1/(duration-lastTime)).multiply(deltaTime(time))
            c.rgb.add(speed)

            if c.rgb.x > 1 then c.rgb.x = 1
            if c.rgb.y > 1 then c.rgb.y = 1
            if c.rgb.z > 1 then c.rgb.z = 1
            if c.rgb.x < 0 then c.rgb.x = 0
            if c.rgb.y < 0 then c.rgb.y = 0
            if c.rgb.z < 0 then c.rgb.z = 0
        else
            c.rgb.set(math.max(0.0, math.min(1.0, target.x)), math.max(0.0, math.min(1.0, target.y)), math.max(0.0, math.min(1.0, target.z)))
            isFinished = true

    override def compile(p:CoreParticle): Unit ={}

    override def copy = ParticleAction.changeColourTo(target.x, target.y, target.z, duration)

class ColourChangeForAction extends ParticleAction
{
    var delta = Vector3.zero.copy
    var duration = 0.0

    override def canOperate(p:CoreParticle) = p.isInstanceOf[TColourParticle]

    override def operate(p:CoreParticle, time:Double): Unit =
        val c = p.asInstanceOf[TColourParticle]
        c.rgb.add(delta.copy.multiply(boundedDeltaTime(time, duration)))
        c.rgb.x = math.max(0.0, math.min(1.0, c.rgb.x))
        c.rgb.y = math.max(0.0, math.min(1.0, c.rgb.y))
        c.rgb.z = math.max(0.0, math.min(1.0, c.rgb.z))
        if time >= duration then isFinished = true

    override def compile(p:CoreParticle): Unit ={}

    override def copy = ParticleAction.changeColourFor(delta.x, delta.y, delta.z, duration)
}
