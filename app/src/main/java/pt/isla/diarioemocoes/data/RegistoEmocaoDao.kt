package pt.isla.diarioemocoes.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RegistoEmocaoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirRegisto(registo: RegistoEmocao)

    @Query("SELECT * FROM diario_emocoes ORDER BY id DESC")
    fun obterTodosOsRegistos(): Flow<List<RegistoEmocao>>

    @Query("DELETE FROM diario_emocoes WHERE id = :id")
    suspend fun apagarRegistoPorId(id: Long)
}
