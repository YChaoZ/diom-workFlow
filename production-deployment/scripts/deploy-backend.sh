#!/bin/bash
#############################################
# DIOM Workflow 后端部署脚本
#
# 功能:
# 1. 编译后端服务
# 2. 上传到服务器
# 3. 停止旧服务
# 4. 启动新服务
# 5. 健康检查
#
# 使用方式:
#   ./deploy-backend.sh [服务名] [环境]
#   
# 示例:
#   ./deploy-backend.sh all prod        # 部署所有服务
#   ./deploy-backend.sh gateway prod    # 只部署gateway
#   ./deploy-backend.sh auth prod       # 只部署认证服务
#############################################

set -e  # 遇到错误立即退出

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 配置项
PROJECT_ROOT=$(cd "$(dirname "$0")/../.." && pwd)
DEPLOY_DIR="/data/diom-workflow"
BACKUP_DIR="/data/backup/diom-workflow"

# 服务配置
declare -A SERVICE_MAP
SERVICE_MAP["gateway"]="diom-gateway:8080:gateway.company.com"
SERVICE_MAP["auth"]="diom-auth-service:8081:backend.company.com"
SERVICE_MAP["web"]="diom-web-service:8082:backend.company.com"
SERVICE_MAP["workflow"]="diom-workflow-service:8085:backend.company.com"

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查参数
if [ $# -lt 2 ]; then
    log_error "参数不足！"
    echo "使用方式: $0 <服务名> <环境>"
    echo "服务名: all|gateway|auth|web|workflow"
    echo "环境: dev|test|prod"
    exit 1
fi

SERVICE_NAME=$1
ENV=$2

# 部署单个服务
deploy_service() {
    local service=$1
    local env=$2
    
    if [ -z "${SERVICE_MAP[$service]}" ]; then
        log_error "无效的服务名: $service"
        return 1
    fi
    
    IFS=':' read -r module port host <<< "${SERVICE_MAP[$service]}"
    
    log_info "========================================="
    log_info "部署服务: $module ($env 环境)"
    log_info "========================================="
    
    # 步骤1: 编译服务
    log_info "编译服务: $module"
    cd "$PROJECT_ROOT/$module"
    
    # 复制生产环境配置
    if [ -f "$PROJECT_ROOT/production-deployment/backend/${service}-service-prod.yml" ]; then
        cp "$PROJECT_ROOT/production-deployment/backend/${service}-service-prod.yml" \
           "src/main/resources/application-prod.yml"
        log_info "✓ 配置文件已复制"
    fi
    
    # Maven打包
    log_info "开始Maven打包..."
    mvn clean package -DskipTests -P${env}
    
    JAR_FILE=$(find target -name "${module}*.jar" -not -name "*sources.jar" | head -1)
    
    if [ ! -f "$JAR_FILE" ]; then
        log_error "JAR文件不存在: $JAR_FILE"
        return 1
    fi
    
    log_info "✓ 编译完成: $JAR_FILE"
    log_info "✓ 文件大小: $(du -h $JAR_FILE | awk '{print $1}')"
    
    # 步骤2: 上传到服务器
    log_info "上传到服务器 $host..."
    TIMESTAMP=$(date +%Y%m%d_%H%M%S)
    REMOTE_JAR="${module}-${TIMESTAMP}.jar"
    
    scp "$JAR_FILE" "${host}:/tmp/${REMOTE_JAR}"
    
    if [ $? -ne 0 ]; then
        log_error "上传失败"
        return 1
    fi
    
    log_info "✓ 上传成功"
    
    # 步骤3: 在服务器上部署
    log_info "在服务器上部署..."
    ssh "$host" << EOF
        set -e
        
        # 创建部署目录
        sudo mkdir -p $DEPLOY_DIR/$module
        sudo mkdir -p $BACKUP_DIR/$module
        sudo mkdir -p /var/log/diom-workflow/$module
        
        # 备份当前版本
        if [ -f "$DEPLOY_DIR/$module/${module}.jar" ]; then
            echo "备份当前版本..."
            sudo cp $DEPLOY_DIR/$module/${module}.jar \
                   $BACKUP_DIR/$module/${module}-backup-\$(date +%Y%m%d_%H%M%S).jar
            
            # 只保留最近5个备份
            cd $BACKUP_DIR/$module
            ls -t | tail -n +6 | xargs -r sudo rm --
        fi
        
        # 停止旧服务
        echo "停止旧服务..."
        PID=\$(ps aux | grep ${module}.jar | grep -v grep | awk '{print \$2}')
        if [ -n "\$PID" ]; then
            echo "找到进程: \$PID"
            sudo kill -15 \$PID || true
            sleep 5
            
            # 强制杀死
            PID=\$(ps aux | grep ${module}.jar | grep -v grep | awk '{print \$2}')
            if [ -n "\$PID" ]; then
                echo "强制停止..."
                sudo kill -9 \$PID || true
            fi
        fi
        
        # 部署新版本
        echo "部署新版本..."
        sudo mv /tmp/${REMOTE_JAR} $DEPLOY_DIR/$module/${module}.jar
        sudo chown app:app $DEPLOY_DIR/$module/${module}.jar
        
        # 启动新服务
        echo "启动新服务..."
        cd $DEPLOY_DIR/$module
        
        # 设置环境变量
        export JAVA_OPTS="-Xms512m -Xmx2048m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
        export SPRING_PROFILES_ACTIVE=$env
        
        # 启动（后台运行）
        nohup java \$JAVA_OPTS -jar ${module}.jar \
            --spring.profiles.active=\$SPRING_PROFILES_ACTIVE \
            > /var/log/diom-workflow/$module/app.log 2>&1 &
        
        echo "✓ 服务启动中..."
        sleep 5
        
        # 检查进程
        PID=\$(ps aux | grep ${module}.jar | grep -v grep | awk '{print \$2}')
        if [ -z "\$PID" ]; then
            echo "ERROR: 服务启动失败"
            tail -50 /var/log/diom-workflow/$module/app.log
            exit 1
        fi
        
        echo "✓ 服务已启动 (PID: \$PID)"
EOF
    
    if [ $? -ne 0 ]; then
        log_error "部署失败"
        return 1
    fi
    
    log_info "✓ 部署成功"
    
    # 步骤4: 健康检查
    log_info "执行健康检查..."
    sleep 10  # 等待服务完全启动
    
    # 尝试访问健康检查端点
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "http://${host}:${port}/actuator/health" || echo "000")
    
    if [ "$HTTP_CODE" == "200" ]; then
        log_info "✓ 健康检查通过 (HTTP $HTTP_CODE)"
    else
        log_warn "健康检查失败 (HTTP $HTTP_CODE)，请手动验证"
        log_warn "日志路径: ssh ${host} 'tail -100 /var/log/diom-workflow/$module/app.log'"
    fi
    
    log_info "========================================="
    log_info "服务部署完成！"
    log_info "服务: $module"
    log_info "端口: $port"
    log_info "主机: $host"
    log_info "========================================="
    
    return 0
}

# 主逻辑
if [ "$SERVICE_NAME" == "all" ]; then
    log_info "部署所有服务..."
    for service in gateway auth web workflow; do
        deploy_service "$service" "$ENV"
        if [ $? -ne 0 ]; then
            log_error "服务 $service 部署失败，停止后续部署"
            exit 1
        fi
        sleep 5
    done
else
    deploy_service "$SERVICE_NAME" "$ENV"
fi

log_info ""
log_info "全部部署完成！"
log_info ""
log_info "查看服务状态:"
log_info "  ./health-check.sh"
log_info ""
log_info "如需回滚:"
log_info "  ./rollback.sh <服务名>"

