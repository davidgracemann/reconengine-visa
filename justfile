# ──────────────────────────────────────────────────────────────────────────────
# Recon Engine — Developer Justfile
#
#   Shortcuts for multi-module Spring Boot development.
#   Run 'just --list' to see all commands.
#   Add more as your workflow evolves.
# ──────────────────────────────────────────────────────────────────────────────

# ── Project Build Commands ──────────────────────────────────────────────

# Build the entire project (clean first, skip tests for speed)
build:
  mvn clean install -DskipTests

# Build only ingestion-service (skip tests)
buildi:
  mvn clean install -pl ingestion-service -DskipTests

# Build only match-engine (skip tests)
buildm:
  mvn clean install -pl match-engine -DskipTests

# Build only report-service (skip tests)
buildr:
  mvn clean install -pl report-service -DskipTests

# Build only exception-api (skip tests)
builde:
  mvn clean install -pl exception-api -DskipTests

# Build only scheduler (skip tests)
builds:
  mvn clean install -pl scheduler -DskipTests

# Build only monitor-metrics (skip tests)
buildmm:
  mvn clean install -pl monitor-metrics -DskipTests

# ── Running Services Locally ───────────────────────────────────────────

run:
   mvn spring-boot:run -DskipTests

runi:
  mvn spring-boot:run -pl ingestion-service -DskipTests

runm:
  mvn spring-boot:run -pl match-engine -DskipTests

runr:
  mvn spring-boot:run -pl report-service

rune:
  mvn spring-boot:run -pl exception-api

runs:
  mvn spring-boot:run -pl scheduler

runmm:
  mvn spring-boot:run -pl monitor-metrics

# ── Testing Commands ───────────────────────────────────────────────────

# Run all tests in the project
test:
  mvn test

# Run only ingestion-service tests
testi:
  mvn test -pl ingestion-service

# Run only match-engine tests
testm:
  mvn test -pl match-engine

# Run only report-service tests
testr:
  mvn test -pl report-service

# Run only exception-api tests
teste:
  mvn test -pl exception-api

# Run only scheduler tests
tests:
  mvn test -pl scheduler

# Run only monitor-metrics tests
testmm:
  mvn test -pl monitor-metrics

# ── Clean Project ──────────────────────────────────────────────────────

clean:
  mvn clean

# ───────────────────────────────────────────────────────────────────────
# Add more commands below as your stack expands.
# For advanced just usage: https://github.com/casey/just
# ───────────────────────────────────────────────────────────────────────

# Developer Note:
# Extend with DB migration, Docker, or codegen tasks as you grow.
