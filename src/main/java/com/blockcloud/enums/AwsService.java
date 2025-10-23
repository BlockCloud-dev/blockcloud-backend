package com.blockcloud.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * AWS 서비스 목록 Enum
 * AWS Cost Explorer에서 사용되는 주요 서비스들을 정의합니다.
 */
@Schema(description = "AWS 서비스 목록")
public enum AwsService {
    
    // Compute Services
    @Schema(description = "Amazon Elastic Compute Cloud - Compute")
    EC2_COMPUTE("Amazon Elastic Compute Cloud - Compute"),
    @Schema(description = "Amazon Elastic Compute Cloud - Container")
    EC2_CONTAINER("Amazon Elastic Compute Cloud - Container"),
    @Schema(description = "Amazon Elastic Container Service")
    ECS("Amazon Elastic Container Service"),
    @Schema(description = "Amazon Elastic Kubernetes Service")
    EKS("Amazon Elastic Kubernetes Service"),
    @Schema(description = "AWS Lambda")
    LAMBDA("AWS Lambda"),
    @Schema(description = "AWS Batch")
    BATCH("AWS Batch"),
    @Schema(description = "Amazon Lightsail")
    LIGHTSAIL("Amazon Lightsail"),
    
    // Storage Services
    @Schema(description = "Amazon Simple Storage Service")
    S3("Amazon Simple Storage Service"),
    @Schema(description = "Amazon Elastic Block Store")
    EBS("Amazon Elastic Block Store"),
    @Schema(description = "Amazon Elastic File System")
    EFS("Amazon Elastic File System"),
    @Schema(description = "Amazon FSx")
    FSX("Amazon FSx"),
    @Schema(description = "AWS Backup")
    BACKUP("AWS Backup"),
    
    // Database Services
    @Schema(description = "Amazon Relational Database Service")
    RDS("Amazon Relational Database Service"),
    @Schema(description = "Amazon DynamoDB")
    DYNAMODB("Amazon DynamoDB"),
    @Schema(description = "Amazon Redshift")
    REDSHIFT("Amazon Redshift"),
    @Schema(description = "Amazon ElastiCache")
    ELASTICACHE("Amazon ElastiCache"),
    @Schema(description = "Amazon DocumentDB")
    DOCUMENTDB("Amazon DocumentDB"),
    @Schema(description = "Amazon Neptune")
    NEPTUNE("Amazon Neptune"),
    @Schema(description = "Amazon Timestream")
    TIMESTREAM("Amazon Timestream"),
    
    // Networking Services
    @Schema(description = "Amazon Virtual Private Cloud")
    VPC("Amazon Virtual Private Cloud"),
    @Schema(description = "Amazon CloudFront")
    CLOUDFRONT("Amazon CloudFront"),
    @Schema(description = "Amazon Route 53")
    ROUTE53("Amazon Route 53"),
    @Schema(description = "Amazon API Gateway")
    API_GATEWAY("Amazon API Gateway"),
    @Schema(description = "AWS Direct Connect")
    DIRECT_CONNECT("AWS Direct Connect"),
    @Schema(description = "AWS Global Accelerator")
    GLOBAL_ACCELERATOR("AWS Global Accelerator"),
    @Schema(description = "AWS PrivateLink")
    PRIVATE_LINK("AWS PrivateLink"),
    
    // Security Services
    @Schema(description = "AWS Identity and Access Management")
    IAM("AWS Identity and Access Management"),
    @Schema(description = "AWS Key Management Service")
    KMS("AWS Key Management Service"),
    @Schema(description = "AWS Secrets Manager")
    SECRETS_MANAGER("AWS Secrets Manager"),
    @Schema(description = "AWS Certificate Manager")
    CERTIFICATE_MANAGER("AWS Certificate Manager"),
    @Schema(description = "AWS WAF")
    WAF("AWS WAF"),
    @Schema(description = "AWS Shield")
    SHIELD("AWS Shield"),
    @Schema(description = "Amazon GuardDuty")
    GUARDDUTY("Amazon GuardDuty"),
    
