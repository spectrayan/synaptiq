"""Application settings loaded from environment variables."""
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8")

    # --- App ---
    debug: bool = False
    environment: str = "development"

    # --- MongoDB ---
    mongodb_uri: str = "mongodb://localhost:27017"
    mongodb_db_name: str = "synaptiq"

    # --- Redis ---
    redis_url: str = "redis://localhost:6379"
    chat_session_ttl_seconds: int = 7200  # 2 hours

    # --- Auth ---
    firebase_project_id: str = ""
    firebase_service_account_json: str = ""  # Base64-encoded service account JSON
    firebase_api_key: str = ""  # Web API key for client-side Firebase
    firebase_auth_emulator_host: str = ""  # e.g. "localhost:9099" for emulator mode
    
    # --- LLM ---
    default_llm_provider: str = "vertexai"  # vertexai | openai | anthropic
    vertexai_project: str = ""
    vertexai_location: str = "us-central1"
    gemini_api_key: str = ""  # Google AI Studio API key (non-Vertex)
    gemini_model: str = "gemini-2.5-flash"  # Default Gemini model
    openai_api_key: str = ""
    anthropic_api_key: str = ""

    # --- CORS ---
    cors_origins: list[str] = ["http://localhost:4200"]

    # --- Tenant ---
    base_domain: str = "spectrayan.com"


settings = Settings()
