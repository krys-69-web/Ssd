package com.example.slm.ui

import android.app.Application
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.slm.data.SlmDatabase
import com.example.slm.data.SlmProfileEntity
import com.example.slm.data.SlmRepository
import com.example.slm.engine.SlmGraphicsEngine
import com.example.slm.model.AiGraphicsMode
import com.example.slm.model.DeviceHardwareInfo
import com.example.slm.model.PerformanceMetrics
import com.example.slm.model.ScalingEngine
import com.example.slm.model.ScalingMode
import com.example.slm.model.SlmConfig
import com.example.slm.system.AndroidDisplayAnalyzer
import com.example.slm.system.ShizukuExecutor
import com.example.slm.system.SystemResolutionExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku

data class DisplayNormPreset(
    val name: String,
    val width: Int,
    val height: Int,
    val description: String
)

class SlmViewModel(application: Application) : AndroidViewModel(application) {

    private val analyzer = AndroidDisplayAnalyzer(application)
    val deviceInfo: DeviceHardwareInfo = analyzer.getDeviceHardwareInfo()

    val maxStretchX: Float = analyzer.calculateMaxTechnicalStretch(deviceInfo.nativeWidth)
    val maxStretchY: Float = analyzer.calculateMaxTechnicalStretch(deviceInfo.nativeHeight)

    // Normes standard prédéfinies
    val standardNormPresets = listOf(
        DisplayNormPreset("4:3 ÉTIREMENT", 1080, 1440, "Modèles 3D plus larges (Format compétitif eSports)"),
        DisplayNormPreset("16:10 STRETCH", 1080, 1728, "Étirement modéré sans déformation excessive"),
        DisplayNormPreset("HD+ ÉCO (720p)", 720, (deviceInfo.nativeHeight * 720) / deviceInfo.nativeWidth, "Économise 33% de GPU & hausse les FPS"),
        DisplayNormPreset("ULTRA STRETCH", (deviceInfo.nativeWidth / 1.50f).toInt(), deviceInfo.nativeHeight, "Étirement maximal horizontal"),
        DisplayNormPreset("NATIF 1:1", deviceInfo.nativeWidth, deviceInfo.nativeHeight, "Norme physique d'origine sans étirement")
    )

    private val database = SlmDatabase.getDatabase(application, viewModelScope)
    private val repository = SlmRepository(database.profileDao())

    val allProfiles: StateFlow<List<SlmProfileEntity>> = repository.getAllProfiles()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Mode de contrôle : 0 = BARRES (SLIDERS), 1 = ÉCRIRE LES NORMES (SAISIE DIRECTE)
    private val _inputMode = MutableStateFlow(0)
    val inputMode: StateFlow<Int> = _inputMode.asStateFlow()

    // Champs de saisie directe pour écrire les normes
    private val _widthInputText = MutableStateFlow("720")
    val widthInputText: StateFlow<String> = _widthInputText.asStateFlow()

    private val _heightInputText = MutableStateFlow(deviceInfo.nativeHeight.toString())
    val heightInputText: StateFlow<String> = _heightInputText.asStateFlow()

    private val _dpiInputText = MutableStateFlow(((deviceInfo.densityDpi * 720) / deviceInfo.nativeWidth).toString())
    val dpiInputText: StateFlow<String> = _dpiInputText.asStateFlow()

    // État d'exécution directe système (sans terminal)
    private val _systemExecutionResult = MutableStateFlow<SystemResolutionExecutor.ExecutionResult?>(null)
    val systemExecutionResult: StateFlow<SystemResolutionExecutor.ExecutionResult?> = _systemExecutionResult.asStateFlow()

    private val _isExecutingDirectly = MutableStateFlow(false)
    val isExecutingDirectly: StateFlow<Boolean> = _isExecutingDirectly.asStateFlow()

    // État de la permission système WRITE_SECURE_SETTINGS
    private val _isPermissionGranted = MutableStateFlow(
        SystemResolutionExecutor.hasWriteSecureSettings(application) || ShizukuExecutor.hasShizukuPermission()
    )
    val isPermissionGranted: StateFlow<Boolean> = _isPermissionGranted.asStateFlow()

    // États spécifiques Shizuku
    private val _isShizukuRunning = MutableStateFlow(ShizukuExecutor.isShizukuInstalledAndRunning())
    val isShizukuRunning: StateFlow<Boolean> = _isShizukuRunning.asStateFlow()

    private val _isShizukuPermissionGranted = MutableStateFlow(ShizukuExecutor.hasShizukuPermission())
    val isShizukuPermissionGranted: StateFlow<Boolean> = _isShizukuPermissionGranted.asStateFlow()

