#!/bin/bash

echo "🚀 启动 Plant World 后端服务..."

# 停止现有容器
echo "🛑 停止现有容器..."
docker-compose down

# 构建并启动服务
echo "🔨 构建并启动后端服务..."
docker-compose up --build -d

echo ""
echo "✅ 后端服务启动完成！"
echo "📱 API地址: http://localhost:9999"
echo ""
echo "🔍 查看日志: docker-compose logs -f backend"
echo "🛑 停止服务: docker-compose down"
