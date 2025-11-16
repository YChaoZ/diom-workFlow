#!/bin/bash

echo "========================================="
echo "  diom-auth-service 一键测试"
echo "========================================="
echo ""

cd "$(dirname "$0")"

# 步骤 1: 更新密码
echo "步骤 1: 更新数据库密码..."
mysql -h localhost -P 3306 -u root -p1qaz2wsx < update_passwords.sql 2>/dev/null

if [ $? -eq 0 ]; then
    echo "✅ 密码更新成功"
else
    echo "⚠️  密码更新失败（可能已经是正确的）"
fi

echo ""
echo "步骤 2: 启动服务..."
echo "   Maven 正在启动，请稍候（约 30-60 秒）..."
echo ""

# 启动服务
mvn spring-boot:run &
SERVICE_PID=$!

echo "   服务 PID: $SERVICE_PID"
echo ""

# 等待服务启动
echo "步骤 3: 等待服务就绪..."
MAX_WAIT=90
ELAPSED=0

while [ $ELAPSED -lt $MAX_WAIT ]; do
    sleep 3
    ELAPSED=$((ELAPSED + 3))
    
    response=$(curl -s http://localhost:8081/health 2>/dev/null)
    if [ $? -eq 0 ] && echo "$response" | grep -q "running"; then
        echo "✅ 服务启动成功！（耗时 ${ELAPSED}秒）"
        echo ""
        break
    fi
    
    printf "."
done

if [ $ELAPSED -ge $MAX_WAIT ]; then
    echo ""
    echo "❌ 服务启动超时，请检查日志"
    kill $SERVICE_PID 2>/dev/null
    exit 1
fi

# 运行测试
echo "========================================="
echo "  开始功能测试"
echo "========================================="
echo ""

# 测试 1: 健康检查
echo "【测试 1】健康检查"
curl -s http://localhost:8081/health | jq '.' 2>/dev/null || curl -s http://localhost:8081/health
echo ""

# 测试 2: 登录
echo "【测试 2】用户登录（admin/123456）"
LOGIN=$(curl -s -X POST http://localhost:8081/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}')

echo "$LOGIN" | jq '.' 2>/dev/null || echo "$LOGIN"
echo ""

if echo "$LOGIN" | grep -q "token"; then
    echo "✅ 登录成功"
    TOKEN=$(echo $LOGIN | grep -o '"token":"[^"]*' | cut -d'"' -f4)
    
    # 测试 3: Token 验证
    echo ""
    echo "【测试 3】Token 验证"
    VALIDATE=$(curl -s http://localhost:8081/validate \
      -H "Authorization: Bearer $TOKEN")
    echo "$VALIDATE" | jq '.' 2>/dev/null || echo "$VALIDATE"
    
    if echo "$VALIDATE" | grep -q "userId"; then
        echo "✅ Token 验证成功"
    else
        echo "❌ Token 验证失败"
    fi
    
    # 测试 4: Token 刷新
    echo ""
    echo "【测试 4】Token 刷新"
    REFRESH=$(curl -s -X POST http://localhost:8081/refresh \
      -H "Authorization: Bearer $TOKEN")
    echo "$REFRESH" | jq '.' 2>/dev/null || echo "$REFRESH"
    
    if echo "$REFRESH" | grep -q "token"; then
        echo "✅ Token 刷新成功"
    else
        echo "❌ Token 刷新失败"
    fi
    
    # 测试 5: 用户注册
    echo ""
    echo "【测试 5】用户注册（随机用户）"
    RANDOM_USER="test_$(date +%s)"
    REGISTER=$(curl -s -X POST http://localhost:8081/register \
      -H "Content-Type: application/json" \
      -d "{\"username\":\"$RANDOM_USER\",\"password\":\"123456\",\"nickname\":\"测试用户\"}")
    echo "$REGISTER" | jq '.' 2>/dev/null || echo "$REGISTER"
    
    if echo "$REGISTER" | grep -q "注册成功"; then
        echo "✅ 用户注册成功（用户名：$RANDOM_USER）"
    fi
    
else
    echo "❌ 登录失败"
    echo ""
    echo "可能的原因："
    echo "1. 数据库密码不正确"
    echo "2. 数据库中没有 admin 用户"
    echo ""
    echo "请检查数据库："
    echo "  mysql -h localhost -P 3306 -u root -p1qaz2wsx -e \"USE diom_workflow; SELECT * FROM sys_user;\""
fi

echo ""
echo "========================================="
echo "  测试完成！"
echo "========================================="
echo ""
echo "服务正在运行："
echo "  - PID: $SERVICE_PID"
echo "  - 端口: 8081"
echo "  - 日志: 查看终端输出"
echo ""
echo "停止服务："
echo "  1. 按 Ctrl+C 停止此脚本"
echo "  2. 运行: kill $SERVICE_PID"
echo "  3. 或运行: pkill -f diom-auth-service"
echo ""
echo "保持此窗口打开以查看服务日志..."
echo "按 Ctrl+C 停止服务并退出"
echo ""

# 等待用户中断
wait $SERVICE_PID

