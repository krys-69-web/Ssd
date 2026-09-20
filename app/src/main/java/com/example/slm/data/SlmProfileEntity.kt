package com.example.slm.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.slm.model.AiGraphicsMode
import com.example.slm.model.ScalingEngine
import com.example.slm.model.ScalingMode
import com.example.slm.model.SlmConfig

@Entity(tableName = "slm_profiles")
data class SlmProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val renderWidth: Int,
    val renderHeight: Int,
    val stretchFactorX: Float,
    val stretchFactorY: Float,
    val isFreeStretch: Boolean,
    val scalingMode: String, // from ScalingMode.name
    val scalingEngine: String, // from ScalingEngine.name
    val aiGraphicsMode: String, // from AiGraphicsMode.name
    val aiSharpness: Float,
    val aiDetail: Float,
    val aiQuality: Float,
    val isPreset: Boolean = false
) {
    fun toConfig(nativeW: Int = 1080, nativeH: Int = 2460): SlmConfig {
        return SlmConfig(
            renderWidth = renderWidth,
            renderHeight = renderHeight,
            nativeWidth = nativeW,
            nativeHeight = nativeH,
            stretchFactorX = stretchFactorX,
            stretchFactorY = stretchFactorY,
            isFreeStretch = isFreeStretch,
            scalingMode = runCatching { ScalingMode.valueOf(scalingMode) }.getOrDefault(ScalingMode.FULL_STRETCH),
            scalingEngine = runCatching { ScalingEngine.valueOf(scalingEngine) }.getOrDefault(ScalingEngine.AUTOMATIC),
            aiGraphicsMode = runCatching { AiGraphicsMode.valueOf(aiGraphicsMode) }.getOrDefault(AiGraphicsMode.BALANCED),
            aiSharpness = aiSharpness,
            aiDetail = aiDetail,
            aiQuality = aiQuality
        )
    }

    companion object {
        fun fromConfig(name: String, description: String, config: SlmConfig, isPreset: Boolean = false): SlmProfileEntity {
            return SlmProfileEntity(
                name = name,
                description = description,
                renderWidth = config.renderWidth,
                renderHeight = config.renderHeight,
                stretchFactorX = config.stretchFactorX,
                stretchFactorY = config.stretchFactorY,
                isFreeStretch = config.isFreeStretch,
                scalingMode = config.scalingMode.name,
                scalingEngine = config.scalingEngine.name,
                aiGraphicsMode = config.aiGraphicsMode.name,
                aiSharpness = config.aiSharpness,
                aiDetail = config.aiDetail,
                aiQuality = config.aiQuality,
                isPreset = isPreset
            )
        }
    }
}
