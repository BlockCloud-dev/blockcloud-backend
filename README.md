# BlockCloud Backend

[![codecov](https://codecov.io/gh/BlockCloud-dev/blockcloud-backend/branch/main/graph/badge.svg)](https://codecov.io/gh/BlockCloud-dev/blockcloud-backend)

BlockCloud is a Spring Boot-based backend service designed to support the core functionalities of the BlockCloud application.

---

## 📚 Documentation

* [Installation Guide](./docs/installation.md)
* [Usage Guide](./docs/usage.md)
* [Configuration Guide](./docs/configuration.md)
* [Development Guide](./docs/development.md)

---

## 🏁 Quick Start

### Local Development

```bash
git clone https://github.com/your-username/blockcloud-backend.git
cd blockcloud-backend
./gradlew bootRun
```

Visit: [http://localhost:8080](http://localhost:8080)

### Run with Docker

```bash
# Run in development environment
docker-compose --profile dev up -d

# Run in production environment
docker-compose up -d
```

## 🚀 Deployment

### Automated Deployment (GitHub Actions)

1. Configure GitHub Secrets:

   * `HOST`: Server IP address
   * `USERNAME`: SSH username
   * `SSH_KEY`: SSH private key
   * `PORT`: SSH port (default: 22)

2. Push to the **main** branch to trigger automated deployment.

### Manual Deployment

```bash
# Run the deployment script
chmod +x scripts/deploy.sh
./scripts/deploy.sh production latest
```

## 🔧 Environment Variables

Create a `.env` file at the project root and set the following variables:

```bash
# Spring Boot profile
SPRING_PROFILES_ACTIVE=dev

# Database settings
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/blockcloud_dev
SPRING_DATASOURCE_USERNAME=blockcloud
SPRING_DATASOURCE_PASSWORD=password

# JWT settings
JWT_SECRET=your-jwt-secret-key-here
JWT_EXPIRATION=86400000

# OAuth2 settings
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
```

---

## 📄 License

This project is licensed under the MIT License.

---
