package com.viginfotech.spark.engine

import android.graphics.Canvas

import javax.vecmath.Point3f
import javax.vecmath.Vector3f

/**
 * PLEASE FILL IN THE CLASS DESCRIPTION
 *
 * @author zhaocong
 */
abstract class SparkBase(p: Point3f, v: Vector3f) {

    protected val startTime: Long

    //value manipulated by the physics engine
    var mPosition: Point3f
    var mVelocity: Vector3f
    var gravity: Float = 0.toFloat() //customized gravity
    var drag: Float = 0.toFloat() //drag coefficient posed by the air
    protected var scale: Float = 0.toFloat() //used when convert 3D to 2D

    abstract val isExploding: Boolean

    init {
        startTime = System.currentTimeMillis()
        //copy the data
        mPosition = Point3f(p)
        mVelocity = Vector3f(v)
    }

    abstract fun draw(canvas: Canvas, screenX: Float, screenY: Float, scale: Float, doEffects: Boolean)

    open fun onExplosion(sc: NightScene) {}
}
