# 📦 Cadê a Encomenda? — App Android Nativo

Aplicativo móvel desenvolvido em **Kotlin**, **Jetpack Compose** e **Material Design 3**, pronto para publicação na Google Play Store e monetizado com **Google AdMob**.

---

## 🚀 Tecnologias Utilizadas

- **Linguagem:** Kotlin 100%
- **Interface UI:** Jetpack Compose (Material You / Dynamic Colors)
- **Arquitetura:** MVVM + Clean Architecture (Camadas de Domínio, Dados e Apresentação)
- **Injeção de Dependências:** Hilt (Dagger)
- **Banco de Dados Local:** Room (Cache offline com migrações automáticas)
- **Comunicação de Rede:** Retrofit 2 + Moshi + OkHttp
- **Tarefas em Segundo Plano:** WorkManager (atualização periódica a cada 30-60 minutos com alertas sonoros e visuais)
- **Monetização:** Google Mobile Ads SDK (AdMob)
  - Banner adaptativo fixo
  - Anúncios nativos intercalados na lista
  - Interstitial com controle de frequência (Frequency Capping)
  - Rewarded Ads opcionais
- **Backend Seguro (Proxy):** Firebase Cloud Functions (TypeScript) para proteger as chaves de API das transportadoras.

---

## 📁 Estrutura do Projeto

```
Cade a encomenda/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/cadeaencomenda/app/
│   │   │   ├── CadeAEncomendaApp.kt
│   │   │   ├── MainActivity.kt
│   │   │   ├── domain/               # Entidades, Enums e UseCases de negócio
│   │   │   ├── data/
│   │   │   │   ├── local/            # Room Database, DAOs e Entidades
│   │   │   │   ├── remote/           # Retrofit, DTOs e Mappers
│   │   │   │   ├── preferences/      # DataStore de configurações
│   │   │   │   └── repository/       # Implementação do repositório
│   │   │   ├── di/                   # Módulos Hilt (Database, Network, DataStore, Repo)
│   │   │   ├── worker/               # WorkManager para checagem em segundo plano
│   │   │   └── ui/
│   │   │       ├── components/       # Cards, Badges, Timeline, Sheets, Banners
│   │   │       ├── tracking/         # Tela Principal (Lista + Adicionar)
│   │   │       ├── detail/           # Tela de Detalhes da Encomenda (Timeline)
│   │   │       ├── settings/         # Tela de Configurações e Notificações
│   │   │       ├── onboarding/       # Boas-vindas na primeira inicialização
│   │   │       ├── navigation/       # Rotas e NavHost
│   │   │       └── theme/            # Cores, Tipografia e Tema Material 3
│   │   └── res/                      # Strings, Cores, Ícones e Regras XML
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── backend-firebase/
│   └── functions/                    # Proxy Cloud Function para Ship24 / Correios
├── gradle/                           # Gradle Wrapper e Version Catalogs (libs.versions.toml)
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 🛠️ Como Abrir e Executar

1. Abra o **Android Studio** (versão Ladybug / Iguana ou superior recomendada).
2. Clique em **File > Open** e selecione esta pasta: `c:\Users\gleis\OneDrive\Desktop\Cade a encomenda`.
3. Aguarde o Gradle sincronizar todas as dependências automaticamente.
4. Conecte um celular Android via USB (ou inicie um emulador).
5. Clique no botão verde de **Run** (`Shift + F10`).

---

## 💰 Monetização (Google AdMob)

O projeto já vem pré-configurado com os **IDs de Teste Oficiais do Google**:
- Em modo `debug`: os anúncios de teste do Google funcionam automaticamente sem risco de bloqueio.
- Em modo `release`: basta abrir `app/build.gradle.kts` e inserir os seus IDs reais criados no console da AdMob.

---

## ☁️ Backend Firebase (Opcional para Produção)

Para proteger sua chave de API das transportadoras e evitar engenharia reversa no APK:
1. Instale o Firebase CLI: `npm install -g firebase-tools`
2. Na pasta `backend-firebase/functions`, execute `npm install`
3. Execute `firebase login` e `firebase init`
4. Defina sua chave: `firebase functions:config:set ship24.key="SUA_CHAVE_AQUI"`
5. Faça o deploy: `firebase deploy --only functions`
*(Nota: O app possui simulador inteligente embutido, funcionando perfeitamente para testes mesmo sem a chave do Firebase configurada!)*
