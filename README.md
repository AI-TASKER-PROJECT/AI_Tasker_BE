# AITASKER

## Run local

### Prerequisites
- Java 21
- Docker Desktop

### Start
```bash
docker compose up -d
docker ps
./mvnw spring-boot:run
```

### Attention
- Run the two commands below if you encounter a time zone error
```bash
setx JAVA_TOOL_OPTIONS "-Duser.timezone=Asia/Ho_Chi_Minh"
echo $env:JAVA_TOOL_OPTIONS
```

### Verify
- Open: http://localhost:8080/api/health
- Expected: OK

### Stop
```bash
docker compose down
```