package mrtjp.core

import codechicken.lib.vec.Vector3
import mrtjp.core.fx.*
import mrtjp.core.fx.particles.{BeamPulse2, CoreParticle, SpriteParticle}
import net.minecraft.world.World

object ParticleChecks {
    private def near(actual:Double, expected:Double): Unit =
        assert(scala.math.abs(actual-expected) < 1e-6, s"Expected $expected, got $actual")

    private def at(action:ParticleAction, particle:CoreParticle, time:Double): Unit = {
        action.life = time.toInt
        action.runOn(particle, (time-action.life).toFloat)
    }

    def run(world:World): Unit = {
        val first = new BeamPulse2(world)
        val second = new BeamPulse2(world)
        first.setTarget(3, 4, 5)
        near(first.prevTarget.x, 0)
        near(second.target.x, 0)
        near(Vector3.zero.x, 0)
        val direct = new PositionChangeToAction
        direct.target.x = 42
        near(new PositionChangeToAction().target.x, 0)
        near(Vector3.zero.x, 0)

        val sprite = new SpriteParticle(world)
        sprite.setPos(new Vector3(0, 0, 0))
        val move = ParticleAction.moveTo(10, 0, 0, 10)
        at(move, sprite, 0); at(move, sprite, 5); near(sprite.x, 5)
        at(move, sprite, 9); near(sprite.x, 9)
        at(move, sprite, 12); near(sprite.x, 10); assert(move.isFinished)

        val scale = ParticleAction.scaleTo(2, 1, 1, 10)
        at(scale, sprite, 0); assert(!scale.isFinished)
        at(scale, sprite, 5); near(sprite.scaleX, 1.5); assert(!scale.isFinished)
        at(scale, sprite, 10); near(sprite.scaleX, 2); assert(scale.isFinished)

        val target = ParticleAction.changeTargetTo(10, 0, 0, 10)
        at(target, second, 5); near(second.tx, 5)
        at(target, second, 10); near(second.tx, 10)
        val colour = ParticleAction.changeColourTo(0.25, 0.5, 0.75, 10)
        at(colour, sprite, 5); near(sprite.red, 0.625)
        at(colour, sprite, 10); near(sprite.red, 0.25)
        val alpha = ParticleAction.fadeOut(10)
        at(alpha, sprite, 5); near(sprite.alpha, 0.5)
        at(alpha, sprite, 10); near(sprite.alpha, 0)
        val instant = ParticleAction.moveTo(3, 4, 5, 0)
        at(instant, sprite, 0); near(sprite.x, 3); assert(instant.isFinished)

        val incremental = new SpriteParticle(world)
        incremental.setPos(new Vector3(0, 0, 0))
        val beam = new BeamPulse2(world)
        val actions = Seq(
            (ParticleAction.moveFor(1, 0, 0, 2.5), incremental:CoreParticle),
            (ParticleAction.scaleFor(1, 0, 0, 2.5), incremental:CoreParticle),
            (ParticleAction.changeColourFor(-0.1, 0, 0, 2.5), incremental:CoreParticle),
            (ParticleAction.changeAlphaFor(-0.1, 2.5), incremental:CoreParticle),
            (ParticleAction.changeTargetFor(1, 0, 0, 2.5), beam:CoreParticle))
        for ((action, particle) <- actions; time <- Seq(0.0, 1.0, 3.0)) at(action, particle, time)
        near(incremental.x, 2.5); near(incremental.scaleX, 3.5)
        near(incremental.red, 0.75); near(incremental.alpha, 0.75); near(beam.tx, 2.5)
        assert(actions.forall(_._1.isFinished))

        class MovingSprite extends SpriteParticle(world) {
            def velocity(): Unit = { motionX = 2; hasVelocity = true }
        }
        val moving = new MovingSprite
        moving.setPos(new Vector3(1, 2, 3)); moving.velocity(); moving.onUpdate()
        near(moving.px, 1); near(moving.x, 3)
        val orbiting = new SpriteParticle(world)
        orbiting.setPos(new Vector3(1, 0, 0))
        val orbit = ParticleAction.orbitAround(0, 0, scala.math.Pi/2, 1.5)
        at(orbit, orbiting, 2)
        near(orbiting.x, scala.math.cos(3*scala.math.Pi/4)); near(orbiting.z, scala.math.sin(3*scala.math.Pi/4))

        val calls = new java.util.concurrent.atomic.AtomicInteger()
        class CountingAction extends ParticleAction {
            def operate(p:CoreParticle, time:Double): Unit = { calls.incrementAndGet() }
            def copy:ParticleAction = new CountingAction
        }
        val core = new CoreParticle(world)
        val source = new CountingAction
        core.runAction(source); core.removeAction(source)
        core.renderParticle(null, null, 0, 0, 0, 0, 0, 0)
        assert(calls.get() == 0)
        core.runAction(source)
        core.renderParticle(null, null, 0, 0, 0, 0, 0, 0)
        assert(calls.get() == 1)
        for (action <- Seq(
            ParticleAction.group(ParticleAction.changeTargetTo(1, 0, 0, 1)),
            ParticleAction.sequence(ParticleAction.changeTargetTo(1, 0, 0, 1)),
            ParticleAction.repeatForever(ParticleAction.changeTargetTo(1, 0, 0, 1)))) {
            assert(!action.canOperate(sprite))
            try { sprite.runAction(action); throw new AssertionError("Expected incompatible action rejection") }
            catch { case e:RuntimeException => assert(e.getMessage.contains("incompatible")) }
        }
    }
}
