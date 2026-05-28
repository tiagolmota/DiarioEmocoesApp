package pt.isla.diarioemocoes.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diario_emocoes")
data class RegistoEmocao(
    @PrimaryKey val id: Long,
    val dataHoraLegivel: String,
    val estadoEmocional: String,
    val temperaturaAmbiente: Double,
    val notasTexto: String
)
