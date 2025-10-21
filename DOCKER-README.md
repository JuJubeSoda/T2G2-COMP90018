# Plant World Backend - Docker Compose

## 🚀 Quick Start

### 1. Start Services
```bash
./start-backend.sh
```

### 2. Manual Commands
```bash
# Build and start
docker-compose up --build -d

# View logs
docker-compose logs -f plant-backend

# Stop services
docker-compose down
```

## 📱 API Endpoints

- **Health Check**: http://localhost:9999/health
- **Database Check**: http://localhost:9999/health/db
- **API Documentation**: http://localhost:9999/swagger-ui/index.html
- **User Registration**: POST http://localhost:9999/user/reg
- **User Login**: POST http://localhost:9999/user/login
- **Plant AI**: GET http://localhost:9999/api/plant-ai/ask

## 🧪 Test Commands

```bash
# Health check
curl http://localhost:9999/health

# Database connection test
curl http://localhost:9999/health/db

# Plant AI test
curl "http://localhost:9999/api/plant-ai/ask?question=What%20plants%20should%20I%20grow?"
```

## 🔧 Configuration

- **Port**: 9999
- **Timezone**: Australia/Melbourne
- **Restart Policy**: unless-stopped
- **Network**: plant-network
