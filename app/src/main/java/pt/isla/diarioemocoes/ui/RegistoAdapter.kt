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

// Adaptador do RecyclerView — converte a lista de RegistoEmocao em itens visíveis.
// Uso ListAdapter em vez de RecyclerView.Adapter porque tem DiffUtil integrado:
// quando a lista muda, calcula em background quais items mudaram e anima só esses.
// Com notifyDataSetChanged() o RecyclerView redesenhava tudo — muito mais pesado.
class RegistoAdapter(
    private val onDeleteClick: (RegistoEmocao) -> Unit
) : ListAdapter<RegistoEmocao, RegistoAdapter.RegistoViewHolder>(DiffCallback) {

    // ViewHolder guarda as referências às Views de cada item.
    // Sem isto o RecyclerView chamaria findViewById() em cada scroll —
    // numa lista grande isso nota-se em performance.
    inner class RegistoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvData    : TextView = itemView.findViewById(R.id.tvData)
        val tvEstado  : TextView = itemView.findViewById(R.id.tvEstado)
        val tvNotas   : TextView = itemView.findViewById(R.id.tvNotas)
        val btnApagar : TextView = itemView.findViewById(R.id.btnApagarItem)
    }

    // Chamado quando o RecyclerView precisa de um novo item visual.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RegistoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_registo, parent, false)
        return RegistoViewHolder(view)
    }

    // Chamado para preencher um item com dados concretos.
    override fun onBindViewHolder(holder: RegistoViewHolder, position: Int) {
        val registo = getItem(position)
        holder.tvData.text   = registo.dataHoraLegivel
        holder.tvEstado.text = registo.estadoEmocional
        holder.tvNotas.text  = registo.notasTexto
        holder.btnApagar.setOnClickListener { onDeleteClick(registo) }
    }

    // DiffCallback — compara listas para o ListAdapter saber o que mudou.
    // areItemsTheSame: são o mesmo registo? (compara ID)
    // areContentsTheSame: se é o mesmo, será que mudou algum campo? (compara tudo)
    companion object DiffCallback : DiffUtil.ItemCallback<RegistoEmocao>() {
        override fun areItemsTheSame(a: RegistoEmocao, b: RegistoEmocao) = a.id == b.id
        override fun areContentsTheSame(a: RegistoEmocao, b: RegistoEmocao) = a == b
    }
}
