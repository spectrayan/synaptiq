FROM python:3.12-slim

WORKDIR /app

# Install uv
RUN pip install uv

# Copy dependency files
COPY pyproject.toml uv.lock* ./
COPY src/ ./src/

# Install dependencies
RUN uv sync --frozen --no-dev

# Run the app
ENV PYTHONPATH=/app/src
CMD ["uv", "run", "uvicorn", "synaptiq_api.main:app", "--host", "0.0.0.0", "--port", "8080"]
