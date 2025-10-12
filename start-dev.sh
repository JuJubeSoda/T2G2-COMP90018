#!/bin/bash

echo "🚀 启动 Plant World 开发环境..."

# 检查 Docker 是否运行
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker 未运行，请先启动 Docker"
    exit 1
fi

# 停止现有容器
echo "🛑 停止现有容器..."
docker-compose -f docker-compose.dev.yml down

# 构建并启动服务
echo "🔨 构建并启动服务..."
docker-compose -f docker-compose.dev.yml up --build -d

# 等待服务启动
echo "⏳ 等待服务启动..."
sleep 10

# 检查服务状态
echo "📊 检查服务状态..."
docker-compose -f docker-compose.dev.yml ps

echo ""
echo "✅ 开发环境启动完成！"
echo "📱 后端API: http://localhost:9999"
echo "🗄️  数据库: localhost:5432"
echo "🔴 Redis: localhost:6379"
echo ""
echo "📖 API文档: http://localhost:9999/swagger-ui/index.html"
echo ""
echo "🛑 停止服务: docker-compose -f docker-compose.dev.yml down"
