package pt.isla.diarioemocoes.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pt.isla.diarioemocoes.data.AppDatabase
import pt.isla.diarioemocoes.data.FirestoreRepository
import pt.isla.diarioemocoes.data.RegistoEmocao

/**
 * VIEWMODEL — Camada de Lógica de Negócio
 * =========================================
 * O ViewModel é a peça central do padrão MVVM (Model-View-ViewModel).
 * É o intermediário entre os dados (Room + Firestore) e a interface (Activity).
 *
 * DECISÃO: Porquê AndroidViewModel e não ViewModel?
 * -------------------------------------------------
 * ViewModel padrão não tem acesso ao Context do Android.
 * Precisamos de Context para inicializar a AppDatabase (abrir o ficheiro SQLite).
 *
 * Problema: passar o Context de uma Activity ao ViewModel cria um memory leak —
 * o ViewModel sobrevive a rotações de ecrã, mas a Activity antiga seria destruída.
 * O ViewModel reteria uma referência a um objecto já destruído → fuga de memória.
 *
 * Solução: AndroidViewModel recebe a Application no construtor.
 * Application tem o mesmo ciclo de vida do processo da app — nunca é destruída
 * enquanto a app estiver viva. Passar Application é sempre seguro.
 *
 * DECISÃO: Porquê viewModelScope?
 * --------------------------------
 * viewModelScope é um CoroutineScope ligado ao ciclo de vida do ViewModel.
 * Se o utilizador sair da Activity enquanto uma operação está em curso,
 * viewModelScope cancela automaticamente todas as corrotinas pendentes.
 * Sem isto, corrotinas "perdidas" continuariam a correr em background indefinidamente.
 *
 * DECISÃO: Porquê StateFlow para syncStatus?
 * -------------------------------------------
 * A UI precisa de saber o estado da sincronização (aguardando / sucesso / erro)
 * para mostrar feedback ao utilizador. StateFlow é um stream reactivo que:
 * 1. Sempre tem um valor actual (ao contrário de Channel).
 * 2. A UI pode "observar" e reagir a mudanças.
 * 3. É thread-safe — pode ser escrito de qualquer corrotina.
 */
class RegistoEmocaoViewModel(application: Application) : AndroidViewModel(application) {

    /** Acesso ao DAO via instância Singleton da base de dados local */
    private val registoDao    = AppDatabase.getDatabase(application).registoDao()

    /** Repositório da base de dados externa (Firestore) */
    private val firestoreRepo = FirestoreRepository()

    /**
     * Lista reactiva de todos os registos.
     * A Activity observa este Flow — quando um registo é inserido ou apagado,
     * a lista na ecrã actualiza-se automaticamente sem código extra.
     */
    val todosOsRegistos: Flow<List<RegistoEmocao>> = registoDao.obterTodosOsRegistos()

    /**
     * Estado interno de sincronização — apenas o ViewModel pode alterar (_MutableStateFlow).
     * A Activity lê apenas a versão de leitura (StateFlow) — encapsulamento garantido.
     */
    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus

    /**
     * GUARDAR REGISTO — fluxo offline-first
     *
     * Passo 1: Insere imediatamente no Room (local, sem internet, instantâneo).
     *          A UI já vê o novo registo na lista graças ao Flow reactivo.
     * Passo 2: Actualiza o estado para "a sincronizar" (feedback visual opcional).
     * Passo 3: Tenta sincronizar com Firestore em background.
     * Passo 4: Actualiza o estado com sucesso ou erro — a Activity mostra Toast.
     */
    fun guardarRegisto(registo: RegistoEmocao) {
        viewModelScope.launch {
            // Passo 1: persistência local imediata
            registoDao.inserirRegisto(registo)

            // Passo 2: iniciar sincronização remota
            _syncStatus.value = SyncStatus.Syncing
            val resultado = firestoreRepo.sincronizarRegisto(registo)

            // Passo 3: reportar resultado à UI
            _syncStatus.value = if (resultado.isSuccess) {
                SyncStatus.Success
            } else {
                SyncStatus.Error("Guardado localmente. Sem ligacao ao servidor.")
            }
        }
    }

    /**
     * APAGAR REGISTO — apaga localmente e remove do Firestore
     *
     * As duas operações correm em sequência na mesma corrotina.
     * Se o Firestore falhar (sem internet), o registo já foi apagado localmente —
     * o utilizador não perde o fluxo de trabalho.
     */
    fun apagarRegisto(id: Long) {
        viewModelScope.launch {
            registoDao.apagarRegistoPorId(id)
            firestoreRepo.apagarRegistoRemoto(id) // falha silenciosa se sem internet
        }
    }

    /**
     * Repõe o estado de sincronização para Idle.
     * Chamado pela Activity depois de mostrar o Toast de feedback.
     * Sem este reset, o mesmo Toast seria mostrado novamente em rotações de ecrã.
     */
    fun resetSyncStatus() {
        _syncStatus.value = SyncStatus.Idle
    }
}

/**
 * ESTADOS DE SINCRONIZAÇÃO — sealed class
 * -----------------------------------------
 * sealed class garante que todos os estados possíveis estão definidos aqui.
 * O compilador obriga o when() a tratar TODOS os casos — sem "else" esquecido.
 *
 * Idle    → estado inicial, nenhuma operação em curso
 * Syncing → sincronização em progresso
 * Success → sincronização completada com sucesso
 * Error   → falha na sincronização (com mensagem descritiva)
 */
sealed class SyncStatus {
    object Idle    : SyncStatus()
    object Syncing : SyncStatus()
    object Success : SyncStatus()
    data class Error(val message: String) : SyncStatus()
}
