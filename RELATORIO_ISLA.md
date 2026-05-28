# Smart Emotion Diary
## Relatório de Trabalho Prático
### Unidade Curricular: Desenvolvimento de Aplicações Móveis

---

| Campo | Informação |
|---|---|
| **Instituição** | ISLA Santarém — Instituto Politécnico |
| **Curso** | METSW — Mestrado em Engenharia de Tecnologias e Sistemas Web |
| **UC** | Desenvolvimento de Aplicações Móveis |
| **Docente** | Marco Tereso |
| **Discente** | Tiago Santos Mota |
| **Ano lectivo** | 2025/2026 |
| **Data de entrega** | 12 de Junho de 2025 |
| **Repositório** | https://github.com/tiagolmota/DiarioEmocoesApp |

---

## 1. Introdução e Tema

O presente relatório descreve o processo de desenvolvimento da aplicação **Smart Emotion Diary**, um diário emocional para a plataforma Android. A aplicação permite ao utilizador registar o seu estado emocional, associar notas pessoais e consultar o histórico de registos, com persistência local e sincronização com uma base de dados na nuvem.

O tema foi seleccionado pela sua relevância no contexto do bem-estar digital e pela oportunidade de explorar uma arquitectura de dados híbrida (local + remota), que constitui o principal foco de investigação do enunciado.

O desenvolvimento foi realizado individualmente, com recurso a ferramentas de IA Generativa como auxílio tutorial na compreensão de padrões de arquitectura Android e validação de decisões técnicas. Toda a implementação foi verificada contra a documentação oficial da Android Developers (Google), em cumprimento das normas de integridade académica da instituição.

---

## 2. Revisão de Conhecimentos

### 2.1 Arquitectura MVVM (Model-View-ViewModel)

A arquitectura adoptada no projecto segue o padrão MVVM, recomendado oficialmente pela Google para desenvolvimento Android moderno. Este padrão divide a aplicação em três camadas com responsabilidades distintas:

**Model** — representa os dados e a lógica de acesso aos mesmos. Engloba a entidade `RegistoEmocao`, o DAO (`RegistoEmocaoDao`) e a base de dados (`AppDatabase`). É a única camada que conhece os detalhes de implementação do SQLite e do Firestore.

**ViewModel** — actua como intermediário entre o Model e a View. Contém a lógica de negócio (validações, orquestração de operações) e expõe os dados através de streams reactivos (`Flow`, `StateFlow`). Sobrevive a mudanças de configuração como a rotação do ecrã.

**View** — a camada de interface, implementada pela `MainActivity`. Observa os dados expostos pelo ViewModel e actualiza a UI em resposta a mudanças, sem conter lógica de dados.

Esta separação traz benefícios concretos: testabilidade (o ViewModel pode ser testado sem emulador), manutenibilidade (mudanças na UI não afectam a lógica de dados) e robustez (o estado não se perde em rotações de ecrã).

### 2.2 Persistência Local com Room (SQLite)

A biblioteca Room é uma camada de abstracção sobre o SQLite nativo do Android. A escolha do Room em detrimento do SQLite directo justifica-se por três razões:

**Verificação em tempo de compilação** — as queries SQL escritas nas anotações `@Query` são verificadas pelo compilador antes de a app correr. Um erro de sintaxe SQL é detectado no build, não em runtime.

**Eliminação de boilerplate** — o Room gera automaticamente a implementação do DAO. O programador define apenas o contrato (interface) e o Room cria o código de acesso à base de dados.

**Integração com Coroutines** — através das funções `suspend` e do tipo `Flow`, o Room garante que todas as operações de I/O ocorrem em threads secundárias, nunca bloqueando a interface do utilizador.

A chave primária da tabela utiliza o timestamp em milissegundos (`System.currentTimeMillis()`), garantindo unicidade absoluta mesmo com múltiplos registos no mesmo dia.

### 2.3 Programação Assíncrona com Kotlin Coroutines

O Android proíbe operações lentas (disco, rede) na Main Thread — a thread responsável por desenhar o ecrã. Se uma operação de base de dados corresse na Main Thread, a interface ficaria suspensa durante essa operação, resultando num erro ANR (Application Not Responding).

As Kotlin Coroutines resolvem este problema através de um mecanismo de suspensão cooperativa: a função `suspend` sinaliza que a operação pode ser pausada sem bloquear a thread. A execução é retomada automaticamente quando o resultado fica disponível.

O `viewModelScope` fornece um `CoroutineScope` ligado ao ciclo de vida do ViewModel. Quando o utilizador sai da aplicação, o scope cancela automaticamente todas as corrotinas pendentes, prevenindo fugas de memória.

