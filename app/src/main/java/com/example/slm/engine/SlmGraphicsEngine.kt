package com.example.slm.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import com.example.slm.model.AiGraphicsMode
import com.example.slm.model.ScalingMode
import com.example.slm.model.SlmConfig
import kotlin.math.cos
import kotlin.math.sin

/**
 * Moteur de rendu et de simulation graphique pour SLM Stretch.
 * Génère une scène de test synthétique avec grilles subpixels, cercles, mires d'aliasing,
 * textes et géométries fines, et applique les transformations de stretch et l'upscaling IA.
 */
class SlmGraphicsEngine {

    enum class TestViewMode {
        NORMAL_NATIVE,   // Rendu natif 1:1 sans stretch
        STRETCH_ONLY,    // Rendu basse résolution étiré vers la résolution native
        AI_UPSCALED,     // Rendu basse résolution reconstruit par SLM AI Graphics
        SPLIT_COMPARISON // Vue comparative divisée (Gauche: Stretch seul, Droite: AI Upscaled)
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 28f
        isFakeBoldText = true
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(80, 0, 240, 255)
        strokeWidth = 1.5f
        style = Paint.Style.STROKE
    }

    private val fineGridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(45, 138, 43, 226)
        strokeWidth = 1.0f
        style = Paint.Style.STROKE
    }

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(220, 255, 184, 0)
        strokeWidth = 3f
        style = Paint.Style.STROKE
    }

    private val shapePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(180, 0, 240, 255)
        style = Paint.Style.FILL
    }

    private val radarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(200, 0, 240, 255)
        strokeWidth = 2.5f
    }

    /**
     * Génère la scène de test directement dans un bitmap de résolution spécifiée (w x h)
     */
    fun renderTestScene(width: Int, height: Int, animPhase: Float): Bitmap {
        val safeW = if (width > 0) width else 720
        val safeH = if (height > 0) height else 1280
        val bitmap = Bitmap.createBitmap(safeW, safeH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Fond sombre futuriste
        canvas.drawColor(Color.rgb(10, 14, 22))

        val cx = safeW / 2f
        val cy = safeH / 2f

        // 1. Grille de fond haute fréquence (pour tester l'aliasing)
        val gridStep = 40f
        var x = 0f
        while (x < safeW) {
            canvas.drawLine(x, 0f, x, safeH.toFloat(), fineGridPaint)
            x += gridStep
        }
        var y = 0f
        while (y < safeH) {
            canvas.drawLine(0f, y, safeW.toFloat(), y, fineGridPaint)
            y += gridStep
        }

        // Grille principale
        val majorStep = 120f
        x = 0f
        while (x < safeW) {
            canvas.drawLine(x, 0f, x, safeH.toFloat(), gridPaint)
            x += majorStep
        }
        y = 0f
        while (y < safeH) {
            canvas.drawLine(0f, y, safeW.toFloat(), y, gridPaint)
            y += majorStep
        }

        // 2. Cercles concentriques : en cas d'étirement horizontal ou vertical réel,
        // ces cercles se transforment en ellipses étirées.
        val maxRadius = minOf(safeW, safeH) * 0.35f
        for (i in 1..5) {
            val r = maxRadius * (i / 5f)
            canvas.drawCircle(cx, cy, r, circlePaint)
        }

        // Lignes diagonales à plusieurs angles pour évaluer la reconstruction des bords
        val rayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(120, 255, 255, 255)
            strokeWidth = 1.5f
        }
        for (angleDeg in 0 until 360 step 30) {
            val rad = Math.toRadians(angleDeg.toDouble())
            val rx = cx + (maxRadius * 1.15f * cos(rad)).toFloat()
            val ry = cy + (maxRadius * 1.15f * sin(rad)).toFloat()
            canvas.drawLine(cx, cy, rx, ry, rayPaint)
        }

        // 3. Aiguille animée / radar pour vérifier la fluidité et le frametime
        val animAngleRad = Math.toRadians((animPhase * 360f).toDouble())
        val radarX = cx + (maxRadius * cos(animAngleRad)).toFloat()
        val radarY = cy + (maxRadius * sin(animAngleRad)).toFloat()
        canvas.drawLine(cx, cy, radarX, radarY, radarPaint)

        // 4. Formes géométriques angulaires (hexagone & losanges)
        val polyPath = Path().apply {
            val polyR = maxRadius * 0.5f
            for (i in 0 until 6) {
                val pAngle = Math.toRadians((i * 60 + animPhase * 90).toDouble())
                val px = cx + (polyR * cos(pAngle)).toFloat()
                val py = cy + (polyR * sin(pAngle)).toFloat()
                if (i == 0) moveTo(px, py) else lineTo(px, py)
            }
            close()
        }
        val polyOutlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(220, 0, 240, 255)
            strokeWidth = 3f
            style = Paint.Style.STROKE
        }
        canvas.drawPath(polyPath, polyOutlinePaint)

        // 5. Blocs de calibration chromatique
        val barW = safeW / 6f
        val barH = 24f
        val colors = intArrayOf(
            Color.RED, Color.GREEN, Color.BLUE, Color.CYAN, Color.MAGENTA, Color.YELLOW
        )
        for (i in colors.indices) {
            val rect = RectF(i * barW, safeH - barH - 20f, (i + 1) * barW, safeH - 20f)
            val barPaint = Paint().apply { color = colors[i] }
            canvas.drawRect(rect, barPaint)
        }

        // 6. Textes de test de résolution et mire de netteté
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = maxOf(22f, safeW * 0.035f)
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("SLM STRETCH (X1) - TEST PATTERN", cx, 60f, titlePaint)

        val subTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(200, 0, 240, 255)
            textSize = maxOf(16f, safeW * 0.024f)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("RENDER BUFFER: ${safeW}×${safeH}", cx, 95f, subTitlePaint)

        return bitmap
    }

    /**
     * Assemble l'image finale affichée en appliquant le mode de vue, le stretch et l'upscaling IA.
     */
    fun processDisplayPipeline(
        config: SlmConfig,
        viewportWidth: Int,
        viewportHeight: Int,
        viewMode: TestViewMode,
        splitPosition: Float = 0.5f,
        animPhase: Float = 0f
    ): DisplayPipelineOutput {
        val vpW = maxOf(100, viewportWidth)
        val vpH = maxOf(100, viewportHeight)

        return when (viewMode) {
            TestViewMode.NORMAL_NATIVE -> {
                // Rendu 1:1 à la résolution de la zone d'affichage
                val nativeBmp = renderTestScene(vpW, vpH, animPhase)
                DisplayPipelineOutput(
                    finalBitmap = nativeBmp,
                    aiTimeMs = 0f,
                    renderWidth = vpW,
                    renderHeight = vpH,
                    scaleX = 1.0f,
                    scaleY = 1.0f
                )
            }

            TestViewMode.STRETCH_ONLY -> {
                // Rendu à la résolution personnalisée (par ex. 720 x 2460 proportionnel)
                val rW = maxOf(64, (config.renderWidth.toFloat() / config.nativeWidth.toFloat() * vpW).toInt())
                val rH = maxOf(64, (config.renderHeight.toFloat() / config.nativeHeight.toFloat() * vpH).toInt())

                val lowRes = renderTestScene(rW, rH, animPhase)
                val stretched = applyStretchScaling(lowRes, vpW, vpH, config.scalingMode)

                DisplayPipelineOutput(
                    finalBitmap = stretched,
                    aiTimeMs = 0f,
                    renderWidth = config.renderWidth,
                    renderHeight = config.renderHeight,
                    scaleX = config.effectiveRatioX,
                    scaleY = config.effectiveRatioY
                )
            }

            TestViewMode.AI_UPSCALED -> {
                val rW = maxOf(64, (config.renderWidth.toFloat() / config.nativeWidth.toFloat() * vpW).toInt())
                val rH = maxOf(64, (config.renderHeight.toFloat() / config.nativeHeight.toFloat() * vpH).toInt())

                val lowRes = renderTestScene(rW, rH, animPhase)

                // Reconstruction IA
                val aiResult = SlmAiReconstructionKernel.process(
                    source = lowRes,
                    targetWidth = vpW,
                    targetHeight = vpH,
                    mode = config.aiGraphicsMode,
                    sharpness = config.aiSharpness,
                    detail = config.aiDetail,
                    quality = config.aiQuality
                )

                DisplayPipelineOutput(
                    finalBitmap = aiResult.outputBitmap,
                    aiTimeMs = aiResult.processingTimeMs,
                    renderWidth = config.renderWidth,
                    renderHeight = config.renderHeight,
                    scaleX = config.effectiveRatioX,
                    scaleY = config.effectiveRatioY
                )
            }

            TestViewMode.SPLIT_COMPARISON -> {
                val rW = maxOf(64, (config.renderWidth.toFloat() / config.nativeWidth.toFloat() * vpW).toInt())
                val rH = maxOf(64, (config.renderHeight.toFloat() / config.nativeHeight.toFloat() * vpH).toInt())

                val lowRes = renderTestScene(rW, rH, animPhase)

                // 1. Image gauche : stretch seul
                val stretched = applyStretchScaling(lowRes, vpW, vpH, config.scalingMode)

                // 2. Image droite : reconstruction IA
                val aiResult = SlmAiReconstructionKernel.process(
                    source = lowRes,
                    targetWidth = vpW,
                    targetHeight = vpH,
                    mode = if (config.aiGraphicsMode == AiGraphicsMode.OFF) AiGraphicsMode.BALANCED else config.aiGraphicsMode,
                    sharpness = config.aiSharpness,
                    detail = config.aiDetail,
                    quality = config.aiQuality
                )

                // Fusion avec curseur split
                val splitX = (vpW * splitPosition.coerceIn(0.05f, 0.95f)).toInt()
                val combined = Bitmap.createBitmap(vpW, vpH, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(combined)

                // Dessin partie gauche (Stretch seul)
                val srcRectLeft = Rect(0, 0, splitX, vpH)
                val dstRectLeft = Rect(0, 0, splitX, vpH)
                canvas.drawBitmap(stretched, srcRectLeft, dstRectLeft, null)

                // Dessin partie droite (AI Upscaled)
                val srcRectRight = Rect(splitX, 0, vpW, vpH)
                val dstRectRight = Rect(splitX, 0, vpW, vpH)
                canvas.drawBitmap(aiResult.outputBitmap, srcRectRight, dstRectRight, null)

                // Ligne de démarcation split
                val linePaint = Paint().apply {
                    color = Color.rgb(0, 240, 255)
                    strokeWidth = 4f
                }
                canvas.drawLine(splitX.toFloat(), 0f, splitX.toFloat(), vpH.toFloat(), linePaint)

                // Badges étiquettes
                val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    textSize = 24f
                    isFakeBoldText = true
                }
                canvas.drawText("STRETCH ONLY", 20f, 40f, labelPaint)
                canvas.drawText("SLM AI GRAPHICS", maxOf(splitX + 20f, vpW - 250f), 40f, labelPaint)

                DisplayPipelineOutput(
                    finalBitmap = combined,
                    aiTimeMs = aiResult.processingTimeMs,
                    renderWidth = config.renderWidth,
                    renderHeight = config.renderHeight,
                    scaleX = config.effectiveRatioX,
                    scaleY = config.effectiveRatioY
                )
            }
        }
    }

    /**
     * Applique la mise à l'échelle vers la résolution cible selon le mode de scaling
     */
    private fun applyStretchScaling(
        source: Bitmap,
        targetWidth: Int,
        targetHeight: Int,
        mode: ScalingMode
    ): Bitmap {
        return when (mode) {
            ScalingMode.FULL_STRETCH, ScalingMode.CUSTOM -> {
                // Anamorphique complet : force l'occupation de toute la surface
                Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true)
            }

            ScalingMode.KEEP_ASPECT_RATIO -> {
                // Conserve les proportions d'origine avec pillarbox ou letterbox
                val sRatio = source.width.toFloat() / source.height.toFloat()
                val tRatio = targetWidth.toFloat() / targetHeight.toFloat()

                val finalW: Int
                val finalH: Int

                if (sRatio > tRatio) {
                    finalW = targetWidth
                    finalH = (targetWidth / sRatio).toInt()
                } else {
                    finalH = targetHeight
                    finalW = (targetHeight * sRatio).toInt()
                }

                val scaled = Bitmap.createScaledBitmap(source, finalW, finalH, true)
                val out = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(out)
                canvas.drawColor(Color.BLACK)
                val offsetX = (targetWidth - finalW) / 2f
                val offsetY = (targetHeight - finalH) / 2f
                canvas.drawBitmap(scaled, offsetX, offsetY, null)
                out
            }

            ScalingMode.CENTERED -> {
                // 1:1 au centre
                val out = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(out)
                canvas.drawColor(Color.BLACK)
                val offsetX = (targetWidth - source.width) / 2f
                val offsetY = (targetHeight - source.height) / 2f
                canvas.drawBitmap(source, offsetX, offsetY, null)
                out
            }
        }
    }

    data class DisplayPipelineOutput(
        val finalBitmap: Bitmap,
        val aiTimeMs: Float,
        val renderWidth: Int,
        val renderHeight: Int,
        val scaleX: Float,
        val scaleY: Float
    )
}
