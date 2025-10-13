#!/bin/bash

BASE_URL="http://localhost:9999"

echo "🧪 测试 Plant World Backend API..."
echo "=================================="

# 测试健康检查
echo ""
echo "1️⃣ 测试应用健康状态:"
curl -s "$BASE_URL/health" | python3 -m json.tool

# 测试数据库连接
echo ""
echo "2️⃣ 测试数据库连接:"
curl -s "$BASE_URL/health/db" | python3 -m json.tool

# 测试完整系统状态
echo ""
echo "3️⃣ 测试完整系统状态:"
curl -s "$BASE_URL/health/status" | python3 -m json.tool

# 测试数据库连接详情
echo ""
echo "4️⃣ 测试数据库连接详情:"
curl -s "$BASE_URL/test/db-connection" | python3 -m json.tool

# 测试数据库查询
echo ""
echo "5️⃣ 测试数据库查询:"
curl -s "$BASE_URL/test/db-query" | python3 -m json.tool

# 测试数据库信息
echo ""
echo "6️⃣ 测试数据库信息:"
curl -s "$BASE_URL/test/db-info" | python3 -m json.tool

echo ""
echo "✅ 测试完成！"
echo ""
echo "📖 API文档: $BASE_URL/swagger-ui/index.html"
