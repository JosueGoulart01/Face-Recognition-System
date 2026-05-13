# 🎭 FaceRecognitionSystem
 
**Reconhecimento facial em tempo real com Java 21, Spring Boot 3, OpenCV 4.9 e Java Swing**
 
[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![OpenCV](https://img.shields.io/badge/OpenCV-4.9.0-5C3EE8?style=for-the-badge&logo=opencv&logoColor=white)](https://opencv.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-latest-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![License: MIT](https://img.shields.io/badge/Licença-MIT-yellow?style=for-the-badge)](LICENSE)
 
<br/>
> Aplicação desktop que detecta e reconhece rostos humanos em tempo real via webcam, com interface gráfica Swing, cadastro guiado por poses e retreinamento automático do modelo.
 
</div>
---
 
## 📋 Índice
 
- [Visão Geral](#-visão-geral)
- [Status do Projeto](#-status-do-projeto)
- [Arquitetura](#-arquitetura)
- [Stack Tecnológica](#-stack-tecnológica)
- [Funcionalidades](#-funcionalidades)
- [Fluxo do Sistema](#-fluxo-do-sistema)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Pré-requisitos](#-pré-requisitos)
- [Como Executar](#-como-executar)
- [Configuração](#-configuração)
- [Como Usar](#-como-usar)
- [Interface Gráfica](#-interface-gráfica)
- [Banco de Dados](#-banco-de-dados)
- [Serviços](#-serviços)
- [Otimizações de Performance](#-otimizações-de-performance)
- [Limitações Conhecidas](#-limitações-conhecidas)
- [Roadmap](#-roadmap)
- [Contribuindo](#-contribuindo)
- [Licença](#-licença)
---
 
## 🧠 Visão Geral
 
O **FaceRecognitionSystem** é uma aplicação desktop que realiza detecção e reconhecimento facial em tempo real através de uma webcam convencional. A aplicação apresenta uma interface gráfica completa em **Java Swing** com preview ao vivo da câmera, controles interativos, indicadores de status e um fluxo guiado de cadastro facial por múltiplas poses.
 
Internamente, o sistema utiliza o classificador **Haar Cascade** do OpenCV para detecção e o algoritmo **LBPH** (Local Binary Patterns Histograms) para reconhecimento. A aplicação é construída sobre Spring Boot, adotando uma Arquitetura Orientada a Serviços com injeção de dependências, persistência JPA e pipeline assíncrono via `ExecutorService` para manter a interface sempre responsiva.
 
O processamento ocorre **100% localmente** — nenhum dado facial é enviado a servidores externos.
 
---
 
## 🚦 Status do Projeto
 
| Componente | Status | Descrição |
|------------|--------|-----------|
| Interface Desktop (Swing) | ✅ Funcionando | GUI completa com preview, botões e labels de status |
| Captura de Câmera | ✅ Funcionando | Feed em tempo real via webcam |
| Detecção Facial (Haar Cascade) | ✅ Funcionando | Detecção com bounding boxes na imagem |
| Reconhecimento Facial (LBPH) | ✅ Funcionando | Identificação com score de confiança |
| Cadastro Guiado por Poses | ✅ Funcionando | Front, Esquerda e Direita — 3 imagens cada |
| Retreinamento em Runtime | ✅ Funcionando | Modelo atualizado automaticamente após cadastro |
| Persistência PostgreSQL | ✅ Funcionando | Pessoas e labels persistidos em banco |
| TensorFlow / FaceNet | 🔬 Planejado | Embeddings profundos para versão futura |
 
---
 
## 🏗️ Arquitetura
 
<img width="1440" height="1240" alt="image" src="https://github.com/user-attachments/assets/47819498-fd03-4c14-b559-7a3e0cb8028d" />

### Padrões de Projeto Utilizados
 
| Padrão | Aplicação |
|--------|-----------|
| **Service Layer** | Lógica de negócio isolada em serviços Spring |
| **Repository Pattern** | Acesso a dados via Spring Data JPA |
| **Dependency Injection** | Gerenciado pelo IoC Container do Spring |
 
---
 
## 💻 Stack Tecnológica
 
| Camada | Tecnologia | Versão |
|--------|-----------|--------|
| Linguagem | Java | 21 |
| Framework | Spring Boot | 3.3.5 |
| Visão Computacional | OpenCV | 4.9.0 |
| Bindings CV | JavaCV / JavaCPP Presets | 1.5.10 |
| Interface Gráfica | Java Swing / AWT | JDK nativo |
| Banco de Dados | PostgreSQL | latest |
| ORM | Spring Data JPA / Hibernate | — |
| Utilitários | Lombok | — |
| Concorrência | ExecutorService | JDK nativo |
 
---
 
## ✨ Funcionalidades
 
| Funcionalidade | Descrição |
|---------------|-----------|
| 🖥️ **Interface Gráfica Swing** | Janela completa com feed da câmera, labels de status e botões de ação |
| 📷 **Detecção em Tempo Real** | Rostos detectados quadro a quadro via classificador Haar Cascade |
| 🔍 **Reconhecimento LBPH** | Identificação da pessoa com score de confiança (limiar: 70) |
| 🧭 **Cadastro Guiado** | Captura estruturada em 3 poses (Frente, Esquerda, Direita) × 3 imagens = 9 total |
| ⚡ **Retreinamento Automático** | Modelo LBPH atualizado imediatamente após novo cadastro |
| 🗂️ **Gerenciamento de Dataset** | Imagens organizadas em `data/faces/{label}/` por pessoa |
| 🗄️ **Persistência** | Registro de pessoas e mapeamento de labels no PostgreSQL |
| 🧵 **Processamento Assíncrono** | Pipeline em thread dedicada; UI atualizada via `SwingUtilities.invokeLater` |
| 🎨 **Renderização de Overlay** | Bounding boxes, nomes e scores de confiança sobrepostos ao feed |
| 🚀 **Intervalo de Detecção** | Haar Cascade executa a cada N frames para maximizar o FPS |
 
---
 
## 🔄 Fluxo do Sistema
 
### Pipeline Principal
 
```
Inicialização do Spring Boot
          │
          ▼
Carregamento das Libs Nativas do OpenCV
          │
          ▼
Inicialização da Interface Swing
          │
          ▼
Abertura da Webcam (640×480)
          │
          ▼
┌──── Loop de Captura (Thread em Background) ──────────────────┐
│                                                              │
│  Frame ──▶ Conversão para Escala de Cinza                    │
│                     │                                        │
│                     ▼                                        │
│          Detecção Haar Cascade (a cada N frames)             │
│                     │                                        │
│            ┌────────▼──────────┐                             │
│            │  Rostos Encontrados? │                          │
│            └──┬──────────────┬──┘                            │
│              SIM             NÃO                             │
│               │               └──▶ (próximo frame)           │
│               ▼                                              │
│   Redimensionar (200×200) + Equalização de Histograma        │
│               │                                              │
│               ▼                                              │
│   LBPH Predict → confiança < 70 → Pessoa Conhecida           │
│               │                                              │
│               ▼                                              │
│   Renderizar Bounding Box + Nome + Confiança                 │
│               │                                              │
│               ▼                                              │
│   SwingUtilities.invokeLater → Atualizar Labels da UI        │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```
 
### Fluxo de Cadastro
 
```
Clique em "Cadastrar"
          │
          ▼
Prompt: Informe o nome da pessoa
          │
          ▼
Captura Guiada por Pose:
  ┌──────────────────────────────────────┐
  │  Pose 1: Frente   → 3 imagens        │
  │  Pose 2: Esquerda → 3 imagens        │
  │  Pose 3: Direita  → 3 imagens        │
  └──────────────────────────────────────┘
          │
          ▼
Salvar imagens em data/faces/{label}/
          │
          ▼
Persistir registro no PostgreSQL
          │
          ▼
Retreinar modelo LBPH automaticamente
          │
          ▼
Sistema pronto para reconhecer a nova pessoa
```
 
---
 
## 📁 Estrutura do Projeto
 
```
FaceRecognitionSystem/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/facerecognition/
│       │       ├── FaceRecognitionApplication.java   # Entrypoint Spring Boot
│       │       ├── config/                           # Configurações Spring
│       │       ├── model/
│       │       │   └── Person.java                   # Entidade JPA
│       │       ├── repository/
│       │       │   └── PersonRepository.java         # Spring Data JPA
│       │       ├── service/
│       │       │   ├── CameraService.java            # Gerenciamento da webcam
│       │       │   ├── FaceDetectionService.java     # Detecção Haar Cascade
│       │       │   ├── FaceRecognitionService.java   # Pipeline principal
│       │       │   └── TensorFlowService.java        # Mock — uso futuro
│       │       └── ui/
│       │           └── MainWindow.java               # Janela Swing principal
│       └── resources/
│           ├── application.properties
│           └── haarcascade_frontalface_default.xml   # Classificador pré-treinado
│
├── data/
│   └── faces/
│       └── {label}/                                  # Uma pasta por pessoa
│           ├── frente_1.png
│           ├── frente_2.png
│           ├── frente_3.png
│           ├── esquerda_1.png
│           ├── esquerda_2.png
│           ├── esquerda_3.png
│           ├── direita_1.png
│           ├── direita_2.png
│           └── direita_3.png
│
├── pom.xml
└── README.md
```
 
---
 
## 📦 Pré-requisitos
 
Antes de executar o projeto, certifique-se de ter instalado:
 
- **Java 21** (JDK) — [Download](https://openjdk.org/projects/jdk/21/)
- **Maven 3.8+** — [Download](https://maven.apache.org/download.cgi)
- **PostgreSQL 14+** — local ou via Docker
- **Webcam** funcional conectada à máquina
- **Ambiente gráfico** disponível (necessário para renderizar a janela Swing)
---
 
## 🚀 Como Executar
 
### 1. Clone o repositório
 
```bash
git clone https://github.com/seu-usuario/FaceRecognitionSystem.git
cd FaceRecognitionSystem
```
 
### 2. Crie o banco de dados
 
```sql
CREATE DATABASE face_recognition;
```
 
> O Hibernate cria as tabelas automaticamente na primeira execução via `ddl-auto=update`.
 
### 3. Configure as credenciais
 
Edite o arquivo `src/main/resources/application.properties`:
 
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/face_recognition
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
```
 
### 4. Compile e execute
 
```bash
mvn clean install
mvn spring-boot:run
```
 
A janela gráfica abrirá automaticamente após a inicialização.
 
---
 
### 🐳 Executando o banco via Docker
 
Caso não tenha PostgreSQL instalado, suba uma instância com Docker:
 
```bash
docker run --name face-db \
  -e POSTGRES_DB=face_recognition \
  -e POSTGRES_USER=seu_usuario \
  -e POSTGRES_PASSWORD=sua_senha \
  -p 5432:5432 \
  -d postgres:16
```
 
---
 
### ⚠️ Observações por Sistema Operacional
 
| SO | Observação |
|----|------------|
| **Linux** | Certifique-se de ter permissão de leitura em `/dev/video0`. Caso esteja via SSH com X forwarding, defina `DISPLAY=:0`. |
| **macOS** | Conceda permissão de câmera ao Terminal ou IDE em *Preferências do Sistema → Privacidade e Segurança → Câmera*. |
| **Windows** | Execute normalmente. Pode ser necessário permitir acesso à câmera nas configurações de privacidade do Windows. |
 
---
 
## ⚙️ Configuração
 
| Propriedade | Padrão | Descrição |
|-------------|--------|-----------|
| Largura da câmera | `640` | Largura do frame capturado (pixels) |
| Altura da câmera | `480` | Altura do frame capturado (pixels) |
| Tamanho da face normalizada | `200×200` | Dimensão usada antes do treinamento |
| Poses por cadastro | `3` | Frente, Esquerda, Direita |
| Imagens por pose | `3` | Frames capturados por pose |
| Total de imagens por pessoa | `9` | 3 poses × 3 imagens |
| Diretório do dataset | `data/faces/` | Raiz das imagens faciais |
| Limiar de confiança LBPH | `70` | Valor máximo para aceitar reconhecimento |
| Intervalo de detecção | a cada N frames | Configurável; reduz uso de CPU |
| Arquivo Haar Cascade | `haarcascade_frontalface_default.xml` | Classificador pré-treinado do OpenCV |
 
---
 
## 🎮 Como Usar
 
Após a inicialização, a janela **Reconhecimento Facial** abrirá com o feed da câmera ativo.
 
### Botões de Ação
 
| Botão | Ação |
|-------|------|
| **Cadastrar** | Inicia o fluxo de cadastro — solicita o nome e guia a captura por poses |
| **Capturar** | Aciona manualmente um passo de captura durante o cadastro guiado |
| **Sair** | Libera a câmera e encerra a aplicação de forma limpa |
 
### Fluxo de Cadastro Passo a Passo
 
1. Clique em **Cadastrar** e informe o nome da pessoa.
2. Posicione o rosto **de frente** para a câmera e clique em **Capturar** (3×).
3. Vire levemente para a **esquerda** e clique em **Capturar** (3×).
4. Vire levemente para a **direita** e clique em **Capturar** (3×).
5. O sistema salva as imagens, persiste o registro no banco e **retreina o modelo automaticamente**.
### Resultado do Reconhecimento
 
| Overlay | Significado |
|---------|-------------|
| 🟩 Retângulo verde + nome + confiança | Pessoa reconhecida — confiança abaixo do limiar (< 70) |
| 🟥 Retângulo vermelho + "Desconhecido" | Rosto detectado mas não reconhecido — confiança ≥ 70 |
 
> **Sobre o score de confiança:** quanto **menor** o valor, **maior** a similaridade com o rosto treinado. Um score de 0 indica correspondência perfeita.
 
---
 
## 🖥️ Interface Gráfica
 
### Layout da Janela
 
```
┌────────────────────────────────────────────────┐
│            Reconhecimento Facial               │
├────────────────────────────────────────────────┤
│                                                │
│          ┌──────────────────────┐              │
│          │                      │              │
│          │     Feed da Câmera   │              │
│          │      [640 × 480]     │              │
│          │                      │              │
│          └──────────────────────┘              │
│                                                │
│  Status:     [ Pronto ]                        │
│  Detectado:  [ João Silva  (conf: 23.4) ]      │
│  Rostos:     [ 1 detectado ]                   │
│                                                │
│  [ Cadastrar ]   [ Capturar ]   [ Sair ]       │
└────────────────────────────────────────────────┘
```
 
### Componentes Swing
 
| Componente | Tipo | Finalidade |
|------------|------|------------|
| `cameraLabel` | `JLabel` | Exibe o feed da webcam em tempo real |
| `statusLabel` | `JLabel` | Indica o estado atual do sistema |
| `detectedLabel` | `JLabel` | Exibe o nome da última pessoa reconhecida |
| `facesCountLabel` | `JLabel` | Exibe a quantidade de rostos no frame atual |
| `registerButton` | `JButton` | Inicia o fluxo de cadastro |
| `captureButton` | `JButton` | Aciona captura manual durante o cadastro |
| `exitButton` | `JButton` | Encerra a aplicação |
 
---
 
## 🗄️ Banco de Dados
 
### Esquema
 
```sql
CREATE TABLE persons (
    id     BIGSERIAL    PRIMARY KEY,
    name   VARCHAR(255) NOT NULL,
    label  INTEGER      NOT NULL UNIQUE
);
```
 
### Relacionamento com o Dataset
 
Cada registro na tabela `persons` possui um `label` inteiro que corresponde diretamente a uma pasta em disco:
 
```
persons.label = 1  →  data/faces/1/  (imagens da pessoa 1)
persons.label = 2  →  data/faces/2/  (imagens da pessoa 2)
```
 
Essa convenção garante que o modelo LBPH, ao prever um `label`, consiga recuperar o nome da pessoa diretamente do banco de dados.
 
---
 
## 🔧 Serviços
 
### `CameraService`
Gerencia o ciclo de vida do `OpenCVFrameGrabber` — abertura do dispositivo de câmera, captura de frames sob demanda e liberação de recursos ao encerrar. Isola toda a comunicação com o hardware de captura.
 
### `FaceDetectionService`
Carrega o classificador Haar Cascade a partir do classpath e executa a detecção de rostos nos frames em escala de cinza. Aplica o pipeline de pré-processamento antes de entregar as ROIs para o reconhecimento:
 
```
Frame colorido
    └──▶ Conversão para escala de cinza
              └──▶ Detecção Haar Cascade
                        └──▶ Redimensionamento para 200×200
                                  └──▶ Equalização de histograma
                                            └──▶ ROI pronta para LBPH
```
 
### `FaceRecognitionService`
Orquestrador central do sistema. Responsabilidades:
- Treinamento e atualização do modelo LBPH a partir do dataset em disco
- Predição quadro a quadro com aplicação do limiar de confiança
- Condução do fluxo de cadastro guiado (coordenação de captura + persistência de dataset)
- Escrita de novos registros no PostgreSQL e disparo do retreinamento
- Renderização dos overlays sobre os frames
- Atualização thread-safe da interface Swing via `SwingUtilities.invokeLater`
### `TensorFlowService` *(mock)*
Placeholder para o pipeline de aprendizado profundo planejado para versões futuras. Atualmente retorna arrays de floats aleatórios sem inferência real. Destinado à integração com TensorFlow Java ou ONNX Runtime (FaceNet / ArcFace).
 
---
 
## ⚡ Otimizações de Performance
 
| Otimização | Descrição | Benefício |
|------------|-----------|-----------|
| **Intervalo de Detecção** | O Haar Cascade executa apenas a cada N frames, não em cada frame | Redução significativa do uso de CPU sem prejudicar o feed visual |
| **Atualização Assíncrona da UI** | Todos os componentes Swing são atualizados via `SwingUtilities.invokeLater` | Garante que a Event Dispatch Thread nunca seja bloqueada |
| **Reutilização de Objetos** | Objetos `Mat` e frames são reutilizados entre iterações do loop | Redução da pressão no Garbage Collector |
 
---
 
## ⚠️ Limitações Conhecidas
 
- A precisão do LBPH degrada em ambientes com iluminação baixa ou irregular.
- Rotações faciais fora do plano (visão de perfil) podem gerar falsos negativos.
- O `TensorFlowService` não realiza inferência real — os embeddings são placeholders aleatórios.
- O cadastro depende de interação manual; não há detecção automática de borrão ou pontuação de qualidade.
- O sistema não utiliza embeddings faciais modernos (FaceNet, ArcFace, etc.).
- Não há suporte a múltiplas câmeras simultâneas.
---
 
## 🗺️ Roadmap
 
### Curto Prazo
- [ ] Detecção de qualidade de imagem (borrão, iluminação) durante o cadastro
- [ ] Aumento automático do dataset (variações de brilho, rotação, espelhamento)
- [ ] Configuração externalizada via `application.properties` para todos os parâmetros
### Médio Prazo
- [ ] Substituir LBPH por embeddings FaceNet / ArcFace via TensorFlow Java ou ONNX Runtime
- [ ] Persistir embeddings faciais como colunas `pgvector` para busca por similaridade de cosseno
- [ ] Pipeline assíncrono por estágio (threads separadas para captura → detecção → reconhecimento → renderização)
- [ ] Suporte a múltiplas câmeras simultâneas
### Longo Prazo
- [ ] Expor API REST para reconhecimento remoto e integração com frontend React
- [ ] Dashboard web administrativo para gerenciar pessoas e visualizar logs
- [ ] Detecção de anti-spoofing / liveness (evitar fraude com fotos)
- [ ] Reconhecimento facial distribuído
- [ ] Empacotamento como instalador nativo (`.exe`, `.dmg`, `.deb`) via jpackage
---
 
## 🤝 Contribuindo
 
Contribuições são bem-vindas! Siga o processo abaixo:
 
1. Faça um **fork** do repositório
2. Crie sua branch de feature:
   ```bash
   git checkout -b feature/minha-feature
   ```
3. Faça seus commits seguindo o padrão [Conventional Commits](https://www.conventionalcommits.org/pt-br/):
   ```bash
   git commit -m 'feat: adiciona detecção de qualidade de imagem'
   ```
4. Envie para sua branch:
   ```bash
   git push origin feature/minha-feature
   ```
5. Abra um **Pull Request** descrevendo o que foi alterado e por quê.
### Tipos de Commit
 
| Tipo | Uso |
|------|-----|
| `feat` | Nova funcionalidade |
| `fix` | Correção de bug |
| `refactor` | Refatoração sem mudança de comportamento |
| `perf` | Melhoria de performance |
| `docs` | Alteração em documentação |
| `test` | Adição ou ajuste de testes |
| `chore` | Tarefas de manutenção (build, deps) |
 
---
