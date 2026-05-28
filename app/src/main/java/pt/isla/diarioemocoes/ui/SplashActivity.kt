package pt.isla.diarioemocoes.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * SPLASH SCREEN — Ecrã de arranque
 * ==================================
 * Requisito obrigatório do enunciado. Apresenta a identidade visual
 * da app durante 2 segundos antes de avançar para o ecrã principal.
 *
 * DECISÃO: Porquê lifecycleScope.launch em vez de Handler().postDelayed()?
 * -------------------------------------------------------------------------
 * Handler().postDelayed() é a abordagem antiga (Java). Problemas:
 * 1. Não é cancelado automaticamente se a Activity for destruída antes dos 2s.
 *    Resultado: tentativa de iniciar uma nova Activity sobre uma destruída → crash.
 * 2. Mistura lógica de threading com lógica de navegação.
 *
 * lifecycleScope.launch é a abordagem moderna com Kotlin Coroutines:
 * 1. O scope está ligado ao ciclo de vida da Activity.
 * 2. Se a Activity for destruída antes dos 2s, a corrotina é cancelada automaticamente.
 * 3. delay(2000) suspende sem bloquear a thread — a UI mantém-se responsiva.
 *
 * @SuppressLint("CustomSplashScreen"): suprime aviso do linter sobre a API
 * nativa de Splash Screen do Android 12+. Para este projecto académico,
 * a implementação manual é suficiente e mais transparente para aprendizagem.
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Não usamos setContentView porque o fundo é definido no tema (themes.xml)
        // O tema Theme.DiarioEmocoesApp.Splash define a cor de fundo como colorPrimary

        lifecycleScope.launch {
            // Passo 1: aguardar 2 segundos (suspende sem bloquear)
            delay(2000)

            // Passo 2: navegar para o ecrã principal
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))

            // Passo 3: fechar esta Activity para não voltar ao splash ao pressionar "Back"
            finish()
        }
    }
}
