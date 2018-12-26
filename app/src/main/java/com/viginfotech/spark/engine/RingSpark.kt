package com.viginfotech.spark.engine

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path

import javax.vecmath.Point3f
import javax.vecmath.Vector3f

/**
 * PLEASE FILL IN THE CLASS DESCRIPTION
 *
 * @author zhaocong
 */
open class RingSpark : SparkBase {

    //protected Paint paint;

    internal open val lifeSpan: Long
    private var blurFactor = 8
    private var color: Int = 0
    private var cacheIndex = 0
    private var alpha: Int = 0
    private var drawingCache: Array<FloatArray>? = null
    private var isCacheFilled = false

    override val isExploding: Boolean
        get() = System.currentTimeMillis() - startTime > lifeSpan

    constructor(position: Point3f, v: Vector3f, scale: Float, color: Int) : super(position, v) {
        //paint = new Paint();
        this.scale = scale
        this.gravity = -0.75f
        this.drag = 0.988f
        this.color = color
        this.alpha = 50
        blurFactor = 8
        drawingCache = Array(blurFactor) { FloatArray(2) }
        lifeSpan = 5000L
    }

    constructor(position: Point3f, v: Vector3f, scale: Float, color: Int, gravity: Float, streak: Int) : super(
        position,
        v
    ) {
        //paint = new Paint();
        this.scale = scale
        this.gravity = gravity
        this.drag = 0.988f
        this.color = color
        this.alpha = 50
        blurFactor = streak
        lifeSpan = (streak * 300).toLong()
        drawingCache = Array(blurFactor) { FloatArray(2) }
    }

    override fun draw(canvas: Canvas, screenX: Float, screenY: Float, scale: Float, doEffects: Boolean) {
        //reset the painter
        paint.color = color
        drawingCache!![cacheIndex][0] = screenX
        drawingCache!![cacheIndex][1] = screenY
        cacheIndex++
        if (cacheIndex == blurFactor) {
            isCacheFilled = true
            cacheIndex = 0
        }

        if (System.currentTimeMillis() - this.startTime > 1000L) {
            alpha = alpha - 5
        } else {
            //cached not fill yet
            alpha = alpha + 50
        }

        if (alpha > 255) {
            alpha = 255
        } else if (alpha < 0) {
            alpha = 0
        }

        if (isCacheFilled && alpha >= 0) {
            paint.alpha = alpha
            val p = Path()
            p.moveTo(screenX, screenY)
            for (i in blurFactor - 1 downTo 0) {
                p.lineTo(drawingCache!![i][0], drawingCache!![i][1])
                canvas.drawPath(p, paint)
            }
        }
    }

    companion object {

        internal var paint: Paint

        init {
            paint = Paint()
            paint.isAntiAlias = true
        }
    }
}
