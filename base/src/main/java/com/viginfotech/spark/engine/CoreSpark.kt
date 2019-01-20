package com.viginfotech.spark.engine

import android.graphics.Canvas
import android.graphics.Paint

import javax.vecmath.Point3f
import javax.vecmath.Vector3f

class CoreSpark(position: Point3f, v: Vector3f, scale: Float, color: Int) : SparkBase(position, v) {

    protected var paint: Paint

    internal val lifeSpan: Long = 3500 // life span 4 seconds

    override val isExploding: Boolean
        get() = System.currentTimeMillis() - startTime > lifeSpan


    init {
        paint = Paint()
        paint.isAntiAlias = true
        paint.color = color
        this.scale = scale
        this.gravity = -0.75f
        this.drag = 0.96f
    }

    override fun draw(canvas: Canvas, screenX: Float, screenY: Float, scale: Float, doEffects: Boolean) {
        canvas.drawCircle(screenX, screenY, this.scale * scale, paint)
    }
}
