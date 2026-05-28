package pt.isla.diarioemocoes.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

/**
 * REPOSITÓRIO FIRESTORE — Base de Dados Externa
 * ===============================================
 * Esta classe é responsável pela comunicação com o Firebase Firestore —
 * a base de dados na nuvem que satisfaz o requisito de "base de dados externa"
 * do enunciado do trabalho.
 *
 * DECISÃO: Porquê Firebase Firestore e não PHP/MySQL?
 * ---------------------------------------------------
 * O enunciado aceita ambas as abordagens. Firestore foi escolhido porque:
 * 1. Não requer servidor próprio — o Google gere toda a infraestrutura.
 * 2. Suporte nativo a Kotlin Coroutines via extensão .await().
 * 3. Modelo NoSQL (documentos JSON) é mais flexível para dados emocionais
 *    que podem variar em estrutura no futuro.
 * 4. Adequado para protótipo académico — sem custos para volumes pequenos.
 *
 * DECISÃO: Porquê um Repositório separado?
 * -----------------------------------------
 * O padrão Repository abstrai a origem dos dados da lógica de negócio.
 * O ViewModel não sabe (nem precisa de saber) se os dados vêm do Room,
 * do Firestore ou de uma API PHP. Se amanhã mudarmos para PHP, só este
 * ficheiro muda — o ViewModel fica igual.
 *
 * DECISÃO: Estratégia Offline-First
 * ----------------------------------
 * 1. O utilizador guarda → Room persiste localmente (instantâneo, sem internet).
 * 2. Em paralelo → Firestore tenta sincronizar (requer internet).
 * 3. Se falhar → a app funciona na mesma, dado está no Room.
 * Esta estratégia garante que nunca há perda de dados por falta de ligação.
 *
 * DECISÃO: Porquê Result<T> como tipo de retorno?
 * ------------------------------------------------
 * Em vez de lançar excepções (que podem não ser apanhadas e crashar a app),
 * devolvemos Result.success() ou Result.failure(). O ViewModel decide
 * o que fazer com cada caso — separação clara de responsabilidades.
 */
class FirestoreRepository {

    /**
     * Instância do Firestore inicializada com a configuração do google-services.json.
     * Firebase.firestore é um singleton gerido pelo Firebase SDK.
     */
    private val db: FirebaseFirestore = Firebase.firestore

    /**
     * Referência à colecção "diario_emocoes" no Firestore.
     * Em Firestore, uma colecção é equivalente a uma tabela SQL.
     * Os documentos dentro da colecção são equivalentes a linhas.
     */
    private val collection = db.collection("diario_emocoes")

    /**
     * SINCRONIZAR — envia um registo local para o Firestore.
     *
     * Passo 1: Converte o RegistoEmocao numa Map<String, Any> (formato JSON do Firestore).
     * Passo 2: Usa o id (timestamp) como nome do documento — garante que o mesmo
     *          registo não é duplicado se sincronizado duas vezes.
     * Passo 3: .set() substitui o documento se já existir (comportamento upsert).
     * Passo 4: .await() suspende a corrotina até a operação terminar — não bloqueia a UI.
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
            collection.document(registo.id.toString()).set(documento).await()
            Result.success(Unit)
        } catch (e: Exception) {
            // Captura qualquer erro (sem internet, quota excedida, etc.)
            Result.failure(e)
        }
    }

    /**
     * APAGAR REMOTO — remove o documento do Firestore pelo id.
     *
     * Chamado em paralelo com o apagar local (Room) para manter
     * consistência entre as duas bases de dados.
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
     * LER TODOS REMOTOS — obtém todos os registos do Firestore.
     *
     * Caso de uso: restauro de dados após reinstalação da app,
     * ou sincronização inicial num novo dispositivo.
     * Ordenados por id DESC = mais recente primeiro (consistente com o Room).
     */
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
                    null // ignora documentos com estrutura inesperada
                }
            }
            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
