package com.viginfotech.spark.engine


import javax.vecmath.Vector3f

/**
 * PLEASE FILL IN THE CLASS DESCRIPTION
 *
 * @author zhaocong
 */
object PhysicsEngine {

    private val WIND = -0.1f

    internal var velocity = FloatArray(3)

    internal var tempV = Vector3f()

    /**
     * Apply the simple physics to calculate the position
     * @param deltaTime million seconds
     */
     fun move(spark: SparkBase, deltaTime: Long) {
        //calculate the change in velocity
        //assume velocity X does not change over time
        val delaTimeF = deltaTime.toFloat()
        //apply the drag
        //Log.d("delta", "delta value" + ((1 - spark.drag * delaTimeF/1000f) * spark.drag));
        spark.mVelocity.scale((1 - spark.drag * delaTimeF / 200000f) * spark.drag)
        spark.mVelocity.get(velocity)
        //x
        velocity[0] += WIND * deltaTime.toFloat() / 1000f
        //y
        velocity[1] += deltaTime.toFloat() * spark.gravity / 1000f
        spark.mVelocity.set(velocity)
        tempV.set(velocity)
        tempV.scale(delaTimeF / 1000f)
        spark.mPosition.add(tempV)
    }

}
