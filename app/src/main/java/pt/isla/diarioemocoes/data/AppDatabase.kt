package pt.isla.diarioemocoes.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Classe principal da base de dados. Liga a entidade ao DAO e dá nome ao ficheiro .db.
// version = 1 porque ainda não alterei a estrutura. Se mudar uma coluna, tenho de
// incrementar este número e criar uma Migration — aprendi isso da forma difícil.
@Database(entities = [RegistoEmocao::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun registoDao(): RegistoEmocaoDao

    companion object {

        // @Volatile garante que todas as threads vêem o mesmo valor.
        // Sem isto, duas threads podiam criar duas instâncias ao mesmo tempo.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // Se já existe, devolve. Se não, cria uma vez e guarda.
            // O synchronized evita que duas threads entrem aqui ao mesmo tempo
            // e acabem a criar duas instâncias diferentes.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "diario_emocoes_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
