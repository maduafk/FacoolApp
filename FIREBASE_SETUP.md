# Configuração do Firebase

Para conectar o app ao Firebase, siga estes passos:

## 1. Criar projeto no Firebase Console

1. Acesse [Firebase Console](https://console.firebase.google.com/)
2. Clique em "Adicionar projeto" ou selecione um projeto existente
3. Siga as instruções para criar/configurar o projeto

### ⚠️ Habilitar Billing (Plano Gratuito)

**IMPORTANTE**: O Firestore requer que o billing esteja habilitado, mas você pode usar o **plano Spark (gratuito)** que oferece:
- 1 GB de armazenamento
- 10 GB de transferência por mês
- 50.000 leituras, 20.000 gravações e 20.000 exclusões por dia

Para habilitar:
1. Acesse o link fornecido no erro ou vá em: [Google Cloud Console - Billing](https://console.cloud.google.com/billing)
2. Selecione seu projeto Firebase
3. Escolha o **plano Spark (gratuito)** - não há cobrança
4. Complete o processo (pode pedir informações de cartão, mas não será cobrado no plano gratuito)
5. Aguarde alguns minutos para a configuração ser propagada

## 2. Adicionar app Android ao Firebase

1. No Firebase Console, clique no ícone do Android
2. Preencha:
   - **Nome do pacote Android**: `com.facool.app`
   - **Apelido do app** (opcional): Facool
   - **Certificado de depuração SHA-1** (opcional para desenvolvimento)
3. **IMPORTANTE**: Quando perguntado sobre o formato do arquivo de configuração, selecione **"DSL do Kotlin"** (ou "Kotlin DSL"), pois este projeto usa arquivos `build.gradle.kts`
4. Clique em "Registrar app"

## 3. Baixar arquivo google-services.json

1. Após registrar o app, baixe o arquivo `google-services.json`
2. Coloque o arquivo em: `app/google-services.json`
3. Esse arquivo fica só na sua máquina. Ele já está no `.gitignore` e não deve ser commitado. O repositório traz apenas `app/google-services.json.example`, sem chaves.

## 4. Habilitar serviços no Firebase

### Authentication
1. No Firebase Console, vá em "Authentication"
2. Clique em "Começar"
3. Na aba "Sign-in method", habilite "Email/Password"
4. Clique em "Email/Password" e ative, depois salve

### Firestore Database
1. No Firebase Console, vá em "Firestore Database"
2. Clique em "Criar banco de dados"
3. **Certifique-se de que o billing está habilitado** (veja seção acima)
4. Escolha "Começar no modo de teste" (para desenvolvimento)
5. Selecione uma localização (ex: us-central1 ou southamerica-east1 para Brasil)
6. Clique em "Ativar"
7. Se aparecer erro sobre billing, aguarde alguns minutos após habilitar e tente novamente

## 5. Regras de Segurança do Firestore

Para desenvolvimento, você pode usar estas regras básicas (depois ajuste para produção):

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
      
      match /courses/{courseId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
      
      match /absences/{absenceId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
      
      match /tasks/{taskId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }
  }
}
```

Para aplicar as regras:
1. Vá em "Firestore Database" > "Regras"
2. Cole as regras acima
3. Clique em "Publicar"

## 6. Compilar o projeto

Após adicionar o arquivo `google-services.json`, o projeto deve compilar normalmente. O plugin do Google Services processará automaticamente o arquivo durante a compilação.

## Notas

- O app funciona em modo offline usando DataStore local quando não há conexão
- Quando autenticado, os dados são sincronizados com o Firestore
- Certifique-se de que o arquivo `google-services.json` está na pasta `app/` (mesmo nível do `build.gradle.kts`)

