package pt.isla.diarioemocoes.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ENTIDADE DE DADOS — RegistoEmocao
 * ===================================
 * Esta classe define a estrutura de um registo no diário emocional.
 * A anotação @Entity diz ao Room para criar uma tabela SQLite com este formato.
 *
 * DECISÃO: Porquê usar Long (timestamp) como chave primária?
 * ----------------------------------------------------------
 * Alternativa rejeitada: usar a data formatada (ex: "28/05/2026") como @PrimaryKey.
 * Problema: dois registos no mesmo dia teriam o mesmo ID → colisão na base de dados.
 *
 * Solução adoptada: System.currentTimeMillis() devolve o número de milissegundos
 * desde 1 Janeiro 1970. É único por definição (dois registos nunca ocorrem
 * exactamente no mesmo milissegundo) e permite ordenação cronológica directa.
 *
 * DECISÃO: Porquê data class?
 * ---------------------------
 * O Kotlin gera automaticamente equals(), hashCode() e copy() para data classes.
 * O DiffUtil do RecyclerView usa equals() para comparar items — sem data class,
 * a lista nunca sabia quais registos mudaram.
 */
@Entity(tableName = "diario_emocoes")
data class RegistoEmocao(

    /** Chave primária: timestamp em milissegundos — unicidade garantida */
    @PrimaryKey val id: Long,

    /** Data e hora formatada para leitura humana (ex: "28/05/2026 22:15") */
    val dataHoraLegivel: String,

    /** Estado emocional introduzido pelo utilizador (ex: "Ansioso", "Feliz") */
    val estadoEmocional: String,

    /** Temperatura ambiente — campo preparado para integração futura com sensor */
    val temperaturaAmbiente: Double,

    /** Texto livre do diário — notas pessoais do utilizador */
    val notasTexto: String
)
