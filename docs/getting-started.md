# Getting started

This page shows how to add the jEAP Spring Boot DB Migration Starter to a service and run Flyway
migrations. For how the three runtime behaviours are chosen see [Migration modes](migration-modes.md);
for the Kubernetes job wiring see [Kubernetes deployment](kubernetes-deployment.md).

## 1. Add the dependency

```xml
<dependency>
    <groupId>ch.admin.bit.jeap</groupId>
    <artifactId>jeap-spring-boot-db-migration-starter</artifactId>
</dependency>
```

The version is managed by the jEAP Spring Boot parent. The starter pulls in `spring-boot-flyway`,
`spring-boot-starter-data-jpa` and the PostgreSQL driver, and auto-configures a custom
`FlywayMigrationStrategy`.

## 2. Add your migration scripts

Place versioned Flyway SQL scripts on the classpath under the location configured in
`spring.flyway.locations` (by convention `classpath:db/migration`):

```sql
-- src/main/resources/db/migration/V1_0_0__create-schema.sql
CREATE TABLE test
(
    id UUID PRIMARY KEY
);
```

## 3. Configure Flyway and the migration mode

The standard Flyway properties live under `spring.flyway.*`; the starter adds the
`database-migration.*` properties (see [Configuration reference](configuration.md)).

```yaml
spring:
  flyway:
    locations: classpath:db/migration
    # Flyway creates the default schema automatically if it does not exist
    default-schema: data

database-migration:
  startup-migrate-mode-enabled: false
  init-container: ${IS_INIT_CONTAINER_EXECUTION}
```

`IS_INIT_CONTAINER_EXECUTION` is set automatically during a Helm deployment so the same image runs as
both the migration init container and the application. The datasource (`spring.datasource.*`) is
configured as usual; on the BIT container platform the credentials are injected as a separate
properties file (see [Kubernetes deployment](kubernetes-deployment.md)).

## 4. Local development

Outside Kubernetes the starter automatically uses startup-migrate mode — Flyway runs on application
startup, exactly as in a plain Spring Boot service. You do not need to set any
`database-migration.*` property locally. You can also force this mode explicitly with
`database-migration.startup-migrate-mode-enabled=true`.

## Related

- [Migration modes](migration-modes.md)
- [Configuration reference](configuration.md)
- [Kubernetes deployment](kubernetes-deployment.md)
- [jeap-spring-boot-db-migration-starter](../README.md)
