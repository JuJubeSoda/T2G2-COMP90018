#!/bin/bash

echo "🧪 测试注册API..."
echo "=================================="

# 测试1: 完整字段注册
echo ""
echo "1️⃣ 测试完整字段注册:"
curl -X POST https://mobile.kevinauhome.com/user/reg \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser001",
    "phone": "1234567890", 
    "password": "testpass123",
    "email": "test001@example.com"
  }' \
  -w "\nHTTP Status: %{http_code}\n"

# 测试2: 无phone字段注册
echo ""
echo "2️⃣ 测试无phone字段注册:"
curl -X POST https://mobile.kevinauhome.com/user/reg \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser002",
    "password": "testpass123",
    "email": "test002@example.com"
  }' \
  -w "\nHTTP Status: %{http_code}\n"

# 测试3: 最简字段注册
echo ""
echo "3️⃣ 测试最简字段注册:"
curl -X POST https://mobile.kevinauhome.com/user/reg \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser003",
    "password": "testpass123"
  }' \
  -w "\nHTTP Status: %{http_code}\n"

# 测试4: 登录测试
echo ""
echo "4️⃣ 测试登录API:"
curl -X POST https://mobile.kevinauhome.com/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser003",
    "password": "testpass123"
  }' \
  -w "\nHTTP Status: %{http_code}\n"

echo ""
echo "✅ 测试完成！"

