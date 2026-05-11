#!/bin/bash

# 部署脚本 - CreativeNFT User Service
# 使用方法: ./deploy.sh [dev|prod]

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 环境设置
ENV=${1:-dev}
COMPOSE_FILE="docker-compose.yml"

if [ "$ENV" = "prod" ]; then
    COMPOSE_FILE="docker-compose.prod.yml"
fi

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  CreativeNFT User Service 部署脚本${NC}"
echo -e "${BLUE}  环境: $ENV${NC}"
echo -e "${BLUE}========================================${NC}"
echo

# 检查 Docker 和 Docker Compose
check_docker() {
    echo -e "${YELLOW}检查 Docker 环境...${NC}"
    if ! command -v docker &> /dev/null; then
        echo -e "${RED}错误: Docker 未安装${NC}"
        exit 1
    fi
    if ! command -v docker-compose &> /dev/null; then
        echo -e "${RED}错误: Docker Compose 未安装${NC}"
        exit 1
    fi
    echo -e "${GREEN}Docker 环境检查通过${NC}"
}

# 加载环境变量
load_env() {
    if [ -f .env ]; then
        echo -e "${YELLOW}加载环境变量...${NC}"
        export $(cat .env | grep -v '^#' | xargs)
    fi
}

# 创建必要的目录
setup_directories() {
    echo -e "${YELLOW}创建必要的目录...${NC}"
    mkdir -p logs
    mkdir -p backup/mysql
    mkdir -p wallets
    chmod 755 logs backup wallets
    echo -e "${GREEN}目录创建完成${NC}"
}

# 构建项目
build_project() {
    echo -e "${YELLOW}构建后端项目...${NC}"
    if [ ! -f target/*.jar ]; then
        echo -e "${YELLOW}未找到构建产物，开始 Maven 构建...${NC}"
        if command -v mvn &> /dev/null; then
            mvn clean package -DskipTests -q
            echo -e "${GREEN}Maven 构建完成${NC}"
        else
            echo -e "${YELLOW}未找到 Maven，将使用 Docker 构建${NC}"
        fi
    else
        echo -e "${GREEN}已找到构建产物${NC}"
    fi
}

# 停止旧服务
stop_services() {
    echo -e "${YELLOW}停止旧服务...${NC}"
    docker-compose -f $COMPOSE_FILE down --remove-orphans 2>/dev/null || true
    echo -e "${GREEN}旧服务已停止${NC}"
}

# 拉取最新镜像
pull_images() {
    echo -e "${YELLOW}拉取基础镜像...${NC}"
    docker-compose -f $COMPOSE_FILE pull
    echo -e "${GREEN}镜像拉取完成${NC}"
}

# 构建并启动服务
start_services() {
    echo -e "${YELLOW}构建并启动服务...${NC}"
    docker-compose -f $COMPOSE_FILE build --no-cache
    docker-compose -f $COMPOSE_FILE up -d
    echo -e "${GREEN}服务启动完成${NC}"
}

# 等待服务就绪
wait_for_services() {
    echo -e "${YELLOW}等待服务就绪...${NC}"
    echo -e "${YELLOW}等待 MySQL...${NC}"
    sleep 10

    # 检查后端健康状态
    echo -e "${YELLOW}检查后端服务健康状态...${NC}"
    for i in {1..30}; do
        if curl -s http://localhost:8085/actuator/health | grep -q '"status":"UP"'; then
            echo -e "${GREEN}后端服务已就绪${NC}"
            return 0
        fi
        echo -n "."
        sleep 2
    done
    echo
    echo -e "${YELLOW}警告: 后端服务可能尚未完全就绪，请稍后手动检查${NC}"
}

# 显示服务状态
show_status() {
    echo
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}  服务状态${NC}"
    echo -e "${BLUE}========================================${NC}"
    docker-compose -f $COMPOSE_FILE ps
    echo
    echo -e "${GREEN}访问地址:${NC}"
    echo -e "  前端: http://localhost"
    echo -e "  后端 API: http://localhost:8085"
    echo -e "  健康检查: http://localhost:8085/actuator/health"
    echo
    echo -e "${YELLOW}常用命令:${NC}"
    echo -e "  查看日志: docker-compose -f $COMPOSE_FILE logs -f"
    echo -e "  停止服务: docker-compose -f $COMPOSE_FILE down"
    echo -e "  重启服务: docker-compose -f $COMPOSE_FILE restart"
}

# 主流程
main() {
    check_docker
    load_env
    setup_directories
    build_project
    stop_services
    pull_images
    start_services
    wait_for_services
    show_status
    echo
    echo -e "${GREEN}部署完成!${NC}"
}

# 执行主流程
main
