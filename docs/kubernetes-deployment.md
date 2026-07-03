# Kubernetes deployment & the migration job

On the BIT container platform (OpenShift/Kubernetes) the database schema is migrated by a dedicated
job that runs before the application pods start. The same application image is reused: it runs once as
the migration job (init container) and then as the long-running application. This page summarises the
deployment wiring relevant to this starter; database ordering, cluster binding and pgAdmin are covered
by the BIT Container Platform team's documentation.

## Why a separate migration job

The migration job and the application use **different database accounts**:

- The application access account has read/write rights on the tables.
- The migration account has **Full** rights, including the right to change DDL structures.

Running migrations in a separate job with the privileged account keeps DDL rights out of the running
application and ensures the schema is ready before any application pod serves traffic.

## Datasource credentials

The Spring datasource properties usually do not need to be set in the application. On the platform the
credentials are injected by a Vault agent as a separate properties file mounted into the image (e.g.
`/vault/secrets/database-credentials.yml`) and added to the Spring config location at startup. For
services connecting to more than one database see
[Multiple database connections](#multiple-database-connections).

## Helm values for the migration job

In the application's GitOps `values.yaml`, enable the migration job and point it at the migration
service account:

```yaml
deployment:
  database:
    bindings:
      - name: person
        secretPath: creds/static/.../bit-jme-d/<app>-runtime-sa
        secretFormat: springboot
  migrationJob:
    hasMigrationJob: true
    dbUserSecretPath: creds/static/.../bit-jme-d/<app>-migration-sa
    serviceaccount: <app>-migration-sa
    dbUserSecretFormat: springboot
```

To connect to more than one database, add one entry per database to the `bindings` list — the binding
names determine the generated credentials files (see
[Multiple database connections](#multiple-database-connections)).

## Application configuration

The application then sets:

```yaml
spring:
  flyway:
    locations: classpath:db/migration
    default-schema: data

database-migration:
  startup-migrate-mode-enabled: false
  init-container: ${IS_INIT_CONTAINER_EXECUTION}
```

`IS_INIT_CONTAINER_EXECUTION` is set automatically by the Helm deployment: it is `true` for the
migration job pod and `false` for the application pods. As a result the same image:

1. Runs as the migration job ([init-container mode](migration-modes.md)): applies Flyway migrations,
   then the pod exits (0 on success, 1 on failure).
2. Runs as the application (application-container mode): verifies no migration is pending and only then
   starts; otherwise it shuts down with exit code 1.

## Multiple database connections

For each database binding declared under `deployment.database.bindings` in the GitOps `values.yaml`,
the Vault agent injector generates a separate credentials file named after the binding, e.g.
`/vault/secrets/database-credentials-animal.yml` and `/vault/secrets/database-credentials-car.yml`
for bindings named `animal` and `car`. Each file is a regular Spring Boot properties file holding all
necessary connection information for that database, placed under a `spring.datasource.<binding-name>`
section. As with a single database, these datasource properties do not need to be set in the
application itself:

```yaml
spring:
  datasource:
#   Note for RHOS: Database connection info is passed from the Vault.
#   /vault/secrets/database-credentials-animal.yml and /vault/secrets/database-credentials-car.yml
#   are generated with this information by the agent injector:
#    animal:
#        type: com.zaxxer.hikari.HikariDataSource
#        driver-class-name: org.postgresql.Driver
#        url: jdbc:postgresql://${POSTGRESQL_HOST}:5432/${POSTGRESQL_DATABASE_FIRST}
#        username: "${POSTGRESQL_USER_FIRST}"
#        password: "${POSTGRESQL_PASSWORD_FIRST}"
#    car:
#        type: com.zaxxer.hikari.HikariDataSource
#        driver-class-name: org.postgresql.Driver
#        url: jdbc:postgresql://${POSTGRESQL_HOST}:5432/${POSTGRESQL_DATABASE_SECOND}
#        username: "${POSTGRESQL_USER_SECOND}"
#        password: "${POSTGRESQL_PASSWORD_SECOND}"
```

Because Spring Boot only auto-configures a single primary datasource, applications requiring
connections to multiple databases need manual configuration. This includes:

- Defining a `DataSource` bean per database, bound to the corresponding
  `spring.datasource.<binding-name>` properties (e.g. `AnimalDataSourceConfig.java` and
  `CarDataSourceConfig.java`)
- Creating a separate `EntityManagerFactory` and `TransactionManager` for each database (e.g.
  `AnimalJpaConfig.java` and `CarJpaConfig.java`)
- Configuring the Flyway instances manually for each schema, if needed (e.g. `FlywayConfig.java`)

A full example is available in the `jme-rhos-db-example` service; more details can be found on the
*RHOS PostgreSQL* page in the jEAP Confluence space.

## Related

- [Migration modes](migration-modes.md)
- [Configuration reference](configuration.md)
- [Getting started](getting-started.md)
- [jeap-spring-boot-db-migration-starter](../README.md)
