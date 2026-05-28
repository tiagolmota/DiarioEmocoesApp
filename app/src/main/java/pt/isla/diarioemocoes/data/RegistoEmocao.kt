package pt.isla.diarioemocoes.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// Define a estrutura de um registo no diário.
// @Entity diz ao Room para criar a tabela correspondente no SQLite.
@Entity(tableName = "diario_emocoes")
data class RegistoEmocao(

    // Uso o timestamp em milissegundos como ID porque é único por natureza —
    // dois registos nunca acontecem no exacto mesmo milissegundo.
    // Tentei usar a data como chave primária mas percebi que isso limitava
    // a um registo por dia, o que não fazia sentido para um diário.
    @PrimaryKey val id: Long,

    // A data formatada para mostrar ao utilizador (ex: "28/05/2026 22:15")
    val dataHoraLegivel: String,

    // O estado emocional que o utilizador escreve — campo livre
    val estadoEmocional: String,

    // Preparado para integrar com o sensor de temperatura no futuro.
    // Por agora fica com valor 0.0.
    val temperaturaAmbiente: Double,

    // Notas pessoais — texto livre, pode ficar vazio
    val notasTexto: String
)
