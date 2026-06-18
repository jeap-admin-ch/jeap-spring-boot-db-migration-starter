# Configuration reference

The starter adds the properties under the `database-migration` prefix, bound by
`DatabaseMigrationProperties`. They sit on top of the standard Spring Boot Flyway properties
(`spring.flyway.*`) and datasource properties (`spring.datasource.*`), which the starter does not
change.

## `database-migration.*`

| Name                            | Type      | Default | Description                                                                                                                                                                                                                |
|---------------------------------|-----------|---------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `init-container`                | `boolean` | `false` | Whether this process is the migration job pod (`true`) or the application pod (`false`). When `true`, the pod runs the migration and then terminates with the migration result as exit code. When `false`, the application only verifies the migration was applied and then starts. Typically set to `${IS_INIT_CONTAINER_EXECUTION}`, which Helm sets automatically. |
| `startup-migrate-mode-enabled`  | `boolean` | `false` | Whether to run Flyway in-process on startup (classic migrate-on-startup) instead of as a separate job. Intended for local development; keep `false` for staged Kubernetes deployments. Outside Kubernetes this mode is used automatically regardless of this flag. |

Only one of these effectively applies at a time — see [Migration modes](migration-modes.md) for how
the strategy is resolved.

## Relevant Spring Boot Flyway properties

These are standard Spring Boot properties (not defined by this starter); the most relevant ones:

| Name                         | Description                                                                          |
|------------------------------|--------------------------------------------------------------------------------------|
| `spring.flyway.locations`    | Classpath/filesystem locations of the migration scripts (e.g. `classpath:db/migration`). |
| `spring.flyway.default-schema` | Default schema Flyway operates on; Flyway creates it if it does not exist.          |
| `spring.flyway.clean-disabled` | Must be `false` to allow `flyway.clean()` (e.g. for test teardown); `true` in production. |

## Platform detection

Whether the service is "on Kubernetes" is determined by Spring Boot's
`CloudPlatform.KUBERNETES.isActive(environment)`, which reads `spring.main.cloud-platform`. If the
platform is not Kubernetes, the starter logs that startup-migrate mode is enabled by default and runs
Flyway in-process.

## Related

- [Getting started](getting-started.md)
- [Migration modes](migration-modes.md)
- [Kubernetes deployment](kubernetes-deployment.md)
- [jeap-spring-boot-db-migration-starter](../README.md)
