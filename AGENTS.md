# AGENTS.md

Guidance for AI coding agents working **in this repository**. For how to *use* the starter in a
consuming service, read [README.md](README.md) and the [docs/](docs/) folder instead.

## Project

jEAP Spring Boot DB Migration Starter is a multi-module Maven library that runs Flyway database
migrations in jEAP Spring Boot services, with a PostgreSQL focus. Its core is a Spring Boot
auto-configured `FlywayMigrationStrategy` that selects one of three behaviours depending on the
runtime platform and the `database-migration.*` properties: migrate-on-startup (outside Kubernetes
or for local development), a dedicated init-container migration job, or an application-container check
that refuses to start if migrations are still pending.

## Repository layout

```
pom.xml                                        # Parent POM (packaging=pom); declares the modules below
jeap-spring-boot-db-migration-starter/         # The starter itself
  src/main/java/ch/admin/bit/jeap/starter/db/config/
    FlywayMigrationConfiguration.java          # @AutoConfiguration; registers the FlywayMigrationStrategy bean
    FlywayMigrationStrategyResolver.java        # Picks the strategy from platform + properties; handles shutdown/rethrow
    FlywayMigrationStrategyService.java         # The three concrete strategies (startup / init-container / app-container)
    DatabaseMigrationProperties.java            # @ConfigurationProperties(prefix = "database-migration")
    ShutdownService.java                        # Wraps SpringApplication.exit + System.exit
  src/main/resources/META-INF/spring/...AutoConfiguration.imports
jeap-spring-boot-db-migration-starter-it/      # Integration tests against a PostgreSQL Testcontainer
Jenkinsfile, publiccode.yml, CHANGELOG.md, LICENSE
```

The starter depends on `spring-boot-flyway`, `spring-boot-starter-data-jpa` and the `postgresql`
driver. Auto-configuration is registered via `@AutoConfiguration` and the
`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` file (both
`FlywayMigrationConfiguration` and `DatabaseMigrationProperties` are imported).

## Build & test

```bash
./mvnw verify                                  # full build incl. integration tests
./mvnw -pl jeap-spring-boot-db-migration-starter test
```

- Parent: `ch.admin.bit.jeap:jeap-internal-spring-boot-parent` (Spring Boot 4 aligned).
- Integration tests (`jeap-spring-boot-db-migration-starter-it`) start a real PostgreSQL via
  Testcontainers (`PostgresTestContainerBase`) and exercise each mode with `@TestPropertySource`,
  mocking `ShutdownService` so the JVM is not actually terminated.
- Spring Boot 3 maintenance happens on the `release/springboot3` branch; `master` targets Spring Boot 4.

## jEAP conventions

- Java packages live under `ch.admin.bit.jeap.starter.db.config`.
- Configuration properties use the prefix `database-migration.*`; standard Flyway tuning uses the
  Spring Boot `spring.flyway.*` properties.
- The platform is detected with Spring Boot's `CloudPlatform.KUBERNETES.isActive(environment)`
  (driven by `spring.main.cloud-platform`). Outside Kubernetes, startup-migrate mode is used.
- Init-container mode terminates the JVM via `ShutdownService` with the migration result as exit code
  (0 = success, 1 = failure); never call `System.exit` directly elsewhere.

## Docs

When changing public behaviour, update the matching focused file under [docs/](docs/) (one topic per
file) and the documentation index in the README.

## Versioning

- Semantic Versioning; all changes documented in [CHANGELOG.md](./CHANGELOG.md) (Keep a Changelog format).
- `setPomVersions.sh` updates the version across all module POMs.
- When working on a feature branch, increase the version to `x.y.z-SNAPSHOT` in the POMs.
- Always keep the -SNAPSHOT postfix in the POMs; CI removes it when releasing. Do not use the SNAPSHOT
  postfix elsewhere (CHANGELOG, publiccode.yml etc).
- Keep changelog entries concise; follow existing patterns.
- Keep commit messages short, use the JIRA ID from the branch name as a prefix, do not use conventional
  commits (for example: "JEAP-1234 Added feature X").
- When bumping the version, also update the changelog and the version/date in `publiccode.yml`.
