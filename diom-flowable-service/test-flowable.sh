#!/bin/bash

# ===================================
# Flowable 工作流服务测试脚本
# ===================================

echo "======================================"
echo "  Flowable 工作流服务测试"
echo "======================================"
echo ""

BASE_URL="http://localhost:8086"

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 测试计数
PASS=0
FAIL=0

# 测试函数
test_api() {
    local name=$1
    local method=$2
    local url=$3
    local data=$4
    
    echo -n "测试: $name ... "
    
    if [ "$method" == "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" "$url")
    else
        response=$(curl -s -w "\n%{http_code}" -X "$method" -H "Content-Type: application/json" -d "$data" "$url")
    fi
    
    http_code=$(echo "$response" | tail -n 1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
        echo -e "${GREEN}✓ PASS${NC} (HTTP $http_code)"
        PASS=$((PASS + 1))
        return 0
    else
        echo -e "${RED}✗ FAIL${NC} (HTTP $http_code)"
        echo "Response: $body"
        FAIL=$((FAIL + 1))
        return 1
    fi
}

echo "1. 健康检查"
echo "-----------------------------------"
test_api "服务健康检查" "GET" "$BASE_URL/actuator/health"
echo ""

echo "2. 流程定义管理"
echo "-----------------------------------"
test_api "获取流程定义列表" "GET" "$BASE_URL/workflow/definitions"
test_api "获取指定流程定义" "GET" "$BASE_URL/workflow/definition/leave-approval-process"
echo ""

echo "3. 流程实例管理"
echo "-----------------------------------"
test_data='{
  "applicant": "测试用户",
  "reason": "测试请假",
  "days": 3
}'
test_api "启动流程实例" "POST" "$BASE_URL/workflow/start/leave-approval-process" "$test_data"
echo ""

echo "4. 任务管理"
echo "-----------------------------------"
test_api "查询任务列表" "GET" "$BASE_URL/workflow/tasks?assignee=admin"
echo ""

echo "5. 流程设计器 API"
echo "-----------------------------------"
test_api "获取流程设计列表" "GET" "$BASE_URL/workflow/api/process-design/list?page=1&size=10"
echo ""

# 汇总
echo "======================================"
echo "  测试结果汇总"
echo "======================================"
echo -e "通过: ${GREEN}$PASS${NC}"
echo -e "失败: ${RED}$FAIL${NC}"
echo "总计: $((PASS + FAIL))"
echo ""

if [ $FAIL -eq 0 ]; then
    echo -e "${GREEN}✓ 所有测试通过！${NC}"
    exit 0
else
    echo -e "${RED}✗ 部分测试失败${NC}"
    exit 1
fi

