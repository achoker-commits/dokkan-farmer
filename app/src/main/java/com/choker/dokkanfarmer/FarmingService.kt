package com.choker.dokkanfarmer

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent

class FarmingService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false
    private var screenWidth = 1366
    private var screenHeight = 768

    companion object {
        var instance: FarmingService? = null

        // Coordonnées relatives (pourcentage) pour s'adapter à toute taille d'écran
        // Personnage gauche : 22% de la largeur, 88% de la hauteur
        const val CHAR1_X = 0.22f
        const val CHAR1_Y = 0.88f
        // Personnage milieu
        const val CHAR2_X = 0.50f
        const val CHAR2_Y = 0.88f
        // Personnage droite
        const val CHAR3_X = 0.78f
        const val CHAR3_Y = 0.88f
        // Bouton OK / Suivant / Continuer (centre de l'écran, bas)
        const val OK_BTN_X = 0.50f
        const val OK_BTN_Y = 0.72f
        // Bouton "Rejouer" (légèrement à droite du centre)
        const val REPLAY_BTN_X = 0.62f
        const val REPLAY_BTN_Y = 0.78f
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        screenWidth = resources.displayMetrics.widthPixels
        screenHeight = resources.displayMetrics.heightPixels
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // On n'utilise pas les événements — le bot tourne en boucle de temps
    }

    override fun onInterrupt() {
        stopFarming()
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        stopFarming()
    }

    fun startFarming() {
        if (isRunning) return
        isRunning = true
        handler.post(farmingLoop)
    }

    fun stopFarming() {
        isRunning = false
        handler.removeCallbacks(farmingLoop)
    }

    fun isFarming() = isRunning

    // Boucle principale : tape tous les boutons importants en séquence
    private val farmingLoop = object : Runnable {
        override fun run() {
            if (!isRunning) return

            // Phase 1 : attaquer avec les 3 personnages
            tap(CHAR1_X, CHAR1_Y, 0)
            tap(CHAR2_X, CHAR2_Y, 400)
            tap(CHAR3_X, CHAR3_Y, 800)

            // Phase 2 : après 3s, taper OK/Suivant (fin de tour ou fin de bataille)
            handler.postDelayed({ tap(OK_BTN_X, OK_BTN_Y, 0) }, 3000)

            // Phase 3 : après 5s, taper Rejouer (écran fin de stage)
            handler.postDelayed({ tap(REPLAY_BTN_X, REPLAY_BTN_Y, 0) }, 5000)

            // Phase 4 : après 6s, taper encore OK pour valider les récompenses
            handler.postDelayed({ tap(OK_BTN_X, OK_BTN_Y, 0) }, 6000)

            // Recommencer après 8 secondes
            handler.postDelayed(this, 8000)
        }
    }

    // Effectue un tap aux coordonnées relatives données, avec délai optionnel
    private fun tap(relX: Float, relY: Float, delayMs: Long) {
        val absX = screenWidth * relX
        val absY = screenHeight * relY

        val action = Runnable {
            val path = Path().apply { moveTo(absX, absY) }
            val stroke = GestureDescription.StrokeDescription(path, 0, 50)
            val gesture = GestureDescription.Builder().addStroke(stroke).build()
            dispatchGesture(gesture, null, null)
        }

        if (delayMs > 0) handler.postDelayed(action, delayMs)
        else handler.post(action)
    }

    // Appelé par OverlayService pour basculer start/stop
    fun toggle() {
        if (isRunning) stopFarming() else startFarming()
    }
}
