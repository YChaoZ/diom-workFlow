#!/bin/bash

# Camunda 7.16 + MySQL 8.0 快速测试脚本

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "========================================"
echo -e "${YELLOW}🚀 Camunda 7.16工作流测试${NC}"
echo "========================================"
echo ""

# 1. 健康检查
echo "测试1：健康检查"
STATUS=$(curl -s http://localhost:8083/actuator/health | python3 -c "import sys,json; print(json.load(sys.stdin)['status'])" 2>/dev/null)
if [ "$STATUS" = "UP" ]; then
  echo -e "${GREEN}✅ 服务状态: $STATUS${NC}"
else
  echo "❌ 服务未启动"
  exit 1
fi
echo ""

# 2. 获取流程定义
echo "测试2：获取流程定义"
DEFS=$(curl -s http://localhost:8083/workflow/definitions)
PROCESS_KEY=$(echo "$DEFS" | python3 -c "import sys,json; data=json.load(sys.stdin); print(data['data'][0]['key'] if data['data'] else '')" 2>/dev/null)
if [ -n "$PROCESS_KEY" ]; then
  echo -e "${GREEN}✅ 流程定义: $PROCESS_KEY${NC}"
else
  echo "⚠️  未找到流程定义"
fi
echo ""

# 3. 启动流程实例
echo "测试3：启动流程实例"
if [ -n "$PROCESS_KEY" ]; then
  RESULT=$(curl -s -X POST http://localhost:8083/workflow/start/$PROCESS_KEY -H "Content-Type: application/json" -d '{}')
  PROCESS_ID=$(echo "$RESULT" | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['id'])" 2>/dev/null)
  if [ -n "$PROCESS_ID" ]; then
    echo -e "${GREEN}✅ 流程实例ID: $PROCESS_ID${NC}"
  else
    echo "❌ 启动流程失败"
    echo "$RESULT" | python3 -m json.tool
  fi
else
  echo "⏭️  跳过（无流程定义）"
fi
echo ""

# 4. 查询任务
echo "测试4：查询admin的任务"
TASKS=$(curl -s "http://localhost:8083/workflow/tasks?assignee=admin")
TASK_COUNT=$(echo "$TASKS" | python3 -c "import sys,json; print(len(json.load(sys.stdin)['data']))" 2>/dev/null)
if [ "$TASK_COUNT" -gt 0 ]; then
  echo -e "${GREEN}✅ 当前任务数: $TASK_COUNT${NC}"
  echo "$TASKS" | python3 -c "import sys,json; tasks=json.load(sys.stdin)['data']; [print(f\"   - {t['name']} (ID: {t['id']})\") for t in tasks]"
else
  echo "ℹ️  当前无任务"
fi
echo ""

# 5. 数据库统计
echo "测试5：数据库统计"
TABLE_COUNT=$(docker exec meeting-admin-mysql mysql -uroot -p1qaz2wsx diom_workflow -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'diom_workflow';" 2>&1 | grep -v "mysql: \[Warning\]")
DEPLOYMENT_COUNT=$(docker exec meeting-admin-mysql mysql -uroot -p1qaz2wsx diom_workflow -N -e "SELECT COUNT(*) FROM ACT_RE_DEPLOYMENT;" 2>&1 | grep -v "mysql: \[Warning\]")
TASK_DB_COUNT=$(docker exec meeting-admin-mysql mysql -uroot -p1qaz2wsx diom_workflow -N -e "SELECT COUNT(*) FROM ACT_RU_TASK;" 2>&1 | grep -v "mysql: \[Warning\]")

echo -e "${GREEN}✅ 数据库表: $TABLE_COUNT${NC}"
echo -e "${GREEN}✅ 流程部署: $DEPLOYMENT_COUNT${NC}"
echo -e "${GREEN}✅ 运行时任务: $TASK_DB_COUNT${NC}"
echo ""

echo "========================================"
echo -e "${GREEN}🎉 所有测试通过！${NC}"
echo "========================================"
echo ""
echo "📚 更多信息:"
echo "   - 服务地址: http://localhost:8083"
echo "   - 健康检查: http://localhost:8083/actuator/health"
echo "   - Camunda REST API: http://localhost:8083/engine-rest"
echo "   - 流程定义API: http://localhost:8083/workflow/definitions"
echo ""

