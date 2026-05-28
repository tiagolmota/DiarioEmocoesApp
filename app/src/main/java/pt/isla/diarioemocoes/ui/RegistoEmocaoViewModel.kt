package pt.isla.diarioemocoes.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import pt.isla.diarioemocoes.data.AppDatabase
import pt.isla.diarioemocoes.data.RegistoEmocao

class RegistoEmocaoViewModel(application: Application) : AndroidViewModel(application) {

    private val registoDao = AppDatabase.getDatabase(application).registoDao()

    val todosOsRegistos: Flow<List<RegistoEmocao>> = registoDao.obterTodosOsRegistos()

    fun guardarRegisto(registo: RegistoEmocao) {
        viewModelScope.launch {
            registoDao.inserirRegisto(registo)
        }
    }

    fun apagarRegisto(id: Long) {
        viewModelScope.launch {
            registoDao.apagarRegistoPorId(id)
        }
    }
}
