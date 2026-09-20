package com.example

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import com.example.slm.engine.SlmAiReconstructionKernel
import com.example.slm.engine.SlmGraphicsEngine
import com.example.slm.model.AiGraphicsMode
import com.example.slm.model.ScalingEngine
import com.example.slm.model.ScalingMode
import com.example.slm.model.SlmConfig
import com.example.slm.system.AndroidDisplayAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SLM Stretch", appName)
  }

  @Test
  fun `test AI reconstruction kernel processes bitmap without crashing`() {
    val inputBitmap = Bitmap.createBitmap(120, 120, Bitmap.Config.ARGB_8888)
    for (x in 0 until 120) {
      for (y in 0 until 120) {
        inputBitmap.setPixel(x, y, if ((x + y) % 10 == 0) Color.WHITE else Color.BLACK)
      }
    }

    val result = SlmAiReconstructionKernel.process(
      source = inputBitmap,
      targetWidth = 120,
      targetHeight = 120,
      mode = AiGraphicsMode.BALANCED,
      sharpness = 0.7f,
      detail = 0.5f,
      quality = 0.8f
    )

    assertNotNull(result.outputBitmap)
    assertEquals(120, result.outputBitmap.width)
    assertEquals(120, result.outputBitmap.height)
    assertTrue(result.processingTimeMs >= 0f)
  }

  @Test
  fun `test graphics engine pipeline generates valid frame and non-negative AI time`() {
    val engine = SlmGraphicsEngine()
    val config = SlmConfig(
      renderWidth = 360,
      renderHeight = 720,
      nativeWidth = 1080,
      nativeHeight = 2160,
      stretchFactorX = 3.0f,
      stretchFactorY = 1.0f,
      isFreeStretch = false,
      scalingMode = ScalingMode.FULL_STRETCH,
      scalingEngine = ScalingEngine.AUTOMATIC,
      aiGraphicsMode = AiGraphicsMode.PERFORMANCE,
      aiSharpness = 0.6f,
      aiDetail = 0.4f,
      aiQuality = 0.7f
    )

    val output = engine.processDisplayPipeline(
      config = config,
      viewportWidth = 200,
      viewportHeight = 400,
      viewMode = SlmGraphicsEngine.TestViewMode.SPLIT_COMPARISON,
      splitPosition = 0.5f,
      animPhase = 0.2f
    )

    assertNotNull(output.finalBitmap)
    assertEquals(200, output.finalBitmap.width)
    assertEquals(400, output.finalBitmap.height)
    assertTrue(output.aiTimeMs >= 0f)
  }

  @Test
  fun `test display analyzer calculates safe hardware bounds`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val analyzer = AndroidDisplayAnalyzer(context)
    val info = analyzer.getDeviceHardwareInfo()

    assertTrue(info.nativeWidth > 0)
    assertTrue(info.nativeHeight > 0)
    assertTrue(info.refreshRateHz > 0)

    val maxStretch = analyzer.calculateMaxTechnicalStretch(info.nativeWidth)
    assertTrue(maxStretch >= 1.0f && maxStretch <= 4.0f)
  }
}

