package com.viginfotech.spark.engine

import android.graphics.Color

import javax.vecmath.Point3f
import javax.vecmath.Vector3f
import java.util.Random

class GroupSpark(p: Point3f, v: Vector3f) : Spark(p, v) {


    override val isExploding: Boolean
        get() = System.currentTimeMillis() - startTime > 3000L

    override fun onExplosion(scene: NightScene) {
        scene.playExplosionSound()
        val random = Random()
        val colorA = Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255))
        val rootSpeed = random.nextFloat() * 0.3f + 0.5f
        val baseV = Vector3f(rootSpeed, 2f, rootSpeed)

        for (i in 0..71) {
            //generate the sparks
            val newVelocity = Vector3f(baseV)
            MathHelper.rotate(
                newVelocity,
                Math.random() * 3,
                Math.random() * 3,
                Math.random() * 3
            )

            val position = Point3f(this.mPosition)
            val spin = NeedleSpark(position, newVelocity, colorA)
            scene.addSpark(spin)

            val newVelocityInvert = Vector3f()
            newVelocityInvert.scale(-1f, newVelocity)
            val spinInvert = NeedleSpark(position, newVelocityInvert, colorA)
            scene.addSpark(spinInvert)
        }
    }

}
