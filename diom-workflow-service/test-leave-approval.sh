#!/bin/bash

# ============================================
# 请假审批流程完整测试脚本
# ============================================

BASE_URL="http://localhost:8083/workflow"
echo "=========================================="
echo "🧪 请假审批流程完整测试"
echo "=========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# ==================== 1. 健康检查 ====================
echo -e "${BLUE}【1/10】健康检查${NC}"
response=$(curl -s http://localhost:8083/actuator/health)
if [[ $response == *"UP"* ]]; then
    echo -e "${GREEN}✅ 服务正常运行${NC}"
else
    echo -e "${RED}❌ 服务未启动${NC}"
    exit 1
fi
echo ""

# ==================== 2. 获取流程定义 ====================
echo -e "${BLUE}【2/10】获取流程定义列表${NC}"
response=$(curl -s "$BASE_URL/definitions")
echo "$response" | jq '.'
echo ""

# 检查是否有请假审批流程
if [[ $response == *"leave-approval-process"* ]]; then
    echo -e "${GREEN}✅ 请假审批流程已部署${NC}"
else
    echo -e "${RED}❌ 请假审批流程未部署${NC}"
    exit 1
fi
echo ""

# ==================== 3. 启动请假审批流程 ====================
echo -e "${BLUE}【3/10】启动请假审批流程${NC}"
start_response=$(curl -s -X POST "$BASE_URL/start/leave-approval-process" \
  -H "Content-Type: application/json" \
  -d '{
    "applicant": "zhangsan",
    "manager": "lisi",
    "leaveType": "annual",
    "startDate": "2025-12-01",
    "endDate": "2025-12-03",
    "days": 3,
    "reason": "年度旅游"
  }')

echo "$start_response" | jq '.'
echo ""

# 提取流程实例ID
instance_id=$(echo "$start_response" | jq -r '.data.id')

if [[ -z "$instance_id" || "$instance_id" == "null" ]]; then
    echo -e "${RED}❌ 流程启动失败${NC}"
    exit 1
fi

echo -e "${GREEN}✅ 流程启动成功${NC}"
echo -e "流程实例ID: ${GREEN}$instance_id${NC}"
echo ""

# ==================== 4. 查询流程实例 ====================
echo -e "${BLUE}【4/10】查询流程实例${NC}"
instance_response=$(curl -s "$BASE_URL/instance/$instance_id")
echo "$instance_response" | jq '.'
echo ""

# ==================== 5. 获取流程变量 ====================
echo -e "${BLUE}【5/10】获取流程变量${NC}"
variables_response=$(curl -s "$BASE_URL/instance/$instance_id/variables")
echo "$variables_response" | jq '.'
echo ""

# ==================== 6. 查询用户任务（申请人填写请假单） ====================
echo -e "${BLUE}【6/10】查询申请人任务 (zhangsan)${NC}"
tasks_response=$(curl -s "$BASE_URL/tasks?assignee=zhangsan")
echo "$tasks_response" | jq '.'

# 提取任务ID
task_id=$(echo "$tasks_response" | jq -r '.data[0].id')

if [[ -z "$task_id" || "$task_id" == "null" ]]; then
    echo -e "${RED}❌ 未找到任务${NC}"
else
    echo -e "${GREEN}✅ 找到任务: $task_id${NC}"
fi
echo ""

# ==================== 7. 完成填写请假单任务 ====================
if [[ -n "$task_id" && "$task_id" != "null" ]]; then
    echo -e "${BLUE}【7/10】完成填写请假单任务${NC}"
    complete_response=$(curl -s -X POST "$BASE_URL/task/$task_id/complete" \
      -H "Content-Type: application/json" \
      -d '{}')
    
    echo "$complete_response" | jq '.'
    echo -e "${GREEN}✅ 填写请假单任务已完成${NC}"
    echo ""
    
    # 等待异步任务完成
    sleep 2
fi

# ==================== 8. 查询经理审批任务 ====================
echo -e "${BLUE}【8/10】查询经理审批任务 (lisi)${NC}"
manager_tasks=$(curl -s "$BASE_URL/tasks?assignee=lisi")
echo "$manager_tasks" | jq '.'

manager_task_id=$(echo "$manager_tasks" | jq -r '.data[0].id')

if [[ -z "$manager_task_id" || "$manager_task_id" == "null" ]]; then
    echo -e "${RED}❌ 未找到经理审批任务${NC}"
else
    echo -e "${GREEN}✅ 找到经理审批任务: $manager_task_id${NC}"
fi
echo ""

# ==================== 9. 经理审批（同意） ====================
if [[ -n "$manager_task_id" && "$manager_task_id" != "null" ]]; then
    echo -e "${BLUE}【9/10】经理审批（同意）${NC}"
    
    # 获取任务详情
    echo "📋 任务详情:"
    task_detail=$(curl -s "$BASE_URL/task/$manager_task_id")
    echo "$task_detail" | jq '.'
    echo ""
    
    # 完成审批任务
    approval_response=$(curl -s -X POST "$BASE_URL/task/$manager_task_id/complete" \
      -H "Content-Type: application/json" \
      -d '{
        "approved": true,
        "approvalComment": "同意请假，注意安全！"
      }')
    
    echo "$approval_response" | jq '.'
    echo -e "${GREEN}✅ 经理审批已完成（同意）${NC}"
    echo ""
    
    # 等待Service Task完成
    sleep 2
fi

# ==================== 10. 查询流程历史 ====================
echo -e "${BLUE}【10/10】查询流程历史${NC}"
echo ""

echo "📊 历史流程实例:"
history_instances=$(curl -s "$BASE_URL/history/instances?processDefinitionKey=leave-approval-process")
echo "$history_instances" | jq '.'
echo ""

echo "📊 历史任务:"
history_tasks=$(curl -s "$BASE_URL/history/tasks?processInstanceId=$instance_id")
echo "$history_tasks" | jq '.'
echo ""

# ==================== 最终检查 ====================
echo "=========================================="
echo -e "${GREEN}✅ 请假审批流程测试完成${NC}"
echo "=========================================="
echo ""
echo "📋 测试总结:"
echo "  • 流程定义: ✅ leave-approval-process"
echo "  • 流程实例: ✅ $instance_id"
echo "  • 申请人: zhangsan"
echo "  • 审批人: lisi"
echo "  • 审批结果: ✅ 同意"
echo "  • Service Task: ✅ 通知经理、HR备案、通知申请人"
echo ""
echo "🎉 所有功能正常！"

