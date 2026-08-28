package com.hopital.astreinte.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Une ligne = une intervention d'astreinte.
 * Les champs reprennent les colonnes de Classeur_astreinte.xlsx :
 * Qui, Type, Site, Date, Heure Début, Heure Fin, Test Nuit,
 * Description appel, Solution si utile, Appel rejeté, Temps Exact.
 */
@Entity(tableName = "interventions")
data class Intervention(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    val qui: String = "",                 // initiales de l'agent d'astreinte
    val type: String = "",                // menu déroulant : Niveau 1, SSO, Citrix, ...
    val site: String = "",                // menu déroulant : CHY, AIX, ...

    val dateMillis: Long,                 // date de l'appel
    val heureDebutMinutes: Int,           // minutes depuis minuit, rempli auto au décroché
    val heureFinMinutes: Int,             // minutes depuis minuit, rempli auto au raccroché

    val jourNuit: String = "",            // calculé auto : "JOUR" ou "NUIT"
    val descriptionAppel: String = "",    // saisi par l'agent après l'appel
    val solution: String = "",            // saisi par l'agent après l'appel
    val appelRejete: Boolean = false,

    val tempsExactSecondes: Int = 0,      // calculé auto = heureFin - heureDebut
    val complete: Boolean = false         // false tant que l'agent n'a pas rempli la fiche
)
