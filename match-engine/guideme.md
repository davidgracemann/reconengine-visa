## Internal Structure (`src/main/java/com/gracemann/recon/matchengine/`)\*\*

```
match-engine/
├── src/
│   ├── main/
│   │   ├── java/com/gracemann/recon/matchengine/
│   │   │   ├── config/               # Spring, Kafka, DB, app configs
│   │   │   ├── controller/           # REST controllers (if any)
│   │   │   ├── service/              # Business logic, matching engines
│   │   │   ├── repository/           # Spring Data JPA/CRUD repositories
│   │   │   ├── domain/               # Entities (JPA models) + enums
│   │   │   ├── dto/                  # DTOs for messaging/batch/api
│   │   │   ├── matcher/              # Matching logic/strategies
│   │   │   ├── kafka/                # Kafka consumers/producers/config
│   │   │   ├── exception/            # Custom exceptions, handlers
│   │   │   └── util/                 # Utilities/helpers, metrics
│   │   └── resources/
│   │       ├── db/migration/         # Flyway SQL migration scripts
│   │       ├── application.yml       # Config file (switch from .properties)
│   │       └── ...                   # Static/templates if needed
│   └── test/
│       └── java/com/gracemann/recon/matchengine/
│           ├── ...                   # Unit/integration tests mirroring main
└── pom.xml
```

---

## **What Goes Where**

-   **config/**: Kafka, DataSource, app-level config classes
-   **controller/**: Expose REST APIs for status/health/batch management (if you want)
-   **service/**: Main match orchestration, batch job logic, Kafka handler glue code
-   **repository/**: JPA repositories, custom query classes
-   **domain/**: All entities (JPA), enums (`MatchStatus`, etc)
-   **dto/**: Data Transfer Objects for Kafka, API, batch, etc
-   **matcher/**: Your matching/fuzzy/pattern logic (can split by tier)
-   **kafka/**: Consumers, producers, message handler beans/configs
-   **exception/**: Custom exception classes, Spring advice/handler
-   **util/**: Metrics, string/date helpers, Levenshtein, merchant normalization, etc

### kafka topics

```
windows\kafka-topics.bat --list --bootstrap-server localhost:9092

windows\kafka-topics.bat --bootstrap-server localhost:9092 --create --topic match-results --partitions 3 --replication-factor 1

windows\kafka-topics.bat --bootstrap-server localhost:9092 --create --topic match-exceptions --partitions 3 --replication-factor 1

windows\kafka-topics.bat --bootstrap-server localhost:9092 --create --topic match-metrics --partitions 1 --replication-factor 1

```
