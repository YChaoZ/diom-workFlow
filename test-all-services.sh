#!/bin/bash

# 🧪 全服务测试脚本
# 用于验证 Flowable、Gateway、前端是否正常运行

echo "======================================"
echo "🧪 开始测试所有服务..."
echo "======================================"
echo ""

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 测试函数
test_service() {
    local name=$1
    local url=$2
    local expected=$3
    
    echo -n "测试 $name ... "
    
    response=$(curl -s -w "\n%{http_code}" "$url" 2>/dev/null)
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | head -n-1)
    
    if [ "$http_code" = "200" ]; then
        echo -e "${GREEN}✅ 成功${NC} (HTTP $http_code)"
        if [ -n "$expected" ]; then
            if echo "$body" | grep -q "$expected"; then
                echo -e "   ${GREEN}✓${NC} 响应内容包含: $expected"
            else
                echo -e "   ${YELLOW}⚠${NC} 响应内容不包含: $expected"
            fi
        fi
    else
        echo -e "${RED}❌ 失败${NC} (HTTP $http_code)"
        return 1
    fi
    echo ""
}

# 1. 测试 MySQL
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "1️⃣  MySQL 数据库"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
if docker ps | grep -q "diom-mysql"; then
    echo -e "${GREEN}✅ MySQL 容器运行中${NC}"
    
    # 检查表数量
    table_count=$(docker exec diom-mysql mysql -uroot -p1qaz2wsx diom_flowable -se "SHOW TABLES;" 2>/dev/null | wc -l | tr -d ' ')
    echo -e "   ${GREEN}✓${NC} diom_flowable 数据库有 $table_count 张表"
else
    echo -e "${RED}❌ MySQL 容器未运行${NC}"
fi
echo ""

# 2. 测试 Nacos
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "2️⃣  Nacos 服务注册中心"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
test_service "Nacos" "http://localhost:8848/nacos" "nacos"

# 3. 测试 Flowable 服务
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "3️⃣  Flowable 工作流服务"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
test_service "Flowable 健康检查" "http://localhost:8086/actuator/health" "UP"
test_service "Flowable 流程定义" "http://localhost:8086/workflow/definitions" ""

# 检查 Nacos 注册
echo -n "检查 Flowable 服务 Nacos 注册 ... "
if curl -s "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=diom-flowable-service" | grep -q "diom-flowable-service"; then
    echo -e "${GREEN}✅ 已注册${NC}"
else
    echo -e "${RED}❌ 未注册${NC}"
fi
echo ""

# 4. 测试 Gateway
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "4️⃣  Gateway 网关服务"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
test_service "Gateway 健康检查" "http://localhost:8080/actuator/health" "UP"

# 测试 Gateway 路由到 Flowable
echo -n "测试 Gateway → Flowable 路由 ... "
response=$(curl -s -w "\n%{http_code}" "http://localhost:8080/workflow/definitions" 2>/dev/null)
http_code=$(echo "$response" | tail -n1)
if [ "$http_code" = "200" ] || [ "$http_code" = "401" ]; then
    echo -e "${GREEN}✅ 路由正常${NC} (HTTP $http_code)"
else
    echo -e "${RED}❌ 路由失败${NC} (HTTP $http_code)"
fi
echo ""

# 5. 测试前端
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "5️⃣  前端服务"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
test_service "前端首页" "http://localhost:3000" ""

# 总结
echo "======================================"
echo "✅ 测试完成！"
echo "======================================"
echo ""
echo "📝 如果所有服务都通过测试，可以："
echo ""
echo "   1. 浏览器访问前端测试页面："
echo "      http://localhost:3000/workflow/flyflow-test"
echo ""
echo "   2. 浏览器访问 Nacos 控制台："
echo "      http://localhost:8848/nacos (nacos/nacos)"
echo ""
echo "   3. 测试完整的工作流："
echo "      - 查看流程列表"
echo "      - 启动流程实例"
echo "      - 处理待办任务"
echo ""
echo "======================================"

