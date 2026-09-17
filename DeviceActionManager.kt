package com.example.jarvis.service

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import com.example.jarvis.model.BatteryInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DeviceActionManager(private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private var torchCameraId: String? = null

    private val _torchState = MutableStateFlow(false)
    val torchState: StateFlow<Boolean> = _torchState.asStateFlow()

    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
            super.onTorchModeChanged(cameraId, enabled)
            if (cameraId == torchCameraId) {
                _torchState.value = enabled
            }
        }

        override fun onTorchModeUnavailable(cameraId: String) {
            super.onTorchModeUnavailable(cameraId)
            if (cameraId == torchCameraId) {
                _torchState.value = false
            }
        }
    }

    init {
        initTorch()
    }

    private fun initTorch() {
        try {
            cameraManager?.let { cm ->
                for (id in cm.cameraIdList) {
                    val characteristics = cm.getCameraCharacteristics(id)
                    val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                    val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                    if (hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK) {
                        torchCameraId = id
                        break
                    }
                }
                if (torchCameraId == null && cm.cameraIdList.isNotEmpty()) {
                    torchCameraId = cm.cameraIdList[0]
                }
                cm.registerTorchCallback(torchCallback, null)
            }
        } catch (_: Exception) {
            // Torch may not be available on emulators or restricted devices
        }
    }

    fun setTorchMode(enable: Boolean): Result<Boolean> {
        val id = torchCameraId
            ?: return Result.failure(IllegalStateException("Flashlight hardware not available"))

        return try {
            cameraManager?.setTorchMode(id, enable)
            _torchState.value = enable
            Result.success(enable)
        } catch (e: CameraAccessException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun toggleTorch(): Result<Boolean> {
        val newState = !_torchState.value
        return setTorchMode(newState)
    }

    fun getBatteryInfo(): BatteryInfo {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, intentFilter)

        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val percentage = if (level >= 0 && scale > 0) (level * 100) / scale else -1

        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val chargePlug = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val powerSource = when (chargePlug) {
            BatteryManager.BATTERY_PLUGGED_USB -> "USB Port"
            BatteryManager.BATTERY_PLUGGED_AC -> "AC Wall Charger"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Dock"
            else -> if (isCharging) "Connected" else "Battery Powered"
        }

        val tempTenths = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val temperatureCelsius = tempTenths / 10f

        val healthRaw = batteryStatus?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
            ?: BatteryManager.BATTERY_HEALTH_UNKNOWN
        val health = when (healthRaw) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Optimal"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheated"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Depleted"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            else -> "Normal"
        }

        return BatteryInfo(
            percentage = percentage,
            isCharging = isCharging,
            powerSource = powerSource,
            temperatureCelsius = temperatureCelsius,
            health = health
        )
    }

    fun launchCamera(): Result<String> {
        return try {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (cameraIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(cameraIntent)
                Result.success("Camera viewfinder activated")
            } else {
                // Fallback to launcher intent
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_APP_GALLERY)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                Result.success("Imaging interface opened")
            }
        } catch (e: Exception) {
            // Direct launch camera general intent
            try {
                val fallback = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallback)
                Result.success("Camera launched")
            } catch (e2: Exception) {
                Result.failure(e2)
            }
        }
    }

    fun openApp(appNameOrPackage: String): Result<String> {
        val query = appNameOrPackage.trim().lowercase()
        val pm = context.packageManager

        // 1. Direct package match or known mapping
        val knownPackages = mapOf(
            "facebook" to Pair("com.facebook.katana", "https://m.facebook.com"),
            "fb" to Pair("com.facebook.katana", "https://m.facebook.com"),
            "messenger" to Pair("com.facebook.orca", "https://messenger.com"),
            "whatsapp" to Pair("com.whatsapp", "https://web.whatsapp.com"),
            "wa" to Pair("com.whatsapp", "https://web.whatsapp.com"),
            "youtube" to Pair("com.google.android.youtube", "https://youtube.com"),
            "yt" to Pair("com.google.android.youtube", "https://youtube.com"),
            "chrome" to Pair("com.android.chrome", "https://google.com"),
            "browser" to Pair("com.android.chrome", "https://google.com"),
            "internet" to Pair("com.android.chrome", "https://google.com"),
            "maps" to Pair("com.google.android.apps.maps", "https://maps.google.com"),
            "gmail" to Pair("com.google.android.gm", "https://mail.google.com"),
            "play store" to Pair("com.android.vending", "https://play.google.com"),
            "playstore" to Pair("com.android.vending", "https://play.google.com"),
            "calculator" to Pair("com.google.android.calculator", null),
            "clock" to Pair("com.google.android.deskclock", null)
        )

        for ((key, pair) in knownPackages) {
            if (query.contains(key)) {
                val (pkg, fallbackUrl) = pair
                val launchIntent = pm.getLaunchIntentForPackage(pkg)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    return Result.success("Launching $key application")
                } else if (fallbackUrl != null) {
                    // Open web fallback
                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(webIntent)
                    return Result.success("Opening $key via secure browser link")
                }
            }
        }

        // 2. Dynamic search across all installed launcher apps
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
        for (info in resolveInfos) {
            val label = info.loadLabel(pm).toString().lowercase()
            val packageName = info.activityInfo.packageName.lowercase()

            if (label.contains(query) || query.contains(label) || packageName.contains(query)) {
                val launchIntent = pm.getLaunchIntentForPackage(info.activityInfo.packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    return Result.success("Launching ${info.loadLabel(pm)}")
                }
            }
        }

        return Result.failure(Exception("Application '$appNameOrPackage' not found on system"))
    }

    fun openWifiSettings(): Result<String> {
        return try {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Result.success("Opening Wi-Fi configuration")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun openBluetoothSettings(): Result<String> {
        return try {
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Result.success("Opening Bluetooth configuration")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun openSystemSettings(): Result<String> {
        return try {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Result.success("Accessing system control panel")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun cleanup() {
        try {
            cameraManager?.unregisterTorchCallback(torchCallback)
        } catch (_: Exception) {
        }
    }
}
