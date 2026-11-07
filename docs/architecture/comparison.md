# Python vs Java

Both implementations follow the same clean architecture: a domain with no framework imports, use cases that depend on a repository port, an in-memory adapter, and a thin HTTP layer. Only the technology and a few framework idioms change.

## What stays the same

| Aspect | Both versions |
|---|---|
| Layers | domain, application, infrastructure, presentation |
| Dependency rule | Dependencies point inward. The adapter implements a port owned by the application layer |
| Use cases | `CreateGreeting` and `GetTypicalGreetings`, one per operation |
| Language handling | Case-insensitive, regional variants accepted (`es-CO` becomes `es`) |
| Error status | 400 for invalid input and unsupported languages |
| Persistence | In-memory repository, swappable without touching the use cases |

## What differs

| Aspect | Python (FastAPI) | Java (Spring Boot) |
|---|---|---|
| Where the contract comes from | Code-first: the spec is derived from routes and Pydantic models | Spec-first: the interface and DTOs are generated from `greetings.yaml` |
| Request validation | Pydantic schemas, with 422 remapped to 400 | Annotations on the generated interface. Spring answers 400 by default |
| Error body | `{"detail": ...}` | RFC 9457 `application/problem+json` |
| Wiring | `dependencies.py` with `Depends` | `BeanConfiguration`. Use cases are plain classes, not `@Service` |
| Domain model | Frozen dataclasses | Records |
| Port | `typing.Protocol` | Java `interface` |
| Swagger UI assets | Loaded from a CDN | Served by the application (springdoc) |
| Docs path | `/docs` (built in) | `/docs` (springdoc, configured) |

## Tooling

| Concern | Python | Java |
|---|---|---|
| Build and dependencies | uv | Gradle (Kotlin DSL) |
| Unit and API tests | pytest and assertpy | JUnit 5 and AssertJ |
| API tests | FastAPI `TestClient` | MockMvc with `@SpringBootTest` |
| Test doubles | Fake repository | Fake repository |
| Linting | Ruff (blocking set plus an advisory run) | Checkstyle (error and warning severities), PMD |
| Type and bug checks | mypy `--strict` | `javac`, SpotBugs |
| Coverage | pytest-cov | JaCoCo |
| Reports | JSON and HTML in `python/build/reports` | XML and HTML in `java/build/reports` |
| Container image | `uv` base image, Python installed by uv | `eclipse-temurin` JDK to build, JRE to run |

## A point worth discussing in class

The same architecture needs different amounts of code: the Python app wires its dependencies by hand, while Spring offers a container for it. The Java version deliberately keeps the use cases free of Spring annotations to show that the architecture does not depend on the framework.
