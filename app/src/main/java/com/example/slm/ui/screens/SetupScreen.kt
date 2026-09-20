package com.example.slm.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.slm.ui.SlmViewModel
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

/**
 * Écran complet de configuration des permissions ADB pour 'WRITE_SECURE_SETTINGS'.
 * Guide pas-à-pas l'utilisateur pour activer l'autorisation système requise
 * pour modifier la résolution globale et le ratio d'étirement sans terminal.
 */
@Composable
fun SetupScreen(
    viewModel: SlmViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isPermissionGranted by viewModel.isPermissionGranted.collectAsState()
    val scrollState = rememberScrollState()

    // 0 = Méthode PC (ADB), 1 = Sans PC (Shizuku), 2 = Root
    var selectedMethodTab by remember { mutableIntStateOf(0) }

    // 0 = CMD Standard, 1 = PowerShell, 2 = macOS/Linux
    var osShellVariant by remember { mutableIntStateOf(0) }

    // État FAQ déroulée
    var expandedFaqIndex by remember { mutableStateOf<Int?>(null) }

    val baseAdbCommand = "adb shell pm grant ${context.packageName} android.permission.WRITE_SECURE_SETTINGS"
    val powerShellCommand = ".\\adb shell pm grant ${context.packageName} android.permission.WRITE_SECURE_SETTINGS"
    val macLinuxCommand = "./adb shell pm grant ${context.packageName} android.permission.WRITE_SECURE_SETTINGS"

    val currentCommandToDisplay = when (osShellVariant) {
        1 -> powerShellCommand
        2 -> macLinuxCommand
        else -> baseAdbCommand
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ==========================================
        // 1. CARTE DE STATUT EN TEMPS RÉEL
        // ==========================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_status_banner"),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(14.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(
                    if (isPermissionGranted) Color(0xFF00E676) else CyberAmber
                )
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isPermissionGranted) Color(0xFF00E676).copy(alpha = 0.2f)
                                    else CyberAmber.copy(alpha = 0.2f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPermissionGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isPermissionGranted) Color(0xFF00E676) else CyberAmber,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isPermissionGranted) "AUTORISATION ACTIVE" else "AUTORISATION REQUISE",
                                color = if (isPermissionGranted) Color(0xFF00E676) else CyberAmber,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "WRITE_SECURE_SETTINGS",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Bouton de vérification rapide
                    OutlinedButton(
                        onClick = {
                            viewModel.refreshPermissionStatus()
                            if (viewModel.isPermissionGranted.value) {
                                viewModel.showNotification("Succès : Permission active !")
                            } else {
                                viewModel.showNotification("Permission non encore accordée. Suivez les étapes ci-dessous.")
                            }
                        },
                        modifier = Modifier.testTag("btn_check_permission"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("VÉRIFIER", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isPermissionGranted) {
                        "Félicitations ! L'application dispose des droits système requis. Vous pouvez maintenant appliquer directement l'étirement et les résolutions d'écran sans terminal."
                    } else {
                        "Pour modifier la résolution et l'étirement réels de votre écran sans ouvrir de terminal au quotidien, Android requiert d'accorder cette permission système une seule fois."
                    },
                    color = TextPrimary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )

                if (isPermissionGranted) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { viewModel.selectTab(0) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_test_stretch_now"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00E676),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ALLER AUX COMMANDES D'ÉTIRAGE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                } else {
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { viewModel.authorizeShizukuDirectly() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_header_authorize_shizuku"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberCyan,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SANS PC : AUTORISER SHIZUKU (1-CLIC)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // ==========================================
        // 2. SÉLECTEUR DE MÉTHODE D'AUTORISATION
        // ==========================================
        Text(
            text = "CHOISISSEZ VOTRE MÉTHODE D'ACTIVATION",
            color = CyberCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MethodTabButton(
                title = "AVEC PC (ADB)",
                subtitle = "Recommandé",
                icon = Icons.Default.Computer,
                isSelected = selectedMethodTab == 0,
                modifier = Modifier.weight(1f)
            ) { selectedMethodTab = 0 }

            MethodTabButton(
                title = "SANS PC (SHIZUKU)",
                subtitle = "100% Mobile",
                icon = Icons.Default.Wifi,
                isSelected = selectedMethodTab == 1,
                modifier = Modifier.weight(1f)
            ) { selectedMethodTab = 1 }

            MethodTabButton(
                title = "ROOT (SU)",
                subtitle = "Magisk / KSU",
                icon = Icons.Default.Security,
                isSelected = selectedMethodTab == 2,
                modifier = Modifier.weight(1f)
            ) { selectedMethodTab = 2 }
        }

        // ==========================================
        // 3. CONTENU SELON LA MÉTHODE CHOISIE
        // ==========================================
        when (selectedMethodTab) {
            0 -> {
                // ==========================================
                // MÉTHODE 1 : GUIDE PAS-À-PAS VIA ADB & PC
                // ==========================================
                AdbStepGuide(
                    context = context,
                    baseAdbCommand = baseAdbCommand,
                    currentCommandToDisplay = currentCommandToDisplay,
                    osShellVariant = osShellVariant,
                    onSelectOsVariant = { osShellVariant = it },
                    onCopyCommand = {
                        clipboardManager.setText(AnnotatedString(currentCommandToDisplay))
                        viewModel.showNotification("Commande ADB copiée dans le presse-papier !")
                    },
                    onShareCommand = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Commande ADB WRITE_SECURE_SETTINGS")
                            putExtra(Intent.EXTRA_TEXT, currentCommandToDisplay)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Partager la commande ADB"))
                    },
                    onVerify = {
                        viewModel.refreshPermissionStatus()
                        if (viewModel.isPermissionGranted.value) {
                            viewModel.showNotification("Autorisation confirmée ! Vous êtes prêt.")
                        } else {
                            viewModel.showNotification("Permission non détectée. Vérifiez que la commande ADB s'est exécutée sans erreur.")
                        }
                    }
                )
            }
            1 -> {
                // ==========================================
                // MÉTHODE 2 : SANS PC (AUTORISER SHIZUKU)
                // ==========================================
                ShizukuGuide(
                    viewModel = viewModel,
                    context = context,
                    onOpenPlayStore = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=moe.shizuku.privileged.api"))
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api"))
                            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(webIntent)
                        }
                    }
                )
            }
            2 -> {
                // ==========================================
                // MÉTHODE 3 : ACCÈS ROOT
                // ==========================================
                RootGuide(
                    onVerify = {
                        viewModel.refreshPermissionStatus()
                        if (viewModel.isPermissionGranted.value) {
                            viewModel.showNotification("Droits Root confirmés !")
                        } else {
                            viewModel.showNotification("Aucun binaire Root détecté sur cet appareil.")
                        }
                    }
                )
            }
        }

        // ==========================================
        // 4. FOIRE AUX QUESTIONS & DÉPANNAGE (FAQ)
        // ==========================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_faq_section"),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkOutline))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.HelpOutline, contentDescription = null, tint = CyberViolet, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "QUESTIONS FRÉQUENTES & DÉPANNAGE",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                FaqItem(
                    index = 0,
                    question = "Pourquoi cette permission est-elle nécessaire ?",
                    answer = "Android protège la résolution globale de l'écran (wm size et wm density) pour empêcher les applications malveillantes de perturber l'affichage. 'WRITE_SECURE_SETTINGS' est l'autorisation officielle conçue par Google pour permettre ce contrôle.",
                    isExpanded = expandedFaqIndex == 0,
                    onToggle = { expandedFaqIndex = if (expandedFaqIndex == 0) null else 0 }
                )

                FaqItem(
                    index = 1,
                    question = "Dois-je refaire cette manipulation à chaque redémarrage ?",
                    answer = "Non ! Une fois accordée via ADB ou Shizuku, l'autorisation reste enregistrée de façon permanente dans le système Android, même après avoir éteint ou redémarré votre téléphone. Elle n'est révoquée que si vous désinstallez l'application.",
                    isExpanded = expandedFaqIndex == 1,
                    onToggle = { expandedFaqIndex = if (expandedFaqIndex == 1) null else 1 }
                )

                FaqItem(
                    index = 2,
                    question = "Utilisateurs Xiaomi, Redmi, POCO (HyperOS / MIUI) :",
                    answer = "Sur les appareils Xiaomi, vous devez obligatoirement activer 'Débogage USB (paramètres de sécurité)' dans les Options développeurs (nécessite d'insérer une carte SIM et d'avoir un compte Mi). Sans cette option, MIUI bloque l'attribution des permissions via ADB.",
                    isExpanded = expandedFaqIndex == 2,
                    onToggle = { expandedFaqIndex = if (expandedFaqIndex == 2) null else 2 }
                )

                FaqItem(
                    index = 3,
                    question = "Comment révoquer l'autorisation plus tard si souhaité ?",
                    answer = "Pour retirer la permission à tout moment, lancez cette commande sur votre PC :\nadb shell pm revoke com.example android.permission.WRITE_SECURE_SETTINGS",
                    isExpanded = expandedFaqIndex == 3,
                    onToggle = { expandedFaqIndex = if (expandedFaqIndex == 3) null else 3 }
                )
            }
        }
    }
}

