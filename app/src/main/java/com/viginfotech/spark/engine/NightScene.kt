package com.viginfotech.spark.engine

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.SurfaceView
import android.widget.Toast
import bolts.Task
import com.viginfotech.spark.R
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentLinkedQueue
import javax.vecmath.Point3f
import javax.vecmath.Vector3f

/**
 * Stage for animation
 *
 * @author zhaocong
 */
class NightScene : SurfaceView, AudioManager.OnAudioFocusChangeListener {

    private var sceneWidthHalf: Float = 0.toFloat()
    private var sceneHeightHalf: Float = 0.toFloat()
    private var densityDpi: Float = 0.toFloat()

    private val sparks = ArrayList<SparkBase>()

    private val recycleList = ArrayList<SparkBase>()

    internal var dpToMeterRatio: Float = 0.toFloat() //dp per meter
    internal var pixelMeterRatio: Float = 0.toFloat() //pixels per meter
    internal var sceneWidth: Float = 0.toFloat()
    internal var sceneDepth = 80f
    internal var sceneHeight = 200f //expect to support scene with 200 m
    private var isShowOngoing = true
    private var isFocusAcquired: Boolean = false
    private var isSlientHintShown = false
    private var mRandom: Random? = null
    protected var soundPool: SoundPool? = null
    private val waitingList = ConcurrentLinkedQueue<Spark>()

    private var mVolume: Int = 0
    private var explosionSoundId = 0
    private var isAudioResourceReady = true

    private var mostRecentHintIssuedTime = 0L

    internal var time: Long = 0
    internal var lastFireTime: Long = 0

    private var inDuckMode = false

    constructor(context: Context) : super(context) {
        initDpi(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initDpi(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initDpi(context)
    }

    @TargetApi(21)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        initDpi(context)
    }

    private fun initDpi(context: Context) {
        val resources = context.resources
        val metrics = resources.displayMetrics
        densityDpi = metrics.densityDpi.toFloat()
    }

    private lateinit var audioManager: AudioManager

     fun init() {
        //add the sparks
        dpToMeterRatio = pixelToDp(height.toFloat()) / sceneHeight
        pixelMeterRatio = height / sceneHeight
        sceneWidth = pixelToDp(width.toFloat()) / dpToMeterRatio //dynamically calculate the width in meters
        sceneWidthHalf = sceneWidth / 2
        sceneHeightHalf = sceneHeight / 2
        mRandom = Random()

        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        if (Build.VERSION.SDK_INT >= 21) {
            val builder = SoundPool.Builder()
            builder.setMaxStreams(5)
            val attributeBuilder = AudioAttributes.Builder()
            attributeBuilder.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            attributeBuilder.setUsage(AudioAttributes.USAGE_GAME)
            attributeBuilder.setLegacyStreamType(AudioManager.STREAM_MUSIC)
            attributeBuilder.setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
            builder.setAudioAttributes(attributeBuilder.build())
            soundPool = builder.build()
        } else {
            soundPool = SoundPool(5, AudioManager.STREAM_MUSIC, 0)
        }


        soundPool!!.setOnLoadCompleteListener { soundPool, sampleId, status ->
            isAudioResourceReady = false
            playLoadedExplosionStream()
        }
    }

     fun addSpark(base: SparkBase) {
        sparks.add(base)
    }

     fun randomFire() {
        val time = System.currentTimeMillis() - lastFireTime
        //implement basic weak boundary check
        if (time > 100) {
            val x = (-mRandom!!.nextFloat() * sceneWidth * .5f + sceneWidthHalf) * .2f
            Log.i("Spark","Random${mRandom!!.nextFloat()} sw: $sceneWidth, swf:$sceneHeightHalf ")

            val y = -mRandom!!.nextFloat() * 30
            val z = -mRandom!!.nextFloat() * sceneDepth / 4 - sceneDepth / 2
            val pos = Point3f(0.01f, y, z)
            Log.i("Spark","x:$x,y$y,z$z")

            //the vertical speed cannot be faster than the frame rate
            val v = Vector3f(0f, 6f, 0f)

            val random = time % 5

            when (random) {
                1L -> waitingList.add(Spark(pos, v))
                2L -> waitingList.add(GroupSpark(pos, v))
                3L -> waitingList.add(BallSpark(pos, v))
                4L -> waitingList.add(BallSpark(pos, v))
                else -> waitingList.add(GroupSpark(pos, v))
            }

            lastFireTime = System.currentTimeMillis()
        }
    }

     fun stop() {
        isShowOngoing = false
        if (soundPool != null) {
            soundPool!!.autoPause()
            isAudioResourceReady = false
        }

        if (audioManager != null) {
            //remove the callbacks
            audioManager.abandonAudioFocus(this)
            isFocusAcquired = false
        }
    }

     fun play() {
        time = System.currentTimeMillis()
        isShowOngoing = true

        // Request audio focus for playback
        val result = audioManager.requestAudioFocus(
            this,
            // Use the music stream.
            AudioManager.STREAM_MUSIC,
            // Request permanent focus.
            AudioManager.AUDIOFOCUS_GAIN
        )

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            isFocusAcquired = true
            Log.d("fix", "get audio focus:$result")
        } else {
            Log.d("fix", "cannot get audio focus:$result")
        }

        object : Thread() {
            override fun run() {
                while (isShowOngoing) {
                    val screenHeight = height
                    val newTime = System.currentTimeMillis()
                    val timeDelta = newTime - time
                    val canvas = holder.lockCanvas()
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                    for (s in sparks) {
                        if (s.isExploding) {
                            recycleList.add(s)
                        } else {
                            PhysicsEngine.move(s, timeDelta)
                            //convert 3D to 2D
                            val scale = 1.5f * sceneDepth / (sceneDepth + s.mPosition.z)
                            val x2d = s.mPosition.x * scale + sceneWidthHalf
                            val y2d = s.mPosition.y * scale + sceneHeightHalf
                            s.draw(
                                canvas,
                                (x2d * pixelMeterRatio).toInt().toFloat(),
                                (screenHeight - (y2d * pixelMeterRatio).toInt()).toFloat(),
                                scale,
                                true
                            )
                        }
                    }
                    sparks.removeAll(recycleList)
                    for (s in recycleList) {
                        s.onExplosion(this@NightScene)
                    }
                    recycleList.clear()
                    if (sparks.size > 0) {
                        //do nothing
                        mostRecentHintIssuedTime = newTime
                    } else {
                        //randomFire();
                        try {
                            //60fps if possible
                            Thread.sleep(16)
                        } catch (e: Exception) {
                            //DO NOTHING
                        }

                       /* if (newTime - mostRecentHintIssuedTime > 5000L) {
                            showClappingHint(newTime)
                        }*/
                    }
                    //randomFire();
                    time = newTime
                    holder.unlockCanvasAndPost(canvas)
                    while (waitingList.size > 0) {
                        //remove the item
                        sparks.add(waitingList.poll())
                    }
                }
            }
        }.start()
    }

