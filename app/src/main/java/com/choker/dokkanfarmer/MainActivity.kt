package com.choker.dokkanfarmer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnAccessibility = findViewById<Button>(R.id.btnAccessibility)
        val btnOverlay = findViewById<Button>(R.id.btnOverlay)
        val btnLaunch = findViewById<Button>(R.id.btnLaunch)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)

        // Étape 1 : activer le service d'accessibilité
        btnAccessibility.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        // Étape 2 : autoriser l'overlay (affichage par-dessus d'autres apps)
        btnOverlay.setOnClickListener {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }

        // Étape 3 : lancer l'overlay flottant puis ouvrir Dokkan
        btnLaunch.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                tvStatus.text = "⚠️ Autorise d'abord l'affichage par-dessus les autres apps (étape 2)"
                return@setOnClickListener
            }
            if (!isAccessibilityEnabled()) {
                tvStatus.text = "⚠️ Active d'abord le service d'accessibilité (étape 1)"
                return@setOnClickListener
            }

            // Lancer l'overlay
            startService(Intent(this, OverlayService::class.java))

            // Ouvrir Dokkan Battle directement
            val dokkanIntent = packageManager.getLaunchIntentForPackage("jp.co.bandainamcoent.DBZDOKKAN")
            if (dokkanIntent != null) {
                startActivity(dokkanIntent)
            } else {
                tvStatus.text = "Dokkan Battle non trouvé — lance-le manuellement"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val accessOk = isAccessibilityEnabled()
        val overlayOk = Settings.canDrawOverlays(this)

        tvStatus.text = buildString {
            append(if (accessOk) "✅" else "❌")
            append(" Service accessibilité\n")
            append(if (overlayOk) "✅" else "❌")
            append(" Permission overlay\n\n")
            if (accessOk && overlayOk) append("Tout est prêt — clique Lancer !")
            else append("Complete les étapes 1 et 2 avant de lancer")
        }
    }

    private fun isAccessibilityEnabled(): Boolean {
        val service = "$packageName/${FarmingService::class.java.canonicalName}"
        val enabled = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabled.contains(service)
    }
}