// =============================================================================
// COMPOSANTS DU GUIDE ADB PAS-À-PAS
// =============================================================================

@Composable
private fun AdbStepGuide(
    context: Context,
    baseAdbCommand: String,
    currentCommandToDisplay: String,
    osShellVariant: Int,
    onSelectOsVariant: (Int) -> Unit,
    onCopyCommand: () -> Unit,
    onShareCommand: () -> Unit,
    onVerify: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        // ÉTAPE 1
        StepCard(
            stepNumber = "1",
            title = "Activer les Options pour les développeurs",
            description = "Ouvrez les Paramètres de votre téléphone > 'À propos du téléphone', puis appuyez 7 fois de suite sur 'Numéro de build' (ou version logicielle) jusqu'à voir le message 'Vous êtes désormais développeur !'."
        ) {
            OutlinedButton(
                onClick = { openDeviceSettings(context) },
                modifier = Modifier.testTag("btn_open_android_settings"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("OUVRIR PARAMÈTRES ANDROID", fontSize = 11.sp)
            }
        }

        // ÉTAPE 2
        StepCard(
            stepNumber = "2",
            title = "Activer le Débogage USB",
            description = "Ouvrez les 'Options pour les développeurs' (situées dans Système ou Paramètres supplémentaires) et activez l'interrupteur 'Débogage USB'."
        ) {
            OutlinedButton(
                onClick = { openDevelopmentSettings(context) },
                modifier = Modifier.testTag("btn_open_dev_settings"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("OUVRIR OPTIONS DÉVELOPPEURS", fontSize = 11.sp)
            }
        }

        // ÉTAPE 3
        StepCard(
            stepNumber = "3",
            title = "Connecter le téléphone à votre PC",
            description = "Branchez votre smartphone à votre ordinateur avec un câble USB. Si une fenêtre contextuelle 'Autoriser le débogage USB ?' apparaît sur votre écran, cochez 'Toujours autoriser sur cet ordinateur' puis validez."
        )

        // ÉTAPE 4
        StepCard(
            stepNumber = "4",
            title = "Exécuter la commande dans votre terminal PC",
            description = "Ouvrez un terminal sur votre PC (CMD, PowerShell ou Terminal macOS/Linux) et exécutez cette commande exacte :"
        ) {
            Column {
                // Sélecteur d'OS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OsVariantChip("Windows (CMD)", 0, osShellVariant) { onSelectOsVariant(0) }
                    OsVariantChip("PowerShell", 1, osShellVariant) { onSelectOsVariant(1) }
                    OsVariantChip("macOS / Linux", 2, osShellVariant) { onSelectOsVariant(2) }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bloc de code avec la commande
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkBackground)
                        .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = currentCommandToDisplay,
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Boutons d'action pour la commande
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onCopyCommand,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_copy_adb_cmd"),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("COPIER", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = onShareCommand,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_share_adb_cmd"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("PARTAGER", fontSize = 11.sp)
                    }
                }
            }
        }

        // ÉTAPE 5
        StepCard(
            stepNumber = "5",
            title = "Vérifier l'activation",
            description = "Dès que vous avez appuyé sur Entrée dans votre terminal PC, appuyez sur le bouton ci-dessous pour confirmer que la permission a bien été accordée :"
        ) {
            Button(
                onClick = onVerify,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_verify_step_final"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberViolet,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("J'AI EXÉCUTÉ LA COMMANDE : VÉRIFIER LE STATUT", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
    }
}

