package com.example.slm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.slm.system.SystemCapabilityExploration
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ArchitectureScreen(
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // En-tête
        Text(
            text = "SLM ARCHITECTURE & ANDROID 16 REALITY",
            color = CyberCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )

        // 1. CLARIFICATION CONCEPTUELLE ABSOLUE
        ArchitectureCard(
            title = "DPI ≠ RÉSOLUTION ≠ RENDU ≠ SCALING ≠ ZOOM ≠ STRETCH ≠ IA",
            icon = Icons.Default.Info,
            iconTint = CyberCyan
        ) {
            SystemCapabilityExploration.CONCEPTS.forEach { concept ->
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Text(
                        text = concept.term,
                        color = CyberCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = concept.definition,
                        color = TextPrimary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Impact : ${concept.technicalRole}",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // 2. COMPATIBILITÉ DES JEUX : ANALYSE DES 4 CATÉGORIES
        ArchitectureCard(
            title = "COMPATIBILITÉ JEUX VIDÉO SUR ANDROID 16 SANS ROOT",
            icon = Icons.Default.SportsEsports,
            iconTint = CyberAmber
        ) {
            SystemCapabilityExploration.GAME_CATEGORIES.forEach { cat ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${cat.category} : ${cat.title}",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        when {
                                            cat.possibility.contains("100%") -> CyberCyan.copy(alpha = 0.2f)
                                            cat.possibility.contains("Impossible") -> CyberRed.copy(alpha = 0.2f)
                                            else -> CyberAmber.copy(alpha = 0.2f)
                                        }
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = cat.possibility,
                                    color = when {
                                        cat.possibility.contains("100%") -> CyberCyan
                                        cat.possibility.contains("Impossible") -> CyberRed
                                        else -> CyberAmber
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = cat.technicalExplanation,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // 3. ÉTUDE COMPOSANTS SYSTÈME : SURFACECONTROL & HARDWARE COMPOSER
        ArchitectureCard(
            title = "SURFACECONTROL, HAL & HARDWARE COMPOSER",
            icon = Icons.Default.Layers,
            iconTint = CyberViolet
        ) {
            SystemCapabilityExploration.ARCHITECTURE_ANALYSIS.forEach { item ->
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Text(
                        text = item.component,
                        color = CyberViolet,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = item.role,
                        color = TextPrimary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = item.nonRootPermissionVerdict,
                        color = if (item.nonRootPermissionVerdict.contains("AUTORISÉ")) CyberCyan else CyberAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // 4. SÉCURITÉ & SANDBOXING ANDROID 16 SANS ROOT
        ArchitectureCard(
            title = "ENGAGEMENT DE TRANSPARENCE : SANS ROOT",
            icon = Icons.Default.Security,
            iconTint = CyberCyan
        ) {
            Text(
                text = "SLM Stretch (X1) fonctionne dans le respect le plus strict des permissions Android 16 (API 36) et des politiques de sécurité Google Play.\n\n" +
                        "• Aucun droit root, aucune modification système ni débogage ADB requis.\n" +
                        "• Les fonctionnalités de scaling de rendu et de reconstruction IA sont exécutées via notre moteur graphique optimisé.\n" +
                        "• L'application n'utilise aucun faux filtre de netteté maquillé en IA ni de recadrage trompeur : chaque paramètre correspond à un calcul mathématique et graphique réel.",
                color = TextPrimary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ArchitectureCard(
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
