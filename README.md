# Smart Emotion Diary — App Movel (Mestrado DAM)

> Unidade Curricular: Desenvolvimento de Aplicacoes Moveis  
> Docente: Prof. Marco Tereso  
> Discente: Tiago Santos Mota | METSW — ISLA Santarem  
> Repositorio: https://github.com/tiagolmota/DiarioEmocoesApp

---

## 1. Descricao do Projeto

O **Smart Emotion Diary** e uma aplicacao Android de diario pessoal inteligente que permite o registo contextualizado de estados emocionais, integrando persistencia de dados local, processamento assincrono e uma interface de utilizador responsiva.

O projeto foi desenvolvido com enfase na fundamentacao arquitetural, seguindo os principios de **Clean Architecture** e o padrao **MVVM (Model-View-ViewModel)**, garantindo a separacao de preocupacoes (*separation of concerns*) e a escalabilidade do codigo.

---

## 2. Arquitetura e Tecnologias

| Camada | Componente | Tecnologia |
|--------|-----------|------------|
| **Model** | Entidade de dados | Room Entity (`@Entity`) |
| **Model** | Acesso a dados | Room DAO (`@Dao` + `Flow`) |
| **Model** | Base de dados | Room `RoomDatabase` (Singleton) |
| **ViewModel** | Logica de negocio | `AndroidViewModel` + `viewModelScope` |
| **View** | Interface principal | `ConstraintLayout` + Material Design 3 |
| **View** | Listagem reativa | `RecyclerView` + `ListAdapter` |
| **View** | Ecrã de arranque | `SplashActivity` + Coroutines |

### Stack Tecnica

- **Linguagem:** Kotlin
- **Persistencia local:** Room (SQLite abstraction layer)
- **Assincronia:** Kotlin Coroutines + `Flow`
- **Ciclo de vida:** `AndroidViewModel` (prevencao de memory leaks)
- **UI:** XML Layouts com `ConstraintLayout` e Material Components
- **Gestao de versoes:** Git + GitHub

---

## 3. Plano de Desenvolvimento Estruturado

### Fase 1 — Fundacao e Estrutura (Setup)
- [x] Criacao do projeto no Android Studio com Kotlin
- [x] Inicializacao do repositorio Git e `.gitignore`
- [x] Integracao das dependencias no `build.gradle.kts` (Room, ViewModel, Lifecycle, Material)

### Fase 2 — Camada de Dados (Persistencia)
- [x] `RegistoEmocao.kt` — Entidade Room com `Long` (timestamp) como chave primaria
- [x] `RegistoEmocaoDao.kt` — DAO com `insert`, `delete` e `query` via `Flow`
- [x] `AppDatabase.kt` — Singleton thread-safe com double-checked locking

### Fase 3 — Camada de Logica (Business Logic)
- [x] `RegistoEmocaoViewModel.kt` — herda `AndroidViewModel` (acesso seguro ao `Context`)
- [x] Ponte ViewModel <-> DAO via corrotinas (`viewModelScope.launch`)

### Fase 4 — Camada de Apresentacao (UI)
- [x] `SplashActivity.kt` — ecrã de arranque com transicao de 2s via `lifecycleScope`
- [x] `activity_main.xml` — `ConstraintLayout` com correntes horizontais nos botoes
- [x] `item_registo.xml` — `MaterialCardView` para listagem elegante
- [x] `RegistoAdapter.kt` — `ListAdapter` com `DiffUtil` para atualizacoes eficientes
- [x] `Toast` — feedback de sucesso ao guardar/apagar
- [x] `AlertDialog` — confirmacao em acoes destrutivas (Limpar e Apagar)
- [x] `Action Bar` — `Toolbar` com Material Theme

### Fase 5 — Validacao e Documentacao
- [x] README academico com arquitetura e checklist
- [ ] Testes de integracao (fluxo Inserir -> Persistir -> Listar)
- [ ] Integracao com base de dados externa (Firebase / API PHP)
- [ ] Relatório final com revisao de conhecimentos

---

## 4. Estrutura do Repositorio

```
DiarioEmocoesApp/
├── app/
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/pt/isla/diarioemocoes/
│       │   ├── data/
│       │   │   ├── RegistoEmocao.kt        # Entidade Room
│       │   │   ├── RegistoEmocaoDao.kt     # CRUD + Flow
│       │   │   └── AppDatabase.kt          # Singleton DB
│       │   └── ui/
│       │       ├── SplashActivity.kt       # Splash Screen
│       │       ├── MainActivity.kt         # CRUD + AlertDialog + Toast
│       │       ├── RegistoEmocaoViewModel.kt  # AndroidViewModel
│       │       └── RegistoAdapter.kt       # RecyclerView ListAdapter
│       └── res/
│           ├── layout/
│           │   ├── activity_main.xml       # Layout principal
│           │   ├── activity_splash.xml     # Layout splash
│           │   └── item_registo.xml        # Card de cada registo
│           └── values/
│               ├── strings.xml
│               ├── colors.xml
│               └── themes.xml
├── build.gradle.kts
├── settings.gradle.kts
└── gradle/libs.versions.toml
```

---

## 5. Requisitos do Enunciado — Checklist

| Requisito | Estado | Implementacao |
|-----------|--------|---------------|
| Splash Screen | OK | `SplashActivity` com delay de 2s |
| Action Bar | OK | `Toolbar` com `setSupportActionBar` |
| Alert Dialog | OK | Confirmar Limpar e Apagar registo |
| Toast | OK | Feedback ao guardar e apagar |
| CRUD SQL | OK | Room: Insert, Select (Flow), Delete |
| Base de dados local | OK | SQLite via Room |
| RecyclerView | OK | `ListAdapter` + `DiffUtil` |
| Arquitetura MVVM | OK | Model / ViewModel / View separados |

---

## 6. Como Importar no Android Studio

```
1. File > New > Project from Version Control
2. URL: https://github.com/tiagolmota/DiarioEmocoesApp.git
3. Aguardar sync do Gradle
4. Run > Run 'app'
```

**Requisitos:** Android Studio Hedgehog ou superior | SDK minimo: API 24 (Android 7.0)

---

## 7. Tabela de Responsabilidades (Trabalho de Grupo)

| Tarefa | Objetivo Tecnico | Prioridade |
|--------|-----------------|------------|
| Configurar Git/Gradle | Preparar ambiente de trabalho | Alta |
| Definir Modelo/DAO | Garantir integridade dos dados | Alta |
| Implementar ViewModel | Separacao de logica e View | Media |
| Desenhar Layouts XML | Responsividade e UI/UX | Media |
| Implementar ClickListeners | AlertDialog e Toast | Media |
| Integracao base externa | Firebase ou API PHP | Baixa |

---

## 8. Notas de Transparencia (IAGen)

Este projeto foi desenvolvido com o apoio de ferramentas de IA Generativa para tutoria tecnica e validacao de padroes de arquitetura Android. Toda a implementacao foi escrutinada pelo discente face a documentacao tecnica oficial da Android Developers (Google), garantindo o cumprimento dos requisitos de integridade academica da instituicao, em conformidade com o artigo 3 do Guiao de Procedimentos de IA Generativa adotado pelas IES da Lusofona.

---

*Versao 1.0 — Maio 2026*
