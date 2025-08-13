# BlockCloud Backend
[![codecov](https://codecov.io/gh/BlockCloud-dev/blockcloud-backend/branch/main/graph/badge.svg)](https://codecov.io/gh/BlockCloud-dev/blockcloud-backend)

BlockCloud is a Spring Boot-based backend service designed to support the core functionalities of the BlockCloud application.

---

## 📚 Documentation

- [Installation Guide](./docs/installation.md)
- [Usage Guide](./docs/usage.md)
- [Configuration Guide](./docs/configuration.md)
- [Development Guide](./docs/development.md)

---

## 🏁 Quick Start

### 로컬 개발 환경

```bash
git clone https://github.com/your-username/blockcloud-backend.git
cd blockcloud-backend
./gradlew bootRun
```

Visit: [http://localhost:8080](http://localhost:8080)

### Docker를 사용한 실행

```bash
# 개발 환경 실행
docker-compose --profile dev up -d

# 프로덕션 환경 실행
docker-compose up -d
```

## 🚀 배포

### 자동 배포 (GitHub Actions)

1. GitHub Secrets 설정:
   - `HOST`: 서버 IP 주소
   - `USERNAME`: SSH 사용자명
   - `SSH_KEY`: SSH 개인키
   - `PORT`: SSH 포트 (기본값: 22)

2. main 브랜치에 push하면 자동으로 배포됩니다.

### 수동 배포

```bash
# 배포 스크립트 실행
chmod +x scripts/deploy.sh
./scripts/deploy.sh production latest
```

## 🔧 환경 변수

프로젝트 루트에 `.env` 파일을 생성하고 다음 변수들을 설정하세요:

```bash
# Spring Boot 프로필
SPRING_PROFILES_ACTIVE=dev

# 데이터베이스 설정
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/blockcloud_dev
SPRING_DATASOURCE_USERNAME=blockcloud
SPRING_DATASOURCE_PASSWORD=password

# JWT 설정
JWT_SECRET=your-jwt-secret-key-here
JWT_EXPIRATION=86400000

# OAuth2 설정
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
```

---

## 📄 License

This project is licensed under the MIT License.
