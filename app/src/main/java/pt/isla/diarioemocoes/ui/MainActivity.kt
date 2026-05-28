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

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = getString(R.string.app_name)

        viewModel = ViewModelProvider(this)[RegistoEmocaoViewModel::class.java]

        val btnGuardar  = findViewById<Button>(R.id.buttonGuardar)
        val btnLimpar   = findViewById<Button>(R.id.buttonLimpar)
        val etEstado    = findViewById<EditText>(R.id.editTextEstado)
        val etNotas     = findViewById<EditText>(R.id.editTextNotas)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewRegistos)

        // Adaptador com callback de apagar
        adapter = RegistoAdapter { registo ->
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

        // Observa a lista de registos (Room → UI reactiva)
        lifecycleScope.launch {
            viewModel.todosOsRegistos.collect { lista ->
                adapter.submitList(lista)
            }
        }

        // Observa o estado de sincronização com o Firestore
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

        // Botão Guardar — persiste localmente e sincroniza com Firestore
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

        // Botão Limpar — AlertDialog de confirmação
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
