package com.example.slm.system

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Gestionnaire d'intégration Shizuku officiel.
 * Permet à l'utilisateur d'autoriser l'application 100% sans PC en un seul clic.
 */
object ShizukuExecutor {

    private const val TAG = "ShizukuExecutor"
    const val SHIZUKU_REQUEST_CODE = 9921

    /**
     * Vérifie si le service Shizuku est actif et fonctionnel sur l'appareil
     */
    fun isShizukuInstalledAndRunning(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Throwable) {
            Log.w(TAG, "Shizuku non disponible ou non actif: ${e.message}")
            false
        }
    }

    /**
     * Vérifie si l'application a déjà l'autorisation d'utiliser Shizuku
     */
    fun hasShizukuPermission(): Boolean {
        return try {
            if (!isShizukuInstalledAndRunning()) return false
            if (Shizuku.isPreV11()) return false
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Throwable) {
            Log.w(TAG, "Erreur checkSelfPermission Shizuku: ${e.message}")
            false
        }
    }

    /**
     * Ouvre la boîte de dialogue système Shizuku : "Autoriser SLM Stretch à utiliser Shizuku ?"
     */
    fun requestShizukuPermission(requestCode: Int = SHIZUKU_REQUEST_CODE) {
        try {
            if (isShizukuInstalledAndRunning()) {
                if (!hasShizukuPermission()) {
                    Shizuku.requestPermission(requestCode)
                }
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Erreur lors de la demande de permission Shizuku", e)
        }
    }

    /**
     * Enregistre un écouteur sur le résultat de la demande de permission Shizuku
     */
    fun registerPermissionListener(listener: Shizuku.OnRequestPermissionResultListener) {
        try {
            Shizuku.addRequestPermissionResultListener(listener)
        } catch (e: Throwable) {
            Log.e(TAG, "Erreur addRequestPermissionResultListener", e)
        }
    }

    fun unregisterPermissionListener(listener: Shizuku.OnRequestPermissionResultListener) {
        try {
            Shizuku.removeRequestPermissionResultListener(listener)
        } catch (e: Throwable) {
            Log.e(TAG, "Erreur removeRequestPermissionResultListener", e)
        }
    }

    private fun startShizukuProcess(cmd: Array<String>): Process? {
        return try {
            val method = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            method.isAccessible = true
            method.invoke(null, cmd, null, null) as? Process
        } catch (e: Throwable) {
            Log.e(TAG, "Erreur invocation Shizuku.newProcess", e)
            null
        }
    }

    /**
     * Accorde automatiquement WRITE_SECURE_SETTINGS via le shell privilégié de Shizuku
     * Sans aucun terminal ni manipulation PC requise.
     */
    fun grantWriteSecureSettings(packageName: String): Boolean {
        return try {
            if (!hasShizukuPermission()) {
                Log.w(TAG, "Permission Shizuku non accordée")
                return false
            }

            val cmd = arrayOf("pm", "grant", packageName, "android.permission.WRITE_SECURE_SETTINGS")
            val process = startShizukuProcess(cmd) ?: return false
            val exitCode = process.waitFor()

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
            val output = reader.readText()
            val error = errorReader.readText()

            Log.d(TAG, "Grant via Shizuku exitCode=$exitCode out=$output err=$error")
            exitCode == 0
        } catch (e: Throwable) {
            Log.e(TAG, "Échec de l'octroi de permission via Shizuku", e)
            false
        }
    }

    /**
     * Exécute une commande shell wm directement via Shizuku
     */
    fun executeWmCommand(vararg args: String): Boolean {
        return try {
            if (!hasShizukuPermission()) return false
            val cmd = arrayOf("wm") + args
            val process = startShizukuProcess(cmd) ?: return false
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (e: Throwable) {
            Log.e(TAG, "Échec commande wm via Shizuku", e)
            false
        }
    }
}
