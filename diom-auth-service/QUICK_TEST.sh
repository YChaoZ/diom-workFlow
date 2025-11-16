#!/bin/bash

# diom-auth-service 快速测试脚本
# 使用方法: bash QUICK_TEST.sh

echo "================================"
echo "  diom-auth-service 快速测试"
echo "================================"
echo ""

# 检查服务是否运行
echo "1. 检查服务健康状态..."
response=$(curl -s http://localhost:8081/health)
if [ $? -eq 0 ]; then
    echo "✅ 服务运行正常"
    echo "   响应: $response"
else
    echo "❌ 服务未运行，请先启动服务"
    echo "   启动命令: mvn spring-boot:run"
    exit 1
fi

echo ""
echo "2. 测试用户登录..."
login_response=$(curl -s -X POST http://localhost:8081/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}')

if echo "$login_response" | grep -q "token"; then
    echo "✅ 登录成功"
    # 提取 token
    token=$(echo $login_response | grep -o '"token":"[^"]*' | cut -d'"' -f4)
    echo "   Token: ${token:0:50}..."
else
    echo "❌ 登录失败"
    echo "   响应: $login_response"
    exit 1
fi

echo ""
echo "3. 测试 Token 验证..."
validate_response=$(curl -s http://localhost:8081/validate \
  -H "Authorization: Bearer $token")

if echo "$validate_response" | grep -q "userId"; then
    echo "✅ Token 验证成功"
    echo "   响应: $validate_response"
else
    echo "❌ Token 验证失败"
    echo "   响应: $validate_response"
    exit 1
fi

echo ""
echo "4. 测试 Token 刷新..."
refresh_response=$(curl -s -X POST http://localhost:8081/refresh \
  -H "Authorization: Bearer $token")

if echo "$refresh_response" | grep -q "token"; then
    echo "✅ Token 刷新成功"
    new_token=$(echo $refresh_response | grep -o '"token":"[^"]*' | cut -d'"' -f4)
    echo "   新 Token: ${new_token:0:50}..."
else
    echo "❌ Token 刷新失败"
    echo "   响应: $refresh_response"
    exit 1
fi

echo ""
echo "5. 测试用户注册（随机用户名）..."
random_user="testuser_$(date +%s)"
register_response=$(curl -s -X POST http://localhost:8081/register \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$random_user\",\"password\":\"123456\",\"nickname\":\"测试用户\"}")

if echo "$register_response" | grep -q "注册成功"; then
    echo "✅ 用户注册成功"
    echo "   用户名: $random_user"
else
    echo "⚠️  用户注册测试（用户可能已存在）"
    echo "   响应: $register_response"
fi

echo ""
echo "================================"
echo "  ✅ 所有核心功能测试通过！"
echo "================================"
echo ""
echo "详细测试请参考: API_TEST.md"

