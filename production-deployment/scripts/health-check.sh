#!/bin/bash
#############################################
# DIOM Workflow 健康检查脚本
#
# 功能:
# 1. 检查所有服务状态
# 2. 检查Nacos注册情况
# 3. 检查数据库连接
# 4. 生成健康报告
#
# 使用方式:
#   ./health-check.sh [环境]
#############################################

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置项（根据实际情况修改）
GATEWAY_HOST="gateway.company.com"
BACKEND_HOST="backend.company.com"
NACOS_HOST="nacos.company.com"
NACOS_PORT=8848

# 服务端口
declare -A SERVICE_PORTS
SERVICE_PORTS["gateway"]="8080"
SERVICE_PORTS["auth"]="8081"
SERVICE_PORTS["web"]="8082"
SERVICE_PORTS["workflow"]="8085"

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[✓]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[!]${NC} $1"
}

log_error() {
    echo -e "${RED}[✗]${NC} $1"
}

# 检查HTTP服务
check_http_service() {
    local name=$1
    local host=$2
    local port=$3
    local path=$4
    
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "http://${host}:${port}${path}" 2>/dev/null || echo "000")
    
    if [ "$HTTP_CODE" == "200" ]; then
        log_success "$name: 运行中 (HTTP $HTTP_CODE)"
        return 0
    else
        log_error "$name: 异常 (HTTP $HTTP_CODE)"
        return 1
    fi
}

# 检查进程
check_process() {
    local name=$1
    local host=$2
    local jar=$3
    
    PID=$(ssh "$host" "ps aux | grep $jar | grep -v grep | awk '{print \$2}'" 2>/dev/null)
    
    if [ -n "$PID" ]; then
        log_success "$name 进程: 运行中 (PID: $PID)"
        return 0
    else
        log_error "$name 进程: 未运行"
        return 1
    fi
}

# 检查Nacos服务注册
check_nacos_service() {
    local service_name=$1
    
    RESULT=$(curl -s "http://${NACOS_HOST}:${NACOS_PORT}/nacos/v1/ns/instance/list?serviceName=${service_name}" 2>/dev/null)
    
    COUNT=$(echo "$RESULT" | grep -o '"hosts":\[' | wc -l)
    
    if [ "$COUNT" -gt 0 ]; then
        INSTANCE_COUNT=$(echo "$RESULT" | grep -o '"ip":' | wc -l)
        log_success "Nacos - $service_name: 已注册 ($INSTANCE_COUNT 实例)"
        return 0
    else
        log_error "Nacos - $service_name: 未注册"
        return 1
    fi
}

# 主函数
echo ""
echo "========================================="
echo "  DIOM Workflow 健康检查"
echo "  时间: $(date '+%Y-%m-%d %H:%M:%S')"
echo "========================================="
echo ""

# 1. 检查Gateway
echo ">>> 检查 Gateway 服务"
check_http_service "Gateway 健康检查" "$GATEWAY_HOST" "8080" "/actuator/health"
check_process "Gateway" "$GATEWAY_HOST" "diom-gateway.jar"
echo ""

# 2. 检查认证服务
echo ">>> 检查 Auth Service"
check_http_service "Auth 健康检查" "$BACKEND_HOST" "8081" "/actuator/health"
check_process "Auth Service" "$BACKEND_HOST" "diom-auth-service.jar"
echo ""

# 3. 检查Web服务
echo ">>> 检查 Web Service"
check_http_service "Web 健康检查" "$BACKEND_HOST" "8082" "/actuator/health"
check_process "Web Service" "$BACKEND_HOST" "diom-web-service.jar"
echo ""

# 4. 检查工作流服务
echo ">>> 检查 Workflow Service"
check_http_service "Workflow 健康检查" "$BACKEND_HOST" "8085" "/actuator/health"
check_process "Workflow Service" "$BACKEND_HOST" "diom-workflow-service.jar"
echo ""

# 5. 检查Nacos
echo ">>> 检查 Nacos 注册中心"
check_http_service "Nacos" "$NACOS_HOST" "$NACOS_PORT" "/nacos/v1/console/health/readiness"
check_nacos_service "diom-gateway"
check_nacos_service "diom-auth-service"
check_nacos_service "diom-web-service"
check_nacos_service "diom-workflow-service"
echo ""

# 6. 检查前端服务器
echo ">>> 检查前端服务器"
check_http_service "Frontend-A" "frontend-a.company.com" "80" "/health"
check_http_service "Frontend-B" "frontend-b.company.com" "80" "/health"
check_http_service "Frontend-C" "frontend-c.company.com" "80" "/health"
echo ""

# 7. 检查数据库连接（通过Gateway）
echo ">>> 检查数据库连接"
AUTH_HEALTH=$(curl -s "http://${GATEWAY_HOST}:8080/actuator/health" | grep -o '"status":"UP"' | wc -l)
if [ "$AUTH_HEALTH" -gt 0 ]; then
    log_success "数据库连接: 正常"
else
    log_warning "数据库连接: 无法确认"
fi
echo ""

# 8. 生成报告
echo "========================================="
echo "  健康检查报告"
echo "========================================="

# 统计服务状态
TOTAL_SERVICES=8
RUNNING_SERVICES=0

for service in gateway auth web workflow; do
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "http://${BACKEND_HOST}:${SERVICE_PORTS[$service]}/actuator/health" 2>/dev/null || echo "000")
    if [ "$HTTP_CODE" == "200" ]; then
        ((RUNNING_SERVICES++))
    fi
done

echo "服务状态: $RUNNING_SERVICES/$TOTAL_SERVICES 运行中"

if [ "$RUNNING_SERVICES" -eq "$TOTAL_SERVICES" ]; then
    log_success "所有服务运行正常 ✓"
    exit 0
elif [ "$RUNNING_SERVICES" -gt 0 ]; then
    log_warning "部分服务异常，请检查日志"
    exit 1
else
    log_error "所有服务均异常，请立即处理！"
    exit 2
fi

