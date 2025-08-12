#!/bin/bash

# BlockCloud Docker Hub 배포 스크립트
set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 로그 함수
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 환경 변수 설정
ENVIRONMENT=${1:-dev}
DOCKER_USERNAME=${DOCKER_USERNAME:-your-dockerhub-username}
DOCKER_IMAGE=${DOCKER_IMAGE:-$DOCKER_USERNAME/blockcloud-backend}
EC2_HOST=${EC2_HOST:-your-ec2-host}
EC2_USERNAME=${EC2_USERNAME:-ubuntu}

log_info "🚀 BlockCloud Docker Hub 배포 시작 (환경: $ENVIRONMENT)"

# 1. 테스트 실행
log_info "🧪 테스트 실행 중..."
./gradlew test

# 2. Docker 이미지 빌드
log_info "🐳 Docker 이미지 빌드 중..."
docker build -t $DOCKER_IMAGE:latest .

# 3. 이미지 태깅
IMAGE_TAG=$(git rev-parse --short HEAD)
docker tag $DOCKER_IMAGE:latest $DOCKER_IMAGE:$IMAGE_TAG

log_info "📦 이미지 태그: $IMAGE_TAG"

# 4. Docker Hub 로그인 확인
if [ -z "$DOCKER_PASSWORD" ]; then
    log_error "DOCKER_PASSWORD 환경 변수가 설정되지 않았습니다."
    log_info "다음 명령어로 설정하세요:"
    log_info "export DOCKER_PASSWORD=your-dockerhub-password"
    exit 1
fi

# 5. Docker Hub에 로그인
log_info "🔐 Docker Hub 로그인 중..."
echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin

# 6. 이미지 푸시
log_info "📤 Docker Hub에 이미지 푸시 중..."
docker push $DOCKER_IMAGE:latest
docker push $DOCKER_IMAGE:$IMAGE_TAG

log_info "✅ Docker Hub 푸시 완료!"

# 7. EC2 배포 (프로덕션 환경인 경우)
if [ "$ENVIRONMENT" = "prod" ]; then
    log_info "🖥️ EC2에 배포 중..."
    
    # EC2에 SSH로 접속하여 배포
    ssh -o StrictHostKeyChecking=no $EC2_USERNAME@$EC2_HOST << EOF
        # Docker Hub에서 이미지 풀
        docker pull $DOCKER_IMAGE:$IMAGE_TAG
        
        # 기존 컨테이너 중지 및 제거
        docker stop blockcloud-app || true
        docker rm blockcloud-app || true
        
        # 새 컨테이너 실행
        docker run -d \\
          --name blockcloud-app \\
          --restart unless-stopped \\
          -p 8080:8080 \\
          -e SPRING_PROFILES_ACTIVE=prod \\
          -e DB_HOST=\$DB_HOST \\
          -e DB_USERNAME=\$DB_USERNAME \\
          -e DB_PASSWORD=\$DB_PASSWORD \\
          -e JWT_SECRET=\$JWT_SECRET \\
          $DOCKER_IMAGE:$IMAGE_TAG
        
        # 헬스체크
        sleep 30
        if curl -f http://localhost:8080/actuator/health; then
            echo "✅ 배포 성공!"
        else
            echo "❌ 배포 실패!"
            docker logs blockcloud-app
            exit 1
        fi
EOF
else
    log_info "🔄 로컬 환경 배포 완료"
fi

log_info "✅ 배포 완료!"
log_info "📊 이미지: $DOCKER_IMAGE:$IMAGE_TAG"
log_info "🔗 Docker Hub: https://hub.docker.com/r/$DOCKER_USERNAME/blockcloud-backend"
