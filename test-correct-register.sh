#!/bin/bash

echo "🧪 基于实际数据库结构的注册测试..."
echo "=================================="

# 测试1: 基本注册 (必需字段)
echo ""
echo "1️⃣ 基本注册测试 (username, password, email):"
curl -X POST https://mobile.kevinauhome.com/user/reg \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser001",
    "password": "testpass123",
    "email": "test001@example.com"
  }' \
  -w "\nHTTP Status: %{http_code}\n"

# 测试2: 完整注册 (包含可选字段)
echo ""
echo "2️⃣ 完整注册测试 (包含gender, introduction):"
curl -X POST https://mobile.kevinauhome.com/user/reg \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser002",
    "password": "testpass123",
    "email": "test002@example.com",
    "gender": "male",
    "introduction": "This is a test user"
  }' \
  -w "\nHTTP Status: %{http_code}\n"

# 测试3: 登录测试
echo ""
echo "3️⃣ 登录测试:"
curl -X POST https://mobile.kevinauhome.com/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser001",
    "password": "testpass123"
  }' \
  -w "\nHTTP Status: %{http_code}\n"

echo ""
echo "✅ 测试完成！"

