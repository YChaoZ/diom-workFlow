#!/bin/bash

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}   diom-gateway 网关功能测试${NC}"
echo -e "${BLUE}========================================${NC}"

# 1. 健康检查
echo -e "\n${YELLOW}📋 1. 网关健康检查${NC}"
HEALTH=$(curl -s http://localhost:8080/actuator/health)
STATUS=$(echo "$HEALTH" | python3 -c "import sys,json; print(json.load(sys.stdin)['status'])" 2>/dev/null)
if [ "$STATUS" = "UP" ]; then
    echo -e "${GREEN}✅ 网关状态: $STATUS${NC}"
    echo "$HEALTH" | python3 -c "import sys,json; d=json.load(sys.stdin); services=d['components']['discoveryComposite']['components']['discoveryClient']['details']['services']; print(f'   已发现服务: {', '.join(services)}')" 2>/dev/null
else
    echo -e "${RED}❌ 网关状态异常${NC}"
    exit 1
fi

# 2. Nacos 注册检查
echo -e "\n${YELLOW}📋 2. 检查服务注册到 Nacos${NC}"
NACOS_SERVICE=$(curl -s "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=diom-gateway" 2>/dev/null)
HOSTS=$(echo "$NACOS_SERVICE" | python3 -c "import sys,json; print(len(json.load(sys.stdin).get('hosts', [])))" 2>/dev/null)
if [ "$HOSTS" -gt 0 ]; then
    echo -e "${GREEN}✅ 网关已注册到 Nacos，实例数: $HOSTS${NC}"
else
    echo -e "${RED}❌ 网关未注册到 Nacos${NC}"
fi

# 3. 测试白名单路径（无需认证）
echo -e "\n${YELLOW}📋 3. 测试白名单路径（无需 Token）${NC}"
echo -e "${BLUE}   3.1 通过网关访问登录接口${NC}"
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}')
CODE=$(echo "$LOGIN_RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin).get('code', 0))" 2>/dev/null)
if [ "$CODE" = "200" ]; then
    echo -e "${GREEN}✅ 登录成功（通过网关）${NC}"
    TOKEN=$(echo "$LOGIN_RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['token'])" 2>/dev/null)
    echo -e "   Token: ${TOKEN:0:50}..."
else
    echo -e "${RED}❌ 登录失败${NC}"
    echo "$LOGIN_RESPONSE" | python3 -m json.tool 2>/dev/null
    exit 1
fi

# 4. 测试未携带 Token 访问受保护接口
echo -e "\n${YELLOW}📋 4. 测试未携带 Token 访问受保护接口${NC}"
STATUS_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/auth/validate)
if [ "$STATUS_CODE" = "401" ]; then
    echo -e "${GREEN}✅ 正确返回 401 Unauthorized${NC}"
else
    echo -e "${RED}❌ 应该返回 401，实际返回: $STATUS_CODE${NC}"
fi

# 5. 测试携带 Token 访问受保护接口
echo -e "\n${YELLOW}📋 5. 测试携带 Token 访问受保护接口${NC}"
echo -e "${BLUE}   5.1 通过网关验证 Token${NC}"
VALIDATE_RESPONSE=$(curl -s -X GET http://localhost:8080/auth/validate \
  -H "Authorization: Bearer $TOKEN")
CODE=$(echo "$VALIDATE_RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin).get('code', 0))" 2>/dev/null)
if [ "$CODE" = "200" ]; then
    echo -e "${GREEN}✅ Token 验证成功（通过网关）${NC}"
    echo "$VALIDATE_RESPONSE" | python3 -c "import sys,json; d=json.load(sys.stdin); print(f'   用户ID: {d[\"data\"][\"userId\"]}')" 2>/dev/null
else
    echo -e "${RED}❌ Token 验证失败${NC}"
fi

echo -e "${BLUE}   5.2 通过网关刷新 Token${NC}"
REFRESH_RESPONSE=$(curl -s -X POST http://localhost:8080/auth/refresh \
  -H "Authorization: Bearer $TOKEN")
CODE=$(echo "$REFRESH_RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin).get('code', 0))" 2>/dev/null)
if [ "$CODE" = "200" ]; then
    echo -e "${GREEN}✅ Token 刷新成功（通过网关）${NC}"
    NEW_TOKEN=$(echo "$REFRESH_RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['token'][:50])" 2>/dev/null)
    echo -e "   新Token: ${NEW_TOKEN}..."
else
    echo -e "${RED}❌ Token 刷新失败${NC}"
fi

# 6. 测试注册功能
echo -e "\n${YELLOW}📋 6. 测试通过网关注册新用户${NC}"
TIMESTAMP=$(date +%s)
REGISTER_RESPONSE=$(curl -s -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"gateway_test_${TIMESTAMP}\",\"password\":\"123456\",\"nickname\":\"网关测试用户\",\"email\":\"gateway_${TIMESTAMP}@test.com\"}")
CODE=$(echo "$REGISTER_RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin).get('code', 0))" 2>/dev/null)
if [ "$CODE" = "200" ]; then
    echo -e "${GREEN}✅ 用户注册成功（通过网关）${NC}"
else
    echo -e "${YELLOW}⚠️  注册返回码: $CODE（可能是用户已存在）${NC}"
fi

# 7. 检查网关路由信息
echo -e "\n${YELLOW}📋 7. 检查网关路由配置${NC}"
ROUTES=$(curl -s http://localhost:8080/actuator/gateway/routes 2>/dev/null)
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ 成功获取路由信息${NC}"
    echo "$ROUTES" | python3 -c "import sys,json; routes=json.load(sys.stdin); print(f'   配置的路由数量: {len(routes)}'); [print(f'   - {r[\"route_id\"]}: {r[\"uri\"]}') for r in routes]" 2>/dev/null || echo "   (路由信息详见 actuator)"
else
    echo -e "${YELLOW}⚠️  无法获取路由信息（可能未启用 gateway endpoint）${NC}"
fi

# 总结
echo -e "\n${BLUE}========================================${NC}"
echo -e "${GREEN}   🎉 网关测试完成！${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "\n${YELLOW}网关配置摘要：${NC}"
echo -e "  - 网关地址: ${BLUE}http://localhost:8080${NC}"
echo -e "  - 认证服务路由: ${BLUE}/auth/**${NC} -> diom-auth-service"
echo -e "  - Web服务路由: ${BLUE}/api/**${NC} -> diom-web-service"
echo -e "  - 工作流服务路由: ${BLUE}/workflow/**${NC} -> diom-workflow-service"
echo -e "  - JWT 认证: ${GREEN}已启用${NC}"
echo -e "  - 服务发现: ${GREEN}已启用（Nacos）${NC}"
echo -e "  - 跨域配置: ${GREEN}已启用${NC}"

