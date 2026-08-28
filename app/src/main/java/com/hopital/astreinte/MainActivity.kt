package com.hopital.astreinte

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.hopital.astreinte.data.AppDatabase
import com.hopital.astreinte.databinding.ActivityMainBinding
import com.hopital.astreinte.util.CsvExporter
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val adapter = InterventionAdapter()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* résultats gérés silencieusement : sans READ_PHONE_STATE la détection auto ne marchera pas */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        val dao = AppDatabase.getInstance(this).interventionDao()
        dao.getAll().observe(this) { list -> adapter.submitList(list) }

        binding.btnNouvelleFiche.setOnClickListener {
            startActivity(Intent(this, NewInterventionActivity::class.java))
        }

        binding.btnExporter.setOnClickListener {
            lifecycleScope.launch {
                val all = dao.getAllForExport()
                val uri = CsvExporter.export(this@MainActivity, all)
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(shareIntent, "Exporter les astreintes"))
            }
        }

        requestNeededPermissions()
    }

    private fun requestNeededPermissions() {
        val needed = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
            != PackageManager.PERMISSION_GRANTED
        ) needed.add(Manifest.permission.READ_PHONE_STATE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) needed.add(Manifest.permission.POST_NOTIFICATIONS)

        if (needed.isNotEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Autorisations nécessaires")
                .setMessage("Pour détecter automatiquement le début et la fin des appels, l'application a besoin de l'accès à l'état du téléphone et aux notifications.")
                .setPositiveButton("Autoriser") { _, _ -> permissionLauncher.launch(needed.toTypedArray()) }
                .setNegativeButton("Plus tard", null)
                .show()
        }
    }
}
