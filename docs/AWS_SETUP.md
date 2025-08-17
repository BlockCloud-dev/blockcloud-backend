# AWS 인증 설정 가이드

## 필수 환경 변수

Terraform으로 AWS 리소스를 생성하려면 다음 환경 변수들이 필요합니다:

### 1. AWS Access Key 설정
```bash
export AWS_ACCESS_KEY_ID="your-access-key-id"
export AWS_SECRET_ACCESS_KEY="your-secret-access-key"
```

### 2. AWS Region 설정 (선택사항)
```bash
export AWS_DEFAULT_REGION="ap-northeast-2"  # 서울 리전
```

### 3. AWS Profile 사용 (권장)
```bash
# AWS CLI로 프로필 설정
aws configure --profile terraform-test

# 환경 변수로 프로필 지정
export AWS_PROFILE="terraform-test"
```

## AWS IAM 권한

다음 AWS 서비스에 대한 권한이 필요합니다:

- **EC2**: 인스턴스, VPC, 서브넷, 보안 그룹 생성/삭제
- **IAM**: 역할 및 정책 관리 (필요시)
- **VPC**: 가상 프라이빗 클라우드 관리
- **CloudWatch**: 로그 및 모니터링 (선택사항)

### 최소 IAM 정책 예시:
```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "ec2:*",
                "vpc:*",
                "iam:CreateRole",
                "iam:AttachRolePolicy",
                "iam:DetachRolePolicy",
                "iam:DeleteRole"
            ],
            "Resource": "*"
        }
    ]
}
```

## 테스트 방법

### 1. 환경 변수 설정 확인
```bash
echo $AWS_ACCESS_KEY_ID
echo $AWS_SECRET_ACCESS_KEY
echo $AWS_DEFAULT_REGION
```

### 2. AWS CLI 테스트
```bash
aws sts get-caller-identity
```

### 3. Terraform 테스트
```bash
# 프로젝트 루트에서
cd test-terraform
terraform init
terraform validate
terraform plan
```

## 주의사항

⚠️ **중요**: 실제 AWS 리소스가 생성되므로 비용이 발생할 수 있습니다.
- 테스트 후 반드시 `terraform destroy`로 리소스를 정리하세요
- t2.micro 인스턴스는 AWS Free Tier에 포함될 수 있지만, 다른 리소스는 비용이 발생할 수 있습니다
- 프로덕션 환경에서는 더 엄격한 IAM 정책을 사용하세요
