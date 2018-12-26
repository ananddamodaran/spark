package com.viginfotech.spark.engine

import android.graphics.Paint

import javax.vecmath.Point3f
import javax.vecmath.Vector3f

/**
 * PLEASE FILL IN THE CLASS DESCRIPTION
 *
 * @author zhaocong
 */
class NeedleSpark(p: Point3f, v: Vector3f, color: Int) : RingSpark(p, v, 1f, color) {

    internal override val lifeSpan = 2500L // life span 4 seconds

    override val isExploding: Boolean
        get() = System.currentTimeMillis() - startTime > lifeSpan

    init {
        this.gravity = -0.75f
        this.drag = 0.985f
    }

    companion object {

        protected var paint: Paint


        init {
            paint = Paint()
            paint.isAntiAlias = true
        }
    }
}
