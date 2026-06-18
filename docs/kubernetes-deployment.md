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
multiple databases, one file per binding is generated (e.g. `database-credentials-<name>.yml`) and
each datasource/Flyway instance is configured manually.

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

## Related

- [Migration modes](migration-modes.md)
- [Configuration reference](configuration.md)
- [Getting started](getting-started.md)
- [jeap-spring-boot-db-migration-starter](../README.md)
