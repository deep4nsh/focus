package com.example.focus

import android.app.*
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.app.NotificationCompat

class AppBlockService : Service() {

    private val TAG = "AppBlockService"


    private val blockedApps = setOf(
        "com.whatsapp",
        "com.instagram.android",
        "com.google.android.youtube" //can add more if required....
    )

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private val handler = Handler(Looper.getMainLooper())
    private val checkInterval: Long = 150L

    private val removalHandler = Handler(Looper.getMainLooper())
    private val overlayRemovalChecksRequired = 5
    private var overlayRemovalChecks = 0

    private val checkRunnable = object : Runnable {
        override fun run() {
            try {
                val currentApp = getForegroundAppPackage()
                Log.d(TAG, "Foreground app detected: $currentApp")
                if (currentApp != null && blockedApps.contains(currentApp)) {
                    overlayRemovalChecks = 0
                    if (overlayView == null) {
                        Log.d(TAG, "Blocked app detected - showing overlay")
                        showOverlay()
                    }
                } else {
                    if (overlayView != null) {
                        overlayRemovalChecks++
                        if (overlayRemovalChecks >= overlayRemovalChecksRequired) {
                            Log.d(TAG, "Removing overlay after $overlayRemovalChecks consecutive checks")
                            removeOverlay()
                            overlayRemovalChecks = 0
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in overlay loop: ${e.message}")
            } finally {
                handler.postDelayed(this, checkInterval)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Foreground service notification
        val notification = NotificationCompat.Builder(this, "focus_channel")
            .setContentTitle("Focus Mode Active")
            .setContentText("Blocking distracting apps")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setOngoing(true)
            .build()
        startForeground(1, notification)

        handler.post(checkRunnable)
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(checkRunnable)
        removeOverlay()
        super.onDestroy()
    }

    private fun getForegroundAppPackage(): String? {
        val usageStatsManager = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        val events = usageStatsManager.queryEvents(now - 3000, now)  // 3s window for better accuracy
        val event = UsageEvents.Event()
        var recentApp: String? = null
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                recentApp = event.packageName
            }
        }
        return recentApp
    }

    private fun showOverlay() {
        if (overlayView != null) return

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        }

        val textView = TextView(this).apply {
            text = "📚 Study Mode ACTIVE\nAccess blocked"
            textSize = 22f
            setBackgroundColor(0xAA000000.toInt())
            setTextColor(0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER
        }

        overlayView = textView
        overlayView!!.setOnTouchListener { _, event -> event.action == MotionEvent.ACTION_DOWN }

        windowManager.addView(overlayView, layoutParams)

        Log.d(TAG, "Overlay shown")
    }

    private fun removeOverlay() {
        overlayView?.let {
            try {
                windowManager.removeView(it)
                Log.d(TAG, "Overlay removed")
            } catch (e: Exception) {
                Log.w(TAG, "Error removing overlay: ${e.message}")
            }
            overlayView = null
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "focus_channel",
                "Focus Mode Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}