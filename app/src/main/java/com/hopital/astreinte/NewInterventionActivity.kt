package com.hopital.astreinte

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hopital.astreinte.data.AppDatabase
import com.hopital.astreinte.data.Intervention
import com.hopital.astreinte.databinding.ActivityNewInterventionBinding
import kotlinx.coroutines.launch

class NewInterventionActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_INTERVENTION_ID = "extra_intervention_id"
    }

    private lateinit var binding: ActivityNewInterventionBinding
    private var current: Intervention? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewInterventionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dao = AppDatabase.getInstance(this).interventionDao()
        val id = intent.getLongExtra(EXTRA_INTERVENTION_ID, -1L)

        if (id != -1L) {
            // Ouverte automatiquement après un appel détecté : on pré-remplit
            lifecycleScope.launch {
                val loaded = dao.getById(id)
                current = loaded
                loaded?.let { fillFromDraft(it) }
            }
        } else {
            // Saisie manuelle : pas d'appel détecté, l'agent renseigne tout lui-même
            binding.tvDuree.text = "Saisie manuelle"
        }

        binding.btnEnregistrer.setOnClickListener {
            saveEntry(dao)
        }
    }

    private fun fillFromDraft(intervention: Intervention) {
        val h1 = intervention.heureDebutMinutes / 60
        val m1 = intervention.heureDebutMinutes % 60
        val h2 = intervention.heureFinMinutes / 60
        val m2 = intervention.heureFinMinutes % 60
        val dureeMin = intervention.tempsExactSecondes / 60

        binding.tvDuree.text = String.format(
            "Appel de %02d:%02d à %02d:%02d  (%d min, %s)",
            h1, m1, h2, m2, dureeMin, intervention.jourNuit
        )
    }

    private fun saveEntry(dao: com.hopital.astreinte.data.InterventionDao) {
        val type = binding.spinnerType.selectedItem?.toString() ?: ""
        val site = binding.spinnerSite.selectedItem?.toString() ?: ""

        val base = current ?: Intervention(
            dateMillis = System.currentTimeMillis(),
            heureDebutMinutes = 0,
            heureFinMinutes = 0
        )

        val toSave = base.copy(
            qui = binding.etQui.text.toString(),
            type = type,
            site = site,
            descriptionAppel = binding.etDescription.text.toString(),
            solution = binding.etSolution.text.toString(),
            appelRejete = binding.cbRejete.isChecked,
            complete = true
        )

        lifecycleScope.launch {
            if (toSave.id == 0L) dao.insert(toSave) else dao.update(toSave)
            finish()
        }
    }
}
