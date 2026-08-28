package com.hopital.astreinte

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.hopital.astreinte.data.AppDatabase
import com.hopital.astreinte.data.Intervention
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Écoute les changements d'état de l'appareil téléphonique.
 * - Au décroché (OFFHOOK) : enregistre l'heure de début.
 * - Au raccroché (IDLE) après un OFFHOOK : calcule la durée, crée une fiche
 *   "à compléter" en base et envoie une notification pour l'ouvrir.
 *
 * Nécessaire : cette réception ne fonctionne correctement que si l'app est
 * installée manuellement (hors Play Store) avec la permission READ_PHONE_STATE
 * accordée par l'utilisateur.
 */
class CallStateReceiver : BroadcastReceiver() {

    companion object {
        private const val PREFS = "call_state_prefs"
        private const val KEY_CALL_START = "call_start_minutes"
        private const val KEY_CALL_ACTIVE = "call_active"
        const val CHANNEL_ID = "astreinte_channel"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val state = intent.getStringExtra(android.telephony.TelephonyManager.EXTRA_STATE)
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        val now = Calendar.getInstance()
        val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        when (state) {
            android.telephony.TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                // Appel décroché : on démarre le chrono si ce n'est pas déjà fait
                if (!prefs.getBoolean(KEY_CALL_ACTIVE, false)) {
                    prefs.edit()
                        .putBoolean(KEY_CALL_ACTIVE, true)
                        .putInt(KEY_CALL_START, nowMinutes)
                        .apply()
                }
            }

            android.telephony.TelephonyManager.EXTRA_STATE_IDLE -> {
                // Appel raccroché
                if (prefs.getBoolean(KEY_CALL_ACTIVE, false)) {
                    val startMinutes = prefs.getInt(KEY_CALL_START, nowMinutes)
                    prefs.edit().putBoolean(KEY_CALL_ACTIVE, false).apply()

                    createDraftAndNotify(context, startMinutes, nowMinutes)
                }
            }
        }
    }

    private fun createDraftAndNotify(context: Context, startMinutes: Int, endMinutes: Int) {
        val dao = AppDatabase.getInstance(context).interventionDao()
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val jourNuit = if (startMinutes in (7 * 60)..(21 * 60)) "JOUR" else "NUIT"
        val dureeSec = ((endMinutes - startMinutes).coerceAtLeast(0)) * 60

        val draft = Intervention(
            dateMillis = today,
            heureDebutMinutes = startMinutes,
            heureFinMinutes = endMinutes,
            jourNuit = jourNuit,
            tempsExactSecondes = dureeSec,
            complete = false
        )

        CoroutineScope(Dispatchers.IO).launch {
            val id = dao.insert(draft)
            notifyUser(context, id)
        }
    }

    private fun notifyUser(context: Context, interventionId: Long) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Fiches d'astreinte", NotificationManager.IMPORTANCE_HIGH
            )
            nm.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, NewInterventionActivity::class.java).apply {
            putExtra(NewInterventionActivity.EXTRA_INTERVENTION_ID, interventionId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, interventionId.toInt(), openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setContentTitle("Appel terminé")
            .setContentText("Touchez pour compléter la fiche d'intervention")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
        ) {
            nm.notify(interventionId.toInt(), notification)
        }
    }
}
