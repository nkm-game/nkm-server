# NKM Game Server 🎮

<img src="./docs/img/NKM.png" alt="NKM" width="200"/>

[![codecov](https://codecov.io/gh/nkm-game/nkm-server/graph/badge.svg?token=3BK0XQ2MZ3)](https://codecov.io/gh/nkm-game/nkm-server)
![Development Status](https://img.shields.io/badge/Status-Active%20Development-green)
![Docker](https://img.shields.io/badge/Docker-Supported-blue?logo=docker)
![License](https://img.shields.io/badge/License-AGPL%20v3-blue.svg)

## 📋 Table of Contents

- [Overview](#-overview)
- [Getting Started](#-getting-started)
- [Tech Stack](#-tech-stack)
- [Documentation](#-documentation)
- [Contributing](#-contributing)
- [Community & Support](#-community--support)

## 🎯 Overview

NKM is an exciting turn-based multiplayer game where players battle on a hexagonal board. Choose unique characters, each with their own special abilities and stats, and engage in strategic combat!

## 🚀 Getting Started

### Prerequisites

Before you begin, ensure you have the following installed:

- **Docker** 20.10+ ([Download](https://docs.docker.com/get-docker/))
- **Docker Compose** 2.20+ ([Install Guide](https://docs.docker.com/compose/install/))
- **Git** 2.40+ ([Download](https://git-scm.com/downloads))

### Quick Start

```bash
# Clone the repository
git clone https://github.com/nkm-game/nkm-server.git
cd nkm-server

# Start the server
docker-compose up -d

# Check if services are running
docker-compose ps
```

The server will be available at `http://localhost:8080`

### First Steps

1. 📖 Read the [Game Rules](https://nkm-game.github.io/nkm-server/game-rules/) to understand gameplay
2. 🎮 Connect via WebSocket to start playing
3. 🔧 Check our [Docker Setup Guide](https://nkm-game.github.io/nkm-server/backend_development/docker/) for advanced configuration

## 💻 Tech Stack

![Scala](https://img.shields.io/badge/Scala-DC322F?style=flat-square&logo=scala&logoColor=white)
![Akka](https://img.shields.io/badge/Akka-000000?style=flat-square&logo=akka&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white)
![MariaDB](https://img.shields.io/badge/MariaDB-003545?style=flat-square&logo=mariadb&logoColor=white)

- **Backend Framework**: Scala + Akka
- **API**: Akka HTTP + WebSockets
- **Database**: MariaDB
- **Infrastructure**: Docker + Traefik
- **Testing**: ScalaTest

## 📚 Documentation

### 🌐 Online Documentation

Visit our [**Full Documentation Site**](https://nkm-game.github.io/nkm-server/) for comprehensive guides and API reference.

### 📖 Key Resources

- [Game Rules](https://nkm-game.github.io/nkm-server/game-rules/)
- [Architecture Overview](https://nkm-game.github.io/nkm-server/backend_development/architecture/)
- [Development Setup](https://nkm-game.github.io/nkm-server/backend_development/dev-guide/)
- [Adding New Characters](https://nkm-game.github.io/nkm-server/backend_development/adding-new-characters/)
- [Adding New Effects](https://nkm-game.github.io/nkm-server/backend_development/adding-new-effects/)
- [Adding New Actors](https://nkm-game.github.io/nkm-server/backend_development/adding-new-actors/)
- [Docker Setup Guide](https://nkm-game.github.io/nkm-server/backend_development/docker/)
- [Frontend API](https://nkm-game.github.io/nkm-server/frontend_development/api/)

## 🤝 Contributing

We welcome contributions! Whether you're fixing bugs, adding features, or improving documentation, we appreciate your help.

### Contribution Workflow

1.  **Familiarize yourself with the project**: Review our [**Full Documentation Site**](https://nkm-game.github.io/nkm-server/), especially the [Architecture Overview](https://nkm-game.github.io/nkm-server/backend_development/architecture/) and [Development Guide](https://nkm-game.github.io/nkm-server/backend_development/dev-guide/).
2.  **Fork the repository** and create a feature branch (`git checkout -b my-new-feature`).
3.  **Implement your changes**, following our coding standards.
4.  **Add or update tests** to cover your changes.
5.  **Submit a pull request** with a clear description of your work.

### Code of Conduct

By participating in this project, you agree to abide by our community standards. Please be respectful and constructive in all interactions.

## 💬 Community & Support

- **Issues**: [GitHub Issues](https://github.com/nkm-game/nkm-server/issues)
- **Bug Reports**: Use our issue templates for bug reports
- **Feature Requests**: Submit feature requests through GitHub Issues
