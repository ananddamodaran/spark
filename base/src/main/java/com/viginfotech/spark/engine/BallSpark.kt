package com.viginfotech.spark.engine

import android.graphics.Color

import javax.vecmath.Point3f
import javax.vecmath.Vector3f
import java.util.Random

/**
 * Ball-shape spark
 */
class BallSpark(position: Point3f, v: Vector3f) : Spark(position, v) {

    override fun onExplosion(scene: NightScene) {
        scene.playExplosionSound()

        val random = Random()

        val colorA = Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255))

        val rootSpeed = random.nextFloat() * 0.3f + 1.0f
        val baseV = Vector3f(rootSpeed, rootSpeed, rootSpeed)
        //explode the core
        for (i in 0..11) {
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
        val rootSpeedShell = random.nextFloat() * 1.8f + 1.2f // 1.2 - 3.0
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

            val sparkA = ShellSpark(this.mPosition, newVelocity, shellScale, colorA)
            val sparkB = ShellSpark(this.mPosition, newVelocityInvert, shellScale, colorA)

            scene.addSpark(sparkA)
            scene.addSpark(sparkB)
        }
    }
}
