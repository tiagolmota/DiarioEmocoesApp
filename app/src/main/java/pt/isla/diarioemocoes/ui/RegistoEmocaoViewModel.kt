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
 * ViewModel que implementa uma estratégia Offline-First:
 *
 * 1. Todas as escritas vão PRIMEIRO para o Room (local, instantâneo).
 * 2. Em segundo plano, tenta sincronizar com o Firestore (remoto).
 * 3. Se não houver internet, a app funciona na mesma — o dado está no Room.
 *
 * Esta abordagem garante que o utilizador nunca perde dados por falta de ligação.
 */
class RegistoEmocaoViewModel(application: Application) : AndroidViewModel(application) {

    private val registoDao        = AppDatabase.getDatabase(application).registoDao()
    private val firestoreRepo     = FirestoreRepository()

    // Fluxo reativo: a UI observa isto e actualiza-se automaticamente
    val todosOsRegistos: Flow<List<RegistoEmocao>> = registoDao.obterTodosOsRegistos()

    // Estado de sincronização exposto à UI (para mostrar feedback)
    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus

    /**
     * Guarda localmente (Room) e sincroniza com Firestore em paralelo.
     */
    fun guardarRegisto(registo: RegistoEmocao) {
        viewModelScope.launch {
            // Passo 1: persistência local imediata
            registoDao.inserirRegisto(registo)

            // Passo 2: sincronização remota em background
            _syncStatus.value = SyncStatus.Syncing
            val resultado = firestoreRepo.sincronizarRegisto(registo)
            _syncStatus.value = if (resultado.isSuccess) {
                SyncStatus.Success
            } else {
                SyncStatus.Error("Guardado localmente. Sem ligação ao servidor.")
            }
        }
    }

    /**
     * Apaga localmente (Room) e remove do Firestore em paralelo.
     */
    fun apagarRegisto(id: Long) {
        viewModelScope.launch {
            registoDao.apagarRegistoPorId(id)
            firestoreRepo.apagarRegistoRemoto(id)
        }
    }

    /**
     * Repõe o estado de sincronização (chamado após a UI mostrar o feedback).
     */
    fun resetSyncStatus() {
        _syncStatus.value = SyncStatus.Idle
    }
}

/** Estados possíveis de sincronização com o Firestore */
sealed class SyncStatus {
    object Idle    : SyncStatus()
    object Syncing : SyncStatus()
    object Success : SyncStatus()
    data class Error(val message: String) : SyncStatus()
}
