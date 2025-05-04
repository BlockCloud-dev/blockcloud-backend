# 🚀 Usage Guide

## Access Root Endpoint
```http
GET /
```
**Response:**
```
Hello from BlockCloud!
```

## Access H2 Database Console
- URL: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
- JDBC URL: `jdbc:h2:mem:testdb`
- User: `sa`
- Password: *(leave blank)*

You can use this to inspect tables and run SQL queries in-memory.
