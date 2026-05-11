#!/bin/bash

# 快速启动脚本 - 本地开发环境
# 使用方法: ./start.sh

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  CreativeNFT User Service 快速启动${NC}"
echo -e "${BLUE}========================================${NC}"
echo

# 检查 Java
if ! command -v java &> /dev/null; then
    echo -e "${RED}错误: Java 未安装${NC}"
    exit 1
fi

# 检查 Maven
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}错误: Maven 未安装${NC}"
    exit 1
fi

# 检查 Node.js
if ! command -v node &> /dev/null; then
    echo -e "${RED}错误: Node.js 未安装${NC}"
    exit 1
fi

# 创建日志目录
mkdir -p logs

# 构建后端
echo -e "${YELLOW}构建后端项目...${NC}"
mvn clean package -DskipTests -q
echo -e "${GREEN}后端构建完成${NC}"

# 启动后端（后台运行）
echo -e "${YELLOW}启动后端服务...${NC}"
nohup java -jar target/*.jar --spring.profiles.active=dev > logs/backend.log 2>&1 &
BACKEND_PID=$!
echo -e "${GREEN}后端服务已启动 (PID: $BACKEND_PID)${NC}"

# 等待后端启动
sleep 5

# 检查后端健康状态
if curl -s http://localhost:8085/actuator/health | grep -q '"status":"UP"'; then
    echo -e "${GREEN}后端服务运行正常${NC}"
else
    echo -e "${YELLOW}警告: 后端服务可能尚未完全启动${NC}"
fi

# 启动前端
echo -e "${YELLOW}启动前端开发服务器...${NC}"
cd frontend
npm install --silent
npm run dev &
FRONTEND_PID=$!
echo -e "${GREEN}前端服务已启动 (PID: $FRONTEND_PID)${NC}"

cd ..

echo
echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}  服务启动完成!${NC}"
echo -e "${BLUE}========================================${NC}"
echo
echo -e "访问地址:"
echo -e "  前端: http://localhost:3000"
echo -e "  后端: http://localhost:8085"
echo -e "  健康检查: http://localhost:8085/actuator/health"
echo
echo -e "日志文件:"
echo -e "  后端: logs/backend.log"
echo
echo -e "停止服务:"
echo -e "  后端: kill $BACKEND_PID"
echo -e "  前端: kill $FRONTEND_PID"
echo

# 等待用户输入
read -p "按回车键停止所有服务..."

# 停止服务
kill $BACKEND_PID 2>/dev/null || true
kill $FRONTEND_PID 2>/dev/null || true
echo -e "${GREEN}服务已停止${NC}"
