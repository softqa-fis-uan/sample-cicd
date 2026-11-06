# FastAPI

Esta carpeta contiene la implementación del API definido en [`service-contract.yaml`](../service-contract.yaml). Puede ver el swagger de la aplicación en (http://localhost:8000/docs)

Para correr la aplicación usar:

```bash
uv run uvicorn app.main:app --reload
```

para correr las pruebas:

```bash
uv run pytest -v
```

## Arquitectura de la app

### C4 Level 3: Diagrama de componentes

```mermaid
C4Component
    title Component diagram for the Greetings API application

    Person(consumer, "API consumer", "Developer or application that requests greetings.")

    Container_Boundary(app, "API application (Python 3.15, FastAPI)") {

        Boundary(presentation, "presentation layer") {
            Component(routes, "Greetings router", "FastAPI APIRouter", "Exposes POST /greetings and GET /greetings. Maps use case results to response models.")
            Component(schemas, "Request/response schemas", "Pydantic models", "Define and validate the HTTP contract: names must be non-blank, language is required.")
            Component(handlers, "Error handlers", "FastAPI exception handlers", "Turn domain errors and validation failures into HTTP 400 responses.")
            Component(wiring, "Dependency wiring", "FastAPI Depends", "Composition root. Builds the use cases and chooses the repository implementation.")
        }

        Boundary(application, "application layer") {
            Component(create, "CreateGreeting", "Use case", "Normalizes the language code and generates a personalized greeting.")
            Component(typical, "GetTypicalGreetings", "Use case", "Normalizes the language code and returns the language's typical greetings.")
            Component(port, "LanguageRepository", "Port (typing.Protocol)", "Interface the use cases depend on to look up a language profile by code.")
        }

        Boundary(domain, "domain layer") {
            Component(model, "Domain model", "Python dataclasses", "Greeting and LanguageProfile. LanguageProfile.greet() holds the greeting logic.")
            Component(errors, "Domain errors", "Python exceptions", "DomainError and UnsupportedLanguageError.")
        }

        Boundary(infra, "infrastructure layer") {
            Component(repo, "InMemoryLanguageRepository", "Python class", "Adapter that serves language profiles from an in-memory dictionary.")
        }
    }

    Rel(consumer, routes, "Sends requests to", "HTTP/JSON")
    Rel(routes, schemas, "Validates input and builds output with")
    Rel(routes, create, "Calls")
    Rel(routes, typical, "Calls")
    Rel(handlers, errors, "Maps to HTTP 400")
    Rel(wiring, create, "Creates and injects")
    Rel(wiring, typical, "Creates and injects")
    Rel(wiring, repo, "Instantiates")
    Rel(create, port, "Looks up languages through")
    Rel(typical, port, "Looks up languages through")
    Rel(create, model, "Creates greetings with")
    Rel(create, errors, "Raises when language is unsupported")
    Rel(repo, port, "Implements")
    Rel(repo, model, "Returns")

    UpdateLayoutConfig($c4ShapeInRow="4", $c4BoundaryInRow="1")
```

### Capas y dependencias

```mermaid
flowchart TB
    client([HTTP client])
    main["main.py<br/>create_app()"]

    subgraph presentation["presentation"]
        routes["routes.py<br/>APIRouter: POST and GET /greetings"]
        schemas["schemas.py<br/>Pydantic request/response models"]
        deps["dependencies.py<br/>dependency wiring"]
        handlers["error_handlers.py<br/>DomainError and validation errors → 400"]
    end

    subgraph application["application"]
        usecases["use_cases.py<br/>CreateGreeting<br/>GetTypicalGreetings"]
        ports["ports.py<br/>LanguageRepository (Protocol)"]
    end

    subgraph domain["domain"]
        entities["entities.py<br/>Greeting, LanguageProfile"]
        derrors["errors.py<br/>DomainError<br/>UnsupportedLanguageError"]
    end

    subgraph infrastructure["infrastructure"]
        repo["in_memory_repository.py<br/>InMemoryLanguageRepository"]
    end

    client --> routes
    main --> routes
    main --> handlers

    routes --> schemas
    routes --> usecases
    deps -. injects .-> routes
    deps -. builds .-> usecases
    deps -. chooses .-> repo
    handlers --> derrors

    usecases --> ports
    usecases --> entities
    usecases --> derrors
    ports --> entities

    repo -. implements .-> ports
    repo --> entities
```

### Flujo de un request: `POST /greetings`

```mermaid
sequenceDiagram
    actor Client
    participant Routes as routes.py
    participant UC as CreateGreeting
    participant Repo as InMemoryLanguageRepository<br/>(via LanguageRepository port)
    participant Profile as LanguageProfile

    Client->>Routes: POST /greetings {name, language}
    Note over Routes: Body validated by CreateGreetingRequest.<br/>Invalid body → 400 from the validation handler
    Routes->>UC: execute(name, language)
    UC->>UC: normalize language code ("ES-co" → "es")
    UC->>Repo: get(code)
    Repo-->>UC: LanguageProfile or None
    alt language supported
        UC->>Profile: greet(name)
        Profile-->>UC: Greeting
        UC-->>Routes: Greeting
        Routes-->>Client: 200 {greeting, language}
    else unsupported language
        UC-->>Routes: raise UnsupportedLanguageError
        Routes-->>Client: 400 {detail} (via DomainError handler)
    end
```
