package com.hopital.astreinte

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hopital.astreinte.data.Intervention
import com.hopital.astreinte.databinding.ItemInterventionBinding
import java.text.SimpleDateFormat
import java.util.Locale

class InterventionAdapter(
    private var items: List<Intervention> = emptyList()
) : RecyclerView.Adapter<InterventionAdapter.VH>() {

    private val dateFmt = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)

    fun submitList(newItems: List<Intervention>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class VH(val binding: ItemInterventionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemInterventionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val h1 = item.heureDebutMinutes / 60
        val m1 = item.heureDebutMinutes % 60
        val dureeMin = item.tempsExactSecondes / 60

        holder.binding.tvLigne1.text =
            "${dateFmt.format(item.dateMillis)}  ${String.format("%02d:%02d", h1, m1)}  (${item.jourNuit})  —  ${item.site}"
        holder.binding.tvLigne2.text =
            if (item.complete) "${item.type} · ${dureeMin} min — ${item.descriptionAppel}"
            else "⚠ Fiche à compléter — ${dureeMin} min"
    }

    override fun getItemCount() = items.size
}
