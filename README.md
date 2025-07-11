# 📽️ Sistema de Gerenciamento de Locadora de Filmes

Este projeto tem como objetivo gerenciar os **aluguéis de uma locadora de filmes**, oferecendo controle sobre os filmes alugados, os clientes responsáveis pelos empréstimos e suas respectivas datas de devolução.

⚠️ **Atenção:** Este sistema **não** contempla funcionalidades relacionadas à **gestão financeira** da locadora nem ao **controle de funcionários**.

## ✅ Funcionalidades

- Cadastro e controle de aluguéis de filmes;
- Consulta de histórico de aluguéis por cliente;
- Relatórios que auxiliam o atendente da locadora:
  - 📌 **Filmes com devolução em atraso**, para facilitar o contato com os clientes;
  - 🎬 **Gêneros mais alugados** e seus respectivos filmes;
  - 🧾 **Histórico de aluguel por usuário**;
  - 📊 **Relatório de performance por gênero**, com base na receita gerada;
  - 👤 **Relatório de atividades dos clientes**, mostrando:
    - Receita total gerada por cliente;
    - Quantidade de filmes alugados;
    - Segmentação do cliente (Basic, Premium, Platinum, Gold), baseada nos gastos acumulados.

## 🧪 Tecnologias Utilizadas

- Java
- MongoDB (NoSQL)
- Execução via console (sem interface gráfica)

## 🚀 Como Executar o Projeto

1. **Inicializar o MongoDB** com as coleções disponíveis no arquivo `banco_nosql` incluído no projeto.
2. **Configurar a conexão com o banco**:
   - Atualize a string de conexão na classe `MongoConnection` com suas credenciais;
   - OU crie um banco MongoDB com as seguintes credenciais já configuradas:
     - **Usuário:** `admin`
     - **Senha:** `password`
3. **Executar a classe `Main`** para iniciar o sistema via console.

## ⚙️ Observações Importantes

- O sistema **não possui interface gráfica**: a interação é feita diretamente via console.
- A fase 2 do projeto introduziu dois novos relatórios para aumentar o escopo do projeto conforme solicitado pela professora na correção da fase 1 do projeto. Os novos relatórios são:
  - `Genre Performance Report`
  - `Customer Activity Report`
- Além disso, também foi ajustado o diagrama de entidade relacionamento da fase 1 conforme solicitado.:
  
 <img width="1081" height="721" alt="image" src="https://github.com/user-attachments/assets/c8d091fc-9962-4d75-9bf7-c09d9496c793" />

