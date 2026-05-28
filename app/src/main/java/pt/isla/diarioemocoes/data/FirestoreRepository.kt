package pt.isla.diarioemocoes.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

// Trata de tudo o que é comunicação com o Firestore (base de dados na nuvem).
// Separei isto da lógica principal para que o ViewModel não saiba de onde vêm
// os dados — se um dia mudar para PHP só altero aqui.
class FirestoreRepository {

    private val db: FirebaseFirestore = Firebase.firestore

    // "diario_emocoes" no Firestore é o equivalente a uma tabela SQL.
    private val collection = db.collection("diario_emocoes")

    // Envia um registo para o Firestore.
    // Uso o mesmo id do Room como nome do documento para não criar duplicados
    // se a sincronização correr duas vezes.
    // .await() pausa aqui e espera pela resposta sem travar o ecrã.
    suspend fun sincronizarRegisto(registo: RegistoEmocao): Result<Unit> {
        return try {
            val documento = mapOf(
                "id"                  to registo.id,
                "dataHoraLegivel"     to registo.dataHoraLegivel,
                "estadoEmocional"     to registo.estadoEmocional,
                "temperaturaAmbiente" to registo.temperaturaAmbiente,
                "notasTexto"          to registo.notasTexto
            )
            collection.document(registo.id.toString()).set(documento).await()
            Result.success(Unit)
        } catch (e: Exception) {
            // Se não houver internet ou outra falha, devolvo o erro
            // sem deixar a app crashar
            Result.failure(e)
        }
    }

    // Remove o documento do Firestore quando o utilizador apaga um registo local.
    suspend fun apagarRegistoRemoto(id: Long): Result<Unit> {
        return try {
            collection.document(id.toString()).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Busca todos os registos remotos — útil para restaurar dados
    // após reinstalar a app num novo telemóvel.
    suspend fun obterTodosOsRegistosRemotos(): Result<List<RegistoEmocao>> {
        return try {
            val snapshot = collection
                .orderBy("id", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

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
                    null // ignora documentos com dados em falta ou formato errado
                }
            }
            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
