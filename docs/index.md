# NKM Game Server

<img src="img/NKM.png" alt="NKM" width="200"/>

[![codecov](https://codecov.io/gh/nkm-game/nkm-server/graph/badge.svg?token=3BK0XQ2MZ3)](https://codecov.io/gh/nkm-game/nkm-server)

## About

This is a backend for a turn-based multiplayer game played on a hexagonal board. The game features characters with unique abilities and statistics, providing strategic gameplay in a multiplayer environment.

## Tech Stack

- **Scala** - Primary programming language
- **Akka** - Actor-based concurrency framework
- **Akka HTTP** - HTTP server toolkit
- **WebSockets** - Real-time communication
- **ScalaTest** - Testing framework
- **MariaDB** - Database
- **Docker** - Containerization
- **Traefik** - Reverse proxy and load balancer

## Features

- Turn-based multiplayer gameplay on hexagonal maps
- Character system with unique abilities and effects
- Multiple game modes (Deathmatch, Capture the Point)
- Different pick types (Blind pick, Draft pick, All random)
- Real-time communication via WebSockets
- Lobby system for game creation and management
- Comprehensive testing suite

## Quick Start

!!! tip "Getting Started"
New to the project? Start with the [Game Rules](game-rules.md) to understand how the game works, then check out the [Development Guide](backend_development/dev-guide.md) for setting up your development environment.

!!! info "Navigation Tips" - **Search**: Use the search bar to quickly find documentation - **Theme**: Toggle between light and dark modes using the theme switcher in the header - **Colors**: Check out [Theme Colors](theme-colors.md) for popular color scheme options - **Mobile**: All features work seamlessly on mobile devices

## Documentation Sections

### 📚 Game Rules

Learn how to play NKM, understand the game mechanics, and explore different game modes.

[View Game Rules →](game-rules.md){ .md-button .md-button--primary }

### 🔧 Backend Development

Comprehensive guides for developers working on the server-side code.

- [Architecture Overview](backend_development/architecture.md)
- [Development Setup](backend_development/dev-guide.md)
- [Docker Configuration](backend_development/docker.md)
- [Adding New Characters](backend_development/adding-new-characters.md)
- [Adding New Effects](backend_development/adding-new-effects.md)
- [Adding New Actors](backend_development/adding-new-actors.md)

[Explore Backend Docs →](backend_development/architecture.md){ .md-button }

### 🎨 Frontend Development

Resources for frontend developers integrating with the game server.

- [API Documentation](frontend_development/api.md)
- [Event to Animation Mapping](frontend_development/event-to-animation.md)

[View Frontend Docs →](frontend_development/api.md){ .md-button }

## Contributing

We welcome contributions! Please check our development guides and feel free to submit pull requests or open issues on our GitHub repository.

## License

This project is licensed under the terms specified in the [LICENSE](https://github.com/nkm-game/nkm-server/blob/main/LICENSE) file.
