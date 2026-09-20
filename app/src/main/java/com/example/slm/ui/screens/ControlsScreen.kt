package com.example.slm.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.example.slm.system.SystemResolutionExecutor
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
import com.example.slm.model.AiGraphicsMode
import com.example.slm.model.ScalingEngine
import com.example.slm.model.ScalingMode
import com.example.slm.ui.SlmViewModel
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ControlsScreen(
    viewModel: SlmViewModel,
    modifier: Modifier = Modifier
) {
    val currentConfig by viewModel.currentConfig.collectAsState()
    val appliedConfig by viewModel.appliedConfig.collectAsState()
    val inputMode by viewModel.inputMode.collectAsState()
    val widthInputText by viewModel.widthInputText.collectAsState()
    val heightInputText by viewModel.heightInputText.collectAsState()
    val dpiInputText by viewModel.dpiInputText.collectAsState()
    val isExecutingDirectly by viewModel.isExecutingDirectly.collectAsState()
    val systemResult by viewModel.systemExecutionResult.collectAsState()
    val safetyCountdown by viewModel.safetyCountdown.collectAsState()
    val isPermissionGranted by viewModel.isPermissionGranted.collectAsState()
    val showPermissionGuideDialog by viewModel.showPermissionGuideDialog.collectAsState()
    val scrollState = rememberScrollState()

    var showResetDialog by remember { mutableStateOf(false) }
    var showRestoreDefaultDialog by remember { mutableStateOf(false) }

    val hasPendingChanges = currentConfig != appliedConfig

    // Boîte de dialogue de sécurité (Compte à rebours 15s)
    if (safetyCountdown != null) {
        AlertDialog(
            onDismissRequest = { viewModel.revertScreenResolution() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = CyberAmber, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SÉCURITÉ D'AFFICHAGE", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "La nouvelle résolution est active sur votre écran :",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${currentConfig.renderWidth} × ${currentConfig.renderHeight} à $dpiInputText DPI",
                        color = CyberCyan,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Vérifiez que l'écran est bien lisible et que le tactile répond correctement. Annulation automatique dans :",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$safetyCountdown secondes",
                        color = CyberAmber,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmScreenResolution() },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black)
                ) {
                    Text("GARDER CETTE RÉSOLUTION", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.revertScreenResolution() }
                ) {
                    Text("ANNULER (RETOUR)", color = CyberAmber)
                }
            },
            containerColor = DarkSurfaceElevated
        )
    }

    // Boîte de dialogue d'explication si permission requise
    if (systemResult is SystemResolutionExecutor.ExecutionResult.PermissionRequired) {
        val perm = systemResult as SystemResolutionExecutor.ExecutionResult.PermissionRequired
        val clipboard = LocalClipboardManager.current
        AlertDialog(
            onDismissRequest = { viewModel.dismissSystemDialog() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AUTORISATION SYSTÈME", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Pour modifier la résolution globale de votre écran d'un simple clic sans terminal :",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "• Option 1 (100% sans PC) : Installez l'application Shizuku (Google Play) pour autoriser l'application directement sur votre téléphone.",
                        color = TextPrimary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Option 2 : Si vous avez un PC, accordez la permission une seule fois avec :",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurface)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = perm.requiredCommand,
                            color = CyberCyan,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = {
                            viewModel.dismissSystemDialog()
                            viewModel.authorizeShizukuDirectly()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black)
                    ) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("AUTORISER SHIZUKU (SANS PC)", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Button(
                        onClick = {
                            clipboard.setText(AnnotatedString(perm.requiredCommand))
                            viewModel.showNotification("Commande de permission copiée !")
                            viewModel.dismissSystemDialog()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurface, contentColor = TextPrimary)
                    ) {
                        Text("COPIER", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        viewModel.dismissSystemDialog()
                        viewModel.selectTab(1)
                    }
                ) {
                    Text("GUIDE SETUP ADB ▸", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkSurfaceElevated
        )
    }

    // Boîte de dialogue explicative complète des autorisations (ouverte au clic)
    if (showPermissionGuideDialog) {
        val clipboard = LocalClipboardManager.current
        val permCmd = "adb shell pm grant com.example android.permission.WRITE_SECURE_SETTINGS"
        AlertDialog(
            onDismissRequest = { viewModel.closePermissionGuide() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AUTORISATION SYSTÈME", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Où et comment autoriser le changement d'écran ?",
                        color = CyberCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Android protège la résolution d'affichage globale. Pour que l'application puisse modifier l'écran d'un seul clic sans jamais ouvrir de terminal, voici les 3 méthodes :",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "1. SANS ORDINATEUR (100% SUR LE TÉLÉPHONE) :",
                        color = CyberAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Installez l'application Shizuku (Google Play Store).\n• Appuyez directement ci-dessous pour autoriser instantanément sans PC :",
                        color = TextPrimary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = {
                            viewModel.closePermissionGuide()
                            viewModel.authorizeShizukuDirectly()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("btn_dialog_auth_shizuku")
                    ) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("AUTORISER SHIZUKU (SANS PC)", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "2. AVEC UN PC (UNE SEULE COMMANDE UNIQUE ADB) :",
                        color = CyberViolet,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Activez le Débogage USB, branchez votre téléphone au PC et exécutez une seule fois :",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurface)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = permCmd,
                            color = CyberCyan,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "3. TÉLÉPHONE ROOTÉ (MAGISK / KERNELSU) :",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Si l'appareil a les droits SuperUtilisateur (Root), l'application applique la résolution directement sans aucune manipulation.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = {
                            viewModel.closePermissionGuide()
                            viewModel.selectTab(1)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberViolet, contentColor = Color.White)
                    ) {
                        Text("GUIDE COMPLET ▸", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Button(
                        onClick = {
                            clipboard.setText(AnnotatedString(permCmd))
                            viewModel.showNotification("Commande de permission copiée !")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("COPIER", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        viewModel.refreshPermissionStatus()
                        viewModel.closePermissionGuide()
                    }
                ) {
                    Text("VÉRIFIER / FERMER", color = TextPrimary, fontSize = 11.sp)
                }
            },
            containerColor = DarkSurfaceElevated
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ==========================================
        // 1. HEADER HUD : NATIVE VS RENDER DISPLAY
        // ==========================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_resolution_hud"),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkOutline))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SLM DISPLAY PIPELINE",
                        color = CyberCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )

                    // Badge SLM Auto AI
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurfaceElevated)
                            .clickable { viewModel.runAutoAiOptimization() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "SLM AUTO AI",
                                tint = CyberAmber,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "SLM AUTO AI",
                                color = CyberAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // DISPLAY NATIVE
                    Column {
                        Text("DISPLAY (NATIVE)", color = TextSecondary, fontSize = 11.sp)
                        Text(
                            text = "${currentConfig.nativeWidth} × ${currentConfig.nativeHeight}",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${viewModel.deviceInfo.refreshRateHz.toInt()} Hz • ${viewModel.deviceInfo.densityDpi} DPI",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    // FLÈCHE DE PIPELINE
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SCALING", color = CyberViolet, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "→",
                            color = CyberCyan,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // RENDER BUFFER
                    Column(horizontalAlignment = Alignment.End) {
                        Text("RENDER BUFFER", color = TextSecondary, fontSize = 11.sp)
                        Text(
                            text = "${currentConfig.renderWidth} × ${currentConfig.renderHeight}",
                            color = CyberCyan,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Stretch : X${"%.2f".format(currentConfig.effectiveRatioX)} Y${"%.2f".format(currentConfig.effectiveRatioY)}",
                            color = CyberAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // ==========================================
        // SÉLECTEUR DE MODE : BARRES (SLIDERS) vs ÉCRIRE LES NORMES
        // ==========================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("mode_selector_row"),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Mode 0 : BARRES (SLIDERS)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (inputMode == 0) CyberCyan.copy(alpha = 0.2f) else DarkSurface)
                    .border(
                        width = 1.dp,
                        color = if (inputMode == 0) CyberCyan else DarkOutline,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { viewModel.setInputMode(0) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = if (inputMode == 0) CyberCyan else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "BARRES (SLIDERS)",
                        color = if (inputMode == 0) CyberCyan else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Mode 1 : ÉCRIRE LES NORMES
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (inputMode == 1) CyberViolet.copy(alpha = 0.25f) else DarkSurface)
                    .border(
                        width = 1.dp,
                        color = if (inputMode == 1) CyberViolet else DarkOutline,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { viewModel.setInputMode(1) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = if (inputMode == 1) CyberViolet else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ÉCRIRE LES NORMES",
                        color = if (inputMode == 1) CyberViolet else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // ==========================================
        // CAS 1 : MODE "ÉCRIRE LES NORMES" (SAISIE DIRECTE)
        // ==========================================
        if (inputMode == 1) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_direct_norm_input"),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberViolet))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = CyberViolet, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SAISIE DIRECTE DES NORMES",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Entrez manuellement les dimensions de résolution ou choisissez une norme d'affichage courante :",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    // Champs Largeur et Hauteur
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = widthInputText,
                            onValueChange = { viewModel.updateWidthInput(it) },
                            label = { Text("Largeur (px)", fontSize = 11.sp) },
                            placeholder = { Text("ex: 720") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_width"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = DarkOutline,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        OutlinedTextField(
                            value = heightInputText,
                            onValueChange = { viewModel.updateHeightInput(it) },
                            label = { Text("Hauteur (px)", fontSize = 11.sp) },
                            placeholder = { Text("ex: 2400") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_height"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberViolet,
                                unfocusedBorderColor = DarkOutline,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Champ DPI
                    OutlinedTextField(
                        value = dpiInputText,
                        onValueChange = { viewModel.updateDpiInput(it) },
                        label = { Text("Densité d'affichage (DPI recommandé)", fontSize = 11.sp) },
                        placeholder = { Text("ex: 280") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_dpi"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberAmber,
                            unfocusedBorderColor = DarkOutline,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "NORMES D'AFFICHAGE CLASSIQUES :",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Normes standard en boutons 1-clic
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        viewModel.standardNormPresets.forEach { preset ->
                            val isSelected = currentConfig.renderWidth == preset.width && currentConfig.renderHeight == preset.height
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) CyberViolet.copy(alpha = 0.3f) else DarkSurfaceElevated)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) CyberViolet else DarkOutline,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.applyPresetNorm(preset) }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Column {
                                    Text(
                                        text = preset.name,
                                        color = if (isSelected) CyberCyan else TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${preset.width} × ${preset.height}",
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // CAS 2 : MODE "BARRES (SLIDERS)"
        // ==========================================
        if (inputMode == 0) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_stretch_x"),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkOutline))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "STRETCH X (HORIZONTAL)",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "X${"%.2f".format(currentConfig.stretchFactorX)}",
                        color = CyberCyan,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = "Largeur de rendu : ${currentConfig.renderWidth} px → Écran : ${currentConfig.nativeWidth} px",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Slider(
                    value = currentConfig.stretchFactorX,
                    onValueChange = { viewModel.setStretchFactorX(it) },
                    valueRange = 1.00f..viewModel.maxStretchX,
                    colors = SliderDefaults.colors(
                        thumbColor = CyberCyan,
                        activeTrackColor = CyberCyan,
                        inactiveTrackColor = DarkOutline
                    ),
                    modifier = Modifier.testTag("slider_stretch_x")
                )

                // Paliers rapides
                val presetX = listOf(1.00f, 1.10f, 1.25f, 1.50f, 2.00f, 3.00f, viewModel.maxStretchX)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    presetX.forEach { factor ->
                        val isSelected = Math.abs(currentConfig.stretchFactorX - factor) < 0.03f
                        val label = if (factor == viewModel.maxStretchX) "MAX" else "X${"%.2f".format(factor)}"
                        PresetChip(
                            label = label,
                            isSelected = isSelected,
                            onClick = { viewModel.setStretchFactorX(factor) }
                        )
                    }
                }
            }
        }

        // ==========================================
        // 3. STRETCH FACTOR Y (VERTICAL)
        // ==========================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_stretch_y"),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkOutline))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "STRETCH Y (VERTICAL)",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Y${"%.2f".format(currentConfig.stretchFactorY)}",
                        color = CyberViolet,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = "Hauteur de rendu : ${currentConfig.renderHeight} px → Écran : ${currentConfig.nativeHeight} px",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Slider(
                    value = currentConfig.stretchFactorY,
                    onValueChange = { viewModel.setStretchFactorY(it) },
                    valueRange = 1.00f..viewModel.maxStretchY,
                    colors = SliderDefaults.colors(
                        thumbColor = CyberViolet,
                        activeTrackColor = CyberViolet,
                        inactiveTrackColor = DarkOutline
                    ),
                    modifier = Modifier.testTag("slider_stretch_y")
                )

                // Paliers rapides
                val presetY = listOf(1.00f, 1.10f, 1.25f, 1.50f, 2.00f, 3.00f, viewModel.maxStretchY)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    presetY.forEach { factor ->
                        val isSelected = Math.abs(currentConfig.stretchFactorY - factor) < 0.03f
                        val label = if (factor == viewModel.maxStretchY) "MAX" else "Y${"%.2f".format(factor)}"
                        PresetChip(
                            label = label,
                            isSelected = isSelected,
                            onClick = { viewModel.setStretchFactorY(factor) }
                        )
                    }
                }
            }
        }

        // ==========================================
        // 4. FREE STRETCH
        // ==========================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_free_stretch"),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkOutline))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "FREE STRETCH",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Ajustement indépendant largeur / hauteur de rendu",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = currentConfig.isFreeStretch,
                        onCheckedChange = { viewModel.toggleFreeStretch(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyberCyan,
                            checkedTrackColor = DarkSurfaceElevated,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = DarkOutline
                        )
                    )
                }

                AnimatedVisibility(visible = currentConfig.isFreeStretch) {
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        // Slider Render Width
                        Text(
                            text = "RENDER WIDTH : ${currentConfig.renderWidth} px",
                            color = CyberCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Slider(
                            value = currentConfig.renderWidth.toFloat(),
                            onValueChange = {
                                viewModel.setRenderResolution(it.toInt(), currentConfig.renderHeight)
                            },
                            valueRange = 160f..currentConfig.nativeWidth.toFloat(),
                            colors = SliderDefaults.colors(thumbColor = CyberCyan, activeTrackColor = CyberCyan)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Slider Render Height
                        Text(
                            text = "RENDER HEIGHT : ${currentConfig.renderHeight} px",
                            color = CyberViolet,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Slider(
                            value = currentConfig.renderHeight.toFloat(),
                            onValueChange = {
                                viewModel.setRenderResolution(currentConfig.renderWidth, it.toInt())
                            },
                            valueRange = 160f..currentConfig.nativeHeight.toFloat(),
                            colors = SliderDefaults.colors(thumbColor = CyberViolet, activeTrackColor = CyberViolet)
                        )

                        // Boutons d'exemples rapides (demandés dans la consigne)
                        Text(
                            text = "Configurations types :",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ResolutionChip("720 × 2460", 720, 2460, currentConfig) { w, h -> viewModel.setRenderResolution(w, h) }
                            ResolutionChip("1080 × 1920", 1080, 1920, currentConfig) { w, h -> viewModel.setRenderResolution(w, h) }
                            ResolutionChip("720 × 1920", 720, 1920, currentConfig) { w, h -> viewModel.setRenderResolution(w, h) }
                            ResolutionChip("960 × 2460", 960, 2460, currentConfig) { w, h -> viewModel.setRenderResolution(w, h) }
                        }
                    }
                }
            }
        }
        } // Fin if (inputMode == 0)

        // ==========================================
        // 5. SLM DISPLAY SCALING
        // ==========================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_scaling_modes"),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkOutline))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "SLM DISPLAY SCALING",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Mode de mise à l'échelle vers la résolution native",
                    color = TextSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Modes de scaling
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ScalingMode.values().forEach { mode ->
                        FilterChip(
                            selected = currentConfig.scalingMode == mode,
                            onClick = { viewModel.setScalingMode(mode) },
                            label = { Text(mode.label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DarkSurfaceElevated,
                                selectedLabelColor = CyberCyan
                            )
                        )
                    }
                }

                Text(
                    text = currentConfig.scalingMode.description,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Moteur de scaling (Automatic / GPU / Display)
                Text(
                    text = "MOTEUR D'EXÉCUTION DU SCALING",
                    color = CyberCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    ScalingEngine.values().forEach { engine ->
                        FilterChip(
                            selected = currentConfig.scalingEngine == engine,
                            onClick = { viewModel.setScalingEngine(engine) },
                            label = { Text(engine.label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DarkSurfaceElevated,
                                selectedLabelColor = CyberAmber
                            )
                        )
                    }
                }

                Text(
                    text = currentConfig.scalingEngine.technicalDetail,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // ==========================================
        // 6. SLM AI GRAPHICS
        // ==========================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_ai_graphics"),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkOutline))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SLM AI GRAPHICS",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Super-résolution & reconstruction tensorielle de bords",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Text(
                        text = currentConfig.aiGraphicsMode.label,
                        color = if (currentConfig.aiGraphicsMode == AiGraphicsMode.OFF) TextSecondary else CyberCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sélecteur de mode
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AiGraphicsMode.values().forEach { mode ->
                        FilterChip(
                            selected = currentConfig.aiGraphicsMode == mode,
                            onClick = { viewModel.setAiGraphicsMode(mode) },
                            label = { Text(mode.label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DarkSurfaceElevated,
                                selectedLabelColor = if (mode == AiGraphicsMode.OFF) TextSecondary else CyberCyan
                            )
                        )
                    }
                }

                Text(
                    text = currentConfig.aiGraphicsMode.description,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )

                if (currentConfig.aiGraphicsMode != AiGraphicsMode.OFF) {
                    Spacer(modifier = Modifier.height(14.dp))

                    // AI SHARPNESS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("AI SHARPNESS", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("${(currentConfig.aiSharpness * 100).toInt()} %", color = CyberCyan, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                    Slider(
                        value = currentConfig.aiSharpness,
                        onValueChange = { viewModel.setAiSharpness(it) },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(thumbColor = CyberCyan, activeTrackColor = CyberCyan)
                    )

                    // AI DETAIL
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("AI DETAIL", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("${(currentConfig.aiDetail * 100).toInt()} %", color = CyberAmber, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                    Slider(
                        value = currentConfig.aiDetail,
                        onValueChange = { viewModel.setAiDetail(it) },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(thumbColor = CyberAmber, activeTrackColor = CyberAmber)
                    )

                    // AI QUALITY
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("AI QUALITY", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("${(currentConfig.aiQuality * 100).toInt()} %", color = CyberViolet, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                    Slider(
                        value = currentConfig.aiQuality,
                        onValueChange = { viewModel.setAiQuality(it) },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(thumbColor = CyberViolet, activeTrackColor = CyberViolet)
                    )
                }
            }
        }

        // ==========================================
        // APPLICATION DIRECTE SUR L'ÉCRAN (SANS TERMINAL)
        // ==========================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_direct_system_apply"),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberCyan))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "APPLICATION DIRECTE SUR L'ÉCRAN",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Applique instantanément cette résolution (${currentConfig.renderWidth}×${currentConfig.renderHeight}) à votre écran sans avoir besoin d'ouvrir un terminal.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                )

                // Bandeau d'état de l'autorisation système
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isPermissionGranted) Color(0xFF00E676).copy(alpha = 0.15f) else CyberAmber.copy(alpha = 0.15f))
                        .border(
                            width = 1.dp,
                            color = if (isPermissionGranted) Color(0xFF00E676) else CyberAmber,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { viewModel.selectTab(1) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isPermissionGranted) Color(0xFF00E676) else CyberAmber)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPermissionGranted) "AUTORISATION SYSTÈME ACTIVE" else "AUTORISATION REQUISE (NON ACCORDÉE)",
                            color = if (isPermissionGranted) Color(0xFF00E676) else CyberAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = if (isPermissionGranted) "CONFIGURÉ ✓" else "GUIDE SETUP ADB ▸",
                        color = if (isPermissionGranted) Color(0xFF00E676) else CyberAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (!isPermissionGranted) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.authorizeShizukuDirectly() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_quick_auth_shizuku_card"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberCyan,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SANS PC : AUTORISER SHIZUKU (1-CLIC)", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Bouton Appliquer directement sans terminal
                    Button(
                        onClick = { viewModel.applyToScreenDirectly() },
                        enabled = !isExecutingDirectly,
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("btn_apply_screen_direct"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberCyan,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isExecutingDirectly) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("APPLICATION...", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        } else {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("APPLIQUER SUR L'ÉCRAN", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    // Bouton Réinitialiser l'écran en 1 clic
                    OutlinedButton(
                        onClick = { viewModel.revertScreenResolution() },
                        enabled = !isExecutingDirectly,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_reset_screen_direct"),
                        shape = RoundedCornerShape(8.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(CyberAmber))
                    ) {
                        Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, tint = CyberAmber, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("RESET ÉCRAN", color = CyberAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Protection de sécurité : Un compte à rebours de 15 secondes se déclenche pour vous permettre de vérifier l'écran. En l'absence de confirmation, l'affichage revient automatiquement à la normale.",
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }
        }

        // ==========================================
        // 7. GÉNÉRATEUR DE COMMANDES ADB (SYSTÈME & JEUX)
        // ==========================================
        val clipboardManager = LocalClipboardManager.current
        val targetDpi = ((viewModel.deviceInfo.densityDpi * currentConfig.renderWidth.toFloat()) / currentConfig.nativeWidth.toFloat()).toInt().coerceIn(120, 800)
        val fullAdbStretchCmd = "adb shell \"wm size ${currentConfig.renderWidth}x${currentConfig.renderHeight} && wm density $targetDpi\""
        val adbResetCmd = "adb shell \"wm size reset && wm density reset\""

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_adb_generator"),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkOutline))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GÉNÉRATEUR DE COMMANDES ADB",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = "Pour appliquer cet étirement réel à tout le système Android ou aux jeux externes via le Débogage USB :",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                )

                // 1. Commande wm size
                Text(
                    text = "1. Forcer la résolution étirée (wm size) :",
                    color = CyberCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkSurfaceElevated)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "adb shell wm size ${currentConfig.renderWidth}x${currentConfig.renderHeight}",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 2. Commande wm density
                Text(
                    text = "2. Ajustement proportionnel du DPI (évite les bugs d'UI) :",
                    color = CyberAmber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkSurfaceElevated)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "adb shell wm density $targetDpi",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bouton Copier commande complète
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(fullAdbStretchCmd))
                            viewModel.showNotification("Commande ADB complète copiée dans le presse-papier !")
                        },
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("COPIER ÉTIREMENT ADB", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(adbResetCmd))
                            viewModel.showNotification("Commande de réinitialisation ADB copiée !")
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(6.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(DarkOutline))
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = CyberAmber, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("RESET ADB", color = CyberAmber, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Astuce Jeux : Pour réduire la résolution d'un jeu sans déformer l'UI globale :\nadb shell cmd game set --mode 2 --downscale 0.75 <nom.du.paquet>",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // ==========================================
        // 8. ACTIONS : APPLY / RESET / RESTORE DEFAULT
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // APPLY
            Button(
                onClick = { viewModel.applyConfig() },
                modifier = Modifier
                    .weight(1.5f)
                    .testTag("apply_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (hasPendingChanges) "APPLY *" else "APPLY",
                    fontWeight = FontWeight.Bold
                )
            }

            // RESET
            OutlinedButton(
                onClick = { viewModel.resetToApplied() },
                modifier = Modifier
                    .weight(1f)
                    .testTag("reset_button"),
                shape = RoundedCornerShape(8.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(DarkOutline))
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("RESET", color = TextSecondary)
            }

            // RESTORE DEFAULT
            OutlinedButton(
                onClick = { viewModel.restoreDefaultConfig() },
                modifier = Modifier
                    .weight(1f)
                    .testTag("restore_default_button"),
                shape = RoundedCornerShape(8.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(DarkOutline))
            ) {
                Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, tint = CyberAmber, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("DEFAULT", color = CyberAmber)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun PresetChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) CyberCyan.copy(alpha = 0.2f) else DarkSurfaceElevated)
            .border(
                width = 1.dp,
                color = if (isSelected) CyberCyan else DarkOutline,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) CyberCyan else TextPrimary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun ResolutionChip(
    label: String,
    width: Int,
    height: Int,
    currentConfig: com.example.slm.model.SlmConfig,
    onClick: (Int, Int) -> Unit
) {
    val isSelected = currentConfig.renderWidth == width && currentConfig.renderHeight == height
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) CyberViolet.copy(alpha = 0.25f) else DarkSurfaceElevated)
            .border(
                width = 1.dp,
                color = if (isSelected) CyberViolet else DarkOutline,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable { onClick(width, height) }
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) CyberViolet else TextSecondary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
