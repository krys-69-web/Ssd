package com.example.slm.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.slm.engine.SlmGraphicsEngine
import com.example.slm.ui.SlmViewModel
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.isActive

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TestScreen(
    viewModel: SlmViewModel,
    modifier: Modifier = Modifier
) {
    val currentConfig by viewModel.currentConfig.collectAsState()
    val testMode by viewModel.testViewMode.collectAsState()
    val splitPos by viewModel.splitPosition.collectAsState()
    val metrics by viewModel.performanceMetrics.collectAsState()

    val engine = remember { SlmGraphicsEngine() }

    var animPhase by remember { mutableFloatStateOf(0f) }
    var displayedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Mesure réelle des FPS et temps d'exécution
    var frameCount by remember { mutableStateOf(0) }
    var lastFpsCalcTime by remember { mutableStateOf(System.nanoTime()) }
    var currentMeasuredFps by remember { mutableFloatStateOf(60f) }
    var currentAiTimeMs by remember { mutableFloatStateOf(0f) }

    // Boucle d'animation synchronisée avec l'affichage
    LaunchedEffect(Unit) {
        var lastNanos = System.nanoTime()
        while (isActive) {
            withFrameNanos { nowNanos ->
                val deltaSec = (nowNanos - lastNanos) / 1_000_000_000f
                lastNanos = nowNanos
                animPhase = (animPhase + deltaSec * 0.25f) % 1.0f

                frameCount++
                val elapsedFpsNanos = nowNanos - lastFpsCalcTime
                if (elapsedFpsNanos >= 500_000_000L) { // Mise à jour toutes les 500ms
                    currentMeasuredFps = (frameCount * 1_000_000_000f) / elapsedFpsNanos
                    frameCount = 0
                    lastFpsCalcTime = nowNanos

                    val frameTime = if (currentMeasuredFps > 0) 1000f / currentMeasuredFps else 16.6f
                    viewModel.updateMetrics(currentMeasuredFps, frameTime, currentAiTimeMs)
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ==========================================
        // 1. SÉLECTEUR DE MODE DE VUE DE TEST
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkOutline))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "MODE DE COMPARAISON VISUELLE",
                    color = CyberCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TestModeChip("NORMAL (1:1)", testMode == SlmGraphicsEngine.TestViewMode.NORMAL_NATIVE) {
                        viewModel.setTestViewMode(SlmGraphicsEngine.TestViewMode.NORMAL_NATIVE)
                    }
                    TestModeChip("STRETCH", testMode == SlmGraphicsEngine.TestViewMode.STRETCH_ONLY) {
                        viewModel.setTestViewMode(SlmGraphicsEngine.TestViewMode.STRETCH_ONLY)
                    }
                    TestModeChip("AI UPSCALING", testMode == SlmGraphicsEngine.TestViewMode.AI_UPSCALED) {
                        viewModel.setTestViewMode(SlmGraphicsEngine.TestViewMode.AI_UPSCALED)
                    }
                    TestModeChip("SPLIT SLIDER", testMode == SlmGraphicsEngine.TestViewMode.SPLIT_COMPARISON) {
                        viewModel.setTestViewMode(SlmGraphicsEngine.TestViewMode.SPLIT_COMPARISON)
                    }
                }
            }
        }

        // ==========================================
        // 2. ZONE DE RENDU INTERACTIVE & TEST SCENE
        // ==========================================
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0A0E16))
                .border(1.dp, DarkOutline, RoundedCornerShape(12.dp))
                .testTag("test_render_viewport")
        ) {
            val density = LocalDensity.current
            val viewWidthPx = with(density) { maxWidth.roundToPx() }
            val viewHeightPx = with(density) { maxHeight.roundToPx() }

            // Exécution du pipeline de rendu synthétique
            if (viewWidthPx > 50 && viewHeightPx > 50) {
                val output = engine.processDisplayPipeline(
                    config = currentConfig,
                    viewportWidth = viewWidthPx,
                    viewportHeight = viewHeightPx,
                    viewMode = testMode,
                    splitPosition = splitPos,
                    animPhase = animPhase
                )
                displayedBitmap = output.finalBitmap
                currentAiTimeMs = output.aiTimeMs
            }

            displayedBitmap?.let { bmp ->
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Mire de test SLM Stretch",
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Détection du glissement tactile pour le curseur split
            if (testMode == SlmGraphicsEngine.TestViewMode.SPLIT_COMPARISON) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                val newPos = change.position.x / size.width
                                viewModel.setSplitPosition(newPos)
                            }
                        }
                )
            }

            // HUD OVERLAY EN TEMPS RÉEL
            Card(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.75f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                    Text(
                        text = "FPS : ${"%.1f".format(currentMeasuredFps)} • Frame : ${"%.1f".format(metrics.frameTimeMs)} ms",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "AI Compute : ${"%.2f".format(currentAiTimeMs)} ms (${currentConfig.aiGraphicsMode.label})",
                        color = CyberAmber,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Render : ${currentConfig.renderWidth}×${currentConfig.renderHeight} → Écran : ${currentConfig.nativeWidth}×${currentConfig.nativeHeight}",
                        color = TextPrimary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Stretch : X${"%.2f".format(currentConfig.effectiveRatioX)} Y${"%.2f".format(currentConfig.effectiveRatioY)}",
                        color = CyberViolet,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // ==========================================
        // 3. CONTRÔLEUR DU SPLIT SLIDER
        // ==========================================
        if (testMode == SlmGraphicsEngine.TestViewMode.SPLIT_COMPARISON) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Stretch", color = TextSecondary, fontSize = 11.sp)
                    Slider(
                        value = splitPos,
                        onValueChange = { viewModel.setSplitPosition(it) },
                        valueRange = 0.05f..0.95f,
                        colors = SliderDefaults.colors(thumbColor = CyberCyan, activeTrackColor = CyberCyan),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )
                    Text("AI Upscaled", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // ==========================================
        // 4. PANNEAU DE PERFORMANCES & TÉLÉMÉTRIE
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkOutline))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TÉLÉMÉTRIE GRAPHIQUE",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "GPU Load : ${metrics.gpuLoadString}",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatMetricItem("FRAME TIME", "${"%.1f".format(metrics.frameTimeMs)} ms", CyberCyan)
                    StatMetricItem("AI PROCESSING", "${"%.2f".format(currentAiTimeMs)} ms", CyberAmber)
                    StatMetricItem("RATIO ANAMORPH.", "X${"%.2f".format(currentConfig.effectiveRatioX)}", CyberViolet)
                    StatMetricItem("QUALITÉ IA", "${(currentConfig.aiQuality * 100).toInt()}%", TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun TestModeChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = DarkSurfaceElevated,
            selectedLabelColor = CyberCyan
        )
    )
}

@Composable
private fun StatMetricItem(label: String, value: String, color: Color) {
    Column {
        Text(text = label, color = TextSecondary, fontSize = 9.sp)
        Text(
            text = value,
            color = color,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
