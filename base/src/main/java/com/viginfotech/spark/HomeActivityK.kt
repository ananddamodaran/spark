package com.viginfotech.spark

import android.os.Bundle
import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity
import com.google.android.instantapps.InstantApps
import com.google.firebase.analytics.FirebaseAnalytics
import com.viginfotech.spark.base.R
import com.viginfotech.spark.engine.NightScene
import kotlinx.android.synthetic.main.activity_home_k.*

class HomeActivityK : AppCompatActivity() {

    private lateinit var mFirebaseAnalytics: FirebaseAnalytics

    private var status: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_k)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)

        // Determine the current app context, either installed or instant, then
        // set the corresponding user property for Google Analytics.
        status = if (InstantApps.isInstantApp(this)) {
            getString(R.string.status_instant)
        } else {
            getString(R.string.status_installed)
        }
        mFirebaseAnalytics.setUserProperty(getString(R.string.analytics_user_prop),
            status)

        nightScene = findViewById(R.id.night_scene)
        mSurfaceHolder = nightScene.holder
        mSurfaceHolder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                nightScene.init()
                nightScene.play()
                isSurfaceCreated = true


            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                isSurfaceCreated = false
                nightScene.stop()
            }
        })

        spark.setOnClickListener{
            nightScene.randomFire()
        }

    }

    private lateinit var nightScene: NightScene

    private lateinit var mSurfaceHolder: SurfaceHolder

    private var isSurfaceCreated: Boolean =false


    override fun onPause() {
        super.onPause()
        nightScene.stop()
    }
    override fun onDestroy() {
        super.onDestroy()
        nightScene.stop()
    }

    override fun onResume() {
        super.onResume()
        if (isSurfaceCreated) {
            nightScene.postDelayed({ nightScene.play() }, 20)
        }

    }




}
