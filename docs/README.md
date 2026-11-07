# Greetings API: one contract, two implementations

A course example that implements the same REST API twice, with clean architecture, tests, static analysis and CI pipelines for each version.

- **Contract:** [`greetings.yaml`](../greetings.yaml), an OpenAPI 3.0 spec shared by both implementations.
- **Python:** FastAPI, see [`python/README.md`](../python/README.md).
- **Java:** Spring Boot, see [`java/README.md`](../java/README.md).

## Repository layout

```
.
├── greetings.yaml            OpenAPI contract
├── python/                   FastAPI implementation (uv, pytest, ruff, mypy)
├── java/                     Spring Boot implementation (Gradle, JUnit, Checkstyle, PMD, SpotBugs, JaCoCo)
├── docs/                     this documentation
├── .github/workflows/        GitHub Actions: one workflow per project
├── .jenkins/                 Jenkins pipelines: one Jenkinsfile per project, and the Jenkins image
└── docker-compose.yml        both apps and Jenkins
```

## Documentation

| Page | Contents |
|---|---|
| [Python architecture](architecture/python.md) | C4 context, container and component diagrams, layers, request flow |
| [Java architecture](architecture/java.md) | The same set for the Java version |
| [Comparison](architecture/comparison.md) | What stays the same and what differs between the two |
| [CI/CD](ci-cd.md) | Pipelines, quality gates, warning-only checks, demo branches |

## Running everything

```bash
docker compose up -d --build
```

| Service | URL | Notes |
|---|---|---|
| Python app | http://localhost:5000/docs | Swagger UI (container port 8000) |
| Java app | http://localhost:5001/docs | Swagger UI (container port 8080) |
| Jenkins | http://localhost:8080 | Pipelines for both projects |

Both apps expose the same two operations:

```bash
curl -X POST localhost:5000/greetings -H 'Content-Type: application/json' \
  -d '{"name":"Ada","language":"en"}'
curl 'localhost:5001/greetings?lang=es'
```

## The API in one paragraph

`POST /greetings` takes a name and a language code and returns a personalized greeting. `GET /greetings?lang=es` returns the typical greetings of a language. Language codes are case-insensitive and regional variants are accepted (`ES`, `es-CO` and `es_CO` all resolve to `es`). Invalid input or an unsupported language returns **400**.
