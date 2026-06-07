# -Banco-Manager

Sistema de gerenciamento bancário desktop desenvolvido em **Java** com interface gráfica **Swing**.

## 📋 Funcionalidades

- 🔐 **Tela de Login** com autenticação de usuário
- 👤 **Cadastro de Contas** com CPF, email e telefone
- 💰 **Depósito e Saque** com validação de saldo
- 💸 **Pix** entre contas internas
- 📜 **Histórico de Transações** com extrato detalhado
- 📧 **Envio de Email** via Gmail SMTP (sem dependências externas)

<img width="1920" height="1080" alt="{67906073-C040-4651-B380-0D37952C3B0F}" src="https://github.com/user-attachments/assets/b03bbe08-1fff-45e5-ba96-27965650bb5e" />


## 🛠️ Tecnologias

- Java (Swing / AWT)
- SMTP puro via `SSLSocket` + STARTTLS
- Zero dependências externas — roda com JDK padrão

## 🚀 Como executar

```bash
# 1. Compile
javac -encoding UTF-8 Main.java

# 2. Gere o JAR
jar cfm BancoManager.jar MANIFEST.MF *.class

# 3. Execute
java -jar BancoManager.jar
```

Ou use o script `build_windows.bat` (Windows) / `build_linux_mac.sh` (Linux/Mac).

## ⚙️ Configuração de Email

Edite a classe `GmailConfig` no `Main.java`:

```java
public static final String FROM_EMAIL   = "seuemail@gmail.com";
public static final String APP_PASSWORD = "sua_app_password";
```

> Gere sua App Password em: myaccount.google.com → Segurança → Senhas de app

## 📦 Estrutura

```
BancoManager/
├── Main.java          # Código-fonte completo
├── build_windows.bat  # Build para Windows
└── build_linux_mac.sh # Build para Linux/Mac
```

## 📝 Login padrão

| Usuário | Senha |
|---------|-------|
| admin   | 1234  |