### 2.4 Programação Reactiva com Flow

`Flow` é um stream de dados assíncrono do Kotlin. No contexto do Room, um método DAO que devolve `Flow<List<T>>` não executa a query uma vez e termina — mantém-se activo e emite uma nova lista sempre que a tabela é modificada.

Esta abordagem reactiva elimina a necessidade de consultar manualmente a base de dados após cada inserção ou remoção. A UI "subscreve" ao Flow e actualiza-se automaticamente, resultando num código mais simples e menos propenso a inconsistências entre o estado da base de dados e o que é mostrado no ecrã.

### 2.5 Estratégia de Base de Dados Externa — Firebase Firestore

O enunciado estabelece como foco principal de investigação a estratégia de implementação da base de dados externa. Foram consideradas duas abordagens:

**HTTP Request a scripts PHP com resposta JSON** — exige a manutenção de um servidor web com PHP e MySQL, configuração de endpoints REST e tratamento manual de serialização/deserialização JSON.

**Firebase Firestore** — base de dados NoSQL gerida pelo Google, sem necessidade de servidor próprio. Oferece SDK nativo para Android com suporte directo a Kotlin Coroutines através da extensão `.await()`.

A escolha recaiu sobre o **Firebase Firestore** pelos seguintes fundamentos:

1. **Ausência de infraestrutura** — elimina a gestão de servidor, actualizações de segurança e configurações de rede, adequando-se ao contexto de um protótipo académico.

2. **Modelo de dados** — o Firestore usa documentos JSON, estrutura naturalmente compatível com os objectos Kotlin da aplicação.

3. **Integração com Coroutines** — `task.await()` transforma operações assíncronas do Firebase em código sequencial legível, sem callbacks aninhados.

4. **Escalabilidade** — num cenário de produção real com múltiplos utilizadores, o Firestore escala automaticamente, ao contrário de um servidor PHP dimensionado manualmente.

### 2.6 Padrão Offline-First

A estratégia de sincronização implementada segue o padrão **offline-first**: a aplicação funciona na sua totalidade sem ligação à internet, usando o Room como fonte de verdade local. A sincronização com o Firestore é um processo secundário e não-bloqueante.

Fluxo de uma operação de escrita:
1. O utilizador submete o registo → inserção imediata no Room (sem internet necessária).
2. A UI actualiza-se instantaneamente via Flow reactivo.
3. Em background, o ViewModel tenta sincronizar com o Firestore.
4. O resultado (sucesso ou falha de rede) é comunicado à UI via `StateFlow`.

Esta abordagem garante que o utilizador nunca perde dados por ausência de conectividade e que a experiência de utilização não depende da latência de rede.

### 2.7 Padrão Singleton para a Base de Dados

A instanciação da `AppDatabase` segue o padrão Singleton com Double-Checked Locking. Abrir uma ligação SQLite é uma operação dispendiosa que aloca recursos do sistema. Múltiplas instâncias simultâneas introduziriam risco de condições de corrida nos dados e consumo excessivo de memória.

A anotação `@Volatile` garante que todas as threads lêem o valor da variável directamente da memória principal, eliminando leituras de cache inconsistentes em ambientes multi-thread.

---

## 3. Estratégias de Implementação

### 3.1 Componentes Obrigatórios

**Splash Screen** — implementada via `SplashActivity` com `lifecycleScope.launch` e `delay(2000)`. A abordagem com Coroutines foi preferida ao `Handler().postDelayed()` por garantir cancelamento automático se a Activity for destruída antes do delay terminar.

**Action Bar** — implementada via `Toolbar` do Material Design configurada com `setSupportActionBar()`. Permite futura adição de menus e itens de acção sem refactorização da Activity.

**Alert Dialog** — utilizado em dois contextos: confirmação antes de limpar os campos de texto (acção reversível mas disruptiva) e confirmação antes de apagar um registo (acção destrutiva e irreversível). O padrão de mostrar confirmação antes de acções destrutivas é uma boa prática de UX estabelecida pelas Human Interface Guidelines.

**Toast** — feedback rápido após guardar com sucesso, após apagar e em caso de falha de sincronização remota. A duração `LENGTH_SHORT` foi usada para acções positivas e `LENGTH_LONG` para erros, seguindo as convenções da plataforma Android.

**CRUD SQL** — implementado com Room sobre SQLite:
- **Create**: `@Insert` com `OnConflictStrategy.REPLACE`
- **Read**: `@Query` com devolução de `Flow<List<RegistoEmocao>>`
- **Update**: implícito via REPLACE na inserção (mesmo ID substitui o registo)
- **Delete**: `@Query DELETE` com parâmetro de ID

