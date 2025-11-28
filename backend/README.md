# ✂️ CortaAi API (Backend)

Este é o backend do **CortaAi**, um sistema de marketplace para barbearias. A API foi desenvolvida em Java com Spring Boot e tem como objetivo automatizar o agendamento de serviços, gerenciar múltiplos estabelecimentos e conectar clientes, barbeiros e donos de barbearias.

A solução substitui o agendamento manual, que pode gerar desorganização, por uma plataforma centralizada que traz eficiência, organização e visibilidade para o negócio.

---

## 🔥 Funcionalidades Principais

O sistema é construído sobre três papéis principais: **Cliente**, **Barbeiro** e **Barbeiro Dono**.

### 🧔🏻 Para Clientes (`ROLE_CUSTOMER`)
- **Autenticação**: Cadastro e login seguros com tokens JWT.
- **Busca de Barbearias**: Encontra e visualiza detalhes de barbearias, seus serviços e barbeiros.
- **Agendamento Flexível**: Agenda serviços com um barbeiro específico em um horário disponível. A duração do agendamento é calculada automaticamente com base nos serviços selecionados.
- **Gestão de Agendamentos**: Consulta e cancela os próprios agendamentos.
- **Gestão de Perfil**: Atualiza informações pessoais e foto de perfil.

### 💈 Para Barbeiros (`ROLE_BARBER`)
- **Autenticação**: Cadastro e login seguros na plataforma.
- **Gestão de Agenda**: Consulta a própria agenda de trabalho.
- **Vínculo com Barbearias**: Pode solicitar a entrada em uma barbearia existente através do CNPJ.
- **Gestão de Habilidades**: Define quais serviços (criados pelo dono) está apto a realizar.
- **Horário de Trabalho**: Configura seu próprio horário de início e fim de expediente para controlar a disponibilidade.
- **Gestão de Perfil**: Atualiza suas informações e foto profissional.

### 👑 Para Donos de Barbearia (`ROLE_OWNER`)
- **Todas as funcionalidades de Barbeiro**.
- **Gestão da Barbearia**: Cria e gerencia os dados da sua barbearia (nome, endereço, logo, banner e fotos de destaque).
- **Gestão de Serviços**: Cria e gerencia o menu de serviços (atividades) que a barbearia oferece, definindo nome, preço e duração.
- **Gestão de Equipe**: Aprova ou recusa pedidos de entrada de novos barbeiros e pode remover barbeiros da sua equipe.
- **Visão Geral da Agenda**: Consulta a agenda completa de todos os barbeiros da sua loja.

---

## 🛠️ Tecnologias Utilizadas

| Categoria | Tecnologia | Descrição |
|-----------|------------|-------------|
| **Linguagem & Framework** | Java 17 | Linguagem principal do backend. |
| | Spring Boot 3.3 | Framework para criação da aplicação e gerenciamento de dependências. |
| **Persistência de Dados** | MySQL | Banco de dados relacional para armazenar todos os dados da aplicação. |
| | Spring Data JPA / Hibernate | Para o mapeamento objeto-relacional (ORM) e abstração do acesso aos dados. |
| **Segurança** | Spring Security | Para controle de autenticação e autorização. |
| | JWT (JSON Web Token) | Geração e validação de tokens para proteger os endpoints da API. |
| **Upload de Arquivos** | Cloudinary | Serviço de nuvem para armazenamento e gerenciamento de imagens (fotos de perfil, logos, etc.). |
| **Documentação da API**| SpringDoc (Swagger UI) | Geração automática de documentação interativa para os endpoints da API. |
| **Validação** | Spring Validation | Utilizado para validar os dados de entrada (DTOs), incluindo validações customizadas para CPF e CNPJ. |
| **Mapeamento de Objetos** | MapStruct | Gera implementações de mappers para converter Entidades em DTOs e vice-versa. |
| **Utilitários** | Lombok | Reduz código boilerplate (getters, setters, construtores) nas classes de modelo e DTOs. |
| | spring-dotenv | Carrega variáveis de ambiente a partir de um arquivo `.env` para facilitar a configuração. |
| **Build & Dependências**| Maven | Ferramenta para gerenciamento de dependências e build do projeto. |
| **Servidor & Deploy** | AWS Lambda & API Gateway | Configurado para deploy *serverless* na nuvem da AWS. |

---

## 🏗️ Estrutura do Projeto

O backend segue uma arquitetura em camadas para garantir a separação de responsabilidades e a manutenibilidade:

- `src/main/java/ifsp/edu/projeto/cortaai`
    - **`/config`**: Classes de configuração do Spring, como Segurança (SecurityConfig, JWT), CORS (WebConfig) e Swagger.
    - **`/controller`**: Contém os `RestController`s, que definem os endpoints da API, recebem as requisições HTTP e retornam as respostas.
    - **`/dto`**: (Data Transfer Object) Classes que definem a estrutura dos dados que são enviados e recebidos pela API.
    - **`/model`**: Contém as entidades JPA (`@Entity`), que representam as tabelas do banco de dados.
    - **`/repository`**: Interfaces que estendem `JpaRepository`, responsáveis pela abstração do acesso ao banco de dados.
    - **`/service`**: Onde reside a lógica de negócio da aplicação. As classes de serviço orquestram as operações, chamando os repositórios e validando as regras.
    - **`/mapper`**: Interfaces do MapStruct para mapear `Model`s para `DTO`s.
    - **`/validator`**: Validadores customizados (ex: CPF, CNPJ, e-mails únicos).
    - **`/exception`**: Classes de exceções customizadas.

---

## 🚀 Como Executar o Projeto Localmente

### 1. Pré-requisitos
- **Java 17** (ou superior)
- **Maven**
- **MySQL** (um servidor de banco de dados rodando localmente ou na nuvem)
- **Cloudinary** (uma conta para obter as credenciais de upload de imagem)

### 2. Clone o Repositório
```bash
git clone [https://github.com/seu-usuario/arquitetura_completa.git](https://github.com/seu-usuario/arquitetura_completa.git)
cd arquitetura_completa
```

### 3. Configure as Variáveis de Ambiente
1. Na raiz do projeto (`/`), localize o arquivo `.env`.
2. Preencha as variáveis com suas credenciais:

```properties
# Conexão com o banco de dados MySQL
JDBC_DATABASE_URL=jdbc:mysql://localhost:3306/seu_banco_de_dados
JDBC_DATABASE_USERNAME=seu_usuario
JDBC_DATABASE_PASSWORD=sua_senha

# Credenciais do Cloudinary
CLOUDINARY_URL=cloudinary://<API_KEY>:<API_SECRET>@<CLOUD_NAME>

# Chave secreta para assinar os tokens JWT
JWT_SECRET_KEY=gere_uma_chave_secreta_longa_e_segura_aqui
```
* **Importante**: O banco de dados (`seu_banco_de_dados`) deve ser criado manualmente no MySQL antes de iniciar a aplicação. As tabelas serão gerenciadas pelo Hibernate.

### 4. Execute a Aplicação
Você pode rodar a aplicação diretamente pela sua IDE (IntelliJ, Eclipse) ou via linha de comando com o Maven:

```bash
mvn spring-boot:run
```

O servidor será iniciado, por padrão, na porta `8080`.

### 5. Acesse a Documentação da API
Com o servidor rodando, acesse a documentação interativa do Swagger UI para ver e testar todos os endpoints disponíveis:

[http://localhost:8080/](http://localhost:8080/)
