package pt.isla.diarioemocoes.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Ecrã de arranque — mostra 2 segundos e avança para a MainActivity.
// Uso lifecycleScope em vez de Handler().postDelayed() porque se o utilizador
// sair da app antes dos 2 segundos o Handler continuava a correr e tentava
// arrancar uma Activity sobre outra já destruída. Com lifecycleScope isso
// não acontece — cancela automaticamente quando a Activity é destruída.
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Não uso setContentView — o fundo roxo está definido no tema (themes.xml)

        lifecycleScope.launch {
            delay(2000)
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            // finish() para não voltar ao splash ao pressionar o botão Back
            finish()
        }
    }
}
