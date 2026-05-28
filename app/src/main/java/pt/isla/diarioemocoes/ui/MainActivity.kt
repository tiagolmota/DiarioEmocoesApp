package pt.isla.diarioemocoes.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import pt.isla.diarioemocoes.R
import pt.isla.diarioemocoes.data.RegistoEmocao
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ACTIVITY PRINCIPAL — Ecrã de CRUD do Diário Emocional
 * =======================================================
 * Esta Activity é o ponto de entrada da interface após o Splash Screen.
 * Implementa todos os componentes obrigatórios do enunciado:
 *   - Action Bar (Toolbar configurada via setSupportActionBar)
 *   - Alert Dialog (confirmação de Limpar e de Apagar registo)
 *   - Toast (feedback de guardar, apagar e erro de sincronização)
 *   - CRUD visual (inserção via formulário, listagem via RecyclerView, remoção por item)
 *
 * DECISÃO: Porquê AppCompatActivity?
 * ------------------------------------
 * AppCompatActivity fornece retrocompatibilidade com versões antigas do Android
 * e suporte ao Material Design (Toolbar, AlertDialog estilizado, etc.).
 * É a classe base recomendada pela Google para todas as Activities modernas.
 *
 * DECISÃO: Porquê ViewModelProvider em vez de instanciar directamente?
 * ---------------------------------------------------------------------
 * ViewModelProvider garante que o mesmo ViewModel é reutilizado se a Activity
 * for recriada (ex: rotação do ecrã). Se usássemos RegistoEmocaoViewModel(app)
 * directamente, criaria uma nova instância a cada rotação — perdendo o estado.
 *
 * DECISÃO: Porquê lifecycleScope.launch para observar o Flow?
 * ------------------------------------------------------------
 * collect() num Flow é uma operação suspensa (não termina — espera por valores).
 * lifecycleScope garante que a colecção é cancelada quando a Activity é destruída,
 * evitando fugas de memória e actualizações de UI em Activities mortas.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: RegistoEmocaoViewModel
    private lateinit var adapter: RegistoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // --- ACTION BAR (requisito do enunciado) ---
        // Configura a Toolbar como ActionBar nativa da Activity
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = getString(R.string.app_name)

        // --- VIEWMODEL ---
        // ViewModelProvider reutiliza a instância existente após rotações de ecrã
        viewModel = ViewModelProvider(this)[RegistoEmocaoViewModel::class.java]

        // --- REFERÊNCIAS AOS ELEMENTOS DA UI ---
        val btnGuardar   = findViewById<Button>(R.id.buttonGuardar)
        val btnLimpar    = findViewById<Button>(R.id.buttonLimpar)
        val etEstado     = findViewById<EditText>(R.id.editTextEstado)
        val etNotas      = findViewById<EditText>(R.id.editTextNotas)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewRegistos)

        // --- RECYCLERVIEW + ADAPTADOR ---
        // O adaptador recebe um lambda que é chamado quando o utilizador clica "Apagar"
        adapter = RegistoAdapter { registo ->
            // ALERT DIALOG — confirmação de apagar (requisito do enunciado)
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_delete_title))
                .setMessage(getString(R.string.dialog_delete_message))
                .setPositiveButton(getString(R.string.dialog_yes)) { _, _ ->
                    viewModel.apagarRegisto(registo.id)
                    // TOAST — feedback de apagar (requisito do enunciado)
                    Toast.makeText(this, getString(R.string.toast_deleted), Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(getString(R.string.dialog_no), null) // null = fechar sem acção
                .show()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // --- OBSERVAR LISTA DE REGISTOS ---
        // Quando o Room detecta uma mudança na tabela, o Flow emite a lista actualizada
        // e o submitList() do ListAdapter calcula o diff e anima apenas as mudanças
        lifecycleScope.launch {
            viewModel.todosOsRegistos.collect { lista ->
                adapter.submitList(lista)
            }
        }

        // --- OBSERVAR ESTADO DE SINCRONIZAÇÃO COM FIRESTORE ---
        // Reage ao SyncStatus emitido pelo ViewModel após cada operação remota
        lifecycleScope.launch {
            viewModel.syncStatus.collect { status ->
                when (status) {
                    is SyncStatus.Success -> {
                        Toast.makeText(
                            this@MainActivity,
                            "Guardado e sincronizado com a nuvem.",
                            Toast.LENGTH_SHORT
                        ).show()
                        viewModel.resetSyncStatus() // evita re-mostrar em rotações
                    }
                    is SyncStatus.Error -> {
                        // Informa o utilizador que ficou guardado localmente
                        Toast.makeText(
                            this@MainActivity,
                            status.message,
                            Toast.LENGTH_LONG
                        ).show()
                        viewModel.resetSyncStatus()
                    }
                    else -> Unit // Idle e Syncing não precisam de acção na UI
                }
            }
        }

        // --- BOTÃO GUARDAR ---
        btnGuardar.setOnClickListener {
            val estado = etEstado.text.toString().trim()
            val notas  = etNotas.text.toString().trim()

            // Validação: estado emocional é obrigatório
            if (estado.isEmpty()) {
                etEstado.error = getString(R.string.error_empty_state)
                return@setOnClickListener
            }

            // Formata a data actual para leitura humana
            val dataHora = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

            // Cria o objecto com timestamp como ID (unicidade garantida)
            val novoRegisto = RegistoEmocao(
                id                  = System.currentTimeMillis(),
                dataHoraLegivel     = dataHora,
                estadoEmocional     = estado,
                temperaturaAmbiente = 0.0, // campo para expansão futura (sensor)
                notasTexto          = notas
            )

            // Delega ao ViewModel (offline-first: Room → Firestore)
            viewModel.guardarRegisto(novoRegisto)

            // Limpa os campos após guardar
            etEstado.text.clear()
            etNotas.text.clear()
        }

        // --- BOTÃO LIMPAR ---
        // ALERT DIALOG — confirmação antes de limpar campos (requisito do enunciado)
        btnLimpar.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_clear_title))
                .setMessage(getString(R.string.dialog_clear_message))
                .setPositiveButton(getString(R.string.dialog_yes)) { _, _ ->
                    etEstado.text.clear()
                    etNotas.text.clear()
                }
                .setNegativeButton(getString(R.string.dialog_no), null)
                .show()
        }
    }
}
