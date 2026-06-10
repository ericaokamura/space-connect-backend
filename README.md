# OrbitCycle — Backend

Resumo curto
- Backend para cálculo e exposição de dados orbitais (propagação, elementos orbitais, visibilidades) desenvolvido em Java 17, Spring Boot e Orekit.

Propósito da aplicação
- Fornecer serviços REST para gerar, propagar e consultar estados orbitais de satélites e objetos em órbita, usados por frontends e pipelines de análise.

Como o Orekit é utilizado
- Inicialização dos dados astronômicos (ephemeris, frames, Earth orientation).
- Conversão entre representações (TLE ↔ elementos keplerianos ↔ estado Cartesian).
- Propagação de órbitas com propagadores (numerical, analytical, SGP4 quando TLE).
- Cálculo de eventos: passes, visibilidade, elevação/azimute, tempo de acesso.
- Gerenciamento de tempos com TimeScales (UTC, TAI).

Dependências principais
- Java 17
- Spring Boot (web, data)
- Orekit (biblioteca orbital)
- Maven (build)
- Lombok, PostgreSQL

Configurar o ambiente
1. Instalar Java 17 (JDK).
2. Instalar Maven 3.6+.
3. Baixar e disponibilizar os dados do Orekit (orekit-data):
    - Colocar a pasta `orekit-data` em um local acessível.
    - Colocar `orekit-data` em `src/main/resources` e carregá-lo via DirectoryCrawler.

Executar via Maven
- Compilar:
  mvn clean package
- Executar diretamente:
  mvn spring-boot:run

Executar via IDE
- Importar como projeto Maven.
- Garantir JDK 17 no projeto.
- Executar a classe principal anotada com @SpringBootApplication (por ex. OrbitCycleApplication).

Ingestão de dados TLE
- Endpoint REST para ingestão de TLEs.
- Processamento e armazenamento em banco de dados PostgreSQL.
- Validação de TLEs e conversão para coordenadas cartesianas.
- Endpoint para ingestão de dados TLE:
POST /space-connect/process