    // Management Services
    @Schema(description = "Amazon CloudWatch")
    CLOUDWATCH("Amazon CloudWatch"),
    @Schema(description = "AWS CloudTrail")
    CLOUDTRAIL("AWS CloudTrail"),
    @Schema(description = "AWS Config")
    CONFIG("AWS Config"),
    @Schema(description = "AWS Systems Manager")
    SYSTEMS_MANAGER("AWS Systems Manager"),
    @Schema(description = "AWS Trusted Advisor")
    TRUSTED_ADVISOR("AWS Trusted Advisor"),
    @Schema(description = "AWS Personal Health Dashboard")
    PERSONAL_HEALTH_DASHBOARD("AWS Personal Health Dashboard"),
    
    // Analytics Services
    @Schema(description = "Amazon Kinesis")
    KINESIS("Amazon Kinesis"),
    @Schema(description = "Amazon EMR")
    EMR("Amazon EMR"),
    @Schema(description = "Amazon QuickSight")
    QUICKSIGHT("Amazon QuickSight"),
    @Schema(description = "Amazon Athena")
    ATHENA("Amazon Athena"),
    @Schema(description = "AWS Glue")
    GLUE("AWS Glue"),
    @Schema(description = "AWS Lake Formation")
    LAKE_FORMATION("AWS Lake Formation"),
    
    // AI/ML Services
    @Schema(description = "Amazon SageMaker")
    SAGEMAKER("Amazon SageMaker"),
    @Schema(description = "Amazon Comprehend")
    COMPREHEND("Amazon Comprehend"),
    @Schema(description = "Amazon Translate")
    TRANSLATE("Amazon Translate"),
    @Schema(description = "Amazon Rekognition")
    REKOGNITION("Amazon Rekognition"),
    @Schema(description = "Amazon Polly")
    POLLY("Amazon Polly"),
    @Schema(description = "Amazon Lex")
    LEX("Amazon Lex"),
    
    // Application Services
    @Schema(description = "Amazon Simple Queue Service")
    SQS("Amazon Simple Queue Service"),
    @Schema(description = "Amazon Simple Notification Service")
    SNS("Amazon Simple Notification Service"),
    @Schema(description = "Amazon Simple Email Service")
    SES("Amazon Simple Email Service"),
    @Schema(description = "Amazon Simple Notification Service - Mobile")
    SNS_MOBILE("Amazon Simple Notification Service - Mobile"),
    @Schema(description = "Amazon WorkSpaces")
    WORKSPACES("Amazon WorkSpaces"),
    @Schema(description = "Amazon AppStream 2.0")
    APPSTREAM("Amazon AppStream 2.0"),
    
    // Developer Tools
    @Schema(description = "AWS CodeBuild")
    CODEBUILD("AWS CodeBuild"),
    @Schema(description = "AWS CodePipeline")
    CODEPIPELINE("AWS CodePipeline"),
    @Schema(description = "AWS CodeCommit")
    CODECOMMIT("AWS CodeCommit"),
    @Schema(description = "AWS CodeDeploy")
    CODEDEPLOY("AWS CodeDeploy"),
    @Schema(description = "AWS X-Ray")
    X_RAY("AWS X-Ray"),
    @Schema(description = "AWS Cloud9")
    CLOUD9("AWS Cloud9"),
    
    // Other Services
    @Schema(description = "AWS Support")
    SUPPORT("AWS Support"),
    @Schema(description = "AWS Marketplace")
    MARKETPLACE("AWS Marketplace"),
    @Schema(description = "AWS Data Transfer")
    DATA_TRANSFER("AWS Data Transfer"),
    @Schema(description = "Tax")
    TAX("Tax"),
    @Schema(description = "Other")
    OTHER("Other");
    
    private final String serviceName;
    
    AwsService(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
}