## Dev guide

### Development flow

- Frequently after some changes to the code `=>` run unit tests, they are really fast
- Before commit `=>` run all tests

!!! important
    In order to run tests, a database is needed.

### Running tests or starting the app for the first time

A mysql / mariadb database needs to run with parameters specified in `application.conf`.

You can start the database with docker:

```bash
docker compose -f .\docker-compose.dev.yml up db
```

!!! note
    You should consider installing a native database for development for better performance.

    Integration tests will be slower otherwise.

## Development Guide

This guide will help you set up your local development environment.

### Prerequisites

- JDK 11
- sbt
- Docker & Docker Compose

### Running the server

You can run the server using sbt:

```
sbt run
```

Alternatively, you can use Docker:

```
docker-compose up
```

### Local Documentation Setup

To serve the documentation locally:

```bash
# Install documentation dependencies
pip install -r docs/requirements.txt

# Start local documentation server
mkdocs serve
```

The documentation will be available at `http://127.0.0.1:8000`.
