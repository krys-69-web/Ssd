package com.example.slm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.slm.data.SlmProfileEntity
import com.example.slm.ui.SlmViewModel
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
fun ProfilesScreen(
    viewModel: SlmViewModel,
    modifier: Modifier = Modifier
) {
    val profiles by viewModel.allProfiles.collectAsState()
    val currentConfig by viewModel.currentConfig.collectAsState()
    val scrollState = rememberScrollState()

    var showSaveDialog by remember { mutableStateOf(false) }
    var newProfileName by remember { mutableStateOf("") }
    var newProfileDesc by remember { mutableStateOf("") }

    var showRestoreDefaultsDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // En-tête
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "PROFILS GRAPHIQUES & GAMING",
                    color = CyberCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Préréglages d'étirement et de super-résolution IA",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            Button(
                onClick = {
                    newProfileName = "Mon Profil"
                    newProfileDesc = "Stretch X${"%.2f".format(currentConfig.stretchFactorX)} - Mode ${currentConfig.aiGraphicsMode.label}"
                    showSaveDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("button_save_profile")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("SAUVEGARDER", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Liste des profils
        profiles.forEach { profile ->
            val isCurrentActive = profile.renderWidth == currentConfig.renderWidth
                    && profile.renderHeight == currentConfig.renderHeight
                    && profile.aiGraphicsMode == currentConfig.aiGraphicsMode.name

            ProfileCard(
                profile = profile,
                isActive = isCurrentActive,
                onSelect = { viewModel.loadProfile(profile) },
                onDelete = if (!profile.isPreset) {
                    { viewModel.deleteProfile(profile) }
                } else null
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Bouton de restauration usine
        OutlinedButton(
            onClick = { showRestoreDefaultsDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(DarkOutline))
        ) {
            Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, tint = CyberAmber, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("RESTAURER LES PROFILS D'USINE", color = CyberAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Dialogue d'enregistrement de profil
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Enregistrer la configuration actuelle", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Rendu actuel : ${currentConfig.renderWidth}×${currentConfig.renderHeight} (X${"%.2f".format(currentConfig.stretchFactorX)} Y${"%.2f".format(currentConfig.stretchFactorY)}) • IA: ${currentConfig.aiGraphicsMode.label}",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    OutlinedTextField(
                        value = newProfileName,
                        onValueChange = { newProfileName = it },
                        label = { Text("Nom du profil") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = DarkOutline
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newProfileDesc,
                        onValueChange = { newProfileDesc = it },
                        label = { Text("Description") },
                        maxLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = DarkOutline
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newProfileName.isNotBlank()) {
                            viewModel.saveCurrentProfile(newProfileName.trim(), newProfileDesc.trim())
                            showSaveDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black)
                ) {
                    Text("ENREGISTRER", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("ANNULER", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Dialogue de restauration usine
    if (showRestoreDefaultsDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDefaultsDialog = false },
            title = { Text("Restaurer les profils d'usine ?", color = TextPrimary) },
            text = {
                Text(
                    "Cette action réinitialisera tous les préréglages (NORMAL, GAMING, PERFORMANCE, QUALITY, MINECRAFT, CUSTOM) et supprimera les profils personnalisés créés.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.restoreFactoryProfiles()
                        showRestoreDefaultsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberAmber, contentColor = Color.Black)
                ) {
                    Text("CONFIRMER", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDefaultsDialog = false }) {
                    Text("ANNULER", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
private fun ProfileCard(
    profile: SlmProfileEntity,
    isActive: Boolean,
    onSelect: () -> Unit,
    onDelete: (() -> Unit)?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) DarkSurfaceElevated else DarkSurface
        ),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(if (isActive) CyberCyan else DarkOutline)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = null,
                        tint = if (isActive) CyberCyan else CyberViolet,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = profile.name,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    if (profile.isPreset) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CyberViolet.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("PRÉSÉLECTION", color = CyberViolet, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isActive) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CyberCyan.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("ACTIF", color = CyberCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (onDelete != null) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Supprimer", tint = CyberRed, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Text(
                text = profile.description,
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )

            // Détails techniques du profil
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Rendu: ${profile.renderWidth}×${profile.renderHeight}",
                    color = CyberCyan,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Stretch: X${"%.2f".format(profile.stretchFactorX)} Y${"%.2f".format(profile.stretchFactorY)}",
                    color = CyberAmber,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "IA: ${profile.aiGraphicsMode}",
                    color = CyberViolet,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
