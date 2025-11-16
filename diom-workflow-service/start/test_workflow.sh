#!/bin/bash

echo "======================================"
echo "   工作流服务功能测试"
echo "======================================"
echo ""

BASE_URL="http://localhost:8083"

# 1. 测试流程定义列表
echo "【测试1】获取流程定义列表..."
curl -s "$BASE_URL/workflow/definitions" | python3 -m json.tool
echo ""

# 2. 启动流程实例
echo "【测试2】启动流程实例..."
INSTANCE=$(curl -s -X POST "$BASE_URL/workflow/start/simple-process" \
  -H "Content-Type: application/json" \
  -d '{"applicant": "张三", "amount": 5000}')
echo "$INSTANCE" | python3 -m json.tool
INSTANCE_ID=$(echo "$INSTANCE" | python3 -c "import sys, json; print(json.load(sys.stdin)['data']['id'])" 2>/dev/null)
echo "流程实例ID: $INSTANCE_ID"
echo ""

# 3. 查询流程实例
if [ -n "$INSTANCE_ID" ]; then
  echo "【测试3】查询流程实例..."
  curl -s "$BASE_URL/workflow/instance/$INSTANCE_ID" | python3 -m json.tool
  echo ""
fi

# 4. 查询任务列表
echo "【测试4】查询任务列表（assignee=admin）..."
curl -s "$BASE_URL/workflow/tasks?assignee=admin" | python3 -m json.tool
echo ""

# 5. 检查数据持久化
echo "【测试5】数据持久化检查..."
if [ -d "../camunda-data" ]; then
  ls -lh ../camunda-data/
  echo "✅ 数据已持久化到磁盘"
else
  echo "⚠️  数据目录未找到"
fi
echo ""

echo "======================================"
echo "   测试完成！"
echo "======================================"
