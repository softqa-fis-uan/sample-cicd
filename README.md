# Integración continua y despliegues continuos

Este repositorio contiene ejemplo de integración y despliegues continuos usando herramientas como Jenkins y Github Actions. Un flujo de trabajo típico de integración continua incluye tareas como:

1. Compilar
1. Correr pruebas unitarias
1. Analizar la cobertura
1. Correr otro tipo de pruebas
1. Análisis estático del código
1. Versionar artefactos
1. Desplegar

```mermaid
flowchart LR
    A[Compilar] --> B[Correr pruebas unitarias]
    B --> C[Analizar la cobertura]
    C --> D[Correr otro tipo de pruebas]
    D --> E[Análisis estático del código]
    E --> F[Versionar artefactos]
    F --> G[Desplegar]

    classDef step1 fill:#e3f2fd,stroke:#90caf9,color:#0d47a1,stroke-width:2px
    classDef step2 fill:#bbdefb,stroke:#64b5f6,color:#0d47a1,stroke-width:2px
    classDef step3 fill:#90caf9,stroke:#42a5f5,color:#0d47a1,stroke-width:2px
    classDef step4 fill:#64b5f6,stroke:#1e88e5,color:#ffffff,stroke-width:2px
    classDef step5 fill:#42a5f5,stroke:#1976d2,color:#ffffff,stroke-width:2px
    classDef step6 fill:#1e88e5,stroke:#1565c0,color:#ffffff,stroke-width:2px
    classDef step7 fill:#0d47a1,stroke:#082f6b,color:#ffffff,stroke-width:2px

    class A step1
    class B step2
    class C step3
    class D step4
    class E step5
    class F step6
    class G step7

    linkStyle default stroke:#1976d2,stroke-width:2px
```

## Como usar este repositorio

Para poner esto a prueba vamos a usar dos aplicaciones, una escrita en [Java](java/) y otra en [Python](python/). Las dos exponen un [API](service-contract.yaml) al cual le haremos pruebas unitarias, pruebas de integración, y validación estática de código para encontrar posible problemas de lógica o de seguridad.

Tenemos tambien pipelines de integracion continua configurados para [Jenkins](.jenkins) y para [Github Actions](.github/workflows/). Puede ver el resultado de la ejecucion de este ultimo en la pestanna de `Acciones` en GitHub. Para usar Jenkins este repositorio tambien tiene preconfigurados contenedores que ejecutan las aplicaciones y tambien Jenkins. Este estara disponible en el puerto 8080 (http://localhost:8080) con el usuario admin. Antes de iniciar docker compose cree un archivo `.env` usando como referencia el archivo `.env.sample` indicando cual es la clave que quiere usar para el usuario administrador de Jenkins. Para iniciar los contenedores use el comando `docker compose up -d`.
