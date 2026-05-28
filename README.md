# Diario de Emocoes App

Projeto desenvolvido para a UC Desenvolvimento de Aplicacoes Moveis  
Mestrado em Engenharia de Tecnologias e Sistemas Web — ISLA Santarem  
Docente: Prof. Marco Tereso

## Arquitetura

MVVM (Model-View-ViewModel) com persistencia local via Room/SQLite.

```
app/src/main/java/pt/isla/diarioemocoes/
├── data/
│   ├── RegistoEmocao.kt      # Entidade Room
│   ├── RegistoEmocaoDao.kt   # Data Access Object
│   └── AppDatabase.kt        # Singleton da base de dados
└── ui/
    ├── SplashActivity.kt     # Ecra de carregamento (requisito)
    ├── MainActivity.kt       # Actividade principal (CRUD)
    ├── RegistoEmocaoViewModel.kt  # ViewModel (MVVM)
    └── RegistoAdapter.kt     # RecyclerView Adapter
```

## Tecnologias

- Kotlin
- Android Room (SQLite)
- Kotlin Coroutines + Flow
- ViewModel / AndroidViewModel
- Material Design 3
- ConstraintLayout

## Requisitos cumpridos

- [x] Splash Screen
- [x] Action Bar (Toolbar)
- [x] Alert Dialog (confirmacao de apagar / limpar)
- [x] Toast (feedback de guardar / apagar)
- [x] CRUD com base de dados local (Room/SQLite)
- [x] RecyclerView para listagem de registos

## Como importar no Android Studio

1. File > New > Project from Version Control
2. URL: `https://github.com/tiagolmota/DiarioEmocoesApp.git`
3. Aguardar sync do Gradle
4. Run > Run 'app'
