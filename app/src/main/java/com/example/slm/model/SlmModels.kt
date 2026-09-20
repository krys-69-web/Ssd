package com.example.slm.model

/**
 * Modes de scaling d'affichage SLM
 */
enum class ScalingMode(val label: String, val description: String) {
    FULL_STRETCH(
        "Full Stretch",
        "Écrase/étire le rendu pour occuper 100% de la surface de l'écran natif."
    ),
    KEEP_ASPECT_RATIO(
        "Keep Aspect Ratio",
        "Conserve les proportions du rendu avec bandes noires (pillarbox/letterbox)."
    ),
    CENTERED(
        "Centered",
        "Mappage pixel à pixel (1:1) au centre de l'écran, sans interpolation."
    ),
    CUSTOM(
        "Custom",
        "Permet un étirement libre et un décalage personnalisé selon la configuration utilisateur."
    )
}

/**
 * Moteurs de scaling disponibles selon l'API Android et le GPU
 */
enum class ScalingEngine(val label: String, val technicalDetail: String) {
    AUTOMATIC(
        "Automatic",
        "Sélection dynamique : SurfaceView Hardware Composer si disponible, sinon GPU Texture sampling."
    ),
    GPU_SCALING(
        "GPU Scaling",
        "Interpolation et transformation matricielle exécutées dans le shader GPU (OpenGL ES / Vulkan)."
    ),
    DISPLAY_SCALING(
        "Display Scaling",
        "Mise à l'échelle matérielle par le Display Controller / Hardware Composer via SurfaceHolder.setFixedSize()."
    )
}

/**
 * Niveaux du moteur SLM AI Graphics
 */
enum class AiGraphicsMode(val label: String, val description: String, val passes: Int) {
    OFF(
        "OFF",
        "Désactivé : échantillonnage bilinéaire standard sans reconstruction.",
        0
    ),
    PERFORMANCE(
        "PERFORMANCE",
        "Priorité maximale aux FPS. Filtre de reconstruction spatial directionnel léger.",
        1
    ),
    BALANCED(
        "BALANCED",
        "Équilibre qualité/performances. Analyse tensorielle des gradients et reconstruction des contours.",
        2
    ),
    QUALITY(
        "QUALITY",
        "Priorité qualité d'image. Reconstruction fine des hautes fréquences et anti-aliasing adaptatif.",
        3
    ),
    ULTRA_QUALITY(
        "ULTRA QUALITY",
        "Fidélité maximale. Reconstruction multi-passes, détection sous-pixel et synthèse de détails.",
        4
    )
}

/**
 * Configuration complète de SLM Stretch
 */
data class SlmConfig(
    val renderWidth: Int = 720,
    val renderHeight: Int = 2460,
    val nativeWidth: Int = 1080,
    val nativeHeight: Int = 2460,
    val stretchFactorX: Float = 1.50f,
    val stretchFactorY: Float = 1.00f,
    val isFreeStretch: Boolean = false,
    val scalingMode: ScalingMode = ScalingMode.FULL_STRETCH,
    val scalingEngine: ScalingEngine = ScalingEngine.AUTOMATIC,
    val aiGraphicsMode: AiGraphicsMode = AiGraphicsMode.BALANCED,
    val aiSharpness: Float = 0.65f, // 0.0 to 1.0
    val aiDetail: Float = 0.50f,    // 0.0 to 1.0
    val aiQuality: Float = 0.75f    // 0.0 to 1.0
) {
    /**
     * Ratio d'étirement effectif calculé
     */
    val effectiveRatioX: Float
        get() = if (renderWidth > 0) nativeWidth.toFloat() / renderWidth.toFloat() else 1.0f

    val effectiveRatioY: Float
        get() = if (renderHeight > 0) nativeHeight.toFloat() / renderHeight.toFloat() else 1.0f
}

/**
 * Informations matérielles réelles de l'appareil détectées par le système
 */
data class DeviceHardwareInfo(
    val nativeWidth: Int,
    val nativeHeight: Int,
    val refreshRateHz: Float,
    val densityDpi: Int,
    val densityScale: Float,
    val gpuVendor: String,
    val gpuRenderer: String,
    val glesVersion: String,
    val isVulkanSupported: Boolean,
    val vulkanVersion: String,
    val vulkanHardwareLevel: Int,
    val cpuModel: String,
    val cpuCores: Int,
    val totalRamMb: Long,
    val availableRamMb: Long,
    val isNpuAvailable: Boolean,
    val aiAccelerationType: String,
    val androidRelease: String,
    val sdkInt: Int,
    val thermalStatus: String
)

/**
 * Métriques de performances en temps réel
 */
data class PerformanceMetrics(
    val fps: Float = 0f,
    val frameTimeMs: Float = 0f,
    val aiProcessingTimeMs: Float = 0f,
    val renderWidth: Int = 720,
    val renderHeight: Int = 2460,
    val nativeWidth: Int = 1080,
    val nativeHeight: Int = 2460,
    val scaleFactorX: Float = 1.5f,
    val scaleFactorY: Float = 1.0f,
    val gpuLoadString: String = "Non disponible sur cet appareil"
)