    private val shizukuPermissionListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == ShizukuExecutor.SHIZUKU_REQUEST_CODE) {
            val granted = grantResult == PackageManager.PERMISSION_GRANTED
            _isShizukuPermissionGranted.value = granted
            if (granted) {
                // Octroi automatique immédiat de WRITE_SECURE_SETTINGS en arrière-plan
                viewModelScope.launch(Dispatchers.IO) {
                    val ok = ShizukuExecutor.grantWriteSecureSettings(getApplication<Application>().packageName)
                    withContext(Dispatchers.Main) {
                        refreshPermissionStatus()
                        _statusMessage.value = "Succès : Autorisation Shizuku validée ! Écran déverrouillé."
                    }
                }
            } else {
                _statusMessage.value = "Autorisation Shizuku refusée par l'utilisateur."
            }
        }
    }

    init {
        ShizukuExecutor.registerPermissionListener(shizukuPermissionListener)
    }

    override fun onCleared() {
        super.onCleared()
        ShizukuExecutor.unregisterPermissionListener(shizukuPermissionListener)
    }

    fun checkShizukuStatus() {
        _isShizukuRunning.value = ShizukuExecutor.isShizukuInstalledAndRunning()
        _isShizukuPermissionGranted.value = ShizukuExecutor.hasShizukuPermission()
    }

    /**
     * Option "Autoriser Shizuku" 100% sans PC :
     * Déclenche la popup native d'autorisation Shizuku ou applique directement les privilèges.
     */
    fun authorizeShizukuDirectly() {
        checkShizukuStatus()
        if (!_isShizukuRunning.value) {
            _statusMessage.value = "Shizuku n'est pas encore démarré. Lancez l'application Shizuku."
            return
        }

        if (_isShizukuPermissionGranted.value) {
            viewModelScope.launch(Dispatchers.IO) {
                val ok = ShizukuExecutor.grantWriteSecureSettings(getApplication<Application>().packageName)
                withContext(Dispatchers.Main) {
                    refreshPermissionStatus()
                    _statusMessage.value = if (ok || _isPermissionGranted.value) {
                        "Succès : WRITE_SECURE_SETTINGS accordé via Shizuku !"
                    } else {
                        "Shizuku actif et prêt pour l'application d'écran"
                    }
                }
            }
        } else {
            ShizukuExecutor.requestShizukuPermission()
            _statusMessage.value = "Demande d'autorisation Shizuku envoyée..."
        }
    }

    // Affichage de la boîte de dialogue du guide d'autorisation
    private val _showPermissionGuideDialog = MutableStateFlow(false)
    val showPermissionGuideDialog: StateFlow<Boolean> = _showPermissionGuideDialog.asStateFlow()

    fun refreshPermissionStatus() {
        checkShizukuStatus()
        val hasSecure = SystemResolutionExecutor.hasWriteSecureSettings(getApplication())
        val hasShizuku = ShizukuExecutor.hasShizukuPermission()
        _isPermissionGranted.value = hasSecure || hasShizuku
    }

    fun openPermissionGuide() {
        _showPermissionGuideDialog.value = true
    }

    fun closePermissionGuide() {
        _showPermissionGuideDialog.value = false
    }

    // Compte à rebours de sécurité (15s) pour confirmer la nouvelle résolution
    private val _safetyCountdown = MutableStateFlow<Int?>(null)
    val safetyCountdown: StateFlow<Int?> = _safetyCountdown.asStateFlow()
    private var countdownJob: Job? = null

    // Configuration en cours de modification
    private val _currentConfig = MutableStateFlow(
        SlmConfig(
            renderWidth = 720,
            renderHeight = deviceInfo.nativeHeight,
            nativeWidth = deviceInfo.nativeWidth,
            nativeHeight = deviceInfo.nativeHeight,
            stretchFactorX = 1.50f,
            stretchFactorY = 1.00f,
            isFreeStretch = false,
            scalingMode = ScalingMode.FULL_STRETCH,
            scalingEngine = ScalingEngine.AUTOMATIC,
            aiGraphicsMode = AiGraphicsMode.PERFORMANCE,
            aiSharpness = 0.70f,
            aiDetail = 0.50f,
            aiQuality = 0.75f
        )
    )
    val currentConfig: StateFlow<SlmConfig> = _currentConfig.asStateFlow()

    // Configuration confirmée / appliquée
    private val _appliedConfig = MutableStateFlow(_currentConfig.value)
    val appliedConfig: StateFlow<SlmConfig> = _appliedConfig.asStateFlow()

    // Onglet sélectionné (0: Contrôles, 1: Test, 2: Device, 3: Profils, 4: Architecture)
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Mode de vue du banc d'essai
    private val _testViewMode = MutableStateFlow(SlmGraphicsEngine.TestViewMode.SPLIT_COMPARISON)
    val testViewMode: StateFlow<SlmGraphicsEngine.TestViewMode> = _testViewMode.asStateFlow()

    // Position du curseur de comparaison split-screen (0..1)
    private val _splitPosition = MutableStateFlow(0.5f)
    val splitPosition: StateFlow<Float> = _splitPosition.asStateFlow()

    // Métriques temps réel de performance
    private val _performanceMetrics = MutableStateFlow(
        PerformanceMetrics(
            fps = 60f,
            frameTimeMs = 16.6f,
            aiProcessingTimeMs = 2.4f,
            renderWidth = 720,
            renderHeight = deviceInfo.nativeHeight,
            nativeWidth = deviceInfo.nativeWidth,
            nativeHeight = deviceInfo.nativeHeight,
            scaleFactorX = 1.5f,
            scaleFactorY = 1.0f
        )
    )
    val performanceMetrics: StateFlow<PerformanceMetrics> = _performanceMetrics.asStateFlow()

    // Message d'état temporaire (ex: "Configuration appliquée avec succès")
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun selectTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun setTestViewMode(mode: SlmGraphicsEngine.TestViewMode) {
        _testViewMode.value = mode
    }

    fun setSplitPosition(pos: Float) {
        _splitPosition.value = pos.coerceIn(0.05f, 0.95f)
    }

    fun setStretchFactorX(factor: Float) {
        val clamped = factor.coerceIn(1.00f, maxStretchX)
        val newRenderWidth = (deviceInfo.nativeWidth / clamped).toInt().coerceAtLeast(160)
        _currentConfig.value = _currentConfig.value.copy(
            stretchFactorX = clamped,
            renderWidth = newRenderWidth
        )
    }

    fun setStretchFactorY(factor: Float) {
        val clamped = factor.coerceIn(1.00f, maxStretchY)
        val newRenderHeight = (deviceInfo.nativeHeight / clamped).toInt().coerceAtLeast(160)
        _currentConfig.value = _currentConfig.value.copy(
            stretchFactorY = clamped,
            renderHeight = newRenderHeight
        )
    }

    fun setRenderResolution(width: Int, height: Int) {
        val safeW = width.coerceIn(160, deviceInfo.nativeWidth)
        val safeH = height.coerceIn(160, deviceInfo.nativeHeight)
        val calcFactorX = deviceInfo.nativeWidth.toFloat() / safeW.toFloat()
        val calcFactorY = deviceInfo.nativeHeight.toFloat() / safeH.toFloat()

        _currentConfig.value = _currentConfig.value.copy(
            renderWidth = safeW,
            renderHeight = safeH,
            stretchFactorX = calcFactorX,
            stretchFactorY = calcFactorY
        )
    }

    fun toggleFreeStretch(enabled: Boolean) {
        _currentConfig.value = _currentConfig.value.copy(
            isFreeStretch = enabled,
            scalingMode = if (enabled) ScalingMode.CUSTOM else ScalingMode.FULL_STRETCH
        )
    }

    fun setScalingMode(mode: ScalingMode) {
        _currentConfig.value = _currentConfig.value.copy(scalingMode = mode)
    }

    fun setScalingEngine(engine: ScalingEngine) {
        _currentConfig.value = _currentConfig.value.copy(scalingEngine = engine)
    }

    fun setAiGraphicsMode(mode: AiGraphicsMode) {
        _currentConfig.value = _currentConfig.value.copy(aiGraphicsMode = mode)
    }

    fun setAiSharpness(sharpness: Float) {
        _currentConfig.value = _currentConfig.value.copy(aiSharpness = sharpness.coerceIn(0f, 1f))
    }

    fun setAiDetail(detail: Float) {
        _currentConfig.value = _currentConfig.value.copy(aiDetail = detail.coerceIn(0f, 1f))
    }

    fun setAiQuality(quality: Float) {
        _currentConfig.value = _currentConfig.value.copy(aiQuality = quality.coerceIn(0f, 1f))
    }

    /**
     * Applique la configuration actuelle au moteur de rendu
     */
    fun applyConfig() {
        val config = _currentConfig.value
        _appliedConfig.value = config
        repository.updateActiveConfig(config)
        _statusMessage.value = "Configuration appliquée : ${config.renderWidth}×${config.renderHeight} (X${"%.2f".format(config.stretchFactorX)} Y${"%.2f".format(config.stretchFactorY)})"
    }

    /**
     * Annule les modifications non appliquées et restaure la dernière configuration confirmée
     */
    fun resetToApplied() {
        _currentConfig.value = _appliedConfig.value
        _statusMessage.value = "Modifications réinitialisées"
    }

    /**
     * Rétablit la configuration standard par défaut
     */
    fun restoreDefaultConfig() {
        val defaultConfig = SlmConfig(
            renderWidth = 720,
            renderHeight = deviceInfo.nativeHeight,
            nativeWidth = deviceInfo.nativeWidth,
            nativeHeight = deviceInfo.nativeHeight,
            stretchFactorX = 1.50f,
            stretchFactorY = 1.00f,
            isFreeStretch = false,
            scalingMode = ScalingMode.FULL_STRETCH,
            scalingEngine = ScalingEngine.AUTOMATIC,
            aiGraphicsMode = AiGraphicsMode.BALANCED,
            aiSharpness = 0.65f,
            aiDetail = 0.50f,
            aiQuality = 0.75f
        )
        _currentConfig.value = defaultConfig
        _appliedConfig.value = defaultConfig
        repository.updateActiveConfig(defaultConfig)
        _statusMessage.value = "Paramètres par défaut restaurés"
    }

    /**
     * SLM AUTO AI : Analyse automatique et sélection du meilleur compromis
     */
    fun runAutoAiOptimization() {
        val optimized = analyzer.computeAutoAiProfile(deviceInfo, _currentConfig.value)
        _currentConfig.value = optimized
        _statusMessage.value = "SLM AUTO AI : Mode ${optimized.aiGraphicsMode.label} sélectionné selon votre GPU/RAM"
    }

    fun loadProfile(profile: SlmProfileEntity) {
        val config = profile.toConfig(deviceInfo.nativeWidth, deviceInfo.nativeHeight)
        _currentConfig.value = config
        _appliedConfig.value = config
        repository.updateActiveConfig(config)
        _statusMessage.value = "Profil '${profile.name}' chargé avec succès"
    }

    fun saveCurrentProfile(name: String, description: String) {
        viewModelScope.launch {
            repository.saveProfile(name, description, _currentConfig.value)
            _statusMessage.value = "Profil '$name' sauvegardé"
        }
    }

    fun deleteProfile(profile: SlmProfileEntity) {
        viewModelScope.launch {
            repository.deleteProfile(profile)
            _statusMessage.value = "Profil '${profile.name}' supprimé"
        }
    }

    fun restoreFactoryProfiles() {
        viewModelScope.launch {
            repository.restoreDefaultProfiles()
            restoreDefaultConfig()
            _statusMessage.value = "Profils d'usine restaurés"
        }
    }

    fun updateMetrics(fps: Float, frameTimeMs: Float, aiTimeMs: Float) {
        val cfg = _currentConfig.value
        _performanceMetrics.value = PerformanceMetrics(
            fps = fps,
            frameTimeMs = frameTimeMs,
            aiProcessingTimeMs = aiTimeMs,
            renderWidth = cfg.renderWidth,
            renderHeight = cfg.renderHeight,
            nativeWidth = cfg.nativeWidth,
            nativeHeight = cfg.nativeHeight,
            scaleFactorX = cfg.effectiveRatioX,
            scaleFactorY = cfg.effectiveRatioY,
            gpuLoadString = "Non disponible sur cet appareil"
        )
    }

    fun showNotification(message: String) {
        _statusMessage.value = message
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    // =========================================================================
    // MODES DE CONTRÔLE : BARRES (SLIDERS) OU ÉCRIRE LES NORMES
    // =========================================================================

    fun setInputMode(mode: Int) {
        _inputMode.value = mode
        if (mode == 1) {
            // Synchroniser les champs textes avec la configuration actuelle
            val cfg = _currentConfig.value
            _widthInputText.value = cfg.renderWidth.toString()
            _heightInputText.value = cfg.renderHeight.toString()
            val targetDpi = ((deviceInfo.densityDpi * cfg.renderWidth.toFloat()) / cfg.nativeWidth.toFloat()).toInt()
            _dpiInputText.value = targetDpi.toString()
        }
    }

    fun updateWidthInput(text: String) {
        _widthInputText.value = text
        val widthVal = text.toIntOrNull()
        if (widthVal != null && widthVal in 160..deviceInfo.nativeWidth) {
            val calcFactorX = deviceInfo.nativeWidth.toFloat() / widthVal.toFloat()
            _currentConfig.value = _currentConfig.value.copy(
                renderWidth = widthVal,
                stretchFactorX = calcFactorX
            )
            // Recalculer le DPI recommandé
            val newDpi = ((deviceInfo.densityDpi * widthVal.toFloat()) / deviceInfo.nativeWidth.toFloat()).toInt()
            _dpiInputText.value = newDpi.toString()
        }
    }

    fun updateHeightInput(text: String) {
        _heightInputText.value = text
        val heightVal = text.toIntOrNull()
        if (heightVal != null && heightVal in 160..deviceInfo.nativeHeight) {
            val calcFactorY = deviceInfo.nativeHeight.toFloat() / heightVal.toFloat()
            _currentConfig.value = _currentConfig.value.copy(
                renderHeight = heightVal,
                stretchFactorY = calcFactorY
            )
        }
    }

    fun updateDpiInput(text: String) {
        _dpiInputText.value = text
    }

    fun applyPresetNorm(preset: DisplayNormPreset) {
        val safeW = preset.width.coerceIn(160, deviceInfo.nativeWidth)
        val safeH = preset.height.coerceIn(160, deviceInfo.nativeHeight)
        val calcFactorX = deviceInfo.nativeWidth.toFloat() / safeW.toFloat()
        val calcFactorY = deviceInfo.nativeHeight.toFloat() / safeH.toFloat()

        _currentConfig.value = _currentConfig.value.copy(
            renderWidth = safeW,
            renderHeight = safeH,
            stretchFactorX = calcFactorX,
            stretchFactorY = calcFactorY,
            isFreeStretch = (safeW != 720 || safeH != deviceInfo.nativeHeight)
        )

        _widthInputText.value = safeW.toString()
        _heightInputText.value = safeH.toString()
        val newDpi = ((deviceInfo.densityDpi * safeW.toFloat()) / deviceInfo.nativeWidth.toFloat()).toInt()
        _dpiInputText.value = newDpi.toString()

        _statusMessage.value = "Norme appliquée : ${preset.name} (${safeW}×${safeH})"
    }

    // =========================================================================
    // EXÉCUTION SYSTÈME DIRECTE SANS TERMINAL
    // =========================================================================

    fun applyToScreenDirectly() {
        val cfg = _currentConfig.value
        val dpi = _dpiInputText.value.toIntOrNull()
            ?: ((deviceInfo.densityDpi * cfg.renderWidth.toFloat()) / cfg.nativeWidth.toFloat()).toInt()

        viewModelScope.launch {
            _isExecutingDirectly.value = true
            val result = SystemResolutionExecutor.applyResolutionDirectly(
                context = getApplication(),
                width = cfg.renderWidth,
                height = cfg.renderHeight,
                densityDpi = dpi
            )
            _isExecutingDirectly.value = false
            _systemExecutionResult.value = result

            if (result is SystemResolutionExecutor.ExecutionResult.Success) {
                // Lancer le compte à rebours de sécurité (15s)
                startSafetyCountdown()
            }
        }
    }

    fun confirmScreenResolution() {
        countdownJob?.cancel()
        countdownJob = null
        _safetyCountdown.value = null
        _systemExecutionResult.value = null
        _statusMessage.value = "Résolution système confirmée et conservée !"
    }

    fun revertScreenResolution() {
        countdownJob?.cancel()
        countdownJob = null
        _safetyCountdown.value = null
        _systemExecutionResult.value = null

        viewModelScope.launch {
            _isExecutingDirectly.value = true
            val result = SystemResolutionExecutor.resetResolutionDirectly()
            _isExecutingDirectly.value = false
            when (result) {
                is SystemResolutionExecutor.ExecutionResult.Success -> {
                    _statusMessage.value = "Écran réinitialisé à sa résolution physique normale"
                }
                is SystemResolutionExecutor.ExecutionResult.Error -> {
                    _statusMessage.value = "Erreur de réinitialisation : ${result.errorMessage}"
                }
                else -> {}
            }
        }
    }

    fun dismissSystemDialog() {
        _systemExecutionResult.value = null
    }

    private fun startSafetyCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (sec in 15 downTo 1) {
                _safetyCountdown.value = sec
                delay(1000)
            }
            // Si l'utilisateur n'a pas confirmé après 15s, restauration automatique de sécurité
            _safetyCountdown.value = null
            revertScreenResolution()
            _statusMessage.value = "Temps écoulé : Résolution réinitialisée automatiquement par sécurité"
        }
    }
}
