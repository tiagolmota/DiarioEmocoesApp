package pt.isla.diarioemocoes.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

/**
 * Repositório responsável pela sincronização com o Firebase Firestore.
 *
 * Arquitectura: este repositório actua como a camada de abstracção entre
 * a lógica de negócio (ViewModel) e a fonte de dados remota (Firestore).
 * A app usa o Room como "fonte de verdade" local (offline-first) e o
 * Firestore como backup/sincronização na nuvem.
 */
class FirestoreRepository {

    private val db: FirebaseFirestore = Firebase.firestore

    // Nome da colecção no Firestore (equivale a uma "tabela" no SQL)
    private val collection = db.collection("diario_emocoes")

    /**
     * Envia um registo local para o Firestore.
     * Usa o mesmo id (timestamp) do Room para manter consistência entre as duas BDs.
     */
    suspend fun sincronizarRegisto(registo: RegistoEmocao): Result<Unit> {
        return try {
            val documento = mapOf(
                "id"                  to registo.id,
                "dataHoraLegivel"     to registo.dataHoraLegivel,
                "estadoEmocional"     to registo.estadoEmocional,
                "temperaturaAmbiente" to registo.temperaturaAmbiente,
                "notasTexto"          to registo.notasTexto
            )
            // Usa o id como nome do documento para evitar duplicados no Firestore
            collection.document(registo.id.toString()).set(documento).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Remove um registo do Firestore pelo seu id.
     */
    suspend fun apagarRegistoRemoto(id: Long): Result<Unit> {
        return try {
            collection.document(id.toString()).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtém todos os registos do Firestore (útil para restauro ou migração).
     */
    suspend fun obterTodosOsRegistosRemotos(): Result<List<RegistoEmocao>> {
        return try {
            val snapshot = collection.orderBy("id",
                com.google.firebase.firestore.Query.Direction.DESCENDING).get().await()
            val lista = snapshot.documents.mapNotNull { doc ->
                try {
                    RegistoEmocao(
                        id                  = doc.getLong("id") ?: 0L,
                        dataHoraLegivel     = doc.getString("dataHoraLegivel") ?: "",
                        estadoEmocional     = doc.getString("estadoEmocional") ?: "",
                        temperaturaAmbiente = doc.getDouble("temperaturaAmbiente") ?: 0.0,
                        notasTexto          = doc.getString("notasTexto") ?: ""
                    )
                } catch (e: Exception) {
                    null // ignora documentos malformados
                }
            }
            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