    /**
     * This is usually called via the background thread to play the sound effect
     */
     fun playExplosionSound() {
        if (!isFocusAcquired) {
            return
        }

        if (!isAudioResourceReady || explosionSoundId == 0) {
            Task.call(Callable<Void> {
                mVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                if (mVolume <= 0) {
                    if (!isSlientHintShown) {
                        Toast.makeText(context, R.string.open_audio_hint, Toast.LENGTH_SHORT).show()
                        isSlientHintShown = true
                    }
                    return@Callable null // do nothing to save the memory
                }
                explosionSoundId = soundPool!!.load(context, R.raw.explosion_fuzz, 1)
                null
            }, Task.BACKGROUND_EXECUTOR)
        } else {
            Task.call(Callable<Void> {
                //play the sound effect directly
                playLoadedExplosionStream()
                null
            }, Task.UI_THREAD_EXECUTOR)
        }
    }

    private fun playLoadedExplosionStream() {
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        var normalized = mVolume.toFloat() / maxVolume.toFloat()
        if (inDuckMode) {
            normalized = normalized / 2
        }
        soundPool!!.play(explosionSoundId, normalized, normalized, 1, 0, 1.2f)
    }

    protected fun onDestroy() {
        if (soundPool != null) {
            soundPool!!.release()
        }
    }

    private fun showClappingHint(newTime: Long) {
        Task.call(Callable<Void> {
            Toast.makeText(context, R.string.make_some_noise, Toast.LENGTH_SHORT).show()

            null
        }, Task.UI_THREAD_EXECUTOR)
        mostRecentHintIssuedTime = newTime
    }

    override fun onAudioFocusChange(focusChange: Int) {
        Log.d("fix", "onAudioFocusChange:$focusChange")
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            //lower the volume
            inDuckMode = true
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            isFocusAcquired = false
            inDuckMode = false
        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            isFocusAcquired = true
            inDuckMode = false
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            //no way to get the focus back, don't set the flag as I may just request the focus successfully
        } else {
            isFocusAcquired = false
            inDuckMode = false
        }
    }

    private fun pixelToDp(px: Float): Float {
        return px / (densityDpi / DisplayMetrics.DENSITY_DEFAULT)
    }
}
