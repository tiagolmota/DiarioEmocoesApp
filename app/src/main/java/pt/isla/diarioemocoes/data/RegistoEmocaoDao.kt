package pt.isla.diarioemocoes.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// Interface que define o que posso fazer com a tabela diario_emocoes.
// O Room gera o código SQL por baixo — eu só escrevo o contrato.
@Dao
interface RegistoEmocaoDao {

    // Inserir registo. Se o ID já existir substitui — funciona como update também.
    // Descobri o REPLACE quando tentei editar um registo e a app crashava com
    // UNIQUE constraint failed. Esta estratégia resolve isso.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirRegisto(registo: RegistoEmocao)

    // Buscar todos os registos, do mais recente para o mais antigo.
    // Flow faz com que a lista na UI se actualize sozinha quando há mudanças —
    // não preciso de chamar esta função de novo após cada inserção.
    @Query("SELECT * FROM diario_emocoes ORDER BY id DESC")
    fun obterTodosOsRegistos(): Flow<List<RegistoEmocao>>

    // Apagar um registo específico pelo ID.
    // O :id é substituído pelo argumento passado à função.
    @Query("DELETE FROM diario_emocoes WHERE id = :id")
    suspend fun apagarRegistoPorId(id: Long)
}
