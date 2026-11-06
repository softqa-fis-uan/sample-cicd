# FastAPI

Esta carpeta contiene la implemnetacion del API definido en [`service-contract.yaml`](../service-contract.yaml).

Para correr la aplicacion usar:

```bash
uv run uvicorn app.main:app --reload
```

para correr las pruebas:

```bash
uv run pytest -v
```