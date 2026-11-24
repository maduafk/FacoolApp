# Facool

Aplicativo Android para organizar a rotina da faculdade: aulas, faltas, notas, provas e um timer Pomodoro.

Feito com **Kotlin** e **Jetpack Compose**. Os dados ficam no celular (Room) e sincronizam com a conta do usuário no **Firebase** (Authentication e Firestore). A tela de fotos consome a API pública [Picsum](https://picsum.photos/).

## O que dá para fazer

- Criar conta e entrar com e-mail e senha
- Montar o cronograma da semana e ver a próxima aula na tela inicial
- Registrar faltas, notas e provas
- Usar um Pomodoro com notificação
- Ver fotos de uma API externa
- Sair da conta e limpar os dados locais

## Como rodar

1. Abra a pasta no Android Studio (JDK 17). O Studio cria o `local.properties` com o caminho do Android SDK. Esse arquivo não entra no Git.
2. Crie um projeto no [Firebase](https://console.firebase.google.com/), registre o app com o pacote `com.facool.app` e baixe o `google-services.json`.
3. Coloque esse arquivo em `app/google-services.json`. O passo a passo está em [FIREBASE_SETUP.md](FIREBASE_SETUP.md).
4. Ative Authentication (e-mail/senha) e o Firestore.
5. Rode o app em um emulador ou celular com Android 8 ou mais recente (`minSdk 26`).

Sem o `google-services.json` o projeto abre, mas login e sincronização não funcionam. Há um modelo sem chaves em `app/google-services.json.example`.

## Stack

Kotlin · Jetpack Compose · Material 3 · Navigation · ViewModel · Room · DataStore · Firebase Auth · Firestore · Retrofit · Coil
