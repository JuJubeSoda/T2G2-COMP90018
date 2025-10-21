#!/bin/bash

echo "🚀 Starting Plant World Backend Service..."

# Stop existing containers
echo "🛑 Stopping existing containers..."
docker-compose down

# Build and start services
echo "🔨 Building and starting backend service..."
docker-compose up --build -d

echo ""
echo "✅ Backend service started successfully!"
echo "📱 API Address: http://localhost:9999"
echo ""
echo "📚 API Documentation: http://localhost:9999/swagger-ui/index.html"
echo ""
echo "🧪 Test Commands:"
echo "curl http://localhost:9999/health"
echo "curl http://localhost:9999/health/db"
echo ""
echo "🔍 View logs: docker-compose logs -f plant-backend"
echo "🛑 Stop service: docker-compose down"
