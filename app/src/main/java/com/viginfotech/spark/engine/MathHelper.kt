package com.viginfotech.spark.engine

import javax.vecmath.Tuple3f


object MathHelper {

    /**
     * Rotate the tuple in place
     * @param origin the original tuple
     * @param x rotate around x
     * @param y rotate around y
     * @param z rotate around z
     */
    fun rotate(origin: Tuple3f, x: Double, y: Double, z: Double) {
        rotateX(origin, x)
        rotateY(origin, y)
        rotateZ(origin, z)
    }

    fun rotateX(origin: Tuple3f, x: Double) {
        val yd = origin.y.toDouble()
        val zd = origin.z.toDouble()

        val sinX = Math.sin(x)
        val cosX = Math.cos(x)

        val yy = yd * cosX - zd * sinX
        val zz = yd * sinX + zd * cosX

        origin.y = yy.toFloat()
        origin.z = zz.toFloat()
    }


    fun rotateY(origin: Tuple3f, y: Double) {
        val xd = origin.x.toDouble()
        val zd = origin.z.toDouble()
        val sinY = Math.sin(y)
        val cosY = Math.cos(y)

        val xx = xd * cosY + zd * sinY
        val zz = -xd * sinY + zd * cosY
        origin.x = xx.toFloat()
        origin.z = zz.toFloat()
    }

    fun rotateZ(origin: Tuple3f, z: Double) {
        val xd = origin.x.toDouble()
        val yd = origin.y.toDouble()
        val sinZ = Math.sin(z)
        val cosZ = Math.cos(z)
        val xx = xd * cosZ - yd * sinZ
        val yy = xd * sinZ + yd * cosZ
        origin.x = xx.toFloat()
        origin.y = yy.toFloat()
    }
}
