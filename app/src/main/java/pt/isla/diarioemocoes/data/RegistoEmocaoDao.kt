package pt.isla.diarioemocoes.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO — Data Access Object (Objeto de Acesso a Dados)
 * ====================================================
 * O DAO é a interface que define TODAS as operações possíveis sobre a tabela
 * "diario_emocoes". O Room gera automaticamente a implementação em tempo de
 * compilação — o programador escreve apenas o contrato (interface), não o SQL.
 *
 * DECISÃO: Porquê usar uma interface e não uma classe?
 * ----------------------------------------------------
 * O Room precisa de gerar código em tempo de compilação (via kapt).
 * Uma interface permite que o Room crie a implementação concreta sem
 * conflito com código do programador. É o padrão DAO clássico do Java/Kotlin.
 *
 * DECISÃO: Porquê suspend nas funções de escrita?
 * -----------------------------------------------
 * O Android proíbe operações lentas (I/O de disco, rede) na Main Thread —
 * a thread que desenha o ecrã. Se o fizéssemos, a app "travaria" (ANR).
 * A palavra suspend marca a função como uma corrotina: ela suspende
 * a execução sem bloquear a thread, aguarda o resultado, e retoma.
 *
 * DECISÃO: Porquê Flow na leitura?
 * ---------------------------------
 * Flow é um stream reactivo. Quando um novo registo é inserido na BD,
 * o Flow emite automaticamente a lista actualizada para todos os
 * observadores (a UI). Sem Flow, teríamos de consultar a BD manualmente
 * após cada inserção. Com Flow, a lista na ecrã actualiza-se sozinha.
 */
@Dao
interface RegistoEmocaoDao {

    /**
     * CRIAR / ACTUALIZAR — operação C do CRUD
     *
     * OnConflictStrategy.REPLACE: se um registo com o mesmo ID já existir,
     * substitui-o. Isto funciona como um "upsert" (insert + update em um passo).
     * Útil para sincronizações futuras com o Firestore.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirRegisto(registo: RegistoEmocao)

    /**
     * LER — operação R do CRUD
     *
     * ORDER BY id DESC = mais recente primeiro (id é timestamp, logo cronológico).
     * Devolve Flow: a UI observa este stream e actualiza automaticamente.
     */
    @Query("SELECT * FROM diario_emocoes ORDER BY id DESC")
    fun obterTodosOsRegistos(): Flow<List<RegistoEmocao>>

    /**
     * APAGAR — operação D do CRUD
     *
     * O parâmetro :id é substituído pelo valor passado à função.
     * A operação é selectiva — só apaga o registo com esse id específico.
     */
    @Query("DELETE FROM diario_emocoes WHERE id = :id")
    suspend fun apagarRegistoPorId(id: Long)
}
