package com.hopital.astreinte.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.hopital.astreinte.data.Intervention
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Génère un CSV avec les mêmes colonnes que Classeur_astreinte.xlsx :
 * Qui, Type, Site, Date, Heure Début, Heure Fin, Test Nuit,
 * Description appel, Solution si utile, Appel rejeté, Temps Exact
 * -> fichier ouvrable directement dans Excel pour fusion avec le suivi existant.
 */
object CsvExporter {

    fun export(context: Context, interventions: List<Intervention>): Uri {
        val dateFmt = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
        val sb = StringBuilder()
        sb.append("Qui;Type;Site;Date;Heure Debut;Heure Fin;Test Nuit;Description appel;Solution si utile;Appel rejete;Temps Exact (min)\n")

        for (i in interventions) {
            val date = dateFmt.format(i.dateMillis)
            val hDebut = String.format("%02d:%02d", i.heureDebutMinutes / 60, i.heureDebutMinutes % 60)
            val hFin = String.format("%02d:%02d", i.heureFinMinutes / 60, i.heureFinMinutes % 60)
            val tempsMin = i.tempsExactSecondes / 60

            fun esc(s: String) = "\"" + s.replace("\"", "\"\"") + "\""

            sb.append(
                listOf(
                    esc(i.qui), esc(i.type), esc(i.site), date, hDebut, hFin, i.jourNuit,
                    esc(i.descriptionAppel), esc(i.solution),
                    if (i.appelRejete) "Oui" else "Non", tempsMin.toString()
                ).joinToString(";")
            )
            sb.append("\n")
        }

        val file = File(context.cacheDir, "export_astreintes.csv")
        file.writeText(sb.toString())

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}
