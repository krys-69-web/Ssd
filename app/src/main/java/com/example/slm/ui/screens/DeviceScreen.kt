package com.example.slm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.slm.model.DeviceHardwareInfo
import com.example.slm.ui.SlmViewModel
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DeviceScreen(
    viewModel: SlmViewModel,
    modifier: Modifier = Modifier
) {
    val info = viewModel.deviceInfo
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // En-tête Device
        Text(
            text = "SLM DEVICE HARDWARE PROFILER",
            color = CyberCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )

        // 1. ÉCRAN & AFFICHAGE
        HardwareSectionCard(
            title = "AFFICHAGE & DALLE PHYSIQUE",
            icon = Icons.Default.Smartphone,
            iconTint = CyberCyan
        ) {
            HardwareRow("Résolution Native", "${info.nativeWidth} × ${info.nativeHeight} px")
            HardwareRow("Fréquence de Rafraîchissement", "${info.refreshRateHz.toInt()} Hz")
            HardwareRow("Densité d'Affichage", "${info.densityDpi} DPI (Scale: ${info.densityScale}x)")
            HardwareRow("Rapport d'Aspect Natif", "%.2f:1".format(info.nativeHeight.toFloat() / info.nativeWidth.toFloat()))
            HardwareRow("Limite Technique d'Étirement", "X${"%.2f".format(viewModel.maxStretchX)} / Y${"%.2f".format(viewModel.maxStretchY)}")
        }

        // 2. GPU & ACCÉLÉRATION GRAPHIQUE
        HardwareSectionCard(
            title = "GPU & APIS GRAPHIQUES",
            icon = Icons.Default.VideogameAsset,
            iconTint = CyberAmber
        ) {
            HardwareRow("Fabricant GPU / SoC", info.gpuVendor)
            HardwareRow("Rendu Matériel", info.gpuRenderer)
            HardwareRow("OpenGL ES", info.glesVersion)
            HardwareRow(
                "Vulkan API",
                if (info.isVulkanSupported) "Supporté (${info.vulkanVersion})" else "Non Supporté",
                highlightColor = if (info.isVulkanSupported) CyberCyan else CyberAmber
            )
            HardwareRow("Vulkan Hardware Level", "Niveau ${info.vulkanHardwareLevel}")
        }

        // 3. IA & ACCÉLÉRATION NPU
        HardwareSectionCard(
            title = "ACCÉLÉRATION IA & COMPUTE",
            icon = Icons.Default.DeveloperBoard,
            iconTint = CyberViolet
        ) {
            HardwareRow("Moteur d'Accélération", info.aiAccelerationType)
            HardwareRow(
                "NPU / Neural Hardware",
                if (info.isNpuAvailable) "Disponible & Détecté" else "Émulation GPU Compute",
                highlightColor = if (info.isNpuAvailable) CyberCyan else TextSecondary
            )
            HardwareRow("Precision Mode", "FP16 / INT8 Quantized Shaders")
        }

        // 4. CPU & MÉMOIRE RAM
        HardwareSectionCard(
            title = "SOC, CPU & MÉMOIRE VIVE",
            icon = Icons.Default.Memory,
            iconTint = CyberCyan
        ) {
            HardwareRow("Architecture CPU", info.cpuModel)
            HardwareRow("Nombre de Cœurs CPU", "${info.cpuCores} cœurs d'exécution")
            HardwareRow("RAM Totale", "${info.totalRamMb} Mo")
            HardwareRow("RAM Disponible", "${info.availableRamMb} Mo")
        }

        // 5. SYSTÈME & THERMIQUES
        HardwareSectionCard(
            title = "OS & ÉTAT THERMIQUE ANDROID 16",
            icon = Icons.Default.Thermostat,
            iconTint = CyberAmber
        ) {
            HardwareRow("Version du Système", info.androidRelease)
            HardwareRow("Niveau d'API Android", "API ${info.sdkInt} (Android 16)")
            HardwareRow("Statut Thermique Actuel", info.thermalStatus)
            HardwareRow("Sandboxing", "SELinux Enforcing (Non-Root)")
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun HardwareSectionCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkOutline))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            content()
        }
    }
}

@Composable
private fun HardwareRow(
    label: String,
    value: String,
    highlightColor: Color = TextPrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextSecondary, fontSize = 12.sp)
        Text(
            text = value,
            color = highlightColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace
        )
    }
}
