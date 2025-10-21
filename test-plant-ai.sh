#!/bin/bash

BASE_URL="https://mobile.kevinauhome.com"

echo "🌱 植物AI功能测试..."
echo "=================================="

# 测试1: 植物相关问答
echo ""
echo "1️⃣ 植物相关问答测试:"
curl -X GET "$BASE_URL/api/plant-ai/ask?question=西红柿什么时候种植最好" \
  -H "Content-Type: application/json" \
  -w "\nHTTP Status: %{http_code}\n"

# 测试2: 非植物相关问答（应该被限制）
echo ""
echo "2️⃣ 非植物相关问答测试（应该被限制）:"
curl -X GET "$BASE_URL/api/plant-ai/ask?question=今天天气怎么样" \
  -H "Content-Type: application/json" \
  -w "\nHTTP Status: %{http_code}\n"

# 测试3: 获取种植建议
echo ""
echo "3️⃣ 种植建议测试:"
curl -X POST "$BASE_URL/api/plant-ai/recommendations" \
  -H "Content-Type: application/json" \
  -d '{
    "location": "北京",
    "sensorData": {
      "temperature": "20°C",
      "humidity": "50%",
      "lightLevel": "充足",
      "soilMoisture": "适中",
      "phLevel": "6.8"
    }
  }' \
  -w "\nHTTP Status: %{http_code}\n"

# 测试4: 植物养护建议
echo ""
echo "4️⃣ 植物养护建议测试:"
curl -X POST "$BASE_URL/api/plant-ai/care-advice" \
  -H "Content-Type: application/json" \
  -d '{
    "plantName": "西红柿",
    "currentConditions": {
      "temperature": "25°C",
      "humidity": "60%",
      "soilMoisture": "干燥",
      "growthStage": "开花期"
    }
  }' \
  -w "\nHTTP Status: %{http_code}\n"

# 测试5: 传感器数据示例
echo ""
echo "5️⃣ 传感器数据示例:"
curl -X GET "$BASE_URL/api/plant-ai/sensor-example" \
  -H "Content-Type: application/json" \
  -w "\nHTTP Status: %{http_code}\n"

echo ""
echo "✅ 植物AI功能测试完成！"
echo ""
echo "📝 Postman测试用例："
echo "1. 植物问答: GET $BASE_URL/api/plant-ai/ask?question=你的问题"
echo "2. 种植建议: POST $BASE_URL/api/plant-ai/recommendations"
echo "3. 养护建议: POST $BASE_URL/api/plant-ai/care-advice"
echo "4. 植物识别: POST $BASE_URL/api/plant-ai/identify (需要上传图片)"
