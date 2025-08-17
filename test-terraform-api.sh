#!/bin/bash

# Terraform API 테스트 스크립트
# 사용법: ./test-terraform-api.sh [JWT_TOKEN]

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 기본 설정
API_BASE="http://localhost:8080"
PROJECT_ID=1

# JWT 토큰 설정
if [ -z "$1" ]; then
    echo -e "${RED}JWT 토큰이 필요합니다.${NC}"
    echo "사용법: $0 <JWT_TOKEN>"
    exit 1
fi

JWT_TOKEN=$1

# AWS VPC와 EC2를 생성하는 Terraform 코드
TERRAFORM_CODE='terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = "ap-northeast-2"
}

resource "aws_vpc" "test_vpc" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Name = "test-vpc"
  }
}

resource "aws_internet_gateway" "test_igw" {
  vpc_id = aws_vpc.test_vpc.id

  tags = {
    Name = "test-igw"
  }
}

resource "aws_subnet" "test_subnet" {
  vpc_id                  = aws_vpc.test_vpc.id
  cidr_block              = "10.0.1.0/24"
  availability_zone       = "ap-northeast-2a"
  map_public_ip_on_launch = true

  tags = {
    Name = "test-subnet"
  }
}

resource "aws_route_table" "test_rt" {
  vpc_id = aws_vpc.test_vpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.test_igw.id
  }

  tags = {
    Name = "test-rt"
  }
}

resource "aws_route_table_association" "test_rta" {
  subnet_id      = aws_subnet.test_subnet.id
  route_table_id = aws_route_table.test_rt.id
}

resource "aws_security_group" "test_sg" {
  name        = "test-security-group"
  description = "Test security group for EC2"
  vpc_id      = aws_vpc.test_vpc.id

  ingress {
    description = "SSH"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "test-sg"
  }
}

resource "aws_instance" "test_instance" {
  ami                    = "ami-0c9c942bd7bf113a2"
  instance_type          = "t2.micro"
  subnet_id              = aws_subnet.test_subnet.id
  vpc_security_group_ids = [aws_security_group.test_sg.id]

  tags = {
    Name = "test-instance"
  }
}

output "vpc_id" {
  value = aws_vpc.test_vpc.id
}

output "instance_id" {
  value = aws_instance.test_instance.id
}'

echo -e "${BLUE}=== Terraform API 테스트 시작 ===${NC}"
echo "API Base: $API_BASE"
echo "Project ID: $PROJECT_ID"
echo ""

# 1. Validate 테스트
echo -e "${YELLOW}1. Terraform Validate 테스트${NC}"
VALIDATE_RESPONSE=$(curl -s -X POST "$API_BASE/api/projects/$PROJECT_ID/terraform/validate" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"terraformCode\": \"$TERRAFORM_CODE\"}")

echo "Response: $VALIDATE_RESPONSE"
echo ""

# 2. Plan 테스트
echo -e "${YELLOW}2. Terraform Plan 테스트${NC}"
PLAN_RESPONSE=$(curl -s -X POST "$API_BASE/api/projects/$PROJECT_ID/terraform/plan" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"terraformCode\": \"$TERRAFORM_CODE\"}")

echo "Response: $PLAN_RESPONSE"
echo ""

# 3. Apply 테스트 (실제 AWS 리소스 생성)
echo -e "${YELLOW}3. Terraform Apply 테스트${NC}"
echo -e "${RED}⚠️  주의: 실제 AWS 리소스가 생성됩니다!${NC}"
read -p "계속하시겠습니까? (y/N): " -n 1 -r
echo

if [[ $REPLY =~ ^[Yy]$ ]]; then
    APPLY_RESPONSE=$(curl -s -X POST "$API_BASE/api/projects/$PROJECT_ID/terraform/apply" \
      -H "Authorization: Bearer $JWT_TOKEN" \
      -H "Content-Type: application/json" \
      -d "{\"terraformCode\": \"$TERRAFORM_CODE\"}")

    echo "Response: $APPLY_RESPONSE"
    echo ""
    
    # 배포 ID 추출
    DEPLOYMENT_ID=$(echo $APPLY_RESPONSE | grep -o '"deploymentId":[0-9]*' | cut -d':' -f2)
    
    if [ ! -z "$DEPLOYMENT_ID" ]; then
        echo -e "${GREEN}배포 ID: $DEPLOYMENT_ID${NC}"
        echo ""
        
        # 4. 배포 상태 확인
        echo -e "${YELLOW}4. 배포 상태 확인${NC}"
        for i in {1..10}; do
            STATUS_RESPONSE=$(curl -s -X GET "$API_BASE/api/projects/$PROJECT_ID/terraform/deployments/$DEPLOYMENT_ID" \
              -H "Authorization: Bearer $JWT_TOKEN")
            
            echo "Status Check $i: $STATUS_RESPONSE"
            
            # 성공 또는 실패 상태 확인
            if echo "$STATUS_RESPONSE" | grep -q '"status":"SUCCESS"'; then
                echo -e "${GREEN}✅ 배포 성공!${NC}"
                break
            elif echo "$STATUS_RESPONSE" | grep -q '"status":"FAILED"'; then
                echo -e "${RED}❌ 배포 실패!${NC}"
                break
            fi
            
            sleep 5
        done
    fi
else
    echo -e "${YELLOW}Apply 테스트를 건너뜁니다.${NC}"
fi

echo ""
echo -e "${BLUE}=== 테스트 완료 ===${NC}"
echo ""
echo -e "${YELLOW}참고:${NC}"
echo "- 실제 AWS 리소스가 생성된 경우, 나중에 destroy API를 사용하여 정리하세요"
echo "- AWS 비용이 발생할 수 있으니 주의하세요"
