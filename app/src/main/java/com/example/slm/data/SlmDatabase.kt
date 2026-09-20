package com.example.slm.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.slm.model.AiGraphicsMode
import com.example.slm.model.ScalingEngine
import com.example.slm.model.ScalingMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [SlmProfileEntity::class], version = 1, exportSchema = false)
abstract class SlmDatabase : RoomDatabase() {
    abstract fun profileDao(): SlmProfileDao

    companion object {
        @Volatile
        private var INSTANCE: SlmDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): SlmDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SlmDatabase::class.java,
                    "slm_stretch_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        val DEFAULT_PRESETS = listOf(
            SlmProfileEntity(
                id = 1,
                name = "NORMAL",
                description = "Rendu 1:1 à la résolution native. Aucune déformation ni interpolation.",
                renderWidth = 1080,
                renderHeight = 2460,
                stretchFactorX = 1.00f,
                stretchFactorY = 1.00f,
                isFreeStretch = false,
                scalingMode = ScalingMode.KEEP_ASPECT_RATIO.name,
                scalingEngine = ScalingEngine.AUTOMATIC.name,
                aiGraphicsMode = AiGraphicsMode.OFF.name,
                aiSharpness = 0.0f,
                aiDetail = 0.0f,
                aiQuality = 0.0f,
                isPreset = true
            ),
            SlmProfileEntity(
                id = 2,
                name = "GAMING",
                description = "Étirement horizontal X1.50 (720×2460 vers 1080×2460). Cibles larges et lisibilité accrue.",
                renderWidth = 720,
                renderHeight = 2460,
                stretchFactorX = 1.50f,
                stretchFactorY = 1.00f,
                isFreeStretch = false,
                scalingMode = ScalingMode.FULL_STRETCH.name,
                scalingEngine = ScalingEngine.GPU_SCALING.name,
                aiGraphicsMode = AiGraphicsMode.PERFORMANCE.name,
                aiSharpness = 0.70f,
                aiDetail = 0.50f,
                aiQuality = 0.70f,
                isPreset = true
            ),
            SlmProfileEntity(
                id = 3,
                name = "PERFORMANCE",
                description = "Rendu optimisé 720×1920 étiré vers le plein écran. Allègement du GPU et framerate maximal.",
                renderWidth = 720,
                renderHeight = 1920,
                stretchFactorX = 1.50f,
                stretchFactorY = 1.28f,
                isFreeStretch = false,
                scalingMode = ScalingMode.FULL_STRETCH.name,
                scalingEngine = ScalingEngine.AUTOMATIC.name,
                aiGraphicsMode = AiGraphicsMode.PERFORMANCE.name,
                aiSharpness = 0.60f,
                aiDetail = 0.40f,
                aiQuality = 0.50f,
                isPreset = true
            ),
            SlmProfileEntity(
                id = 4,
                name = "QUALITY",
                description = "Étirement léger X1.15 avec SLM AI Graphics en mode QUALITY. Reconstruction des contours haute fidélité.",
                renderWidth = 940,
                renderHeight = 2460,
                stretchFactorX = 1.15f,
                stretchFactorY = 1.00f,
                isFreeStretch = false,
                scalingMode = ScalingMode.FULL_STRETCH.name,
                scalingEngine = ScalingEngine.GPU_SCALING.name,
                aiGraphicsMode = AiGraphicsMode.QUALITY.name,
                aiSharpness = 0.85f,
                aiDetail = 0.75f,
                aiQuality = 0.90f,
                isPreset = true
            ),
            SlmProfileEntity(
                id = 5,
                name = "MINECRAFT",
                description = "Profil adapté aux mondes voxel : étirement Y1.28 et filtrage tensoriel réduisant le crénelage sur les blocs distants.",
                renderWidth = 1080,
                renderHeight = 1920,
                stretchFactorX = 1.00f,
                stretchFactorY = 1.28f,
                isFreeStretch = false,
                scalingMode = ScalingMode.FULL_STRETCH.name,
                scalingEngine = ScalingEngine.AUTOMATIC.name,
                aiGraphicsMode = AiGraphicsMode.BALANCED.name,
                aiSharpness = 0.75f,
                aiDetail = 0.60f,
                aiQuality = 0.80f,
                isPreset = true
            ),
            SlmProfileEntity(
                id = 6,
                name = "CUSTOM",
                description = "Configuration libre de la résolution de rendu (Free Stretch X/Y) et des hyperparamètres IA.",
                renderWidth = 720,
                renderHeight = 2460,
                stretchFactorX = 1.50f,
                stretchFactorY = 1.00f,
                isFreeStretch = true,
                scalingMode = ScalingMode.CUSTOM.name,
                scalingEngine = ScalingEngine.GPU_SCALING.name,
                aiGraphicsMode = AiGraphicsMode.ULTRA_QUALITY.name,
                aiSharpness = 0.70f,
                aiDetail = 0.60f,
                aiQuality = 0.85f,
                isPreset = true
            )
        )

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        database.profileDao().insertAll(DEFAULT_PRESETS)
                    }
                }
            }
        }
    }
}
