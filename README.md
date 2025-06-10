<img src=".\docs\img\NKM.png" alt="NKM" width="200"/>

---

[![codecov](https://codecov.io/gh/nkm-game/nkm-server/graph/badge.svg?token=3BK0XQ2MZ3)](https://codecov.io/gh/nkm-game/nkm-server)

## About

This is a backend for a turn-based multiplayer game played on a hexagonal board.

Includes characters with abilities and statistics.

## Tech stack

- Scala
- Akka
- Akka http
- Websockets
- Scala test
- MariaDB
- docker
- traefik

## Documentation

📚 **[Full Documentation Site](https://nkm-game.github.io/nkm-server/)** - Complete documentation built with MkDocs

### Quick Links

[Game rules](docs/game-rules.md)

[Adding new characters](docs/backend_development/adding-new-characters.md)

[Adding new effects](docs/backend_development/adding-new-effects.md)

[Adding new actors](docs/backend_development/adding-new-actors.md)

[Docker setup](docs/backend_development/docker.md)

### Local Documentation

To serve the documentation locally:

```bash
# Install dependencies
pip install -r docs/requirements.txt

# Serve documentation
mkdocs serve
```

The documentation will be available at `http://127.0.0.1:8000`
