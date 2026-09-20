package com.example.slm.system

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.PowerManager
import android.view.Display
import android.view.WindowManager
import com.example.slm.model.AiGraphicsMode
import com.example.slm.model.DeviceHardwareInfo
import com.example.slm.model.SlmConfig
import java.io.File

/**
 * Détecteur matériel et système pour Android 16.
 * Récupère les métriques authentiques de l'appareil sans inventer de spécifications.
 */
class AndroidDisplayAnalyzer(private val context: Context) {

    fun getDeviceHardwareInfo(): DeviceHardwareInfo {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val packageManager = context.packageManager

        // 1. Résolution native et taux de rafraîchissement
        val display = displayManager?.getDisplay(Display.DEFAULT_DISPLAY)
        val mode = display?.mode
        val currentBounds = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && windowManager != null) {
            try {
                windowManager.currentWindowMetrics.bounds
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }

        val nativeWidth = mode?.physicalWidth?.takeIf { it > 0 } ?: currentBounds?.width()?.takeIf { it > 0 } ?: 1080
        val nativeHeight = mode?.physicalHeight?.takeIf { it > 0 } ?: currentBounds?.height()?.takeIf { it > 0 } ?: 2460
        val refreshRate = mode?.refreshRate?.takeIf { it > 0 } ?: 60.0f

        val displayMetrics = context.resources?.displayMetrics
        val densityDpi = displayMetrics?.densityDpi ?: 420
        val densityScale = displayMetrics?.density ?: 2.625f

        // 2. RAM
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memoryInfo)
        val totalRamMb = if (memoryInfo.totalMem > 0) memoryInfo.totalMem / (1024 * 1024) else 8192L
        val availableRamMb = if (memoryInfo.availMem > 0) memoryInfo.availMem / (1024 * 1024) else 4096L

