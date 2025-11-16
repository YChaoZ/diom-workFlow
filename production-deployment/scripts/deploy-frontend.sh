#!/bin/bash
#############################################
# DIOM Workflow 前端部署脚本
#
# 功能:
# 1. 打包前端代码
# 2. 上传到前端服务器
# 3. 部署到Nginx
# 4. 健康检查
#
# 使用方式:
#   ./deploy-frontend.sh [环境] [服务器]
#   
# 示例:
#   ./deploy-frontend.sh prod frontend-a
#   ./deploy-frontend.sh prod frontend-b
#############################################

set -e  # 遇到错误立即退出

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 配置项（根据实际情况修改）
FRONTEND_DIR="../diom-frontend"
DIST_DIR="$FRONTEND_DIR/dist"
BACKUP_DIR="/data/backup/diom-frontend"

# 服务器配置（根据实际情况修改）
declare -A SERVER_MAP
SERVER_MAP["frontend-a"]="user@frontend-a.company.com"
SERVER_MAP["frontend-b"]="user@frontend-b.company.com"
SERVER_MAP["frontend-c"]="user@frontend-c.company.com"

# Nginx部署目录
NGINX_HTML_DIR="/usr/share/nginx/html/diom-frontend"

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
    echo "使用方式: $0 <环境> <服务器>"
    echo "环境: dev|test|prod"
    echo "服务器: frontend-a|frontend-b|frontend-c"
    exit 1
fi

ENV=$1
SERVER_NAME=$2
SERVER_HOST=${SERVER_MAP[$SERVER_NAME]}

if [ -z "$SERVER_HOST" ]; then
    log_error "无效的服务器名称: $SERVER_NAME"
    exit 1
fi

log_info "========================================="
log_info "开始部署前端到 $SERVER_NAME ($ENV 环境)"
log_info "========================================="

# 步骤1: 检查前端目录
cd "$(dirname "$0")"
cd ..  # 回到 production-deployment 目录
PROJECT_ROOT=$(cd .. && pwd)
cd "$PROJECT_ROOT"

if [ ! -d "$FRONTEND_DIR" ]; then
    log_error "前端目录不存在: $FRONTEND_DIR"
    exit 1
fi

log_info "✓ 前端目录: $FRONTEND_DIR"

# 步骤2: 复制生产环境配置
log_info "准备环境配置..."
cp production-deployment/frontend/.env.production diom-frontend/.env.production
log_info "✓ 环境配置已复制"

# 步骤3: 安装依赖（如果需要）
cd "$FRONTEND_DIR"
if [ ! -d "node_modules" ]; then
    log_info "安装前端依赖..."
    npm install
fi

# 步骤4: 打包前端
log_info "开始打包前端代码..."
npm run build

if [ ! -d "$DIST_DIR" ]; then
    log_error "打包失败，dist目录不存在"
    exit 1
fi

log_info "✓ 前端打包完成"

# 步骤5: 压缩打包文件
cd "$PROJECT_ROOT"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
PACKAGE_NAME="diom-frontend-${ENV}-${TIMESTAMP}.tar.gz"

log_info "压缩打包文件: $PACKAGE_NAME"
tar -czf "/tmp/$PACKAGE_NAME" -C "$DIST_DIR" .

log_info "✓ 打包文件大小: $(du -h /tmp/$PACKAGE_NAME | awk '{print $1}')"

# 步骤6: 上传到服务器
log_info "上传到服务器 $SERVER_HOST..."
scp "/tmp/$PACKAGE_NAME" "$SERVER_HOST:/tmp/"

if [ $? -ne 0 ]; then
    log_error "上传失败"
    exit 1
fi

log_info "✓ 上传成功"

# 步骤7: 在服务器上部署
log_info "在服务器上部署..."
ssh "$SERVER_HOST" << EOF
    set -e
    
    # 创建备份目录
    sudo mkdir -p $BACKUP_DIR
    
    # 备份当前版本
    if [ -d "$NGINX_HTML_DIR" ]; then
        echo "备份当前版本..."
        sudo tar -czf $BACKUP_DIR/backup-\$(date +%Y%m%d_%H%M%S).tar.gz -C $NGINX_HTML_DIR .
        
        # 只保留最近5个备份
        cd $BACKUP_DIR
        ls -t | tail -n +6 | xargs -r sudo rm --
    fi
    
    # 创建部署目录
    sudo mkdir -p $NGINX_HTML_DIR
    
    # 解压新版本
    echo "解压新版本..."
    sudo tar -xzf /tmp/$PACKAGE_NAME -C $NGINX_HTML_DIR
    
    # 设置权限
    sudo chown -R nginx:nginx $NGINX_HTML_DIR
    sudo chmod -R 755 $NGINX_HTML_DIR
    
    # 测试Nginx配置
    echo "测试Nginx配置..."
    sudo nginx -t
    
    # 重载Nginx
    echo "重载Nginx..."
    sudo nginx -s reload
    
    # 清理临时文件
    rm -f /tmp/$PACKAGE_NAME
    
    echo "✓ 部署完成"
EOF

if [ $? -ne 0 ]; then
    log_error "部署失败"
    exit 1
fi

log_info "✓ 部署成功"

# 步骤8: 健康检查
log_info "执行健康检查..."
sleep 3

# 获取服务器域名（去掉 user@ 前缀）
DOMAIN=${SERVER_HOST#*@}

# 尝试访问健康检查端点
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "http://$DOMAIN/health" || echo "000")

if [ "$HTTP_CODE" == "200" ]; then
    log_info "✓ 健康检查通过 (HTTP $HTTP_CODE)"
else
    log_warn "健康检查失败 (HTTP $HTTP_CODE)，请手动验证"
fi

# 步骤9: 清理本地临时文件
rm -f "/tmp/$PACKAGE_NAME"

log_info "========================================="
log_info "前端部署完成！"
log_info "服务器: $SERVER_NAME ($SERVER_HOST)"
log_info "环境: $ENV"
log_info "版本: $TIMESTAMP"
log_info "访问地址: http://$DOMAIN"
log_info "========================================="

# 显示回滚提示
log_info ""
log_info "如需回滚，执行以下命令:"
log_info "ssh $SERVER_HOST"
log_info "cd $BACKUP_DIR"
log_info "sudo tar -xzf <备份文件> -C $NGINX_HTML_DIR"
log_info "sudo nginx -s reload"

