# ⚙️ Configuration Guide

All configurations are managed in `src/main/resources/application.yml`

## Default Configuration (H2 DB)
```yaml
spring:
  application:
    name: blockcloud

  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:

  h2:
    console:
      enabled: true

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

To switch to MySQL or PostgreSQL, replace `datasource.*` settings accordingly.
