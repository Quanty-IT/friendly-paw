# Friendly Paw

Visão geral
-----------
Friendly Paw é uma aplicação Desktop em JavaFX para gerenciamento veterinário de uma ONG, permitindo o cadastro de animais, controle de medicamentos, marcas de medicamentos, histórico de aplicação de medicamentos, integração com Google Calendar para agendamentos e Google Drive para gerenciamento de anexos.

Pré-requisitos
--------------
- Java JDK 17 (conforme especificado no pom.xml)
- Maven 3.6+
- PostgreSQL 12+ (banco de dados)
- Credenciais de autenticação Google (para Google Calendar e Google Drive)

Como rodar localmente
---------------------
1. Clone o repositório:
   ```
   git clone <repository-url>
   cd friendly-paw-main
   ```

2. Configure o banco de dados PostgreSQL:
   - Crie um banco de dados vazio
   - Atualize as credenciais no arquivo de configuração da aplicação (em src/config/Database.java)

3. Instale as dependências e compile:
   ```
   mvn clean install
   ```

4. Execute as migrações do banco de dados:
   - As migrações são localizadas em src/migrations/
   - Elas são executadas automaticamente pela classe MigrationRunner.java ao iniciar a aplicação

5. Inicie a aplicação:
   ```
   mvn javafx:run
   ```
   Ou execute o jar diretamente:
   ```
   mvn clean package
   java -jar target/friendly-paw-1.0.0.jar
   ```

Estrutura do projeto
--------------------
```
src/
├── MainApp.java                          # Classe de entrada da aplicação
├── config/
│   └── Database.java                     # Configuração do banco de dados
├── migrations/                           # Scripts de migrações do banco
│   ├── Migration.java
│   ├── MigrationRunner.java
│   ├── M20250908093708CreateUsersTable.java
│   ├── M20250908095542CreateAnimalsTable.java
│   ├── M20250915205000CreateMedicineBrandsTable.java
│   ├── M20250915210000CreateMedicinesTable.java
│   ├── M20250919145100CreateAttachmentsTable.java
│   └── M20251010104951CreateMedicineApplicationsTable.java
├── modules/
│   ├── Animal/                           # Gestão de animais
│   │   ├── controllers/
│   │   ├── models/
│   │   ├── styles/
│   │   └── views/
│   ├── Medicine/                         # Gestão de medicamentos
│   │   ├── controllers/
│   │   ├── models/
│   │   ├── styles/
│   │   └── views/
│   ├── MedicineBrand/                    # Gestão de marcas de medicamentos
│   │   ├── controllers/
│   │   ├── models/
│   │   ├── styles/
│   │   └── views/
│   ├── MedicineApplication/              # Histórico de aplicação de medicamentos
│   │   ├── controllers/
│   │   ├── models/
│   │   ├── styles/
│   │   ├── services/ (integração Google Calendar)
│   │   └── views/
│   ├── Attachment/                       # Gestão de anexos
│   │   ├── controllers/
│   │   ├── models/
│   │   ├── services/ (integração Google Drive)
│   │   ├── styles/
│   │   └── views/
│   ├── User/                             # Autenticação de usuários
│   │   ├── controllers/
│   │   ├── models/
│   │   ├── styles/
│   │   └── views/
│   └── Shared/                           # Componentes compartilhados
│       ├── styles/
│       └── views/
├── utils/
│   ├── CreateMigration.java              # Utilitário para criar novas migrações
│   └── Session.java                      # Gerenciamento de sessão de usuário
└── assets/
    └── icons/                            # Ícones da aplicação
```

Dependências principais
-----------------------
- **JavaFX** 17.0.2: Framework UI para interface gráfica
- **PostgreSQL** 42.7.7: Driver JDBC para banco de dados
- **Google Calendar API** v3: Integração com Google Calendar
- **Google Drive API** v3: Integração com Google Drive
- **Jackson** 2.15.2: Processamento de JSON
- **Apache HttpClient** 4.5.14: Cliente HTTP para requisições

Configuração
------------
A configuração do banco de dados é realizada em `src/config/Database.java`.

Migrações de banco de dados
---------------------------
As migrações estão em `src/migrations/` e são executadas automaticamente pela classe `MigrationRunner.java` ao iniciar a aplicação. Cada migração segue o padrão de nomenclatura `M<timestamp><Description>.java`.

Para criar uma nova migração, use o utilitário:
```
java src/utils/CreateMigration.java NomeDaMigracao
```

Integração com Google APIs
----------------------------
A aplicação integra com:
- **Google Calendar**: Para agendamento de aplicações de medicamentos (classe `GoogleCalendarService.java`)
- **Google Drive**: Para gerenciamento de anexos (classe `GoogleDriveOAuthService.java`)

Certifique-se de ter configurado credenciais OAuth 2.0 no Google Cloud Console.

Compilação e build
------------------
```
mvn clean package
```

Execução de testes
------------------
```
mvn test
```

Boas práticas
-------------
- Manter credenciais sensíveis (tokens Google, senhas) fora do controle de versão
- Realizar commits apenas do código-fonte, não de artefatos compilados (target/)
- Seguir o padrão MVC (Model-View-Controller) já estabelecido na estrutura
- Documentar novas migrações de banco de dados

Checklist antes de rodar
------------------------
- [ ] Java JDK 17 instalado e configurado
- [ ] PostgreSQL instalado e rodando
- [ ] Banco de dados criado e acessível
- [ ] Migrações foram executadas com sucesso
- [ ] Credenciais Google configuradas (se usar Calendar/Drive)
- [ ] Maven instalado
