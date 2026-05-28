package pt.isla.diarioemocoes.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * BASE DE DADOS — AppDatabase (Singleton)
 * ========================================
 * Esta classe é o ponto de entrada para toda a persistência local.
 * Define o "contrato" da base de dados: que tabelas existem e qual a versão.
 *
 * @Database(entities): lista de todas as tabelas. Para adicionar uma nova
 *   tabela no futuro, basta acrescentar a entidade aqui e incrementar a versão.
 *
 * version = 1: número de versão do esquema. Se a estrutura de uma tabela
 *   mudar (ex: adicionar uma coluna), este número deve aumentar e deve-se
 *   fornecer uma Migration — caso contrário o Room apaga e recria a BD.
 *
 * exportSchema = false: desactiva a exportação do esquema para ficheiro JSON.
 *   Num projecto de produção, exportSchema = true é recomendado para manter
 *   histórico de migrações. Aqui está desactivado por simplicidade (protótipo).
 *
 * DECISÃO: Porquê Singleton?
 * --------------------------
 * Abrir uma ligação SQLite é uma operação custosa (cria ficheiro, aloca memória,
 * inicializa buffers). Se cada Activity criasse a sua própria instância,
 * teríamos múltiplas ligações abertas em simultâneo — desperdício de memória
 * e risco de condições de corrida nos dados.
 *
 * O padrão Singleton garante UMA única instância durante toda a vida da app.
 *
 * DECISÃO: Porquê @Volatile?
 * --------------------------
 * Em sistemas multi-thread (Android usa várias threads), uma variável pode
 * estar em cache numa thread e não reflectir o valor actualizado por outra.
 * @Volatile força todas as threads a ler o valor directamente da memória
 * principal — eliminando leituras de cache inconsistentes.
 *
 * DECISÃO: Porquê synchronized(this)?
 * ------------------------------------
 * Garante que apenas UMA thread pode executar o bloco de criação de cada vez.
 * Sem synchronized, duas threads poderiam passar pelo check "INSTANCE == null"
 * ao mesmo tempo e criar duas instâncias — violando o Singleton.
 * Este padrão chama-se Double-Checked Locking.
 */
@Database(entities = [RegistoEmocao::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Expõe o DAO ao exterior da camada de dados.
     * O Room gera a implementação concreta desta função em tempo de compilação.
     */
    abstract fun registoDao(): RegistoEmocaoDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Método de acesso à instância única.
         *
         * Passo 1: Verifica se INSTANCE já existe (caso comum após a primeira criação).
         * Passo 2: Se não existir, entra no bloco synchronized (thread-safe).
         * Passo 3: Verifica novamente dentro do bloco (Double-Checked Locking).
         * Passo 4: Cria a instância com Room.databaseBuilder.
         *
         * applicationContext: usa o contexto da Application (não da Activity)
         * para evitar memory leaks — a BD não retém referências a ecrãs destruídos.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "diario_emocoes_database" // nome do ficheiro .db no dispositivo
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
