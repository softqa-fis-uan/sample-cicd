from fastapi import FastAPI

from app.presentation.error_handlers import register_error_handlers
from app.presentation.routes import router


def create_app() -> FastAPI:
    app = FastAPI(
        title="Greetings API",
        version="1.0.0",
        description="Generate personalized and typical greetings in different languages.",
    )
    app.include_router(router)
    register_error_handlers(app)
    return app


app = create_app()