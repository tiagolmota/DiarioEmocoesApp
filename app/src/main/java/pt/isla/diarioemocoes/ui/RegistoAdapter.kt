package pt.isla.diarioemocoes.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import pt.isla.diarioemocoes.R
import pt.isla.diarioemocoes.data.RegistoEmocao

class RegistoAdapter(
    private val onDeleteClick: (RegistoEmocao) -> Unit
) : ListAdapter<RegistoEmocao, RegistoAdapter.RegistoViewHolder>(DiffCallback) {

    inner class RegistoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvData: TextView = itemView.findViewById(R.id.tvData)
        val tvEstado: TextView = itemView.findViewById(R.id.tvEstado)
        val tvNotas: TextView = itemView.findViewById(R.id.tvNotas)
        val btnApagar: TextView = itemView.findViewById(R.id.btnApagarItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RegistoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_registo, parent, false)
        return RegistoViewHolder(view)
    }

    override fun onBindViewHolder(holder: RegistoViewHolder, position: Int) {
        val registo = getItem(position)
        holder.tvData.text = registo.dataHoraLegivel
        holder.tvEstado.text = registo.estadoEmocional
        holder.tvNotas.text = registo.notasTexto
        holder.btnApagar.setOnClickListener { onDeleteClick(registo) }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<RegistoEmocao>() {
        override fun areItemsTheSame(a: RegistoEmocao, b: RegistoEmocao) = a.id == b.id
        override fun areContentsTheSame(a: RegistoEmocao, b: RegistoEmocao) = a == b
    }
}
