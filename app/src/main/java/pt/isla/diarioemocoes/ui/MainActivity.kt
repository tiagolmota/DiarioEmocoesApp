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

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: RegistoEmocaoViewModel
    private lateinit var adapter: RegistoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Action Bar — requisito do enunciado
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = getString(R.string.app_name)

        // ViewModelProvider reutiliza o mesmo ViewModel se o ecrã rodar.
        // Se criasse directamente com RegistoEmocaoViewModel(app) perdia o estado
        // toda a vez que o utilizador rodasse o telemóvel.
        viewModel = ViewModelProvider(this)[RegistoEmocaoViewModel::class.java]

        val btnGuardar   = findViewById<Button>(R.id.buttonGuardar)
        val btnLimpar    = findViewById<Button>(R.id.buttonLimpar)
        val etEstado     = findViewById<EditText>(R.id.editTextEstado)
        val etNotas      = findViewById<EditText>(R.id.editTextNotas)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewRegistos)

        // Adaptador da lista. Quando o utilizador clica "Apagar" num item,
        // este lambda é chamado com o registo correspondente.
        adapter = RegistoAdapter { registo ->
            // Alert Dialog de confirmação antes de apagar — requisito do enunciado
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_delete_title))
                .setMessage(getString(R.string.dialog_delete_message))
                .setPositiveButton(getString(R.string.dialog_yes)) { _, _ ->
                    viewModel.apagarRegisto(registo.id)
                    Toast.makeText(this, getString(R.string.toast_deleted), Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(getString(R.string.dialog_no), null)
                .show()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Observar a lista de registos.
        // collect() fica activo permanentemente — cada vez que o Room detecta
        // uma mudança na tabela, emite a lista nova e o adapter actualiza o ecrã.
        lifecycleScope.launch {
            viewModel.todosOsRegistos.collect { lista ->
                adapter.submitList(lista)
            }
        }

        // Observar o estado de sincronização com o Firestore.
        // Sucesso → Toast curto. Erro (sem internet) → Toast mais longo a avisar.
        lifecycleScope.launch {
            viewModel.syncStatus.collect { status ->
                when (status) {
                    is SyncStatus.Success -> {
                        Toast.makeText(
                            this@MainActivity,
                            "Guardado e sincronizado com a nuvem.",
                            Toast.LENGTH_SHORT
                        ).show()
                        viewModel.resetSyncStatus()
                    }
                    is SyncStatus.Error -> {
                        Toast.makeText(
                            this@MainActivity,
                            status.message,
                            Toast.LENGTH_LONG
                        ).show()
                        viewModel.resetSyncStatus()
                    }
                    else -> Unit
                }
            }
        }

        // Botão Guardar — valida, cria o registo e delega ao ViewModel
        btnGuardar.setOnClickListener {
            val estado = etEstado.text.toString().trim()
            val notas  = etNotas.text.toString().trim()

            if (estado.isEmpty()) {
                etEstado.error = getString(R.string.error_empty_state)
                return@setOnClickListener
            }

            val dataHora = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

            val novoRegisto = RegistoEmocao(
                id                  = System.currentTimeMillis(),
                dataHoraLegivel     = dataHora,
                estadoEmocional     = estado,
                temperaturaAmbiente = 0.0,
                notasTexto          = notas
            )

            viewModel.guardarRegisto(novoRegisto)
            etEstado.text.clear()
            etNotas.text.clear()
        }

        // Botão Limpar — Alert Dialog de confirmação antes de limpar os campos
        // Coloquei o Dialog aqui porque limpar campos sem avisar é mau UX
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
