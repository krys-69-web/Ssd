package com.example.slm.system

import android.content.Context
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Exécuteur direct des commandes de résolution et d'étirement Android sans terminal.
 * Tente d'appliquer les normes de résolution via shell local, Shizuku ou Root,
 * et gère la restauration de sécurité en cas de mauvaise manipulation.
 */
object SystemResolutionExecutor {

    sealed class ExecutionResult {
        data class Success(val message: String, val appliedWidth: Int, val appliedHeight: Int, val appliedDpi: Int) : ExecutionResult()
        data class PermissionRequired(
            val message: String,
            val requiredCommand: String,
            val isShizukuAvailable: Boolean
        ) : ExecutionResult()
        data class Error(val errorMessage: String) : ExecutionResult()
    }

    /**
     * Tente d'appliquer directement la résolution et la densité sur l'écran sans que l'utilisateur
     * n'ait besoin d'ouvrir un terminal.
     */
    suspend fun applyResolutionDirectly(
        context: Context,
        width: Int,
        height: Int,
        densityDpi: Int
    ): ExecutionResult = withContext(Dispatchers.IO) {
        val sizeCmd = "wm size ${width}x${height}"
        val densityCmd = "wm density $densityDpi"
        val fullCommand = "$sizeCmd && $densityCmd"

        // 1. Tenter via Shizuku si autorisé
        if (ShizukuExecutor.hasShizukuPermission()) {
            val sizeOk = ShizukuExecutor.executeWmCommand("size", "${width}x${height}")
            val densityOk = ShizukuExecutor.executeWmCommand("density", "$densityDpi")
            if (sizeOk && densityOk) {
                return@withContext ExecutionResult.Success(
                    message = "Résolution appliquée via Shizuku : ${width}×${height} à $densityDpi DPI",
                    appliedWidth = width,
                    appliedHeight = height,
                    appliedDpi = densityDpi
                )
            }
        }

        // 2. Tenter l'exécution directe via sh standard (fonctionne si permission accordée)
        val shResult = runShellCommand(fullCommand)
        if (shResult.isSuccess) {
            return@withContext ExecutionResult.Success(
                message = "Résolution appliquée directement : ${width}×${height} à $densityDpi DPI",
                appliedWidth = width,
                appliedHeight = height,
                appliedDpi = densityDpi
            )
        }

        // 3. Tenter via su (si l'appareil est rooté)
        val suResult = runSuCommand(fullCommand)
        if (suResult.isSuccess) {
            return@withContext ExecutionResult.Success(
                message = "Résolution appliquée via Root : ${width}×${height} à $densityDpi DPI",
                appliedWidth = width,
                appliedHeight = height,
                appliedDpi = densityDpi
            )
        }

        // 4. Si permission bloquée par SELinux / Android Security
        val isShizukuInstalled = ShizukuExecutor.isShizukuInstalledAndRunning() || isPackageInstalled(context, "moe.shizuku.privileged.api")
        val errorDetail = shResult.errorOutput.ifBlank { "Permission refusée par le système Android." }

        ExecutionResult.PermissionRequired(
            message = "Android requiert une autorisation système pour modifier la résolution globale : $errorDetail",
            requiredCommand = "adb shell pm grant ${context.packageName} android.permission.WRITE_SECURE_SETTINGS",
            isShizukuAvailable = isShizukuInstalled
        )
    }

    /**
     * Rétablit immédiatement la résolution native physique de l'écran sans terminal.
     */
    suspend fun resetResolutionDirectly(): ExecutionResult = withContext(Dispatchers.IO) {
        if (ShizukuExecutor.hasShizukuPermission()) {
            val sizeReset = ShizukuExecutor.executeWmCommand("size", "reset")
            val densityReset = ShizukuExecutor.executeWmCommand("density", "reset")
            if (sizeReset && densityReset) {
                return@withContext ExecutionResult.Success(
                    message = "Écran réinitialisé à sa résolution native via Shizuku",
                    appliedWidth = 0,
                    appliedHeight = 0,
                    appliedDpi = 0
                )
            }
        }

        val resetCmd = "wm size reset && wm density reset"
        val shResult = runShellCommand(resetCmd)
        if (shResult.isSuccess) {
            return@withContext ExecutionResult.Success(
                message = "Écran réinitialisé à sa résolution d'origine",
                appliedWidth = 0,
                appliedHeight = 0,
                appliedDpi = 0
            )
        }

        val suResult = runSuCommand(resetCmd)
        if (suResult.isSuccess) {
            return@withContext ExecutionResult.Success(
                message = "Écran réinitialisé avec succès via Root",
                appliedWidth = 0,
                appliedHeight = 0,
                appliedDpi = 0
            )
        }

        ExecutionResult.Error("Impossible de réinitialiser directement : ${shResult.errorOutput}")
    }

    /**
     * Vérifie si un override de résolution est actuellement actif sur le système.
     */
    suspend fun checkCurrentSystemResolution(): Pair<String?, String?> = withContext(Dispatchers.IO) {
        val sizeOut = runShellCommand("wm size").standardOutput
        val densityOut = runShellCommand("wm density").standardOutput
        Pair(sizeOut.ifBlank { null }, densityOut.ifBlank { null })
    }

    private data class ShellResult(val isSuccess: Boolean, val standardOutput: String, val errorOutput: String)

    private fun runShellCommand(command: String): ShellResult {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val stdOut = BufferedReader(InputStreamReader(process.inputStream)).use { it.readText() }
            val errOut = BufferedReader(InputStreamReader(process.errorStream)).use { it.readText() }
            val exitCode = process.waitFor()
            ShellResult(
                isSuccess = exitCode == 0 && errOut.isBlank(),
                standardOutput = stdOut.trim(),
                errorOutput = errOut.trim()
            )
        } catch (e: Exception) {
            ShellResult(isSuccess = false, standardOutput = "", errorOutput = e.localizedMessage ?: "Exception inconnue")
        }
    }

    private fun runSuCommand(command: String): ShellResult {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            val stdOut = BufferedReader(InputStreamReader(process.inputStream)).use { it.readText() }
            val errOut = BufferedReader(InputStreamReader(process.errorStream)).use { it.readText() }
            val exitCode = process.waitFor()
            ShellResult(
                isSuccess = exitCode == 0,
                standardOutput = stdOut.trim(),
                errorOutput = errOut.trim()
            )
        } catch (e: Exception) {
            ShellResult(isSuccess = false, standardOutput = "", errorOutput = e.localizedMessage ?: "Pas de root")
        }
    }

    private fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Vérifie si l'application dispose déjà de la permission WRITE_SECURE_SETTINGS.
     */
    fun hasWriteSecureSettings(context: Context): Boolean {
        return context.checkSelfPermission(android.Manifest.permission.WRITE_SECURE_SETTINGS) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    /**
     * Vérifie si un binaire SU (Root) est accessible.
     */
    fun isDeviceRooted(): Boolean {
        val test = runSuCommand("id")
        return test.isSuccess && test.standardOutput.contains("uid=0")
    }
}
