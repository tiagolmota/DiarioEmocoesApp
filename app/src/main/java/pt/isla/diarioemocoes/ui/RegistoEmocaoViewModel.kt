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

// ViewModel — fica no meio entre os dados e o ecrã.
// Uso AndroidViewModel (não ViewModel normal) porque preciso de Context
// para abrir a base de dados. Passar a Activity directamente causava memory leak
// porque o ViewModel sobrevive a rotações de ecrã e ficava com referência
// a uma Activity já destruída. A Application não tem esse problema.
class RegistoEmocaoViewModel(application: Application) : AndroidViewModel(application) {

    private val registoDao    = AppDatabase.getDatabase(application).registoDao()
    private val firestoreRepo = FirestoreRepository()

    // A Activity observa este Flow — quando há mudanças na BD a lista actualiza-se sozinha.
    val todosOsRegistos: Flow<List<RegistoEmocao>> = registoDao.obterTodosOsRegistos()

    // Estado da sincronização com o Firestore para mostrar feedback na UI.
    // MutableStateFlow é privado — só o ViewModel escreve.
    // A Activity lê apenas a versão StateFlow (só leitura).
    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus

    // Guardar: primeiro grava localmente (funciona sem internet), depois tenta o Firestore.
    // Se o Firestore falhar, o registo já está no Room — o utilizador não perde nada.
    fun guardarRegisto(registo: RegistoEmocao) {
        viewModelScope.launch {
            registoDao.inserirRegisto(registo)

            _syncStatus.value = SyncStatus.Syncing
            val resultado = firestoreRepo.sincronizarRegisto(registo)

            _syncStatus.value = if (resultado.isSuccess) {
                SyncStatus.Success
            } else {
                SyncStatus.Error("Guardado localmente. Sem ligacao ao servidor.")
            }
        }
    }

    // Apagar: remove do Room e depois do Firestore.
    // Se o Firestore falhar (sem internet) o registo já foi apagado localmente —
    // não interrompo o fluxo do utilizador por causa disso.
    fun apagarRegisto(id: Long) {
        viewModelScope.launch {
            registoDao.apagarRegistoPorId(id)
            firestoreRepo.apagarRegistoRemoto(id)
        }
    }

    // Repõe o estado para Idle depois de a Activity mostrar o Toast.
    // Sem isto o mesmo Toast aparecia de novo ao rodar o ecrã.
    fun resetSyncStatus() {
        _syncStatus.value = SyncStatus.Idle
    }
}

// Estados possíveis durante a sincronização com o Firestore.
// sealed class obriga o when() a tratar todos os casos — aprendi que sem isto
// é fácil esquecer um estado e ter comportamento inesperado.
sealed class SyncStatus {
    object Idle    : SyncStatus()
    object Syncing : SyncStatus()
    object Success : SyncStatus()
    data class Error(val message: String) : SyncStatus()
}
