package com.viginfotech.spark.engine

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

import javax.vecmath.Point3f
import javax.vecmath.Vector3f
import java.util.Random

/**
 * Showcase of basic spark component
 *
 * @author zhaocong
 */
open class Spark(position: Point3f, v: Vector3f) : SparkBase(position, v) {

    private val lifeSpan = 2000L

    private val streak = 10

    override val isExploding: Boolean
        get() = System.currentTimeMillis() - startTime > lifeSpan

    init {
        this.scale = 1f
        this.gravity = -0.5f
        this.drag = 1f
    }

    override fun draw(canvas: Canvas, screenX: Float, screenY: Float, scale: Float, doEffects: Boolean) {
        paint.alpha = 255
        paint.strokeWidth = 1f * scale
        cacheScreenX = screenX
        cacheScreenY = screenY
        if (System.currentTimeMillis() - startTime > 200) {
            val dy = 2f * scale
            for (i in streak downTo 1) {
                paint.alpha = 255 * i / streak
                paint.strokeWidth = scale * i / streak
                canvas.drawLine(
                    cacheScreenX,
                    cacheScreenY,
                    cacheScreenX, cacheScreenY + dy,
                    paint
                )
                cacheScreenY = cacheScreenY + dy
            }
        }
    }

    override fun onExplosion(scene: NightScene) {
        scene.playExplosionSound()
        val random = Random()

        val colorA = Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255))
        val colorB = Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255))
        val colorC = Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255))

        val rootSpeed = random.nextFloat() * 0.5f + 0.5f

        var baseV = Vector3f(rootSpeed, rootSpeed, rootSpeed)

        //explode the core
        for (i in 0..23) {
            //generate the sparks
            val randomFactor = (random.nextFloat() + 19) / 20f
            val newVelocity = Vector3f(baseV)
            MathHelper.rotate(
                newVelocity,
                Math.random() * 3,
                Math.random() * 3,
                Math.random() * 3
            )
            newVelocity.scale(randomFactor)

            val newVelocityInvert = Vector3f()
            newVelocityInvert.scale(-1f, newVelocity)
            val sparkA = ShellSpark(this.mPosition, newVelocity, 0.5f, colorA)
            val sparkB = ShellSpark(this.mPosition, newVelocityInvert, 0.5f, colorA)
            scene.addSpark(sparkA)
            scene.addSpark(sparkB)
        }

        //explore the shell
        val shellScale = 0.3f * random.nextFloat() + 0.3f
        val rootSpeedShell = random.nextFloat() * 1.8f + 0.8f // 0.8 - 2.6
        val shellVelocity = Vector3f(rootSpeed, rootSpeedShell, rootSpeedShell)
        for (i in 0..71) {
            val newVelocity = Vector3f(shellVelocity)
            MathHelper.rotate(
                newVelocity,
                Math.random() * 6,
                Math.random() * 6,
                Math.random() * 6
            )

            val newVelocityInvert = Vector3f()
            newVelocityInvert.scale(-1f, newVelocity)

            val sparkA = ShellSpark(this.mPosition, newVelocity, shellScale, colorC)
            val sparkB = ShellSpark(this.mPosition, newVelocityInvert, shellScale, colorC)

            scene.addSpark(sparkA)
            scene.addSpark(sparkB)
        }

        val ringScale = 0.95f * random.nextFloat() * 0.8f
        val rootSpeedRing = random.nextFloat() * 0.8f + .8f
        baseV = Vector3f(rootSpeedRing, 0f, rootSpeedRing)

        val rx = 1 - 2 * random.nextFloat()
        val rz = 1 - 2 * random.nextFloat()
        val ringVelocity = Vector3f(baseV)
        //explode the ring
        for (i in 0..35) {
            MathHelper.rotateY(ringVelocity, random.nextDouble() * 3)
            val newVelocity = Vector3f(ringVelocity)
            MathHelper.rotateX(newVelocity, rx.toDouble())
            MathHelper.rotateZ(newVelocity, rz.toDouble())
            newVelocity.scale(random.nextFloat() / 5 + 1.5f)

            val newVelocityInvert = Vector3f()
            newVelocityInvert.scale(-1f, newVelocity)

            val sparkA = RingSpark(this.mPosition, newVelocity, ringScale, colorB)
            val sparkB = RingSpark(this.mPosition, newVelocityInvert, ringScale, colorB)
            scene.addSpark(sparkA)
            scene.addSpark(sparkB)
        }
    }

    companion object {

        protected var paint: Paint

        init {
            paint = Paint()
            paint.isAntiAlias = true
            paint.color = Color.YELLOW
        }

        private var cacheScreenX: Float = 0.toFloat()
        private var cacheScreenY: Float = 0.toFloat()
    }
}