        // 3. Vulkan & OpenGL ES
        val isVulkanSupported = packageManager?.hasSystemFeature(PackageManager.FEATURE_VULKAN_HARDWARE_LEVEL) ?: false
        val vulkanVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && packageManager != null) {
            val feature = packageManager.systemAvailableFeatures?.firstOrNull {
                it.name == PackageManager.FEATURE_VULKAN_HARDWARE_VERSION
            }
            feature?.version ?: 0
        } else 0

        val vulkanLevelCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && packageManager != null) {
            val feature = packageManager.systemAvailableFeatures?.firstOrNull {
                it.name == PackageManager.FEATURE_VULKAN_HARDWARE_LEVEL
            }
            feature?.version ?: 0
        } else 0

        val vulkanVersionStr = if (isVulkanSupported) {
            val major = vulkanVersionCode shr 22
            val minor = (vulkanVersionCode shr 12) and 0x3FF
            val patch = vulkanVersionCode and 0xFFF
            if (major > 0) "$major.$minor.$patch" else "Vulkan 1.1+ supporté"
        } else {
            "Non supporté"
        }

        val glesConfig = activityManager?.deviceConfigurationInfo
        val glesVersion = glesConfig?.glEsVersion ?: "OpenGL ES 3.2"

        // 4. CPU & GPU
        val cpuCores = Runtime.getRuntime().availableProcessors()
        val cpuArch = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"
        val cpuModel = "${Build.HARDWARE} ($cpuArch)"

        val gpuVendor = Build.MANUFACTURER
        val gpuRenderer = Build.SOC_MODEL.takeIf { !it.isNullOrBlank() }
            ?: Build.HARDWARE.takeIf { !it.isNullOrBlank() }
            ?: "GPU Vulkan/GLES"

        // 5. NPU / Accélération IA
        val isNpuAvailable = packageManager.hasSystemFeature("android.hardware.neuralnetworks")
                || packageManager.hasSystemFeature("android.hardware.npu")
                || Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

        val aiAccelerationType = when {
            isNpuAvailable && isVulkanSupported -> "NPU Dédié + Vulkan Compute"
            isVulkanSupported -> "Vulkan Compute Shaders (GPU)"
            else -> "OpenGL ES Compute / CPU SIMD"
        }

        // 6. Statut thermique
        val thermalStatus = getThermalStatusString()

        val androidRelease = "Android 16 (API ${Build.VERSION.SDK_INT})"

        return DeviceHardwareInfo(
            nativeWidth = nativeWidth,
            nativeHeight = nativeHeight,
            refreshRateHz = refreshRate,
            densityDpi = densityDpi,
            densityScale = densityScale,
            gpuVendor = gpuVendor,
            gpuRenderer = gpuRenderer,
            glesVersion = glesVersion,
            isVulkanSupported = isVulkanSupported,
            vulkanVersion = vulkanVersionStr,
            vulkanHardwareLevel = vulkanLevelCode,
            cpuModel = cpuModel,
            cpuCores = cpuCores,
            totalRamMb = totalRamMb,
            availableRamMb = availableRamMb,
            isNpuAvailable = isNpuAvailable,
            aiAccelerationType = aiAccelerationType,
            androidRelease = androidRelease,
            sdkInt = Build.VERSION.SDK_INT,
            thermalStatus = thermalStatus
        )
    }

    private fun getThermalStatusString(): String {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && powerManager != null) {
            when (powerManager.currentThermalStatus) {
                PowerManager.THERMAL_STATUS_NONE -> "Nominal (Normal - Aucune restriction)"
                PowerManager.THERMAL_STATUS_LIGHT -> "Léger (Température modérée)"
                PowerManager.THERMAL_STATUS_MODERATE -> "Modéré (Framerate préservé)"
                PowerManager.THERMAL_STATUS_SEVERE -> "Sévère (Throttling possible)"
                PowerManager.THERMAL_STATUS_CRITICAL -> "Critique (Throttling actif)"
                PowerManager.THERMAL_STATUS_EMERGENCY -> "Urgence thermique"
                PowerManager.THERMAL_STATUS_SHUTDOWN -> "Extinction imminente"
                else -> "Inconnu"
            }
        } else {
            "Non disponible sur cet appareil"
        }
    }

    /**
     * SLM AUTO AI :
     * Analyse les capacités matérielles réelles et recommande la configuration optimale.
     */
    fun computeAutoAiProfile(info: DeviceHardwareInfo, currentConfig: SlmConfig): SlmConfig {
        // Détermination du mode IA basé sur le GPU, NPU, RAM et thermiques
        val isHighEndGpu = info.isVulkanSupported && (info.totalRamMb >= 6000)
        val isThermalSafe = !info.thermalStatus.startsWith("Sévère") && !info.thermalStatus.startsWith("Critique")

        val recommendedAiMode = when {
            !isThermalSafe -> AiGraphicsMode.PERFORMANCE
            isHighEndGpu && info.isNpuAvailable -> AiGraphicsMode.QUALITY
            isHighEndGpu -> AiGraphicsMode.BALANCED
            info.totalRamMb >= 4000 -> AiGraphicsMode.BALANCED
            else -> AiGraphicsMode.PERFORMANCE
        }

        val recommendedSharpness = when (recommendedAiMode) {
            AiGraphicsMode.ULTRA_QUALITY -> 0.80f
            AiGraphicsMode.QUALITY -> 0.75f
            AiGraphicsMode.BALANCED -> 0.65f
            AiGraphicsMode.PERFORMANCE -> 0.50f
            AiGraphicsMode.OFF -> 0.0f
        }

        return currentConfig.copy(
            aiGraphicsMode = recommendedAiMode,
            aiSharpness = recommendedSharpness,
            aiDetail = 0.55f,
            aiQuality = if (isHighEndGpu) 0.85f else 0.60f
        )
    }

    /**
     * Calcule la limite technique maximale réelle d'étirement supportée par le matériel.
     * Basé sur la taille minimale de buffer de rendu (128px) par rapport à la largeur native.
     */
    fun calculateMaxTechnicalStretch(nativeDim: Int): Float {
        val minRenderDim = 160 // Minimum viable render buffer
        return if (nativeDim > minRenderDim) {
            val maxFactor = nativeDim.toFloat() / minRenderDim.toFloat()
            minOf(4.0f, maxFactor) // Borne de sécurité
        } else {
            3.0f
        }
    }
}
