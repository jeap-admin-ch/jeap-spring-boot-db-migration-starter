# jEAP Spring Boot DB Migration Starter

jEAP Spring Boot DB Migration Starter is a Spring Boot starter that runs [Flyway](https://flywaydb.org/)
database migrations in jEAP services with a PostgreSQL focus. On Kubernetes-based platforms it lets the
schema migration run as a dedicated job (init container) that completes before the application pods start,
so the database is ready before the service goes live. For local development it falls back to the familiar
migrate-on-startup behaviour. Adding the starter and a few properties is enough — it auto-configures a
custom Flyway migration strategy. It provides:

* A custom `FlywayMigrationStrategy` that picks the right behaviour for the current platform
* Init-container mode: migrate, then terminate the pod with the migration result as exit code
* Application-container mode: verify the migration was already applied, otherwise refuse to start
* Startup-migrate mode: classic migrate-on-startup, used automatically outside Kubernetes
* Configuration via the `database-migration.*` properties, on top of the standard `spring.flyway.*` properties

## Documentation

Start with [Getting started](docs/getting-started.md), then follow the links below.

| Topic                                                      | File                                                       |
|------------------------------------------------------------|------------------------------------------------------------|
| Getting started (add the dependency, configure)            | [docs/getting-started.md](docs/getting-started.md)         |
| Migration modes & strategy resolution                      | [docs/migration-modes.md](docs/migration-modes.md)         |
| Configuration reference (`database-migration.*`)           | [docs/configuration.md](docs/configuration.md)             |
| Kubernetes deployment & the migration job                  | [docs/kubernetes-deployment.md](docs/kubernetes-deployment.md) |

## Modules

Group id for all modules is `ch.admin.bit.jeap`; the version is managed by the jEAP Spring Boot parent.
Consumers depend on `jeap-spring-boot-db-migration-starter`.

| Module                                        | Purpose                                                                  |
|-----------------------------------------------|--------------------------------------------------------------------------|
| `jeap-spring-boot-db-migration-starter`       | The starter: Flyway auto-configuration and the platform migration strategy |
| `jeap-spring-boot-db-migration-starter-it`    | Integration tests against a PostgreSQL Testcontainer                     |

## Changes

This library is versioned using [Semantic Versioning](http://semver.org/) and all changes are documented in
[CHANGELOG.md](./CHANGELOG.md) following the format defined in [Keep a Changelog](http://keepachangelog.com/).

## Note

This repository is part the open source distribution of jEAP. See [github.com/jeap-admin-ch/jeap](https://github.com/jeap-admin-ch/jeap)
for more information.

## License

This repository is Open Source Software licensed under the [Apache License 2.0](./LICENSE).
