package com.example.focus

import android.app.ActivityManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import androidx.core.net.toUri

class MainActivity : FlutterActivity() {

    private companion object {
        private const val CHANNEL = "focus.study.mode"
        private const val TAG = "MainActivity"
        private const val SERVICE_CLASS = "com.example.focus.AppBlockService" // For flexibility
    }

    private val activityManager by lazy { getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager }
    private val usageStatsManager by lazy { getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            try {
                when (call.method) {
                    "toggleStudyMode" -> {
                        val isOn = call.argument<Boolean>("on") ?: false
                        val serviceIntent = Intent(this, Class.forName(SERVICE_CLASS))
                        val isServiceRunning = isServiceRunning(AppBlockService::class.java.name)

                        if (isOn) {
                            if (!isServiceRunning) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    startForegroundService(serviceIntent)
                                } else {
                                    startService(serviceIntent)
                                }
                                Log.d(TAG, "Study mode started")
                                result.success("started")
                            } else {
                                Log.d(TAG, "Study mode already running")
                                result.success("already_running")
                            }
                        } else {
                            if (isServiceRunning) {
                                stopService(serviceIntent)
                                Log.d(TAG, "Study mode stopped")
                                result.success("stopped")
                            } else {
                                Log.d(TAG, "Study mode not running")
                                result.success("already_stopped")
                            }
                        }
                    }

                    "requestUsageAccess" -> {
                        // OPTIMIZED: Quick check if already granted (test query)
                        val now = System.currentTimeMillis()
                        val stats = usageStatsManager.queryUsageStats(
                            UsageStatsManager.INTERVAL_DAILY,
                            now - 1000, // 1s window
                            now
                        )
                        val isGranted = stats.isNotEmpty()

                        if (isGranted) {
                            Log.d(TAG, "Usage access already granted")
                            result.success("already_granted")
                        } else {
                            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                            startActivity(intent)
                            Log.d(TAG, "Opened usage access settings")
                            result.success("opened_settings")
                        }
                    }

                    "requestOverlayPermission" -> {
                        val isGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            Settings.canDrawOverlays(this)
                        } else {
                            true // Pre-M, no runtime check needed
                        }

                        if (isGranted) {
                            Log.d(TAG, "Overlay permission already granted")
                            result.success("already_granted")
                        } else {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:$packageName")
                            )
                            startActivity(intent)
                            Log.d(TAG, "Opened overlay permission settings")
                            result.success("opened_settings")
                        }
                    }

                    else -> result.notImplemented()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Method call error: ${e.message}", e)
                result.error("ERROR", e.message, null)
            }
        }
    }

    private fun isServiceRunning(serviceClassName: String): Boolean {
        for (service in activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClassName == service.service.className) {
                return true
            }
        }
        return false
    }
}