// =============================================================================
// GUIDE SHIZUKU (SANS PC : OPTION AUTORISER SHIZUKU)
// =============================================================================

@Composable
private fun ShizukuGuide(
    viewModel: SlmViewModel,
    context: Context,
    onOpenPlayStore: () -> Unit
) {
    val isShizukuRunning by viewModel.isShizukuRunning.collectAsState()
    val isShizukuPermissionGranted by viewModel.isShizukuPermissionGranted.collectAsState()
    val isPermissionGranted by viewModel.isPermissionGranted.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_shizuku_direct_option"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isShizukuPermissionGranted || isPermissionGranted) Color(0xFF00E676) else CyberCyan
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Icône bouclier/sécurité
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        if (isShizukuPermissionGranted || isPermissionGranted)
                            Color(0xFF00E676).copy(alpha = 0.15f)
                        else
                            CyberCyan.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isShizukuPermissionGranted || isPermissionGranted) Icons.Default.VerifiedUser else Icons.Default.Security,
                    contentDescription = null,
                    tint = if (isShizukuPermissionGranted || isPermissionGranted) Color(0xFF00E676) else CyberCyan,
                    modifier = Modifier.size(30.dp)
                )
            }

            Text(
                text = "OPTION SANS PC : AUTORISER SHIZUKU",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Aucun terminal ni ordinateur nécessaire. Vous avez simplement besoin d'autoriser Shizuku en un clic pour modifier la résolution de l'écran.",
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center
            )

            // Indicateur de statut en temps réel
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkBackground)
                    .border(1.dp, DarkOutline, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (isShizukuPermissionGranted || isPermissionGranted) Color(0xFF00E676)
                            else if (isShizukuRunning) CyberCyan
                            else CyberAmber
                        )
                )
                Text(
                    text = when {
                        isShizukuPermissionGranted || isPermissionGranted -> "SHIZUKU ACCORDÉ & ÉCRAN DÉVERROUILLÉ"
                        isShizukuRunning -> "SERVICE SHIZUKU ACTIF (PRÊT À ÊTRE AUTORISÉ)"
                        else -> "APPLICATION SHIZUKU EN ATTENTE DE DÉMARRAGE"
                    },
                    color = when {
                        isShizukuPermissionGranted || isPermissionGranted -> Color(0xFF00E676)
                        isShizukuRunning -> CyberCyan
                        else -> CyberAmber
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // LE BOUTON D'ACTION PRINCIPAL : AUTORISER SHIZUKU
            Button(
                onClick = {
                    viewModel.authorizeShizukuDirectly()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_authorize_shizuku"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isShizukuPermissionGranted || isPermissionGranted) Color(0xFF00E676) else CyberCyan,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = if (isShizukuPermissionGranted || isPermissionGranted) Icons.Default.CheckCircle else Icons.Default.Bolt,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isShizukuPermissionGranted || isPermissionGranted)
                        "SHIZUKU AUTORISÉ AVEC SUCCÈS ✓"
                    else
                        "AUTORISER SHIZUKU (1-CLIC)",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp
                )
            }

            // Si Shizuku n'est pas encore démarré ou installé
            if (!isShizukuRunning && !isShizukuPermissionGranted && !isPermissionGranted) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val intent = context.packageManager.getLaunchIntentForPackage("moe.shizuku.privileged.api")
                            if (intent != null) {
                                context.startActivity(intent)
                            } else {
                                onOpenPlayStore()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("OUVRIR SHIZUKU", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.refreshPermissionStatus()
                            if (viewModel.isPermissionGranted.value) {
                                viewModel.showNotification("Succès : Permission active !")
                            } else {
                                viewModel.showNotification("Statut vérifié.")
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ACTUALISER", fontSize = 11.sp)
                    }
                }
            } else if (isShizukuPermissionGranted || isPermissionGranted) {
                // Bouton direct pour retourner aux contrôles d'étirement
                OutlinedButton(
                    onClick = { viewModel.selectTab(0) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00E676))
                ) {
                    Icon(imageVector = Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("RETOURNER AUX CONTRÔLES D'ÉTIRAGE ▸", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// =============================================================================
// GUIDE ROOT
// =============================================================================

@Composable
private fun RootGuide(onVerify: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkOutline))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = CyberViolet, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("ACCÈS ROOT DÉTECTÉ AUTOMATIQUEMENT", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Text(
                text = "Si votre smartphone est rooté via Magisk, KernelSU ou APatch, l'application exécute les commandes d'affichage directement avec les privilèges SuperUtilisateur (su).",
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Text(
                text = "Lors du premier changement d'étirement ou de résolution, une invite Magisk / KernelSU vous demandera d'accorder l'accès Root à 'Infinite Stretch'. Cliquez sur 'Accorder'.",
                color = TextPrimary,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onVerify,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CyberViolet, contentColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("TESTER L'ACCÈS SU / ROOT", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
    }
}

// =============================================================================
// COMPOSANTS RÉUTILISABLES DE L'INTERFACE SETUP
// =============================================================================

@Composable
private fun StepCard(
    stepNumber: String,
    title: String,
    description: String,
    content: (@Composable () -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkOutline))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(CyberCyan.copy(alpha = 0.2f))
                        .border(1.dp, CyberCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stepNumber,
                        color = CyberCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = description,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                modifier = Modifier.padding(top = 8.dp, start = 36.dp)
            )

            if (content != null) {
                Box(modifier = Modifier.padding(top = 10.dp, start = 36.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun MethodTabButton(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) DarkSurfaceElevated else DarkSurface)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) CyberCyan else DarkOutline,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) CyberCyan else TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                color = if (isSelected) TextPrimary else TextSecondary,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1
            )
            Text(
                text = subtitle,
                color = if (isSelected) CyberCyan else TextTertiary,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
private fun OsVariantChip(
    name: String,
    index: Int,
    selectedIndex: Int,
    onSelect: () -> Unit
) {
    val isSelected = index == selectedIndex
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) CyberCyan.copy(alpha = 0.2f) else DarkSurfaceElevated)
            .border(1.dp, if (isSelected) CyberCyan else DarkOutline, RoundedCornerShape(6.dp))
            .clickable(onClick = onSelect)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = name,
            color = if (isSelected) CyberCyan else TextSecondary,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun FaqItem(
    index: Int,
    question: String,
    answer: String,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .clickable(onClick = onToggle)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = question,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (isExpanded) "▲" else "▼",
                color = CyberCyan,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(DarkBackground)
                    .padding(10.dp)
            ) {
                Text(
                    text = answer,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

// =============================================================================
// HELPERS D'OUVERTURE DES PARAMÈTRES ANDROID
// =============================================================================

private fun openDeviceSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_DEVICE_INFO_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (e: Exception) {
        try {
            val genericIntent = Intent(Settings.ACTION_SETTINGS)
            genericIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(genericIntent)
        } catch (_: Exception) {}
    }
}

private fun openDevelopmentSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (e: Exception) {
        try {
            val genericIntent = Intent(Settings.ACTION_SETTINGS)
            genericIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(genericIntent)
        } catch (_: Exception) {}
    }
}