### 3.2 Interface do Utilizador

O layout principal usa `ConstraintLayout` com correntes horizontais (`chains`) para os dois botões de acção. Esta técnica distribui o espaço disponível proporcionalmente entre os botões, garantindo responsividade em ecrãs de diferentes dimensões sem código condicional.

A listagem de registos usa `RecyclerView` com `ListAdapter` e `DiffUtil`. O `DiffUtil` calcula em background as diferenças entre a lista antiga e a nova, animando apenas os itens que realmente mudaram — técnica de optimização de performance recomendada para listas dinâmicas.

Cada item da lista é apresentado num `MaterialCardView`, componente do Material Design 3 que fornece elevação, cantos arredondados e conformidade automática com o tema da aplicação.

---

## 4. Estrutura do Projecto

```
app/src/main/java/pt/isla/diarioemocoes/
├── data/
│   ├── RegistoEmocao.kt          Entidade Room (@Entity)
│   ├── RegistoEmocaoDao.kt       DAO com operações CRUD + Flow
│   ├── AppDatabase.kt            Singleton da base de dados local
│   └── FirestoreRepository.kt    Repositório da base de dados externa
└── ui/
    ├── SplashActivity.kt         Ecrã de arranque (2s)
    ├── MainActivity.kt           Ecrã principal — CRUD + AlertDialog + Toast
    ├── RegistoEmocaoViewModel.kt AndroidViewModel — lógica de negócio + SyncStatus
    └── RegistoAdapter.kt         ListAdapter com DiffUtil para o RecyclerView
```

---

## 5. Dificuldades e Aprendizagens

O desenvolvimento individual desta aplicação representou um primeiro contacto com o ecossistema Android nativo em Kotlin. As principais dificuldades encontradas e as soluções adoptadas foram:

**Compreensão do ciclo de vida das Activities** — a rotação do ecrã destrói e recria a Activity. A solução foi o ViewModel: os dados persistem no ViewModel independentemente das recriações da Activity.

**Threading e operações assíncronas** — a proibição de I/O na Main Thread foi inicialmente contraintuitiva. A compreensão do modelo de Coroutines (suspend, launch, viewModelScope) resolveu este problema de forma elegante.

**Memory leaks e referências ao Context** — a distinção entre `Activity.context` e `Application.context` foi esclarecida pela adopção do `AndroidViewModel`, que recebe a `Application` (ciclo de vida do processo) em vez da `Activity` (ciclo de vida do ecrã).

**Integração Firebase** — a configuração do `google-services.json` e a compreensão do modelo de documentos NoSQL do Firestore foram as principais curvas de aprendizagem na integração com a base de dados externa.

---

## 6. Conclusão

A aplicação **Smart Emotion Diary** cumpre a totalidade dos requisitos definidos no enunciado: Splash Screen, Action Bar, Alert Dialog, CRUD SQL e Toast, com ligação a uma base de dados externa via Firebase Firestore.

A arquitectura MVVM adoptada garante separação de responsabilidades, testabilidade e robustez face a eventos do ciclo de vida Android. A estratégia offline-first assegura que a aplicação é funcional independentemente da disponibilidade de rede.

O processo de desenvolvimento individual, apoiado por tutoria com ferramentas de IA e validação constante contra documentação técnica oficial, demonstrou que é possível produzir código de qualidade mesmo sem experiência prévia em desenvolvimento Android, desde que as decisões técnicas sejam fundamentadas e documentadas.

---

## 7. Referências

- Android Developers. (2024). *Guide to app architecture*. Google. https://developer.android.com/topic/architecture
- Android Developers. (2024). *Room persistence library*. Google. https://developer.android.com/training/data-storage/room
- Android Developers. (2024). *Kotlin coroutines on Android*. Google. https://developer.android.com/kotlin/coroutines
- Android Developers. (2024). *Kotlin flows on Android*. Google. https://developer.android.com/kotlin/flow
- Firebase. (2024). *Cloud Firestore documentation*. Google. https://firebase.google.com/docs/firestore
- Android Developers. (2024). *ViewModel overview*. Google. https://developer.android.com/topic/libraries/architecture/viewmodel
- JetBrains. (2024). *Kotlin coroutines guide*. https://kotlinlang.org/docs/coroutines-guide.html
- Material Design. (2024). *Material Design 3 for Android*. Google. https://m3.material.io/develop/android

---

*Tiago Santos Mota — METSW 2025/2026 — ISLA Santarém*
