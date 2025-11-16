#!/bin/bash

echo "================================"
echo "  diom-auth-service 启动测试"
echo "================================"
echo ""

# 检查数据库连接
echo "步骤 1: 检查数据库连接..."
mysql -h localhost -P 3306 -u root -p1qaz2wsx -e "USE diom_workflow; SELECT COUNT(*) as user_count FROM sys_user;" 2>/dev/null

if [ $? -eq 0 ]; then
    echo "✅ 数据库连接成功"
else
    echo "❌ 数据库连接失败，请检查："
    echo "   - MySQL 是否运行（docker ps）"
    echo "   - 端口 3306 是否映射"
    echo "   - 密码是否正确"
    exit 1
fi

echo ""
echo "步骤 2: 编译项目..."
cd "$(dirname "$0")"
mvn clean package -DskipTests

if [ $? -eq 0 ]; then
    echo "✅ 编译成功"
else
    echo "❌ 编译失败"
    exit 1
fi

echo ""
echo "步骤 3: 启动服务（后台运行）..."
echo "   启动中，请稍候..."

# 启动服务到后台
nohup mvn spring-boot:run > logs/startup.log 2>&1 &
SERVICE_PID=$!

echo "   服务 PID: $SERVICE_PID"
echo "   日志位置: logs/startup.log"

# 等待服务启动（最多等待 60 秒）
echo ""
echo "步骤 4: 等待服务启动..."
MAX_WAIT=60
ELAPSED=0

while [ $ELAPSED -lt $MAX_WAIT ]; do
    sleep 2
    ELAPSED=$((ELAPSED + 2))
    
    response=$(curl -s http://localhost:8081/health 2>/dev/null)
    if [ $? -eq 0 ] && echo "$response" | grep -q "running"; then
        echo "✅ 服务启动成功！（耗时 ${ELAPSED}秒）"
        break
    fi
    
    echo -n "."
done

if [ $ELAPSED -ge $MAX_WAIT ]; then
    echo ""
    echo "❌ 服务启动超时"
    echo "   查看日志: tail -f logs/startup.log"
    exit 1
fi

echo ""
echo "步骤 5: 运行功能测试..."
echo ""

# 测试健康检查
echo "测试 1: 健康检查"
curl -s http://localhost:8081/health | jq '.' || echo "响应: $(curl -s http://localhost:8081/health)"

echo ""
echo "测试 2: 用户登录（admin）"
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8081/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}')

echo "$LOGIN_RESPONSE" | jq '.' 2>/dev/null || echo "$LOGIN_RESPONSE"

if echo "$LOGIN_RESPONSE" | grep -q "token"; then
    echo "✅ 登录成功"
    TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)
    
    echo ""
    echo "测试 3: Token 验证"
    VALIDATE_RESPONSE=$(curl -s http://localhost:8081/validate \
      -H "Authorization: Bearer $TOKEN")
    echo "$VALIDATE_RESPONSE" | jq '.' 2>/dev/null || echo "$VALIDATE_RESPONSE"
    
    if echo "$VALIDATE_RESPONSE" | grep -q "userId"; then
        echo "✅ Token 验证成功"
    else
        echo "❌ Token 验证失败"
    fi
    
    echo ""
    echo "测试 4: Token 刷新"
    REFRESH_RESPONSE=$(curl -s -X POST http://localhost:8081/refresh \
      -H "Authorization: Bearer $TOKEN")
    echo "$REFRESH_RESPONSE" | jq '.' 2>/dev/null || echo "$REFRESH_RESPONSE"
    
    if echo "$REFRESH_RESPONSE" | grep -q "token"; then
        echo "✅ Token 刷新成功"
    else
        echo "❌ Token 刷新失败"
    fi
else
    echo "❌ 登录失败，可能需要重新生成密码"
    echo ""
    echo "请运行以下命令生成正确的密码："
    echo "  cd diom-auth-service"
    echo "  mvn test -Dtest=PasswordEncoderTest#generatePassword"
fi

echo ""
echo "================================"
echo "  测试完成！"
echo "================================"
echo ""
echo "服务信息："
echo "  - PID: $SERVICE_PID"
echo "  - 端口: 8081"
echo "  - 日志: tail -f logs/startup.log"
echo ""
echo "停止服务："
echo "  kill $SERVICE_PID"
echo ""

