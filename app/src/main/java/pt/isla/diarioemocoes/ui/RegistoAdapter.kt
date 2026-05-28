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

/**
 * ADAPTADOR DO RECYCLERVIEW — Lista de Registos
 * ===============================================
 * O RecyclerView não sabe como mostrar os dados — precisa de um Adaptador
 * que converta uma lista de RegistoEmocao em Views visíveis no ecrã.
 *
 * DECISÃO: Porquê ListAdapter e não ArrayAdapter ou RecyclerView.Adapter directo?
 * ---------------------------------------------------------------------------------
 * ListAdapter é a evolução moderna. Diferenças chave:
 *
 * 1. DiffUtil integrado: quando a lista muda, o DiffUtil compara a lista antiga
 *    com a nova em background e anima APENAS as mudanças (inserções, remoções).
 *    ArrayAdapter faz notifyDataSetChanged() — re-desenha TUDO, mesmo que só
 *    um item tenha mudado. Muito mais pesado em performance.
 *
 * 2. submitList(): actualiza a lista de forma assíncrona e thread-safe.
 *    Chamado pela Activity quando o Flow emite uma nova lista.
 *
 * DECISÃO: Porquê ViewHolder?
 * ----------------------------
 * Sem ViewHolder, o RecyclerView chamaria findViewById() para CADA item
 * visível em CADA scroll — operação cara (percorre a hierarquia de Views).
 * O ViewHolder guarda as referências em cache após a primeira criação.
 * Para uma lista com 100 registos, o ganho de performance é significativo.
 *
 * DECISÃO: Porquê receber onDeleteClick como lambda no construtor?
 * ----------------------------------------------------------------
 * O adaptador não deve saber o que acontece ao clicar "Apagar" — essa lógica
 * pertence ao ViewModel. Passando um lambda, o adaptador apenas notifica quem
 * quiser saber. A Activity recebe o evento e delega ao ViewModel.
 * Isto segue o princípio de responsabilidade única (SRP).
 */
class RegistoAdapter(
    private val onDeleteClick: (RegistoEmocao) -> Unit
) : ListAdapter<RegistoEmocao, RegistoAdapter.RegistoViewHolder>(DiffCallback) {

    /**
     * ViewHolder — guarda referências às Views de cada item da lista.
     * Criado uma vez por item visível; reutilizado para items que saem e entram no ecrã.
     */
    inner class RegistoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvData    : TextView = itemView.findViewById(R.id.tvData)
        val tvEstado  : TextView = itemView.findViewById(R.id.tvEstado)
        val tvNotas   : TextView = itemView.findViewById(R.id.tvNotas)
        val btnApagar : TextView = itemView.findViewById(R.id.btnApagarItem)
    }

    /**
     * onCreateViewHolder — chamado quando o RecyclerView precisa de um novo item visual.
     * Infla o layout XML do item e cria o ViewHolder correspondente.
     * Chamado apenas quando não há ViewHolders reciclados disponíveis.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RegistoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_registo, parent, false)
        return RegistoViewHolder(view)
    }

    /**
     * onBindViewHolder — preenche um ViewHolder existente com os dados de um item.
     * Chamado sempre que um item entra na área visível do ecrã.
     * É aqui que os dados do RegistoEmocao são exibidos nas Views.
     */
    override fun onBindViewHolder(holder: RegistoViewHolder, position: Int) {
        val registo = getItem(position) // getItem() é fornecido pelo ListAdapter
        holder.tvData.text    = registo.dataHoraLegivel
        holder.tvEstado.text  = registo.estadoEmocional
        holder.tvNotas.text   = registo.notasTexto

        // Notifica o observador externo (a Activity) com o registo a apagar
        holder.btnApagar.setOnClickListener { onDeleteClick(registo) }
    }

    /**
     * DiffCallback — algoritmo de comparação entre listas.
     *
     * areItemsTheSame: compara identidades (são o mesmo registo?).
     *   Usa o id porque é único — dois registos com o mesmo id são o mesmo registo.
     *
     * areContentsTheSame: se são o mesmo item, verifica se o conteúdo mudou.
     *   data class gera equals() que compara todos os campos — comparação automática.
     *   Se o conteúdo não mudou, o RecyclerView não re-desenha o item.
     */
    companion object DiffCallback : DiffUtil.ItemCallback<RegistoEmocao>() {
        override fun areItemsTheSame(a: RegistoEmocao, b: RegistoEmocao) = a.id == b.id
        override fun areContentsTheSame(a: RegistoEmocao, b: RegistoEmocao) = a == b
    }
}
